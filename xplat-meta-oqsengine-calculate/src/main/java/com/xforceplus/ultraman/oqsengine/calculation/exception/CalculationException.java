package com.xforceplus.ultraman.oqsengine.calculation.exception;

/**
 * 计算失败的异常.
 *
 * @author dongbin
 * @version 0.1 2021/09/17 15:42
 * @since 1.8
 */
public class CalculationException extends RuntimeException {

    public CalculationException() {
    }

    public CalculationException(String message) {
        super(message);
    }

    public CalculationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CalculationException(Throwable cause) {
        super(cause);
    }

    public CalculationException(String message, Throwable cause, boolean enableSuppression,
                                boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
