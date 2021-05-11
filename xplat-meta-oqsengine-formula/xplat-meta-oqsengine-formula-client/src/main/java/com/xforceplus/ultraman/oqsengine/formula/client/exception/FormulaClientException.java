package com.xforceplus.ultraman.oqsengine.formula.client.exception;

/**
 * formula client exception.
 *
 * @author j.xu
 * @version 0.1 2021/05/2021/5/10
 * @since 1.8
 */
public class FormulaClientException extends RuntimeException {

    /**
     * formula-client模块统一抛出的错误.
     */
    public FormulaClientException(String message) {
        super(message);
    }
}
