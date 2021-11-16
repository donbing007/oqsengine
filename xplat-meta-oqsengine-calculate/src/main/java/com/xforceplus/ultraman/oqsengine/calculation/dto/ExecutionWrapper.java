package com.xforceplus.ultraman.oqsengine.calculation.dto;

import java.util.Map;

/**
 *  执行一个表达式的对象表示.
 *
 *  @author  j.xu
 *  @version 0.1 2021/05/2021/5/12
 *  @since 1.8
 */
public class ExecutionWrapper<T> {

    /**
     * 需要执行的表达式对象.
     */
    private ExpressionWrapper expressionWrapper;

    /**
     * 执行表达式的入参.
     */
    private Map<String, Object> params;


    public ExecutionWrapper(ExpressionWrapper expressionWrapper, Map<String, Object> params) {
        this.expressionWrapper = expressionWrapper;
        this.params = params;
    }

    public ExpressionWrapper getExpressionWrapper() {
        return expressionWrapper;
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
