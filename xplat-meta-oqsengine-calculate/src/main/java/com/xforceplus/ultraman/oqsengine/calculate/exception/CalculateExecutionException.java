package com.xforceplus.ultraman.oqsengine.calculate.exception;

/**
 * calculate client exception.
 *
 * @author j.xu
 * @version 0.1 2021/05/2021/5/10
 * @since 1.8
 */
public class CalculateExecutionException extends RuntimeException {

    /**
     * calculate模块统一抛出的错误.
     */
    public CalculateExecutionException(String message) {
        super(message);
    }
}
