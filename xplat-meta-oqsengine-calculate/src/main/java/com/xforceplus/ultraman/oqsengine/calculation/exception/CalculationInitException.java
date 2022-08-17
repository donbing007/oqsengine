package com.xforceplus.ultraman.oqsengine.calculation.exception;

/**
 * 计算字段重算异常.
 */
public class CalculationInitException extends RuntimeException {
    public CalculationInitException() {
        super();
    }

    public CalculationInitException(String message) {
        super(message);
    }

    public CalculationInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public CalculationInitException(Throwable cause) {
        super(cause);
    }

    protected CalculationInitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
