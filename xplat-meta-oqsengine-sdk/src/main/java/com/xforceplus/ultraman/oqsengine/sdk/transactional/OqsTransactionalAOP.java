package com.xforceplus.ultraman.oqsengine.sdk.transactional;

import com.xforceplus.ultraman.oqsengine.sdk.EntityService;
import com.xforceplus.ultraman.oqsengine.sdk.OperationResult;
import com.xforceplus.ultraman.oqsengine.sdk.TransactionUp;
import com.xforceplus.ultraman.oqsengine.sdk.transactional.annotation.OqsTransactional;
import com.xforceplus.ultraman.oqsengine.sdk.transactional.annotation.Propagation;
import com.xforceplus.ultraman.oqsengine.sdk.util.GetResult;
import com.xforceplus.xplat.galaxy.framework.context.ContextService;
import io.vavr.control.Either;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Stack;
import java.util.concurrent.CompletableFuture;

import static com.xforceplus.xplat.galaxy.framework.context.ContextKeys.StringKeys.TRANSACTION_KEY;

/**
 *
 */
@Aspect
@Configuration
public class OqsTransactionalAOP {

    /**
     *
     */
    public enum TransactionKey implements ContextService.ContextKey<Stack<String>> {

        TEMP_TRANSACTION;
    }

    /**
     *
     */
    public enum TransactionRollBackKey implements ContextService.ContextKey<Boolean> {

        ROLL_BACK;
    }

    @Autowired
    private EntityService entityServiceClient;

    @Autowired
    ContextService contextService;

    private static final Logger logger = LoggerFactory.getLogger(OqsTransactionalAOP.class);


    private void createNewTransaction() {
        //create new
        //maybe timeout
        OperationResult result = entityServiceClient
                .begin(TransactionUp.newBuilder().build()).toCompletableFuture().join();
        if (result.getCode() != OperationResult.Code.OK) {
            throw new TransactionCreateErrorException(result.getMessage());
        } else {
            logger.info("Transaction create success with id:{}", result.getTransactionResult());
            contextService.set(TRANSACTION_KEY, result.getTransactionResult());
            logger.debug("set currentService with {}", result.getTransactionResult());
        }
    }

    /**
     * push current transaction to stack
     */
    private void saveCurrentTransaction() {
        String currentTransaction = contextService.get(TRANSACTION_KEY);

        Stack<String> stack = contextService.get(TransactionKey.TEMP_TRANSACTION);

        if (stack == null) {
            stack = new Stack<>();
        }

        stack.push(currentTransaction);
        contextService.set(TransactionKey.TEMP_TRANSACTION, stack);
        logger.info("Suspend transaction key {}, stack {}", currentTransaction, stack);
    }

    private void recoverCurrentTransaction() {
        Stack<String> stack = contextService.get(TransactionKey.TEMP_TRANSACTION);

        String lastTransaction = null;
        if (stack != null) {
            lastTransaction = stack.pop();
        }

        contextService.set(TRANSACTION_KEY, lastTransaction);
        logger.info("recover transaction key {}, stack{}", lastTransaction, stack);
    }


    private Object doTransactional(ProceedingJoinPoint pjp
            , String transactionalKey
            , Class<? extends Throwable>[] noRollBackForClass
            , Class<? extends Throwable>[] rollBackForClass
    ) throws Throwable {
        Object output = null;
        try {
            /**
             * return exception
             * or rollback
             */
            output = pjp.proceed();

            Boolean isRollBack = contextService.get(TransactionRollBackKey.ROLL_BACK);

            if (isRollBack != null && isRollBack) {
                rollBack(transactionalKey);
                return output;
            } else {

                Object finalOutput = output;
                CompletableFuture<Either<String, Object>> commited = entityServiceClient.commit(TransactionUp.newBuilder()
                        .setId(transactionalKey)
                        .build())
                        .exceptionally(throwable -> {
                            logger.error("Transaction with id:{} failed to commit with exception {}", transactionalKey, throwable.getMessage());
                            return OperationResult
                                    .newBuilder()
                                    .setCode(OperationResult.Code.EXCEPTION)
                                    .setMessage(throwable.getMessage())
                                    .buildPartial();
                        }).thenApply(x -> {
                            if (x.getCode() == OperationResult.Code.OK) {
                                logger.info("Transaction with id:{} has committed successfully ", transactionalKey);
                                return Either.<String, Object>right(finalOutput);
                            } else {
                                logger.error("Transaction with id:{} failed to commit", transactionalKey);
                                return Either.<String, Object>left("事务提交失败:" + x.getMessage());
                            }
                        }).toCompletableFuture();

                Either<String, Object> join = commited.join();
                return GetResult.get(join);
            }

        } catch (Throwable throwable) {
            Throwable rootException = getRootException(throwable);
            Boolean triggerRollBack = isTriggerRollBack(rootException, noRollBackForClass, rollBackForClass);

            if (triggerRollBack) {
                logger.info("Transaction {}  trigger rollback when got ex {}", transactionalKey, throwable.getMessage());
                rollBack(transactionalKey);
            } else {
                logger.info("Transaction {}  not trigger rollback, even got a exception {}", transactionalKey, throwable.getMessage());

            }

            throw throwable;
        }
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

    private Throwable getRootException(Throwable throwable) {

        Throwable retThrowable = throwable;

//        while(retThrowable instanceof TransactionalWrappedException){
//            retThrowable = retThrowable.getCause();
//        }

        return retThrowable;
    }

    //TODO check if multi rollback will failed
    private void rollBack(String transactionalKey) {
        logger.info("Roll back {}", transactionalKey);
        try {
            entityServiceClient.rollBack(TransactionUp.newBuilder()
                    .setId(transactionalKey)
                    .build()).toCompletableFuture().join();
        } catch (Throwable rollbackEx) {
            logger.error("Rollback Transaction {} failed", rollbackEx.getMessage());
        }
    }

    @Around("@annotation(oqsTransactional)")
    public Object transactionExecution(ProceedingJoinPoint pjp, OqsTransactional oqsTransactional) throws Throwable {

        Propagation propagation = oqsTransactional.propagation();
        int timeout = oqsTransactional.timeout();
        Class<? extends Throwable>[] noRollBackForClass = oqsTransactional.noRollbackFor();
        Class<? extends Throwable>[] rollBackForClass = oqsTransactional.rollbackFor();

        String currentTransaction = contextService.get(TRANSACTION_KEY);

        boolean omitRollback = false;

        //every time cross this annotation
        Object output = null;

        try {
            saveCurrentTransaction();
            switch (propagation) {
                case REQUIRED:
                    if (StringUtils.isEmpty(currentTransaction)) {
                        createNewTransaction();
                    }

                    currentTransaction = contextService.get(TRANSACTION_KEY);
                    output = doTransactional(pjp, currentTransaction, noRollBackForClass, rollBackForClass);
                    break;
                case REQUIRES_NEW:
                    omitRollback = true;
                    createNewTransaction();
                    currentTransaction = contextService.get(TRANSACTION_KEY);
                    output = doTransactional(pjp, currentTransaction, noRollBackForClass, rollBackForClass);
                    break;
                case MANDATORY:
                    if (StringUtils.isEmpty(currentTransaction)) {
                        throw new TransactionalNotExistsException();
                    }
                    output = doTransactional(pjp, currentTransaction, noRollBackForClass, rollBackForClass);
                    break;
                case NOT_SUPPORTED:
                    contextService.set(TRANSACTION_KEY, null);
                    omitRollback = true;
                    output = pjp.proceed();
                    break;
                case NEVER:
                    //TODO
                    if (StringUtils.isNoneEmpty(currentTransaction)) {
                        throw new TransactionalExistsException();
                    }
                    omitRollback = true;
                    output = pjp.proceed();
                    break;
                case SUPPORTS:
                default:
            }
        } catch (Throwable throwable) {
            if (!omitRollback) {
                contextService.set(TransactionRollBackKey.ROLL_BACK, true);
            }
            throw throwable;
        } finally {
            recoverCurrentTransaction();
        }

        return output;
    }
}
