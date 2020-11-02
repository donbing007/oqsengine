package com.xforceplus.ultraman.oqsengine.sdk.transactional;

/**
 *
 */
public class TransactionCommitException extends RuntimeException {

    public TransactionCommitException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionCommitException(String message) {
        super(message);
    }
}
