package com.xforceplus.ultraman.oqsengine.formula.exception;

/**
 * formula client exception.
 *
 * @author j.xu
 * @version 0.1 2021/05/2021/5/10
 * @since 1.8
 */
public class FormulaExecutionException extends RuntimeException {

    /**
     * formula-client模块统一抛出的错误.
     */
    public FormulaExecutionException(String message) {
        super(message);
    }
}
