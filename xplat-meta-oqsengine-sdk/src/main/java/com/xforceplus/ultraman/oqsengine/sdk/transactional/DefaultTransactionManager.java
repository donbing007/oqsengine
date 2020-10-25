package com.xforceplus.ultraman.oqsengine.sdk.transactional;

import akka.grpc.javadsl.SingleResponseRequestBuilder;
import com.xforceplus.ultraman.oqsengine.sdk.EntityServiceClientPowerApi;
import com.xforceplus.ultraman.oqsengine.sdk.OperationResult;
import com.xforceplus.ultraman.oqsengine.sdk.TransactionUp;
import com.xforceplus.ultraman.oqsengine.sdk.transactional.annotation.Propagation;
import com.xforceplus.xplat.galaxy.framework.context.ContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;

import static com.xforceplus.ultraman.oqsengine.sdk.transactional.OqsTransactionalAOP.TransactionKey.TRANSACTION_STACK;
import static com.xforceplus.xplat.galaxy.framework.context.ContextKeys.StringKeys.TRANSACTION_KEY;

/**
 *
 */
public class DefaultTransactionManager implements TransactionManager{

    private Logger logger = LoggerFactory.getLogger(TransactionManager.class);

    @Autowired
    private ContextService contextService;

    @Autowired
    private EntityServiceClientPowerApi entityServiceClient;

    @Override
    public OqsTransaction getCurrent() {
        Stack<OqsTransaction> oqsTransactions = contextService.get(TRANSACTION_STACK);

        if (oqsTransactions == null) {
            contextService.set(TRANSACTION_STACK, new Stack<>());
            oqsTransactions = contextService.get(TRANSACTION_STACK);
        }

        return oqsTransactions.isEmpty() ? null : oqsTransactions.peek();
    }

    private void pushNewTransaction(OqsTransaction oqsTransaction) {
        Stack<OqsTransaction> oqsTransactions = contextService.get(TRANSACTION_STACK);

        if (oqsTransactions == null) {
            contextService.set(TRANSACTION_STACK, new Stack<>());
            oqsTransactions = contextService.get(TRANSACTION_STACK);
        }
        oqsTransactions.push(oqsTransaction);
    }

    private void popTransaction() {
        Stack<OqsTransaction> oqsTransactions = contextService.get(TRANSACTION_STACK);

        if (oqsTransactions != null) {
            oqsTransactions.pop();
        }
    }

    public OqsTransaction createNewTransaction(int timeout) {
        //create new
        //maybe timeout

        SingleResponseRequestBuilder<TransactionUp, OperationResult> builder = entityServiceClient.begin().addHeader("timeout", String.valueOf(timeout));

        OperationResult result =
                builder.invoke(TransactionUp.newBuilder().build())
                .toCompletableFuture().join();
        if (result.getCode() != OperationResult.Code.OK) {
            throw new TransactionCreateErrorException(result.getMessage());
        } else {
            logger.info("Transaction create success with id:{}", result.getTransactionResult());
            OqsTransaction transaction = new OqsTransaction();
            transaction.setId(result.getTransactionResult());
            return transaction;
        }
    }

    public void commit(String transactionalKey) {
        CompletionStage<OperationResult> commit = entityServiceClient.commit()
                .invoke(TransactionUp.newBuilder()
                .setId(transactionalKey)
                .build());
        OperationResult result = commit.toCompletableFuture().join();
        if (result.getCode() != OperationResult.Code.OK) {
            throw new TransactionCommitException(result.getMessage());
        }

        logger.info("transaction {} committed", transactionalKey);
    }

    private Boolean isTriggerRollBack(Throwable throwable
            , Class<? extends Throwable>[] noRollBackForClass
            , Class<? extends Throwable>[] rollBackForClass) {
        boolean noRollBack = false;
        boolean rollBack = true;

        if (noRollBackForClass != null && noRollBackForClass.length > 0) {
            noRollBack = Arrays.stream(noRollBackForClass).anyMatch(x -> x == throwable.getClass());
        }

        if (rollBackForClass != null && rollBackForClass.length > 0) {
            rollBack = Arrays.stream(rollBackForClass).anyMatch(x -> x == throwable.getClass());
        }

        return !noRollBack && rollBack;
    }

    private void rollBack(String transactionalKey) {
        logger.info("Roll back {}", transactionalKey);
        try {
            entityServiceClient.rollBack()
                    .invoke(TransactionUp.newBuilder()
                    .setId(transactionalKey)
                    .build()).toCompletableFuture().join();
        } catch (Throwable rollbackEx) {
            logger.error("Rollback Transaction {} failed", rollbackEx.getMessage());
        }
    }

    private void logCurrentTransaction(){
        Stack<OqsTransaction> stack = contextService.get(TRANSACTION_STACK);
        logger.info("Transaction stack {}", stack);
    }

    private <T> T doTransactional(Callable<T> callable
            , OqsTransaction transaction
            , OqsTransaction previewTransaction
            , Class<? extends Throwable>[] noRollBackForClass
            , Class<? extends Throwable>[] rollBackForClass
            , boolean isOwner
    ) throws Throwable {

        try {
            contextService.set(TRANSACTION_KEY, transaction.getId());
            T output = callable.call();

            boolean isRollBack = transaction.isRollBack();

            if (isRollBack) {
                if (isOwner) {
                    rollBack(transaction.getId());
                }
                return output;
            } else {
                if (isOwner) {
                    commit(transaction.getId());
                    transaction.setCommit(true);
                }
            }

            return output;
        } catch (Throwable throwable) {
            Throwable rootCause = throwable;
            if(throwable instanceof TransactionWrapperException){
                rootCause = rootCause.getCause();
            }
            if (isTriggerRollBack(rootCause, noRollBackForClass, rollBackForClass)) {
                logger.info("Trigger Transaction {} Rollback with ex {}", transaction.getId(), rootCause);
                if (!isOwner) {
                    transaction.setRollBack(true);
                } else {
                    rollBack(transaction.getId());
                }
            }

            throw rootCause;
        } finally {
            //TODO
            if(previewTransaction != null){
                contextService.set(TRANSACTION_KEY, previewTransaction.getId());
            } else {
                contextService.set(TRANSACTION_KEY, null);
            }

        }
    }

    @Override
    public <T> T transactionExecution(Propagation propagation
            , int timeout
            , Class<? extends Throwable>[] noRollBackForClass
            , Class<? extends Throwable>[] rollBackForClass
            , Callable<T> callable) throws Throwable {

        OqsTransaction currentTransaction = getCurrent();

        T output = null;

        switch (propagation) {
            case REQUIRED:
                if (currentTransaction == null) {
                    OqsTransaction newTransaction = createNewTransaction(timeout);
                    pushNewTransaction(newTransaction);
                    try {
                        output = doTransactional(callable, newTransaction, null, noRollBackForClass, rollBackForClass, true);
                    } finally {
                        popTransaction();
                    }
                } else {
                    output = doTransactional(callable, currentTransaction, currentTransaction, noRollBackForClass, rollBackForClass, false);
                }

                break;
            case REQUIRES_NEW:
                OqsTransaction newTransaction = createNewTransaction(timeout);
                pushNewTransaction(newTransaction);
                try {
                    output = doTransactional(callable, newTransaction, currentTransaction, noRollBackForClass, rollBackForClass, true);
                } finally {
                    popTransaction();
                }
                break;
            case MANDATORY:
                if (currentTransaction == null) {
                    throw new TransactionalNotExistsException();
                }
                output = doTransactional(callable, currentTransaction, currentTransaction, noRollBackForClass, rollBackForClass, false);
                break;
            case NOT_SUPPORTED:
                contextService.set(TRANSACTION_KEY, null);
                output = callable.call();
                break;
            case NEVER:
                //TODO
                if (currentTransaction != null) {
                    throw new TransactionalExistsException();
                }
                output = callable.call();
                break;
            case SUPPORTS:
                if (currentTransaction == null) {
                    output = callable.call();
                } else {
                    output = doTransactional(callable, currentTransaction, currentTransaction, noRollBackForClass, rollBackForClass, false);
                }
                break;
            default:
        }

        logCurrentTransaction();

        return output;
    }
}
