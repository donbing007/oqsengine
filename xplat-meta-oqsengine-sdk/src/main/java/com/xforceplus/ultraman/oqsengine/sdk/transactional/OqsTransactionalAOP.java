package com.xforceplus.ultraman.oqsengine.sdk.transactional;

import com.xforceplus.ultraman.oqsengine.sdk.transactional.annotation.OqsTransactional;
import com.xforceplus.ultraman.oqsengine.sdk.transactional.annotation.Propagation;
import com.xforceplus.xplat.galaxy.framework.context.ContextService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Stack;

/**
 *
 */
@Aspect
@Configuration
public class OqsTransactionalAOP {

    /**
     * transaction stack
     */
    public enum TransactionKey implements ContextService.ContextKey<Stack<OqsTransaction>> {
        TRANSACTION_STACK;
    }

    @Autowired
    private OqsTransactionManager oqsTransactionManager;

    private Logger logger = LoggerFactory.getLogger(OqsTransactionalAOP.class);


    @Around("@annotation(oqsTransactional)")
    public Object transactionExecution(ProceedingJoinPoint pjp, OqsTransactional oqsTransactional) throws Throwable {

        Propagation propagation = oqsTransactional.propagation();
        int timeout = oqsTransactional.timeout();
        Class<? extends Throwable>[] noRollBackForClass = oqsTransactional.noRollbackFor();
        Class<? extends Throwable>[] rollBackForClass = oqsTransactional.rollbackFor();

        try {
            Object output = oqsTransactionManager.transactionExecution(propagation, timeout, noRollBackForClass
                    , rollBackForClass,
                    () -> {
                        try {
                            return pjp.proceed();
                        } catch (Throwable throwable) {
                            throw new TransactionWrapperException(throwable);
                        }
                    });
            return output;
        } catch (Throwable throwable) {
            if (throwable instanceof TransactionWrapperException) {
                throw throwable.getCause();
            } else {
                throw throwable;
            }
        }
    }
}
