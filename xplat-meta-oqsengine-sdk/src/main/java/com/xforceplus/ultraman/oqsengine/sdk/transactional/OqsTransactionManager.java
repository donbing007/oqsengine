package com.xforceplus.ultraman.oqsengine.sdk.transactional;

import com.xforceplus.ultraman.oqsengine.sdk.transactional.annotation.Propagation;

import java.util.concurrent.Callable;

/**
 *
 */
public interface OqsTransactionManager {
    OqsTransaction getCurrent();

    <T> T transactionExecution(Propagation propagation
            , int timeout
            , Class<? extends Throwable>[] noRollBackForClass
            , Class<? extends Throwable>[] rollBackForClass
            , Callable<T> callable) throws Throwable;
}
