package com.xforceplus.ultraman.oqsengine.calculation.exception;

/**
 * 表示计算异常.
 *
 * @author dongbin
 * @version 0.1 2021/07/01 17:58
 * @since 1.8
 */
public class CalculationLogicException extends Exception {

    public CalculationLogicException() {
    }

    public CalculationLogicException(String message) {
        super(message);
    }

    public CalculationLogicException(String message, Throwable cause) {
        super(message, cause);
    }

    public CalculationLogicException(Throwable cause) {
        super(cause);
    }

    /**
     * 构造新的异常实例.
     */
    public CalculationLogicException(
        String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {

        super(message, cause, enableSuppression, writableStackTrace);

    }
}
