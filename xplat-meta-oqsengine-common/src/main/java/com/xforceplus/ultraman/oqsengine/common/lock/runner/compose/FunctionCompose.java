package com.xforceplus.ultraman.oqsengine.common.lock.runner.compose;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * desc :
 * name : FunctionCompose
 *
 * @author : xujia
 * date : 2020/10/14
 * @since : 1.8
 * @param <V> params
 * @param <R> result
 */
public class FunctionCompose<V, R> {
    private Function<V, R> function;
    private String functionName;
    private V params;
    private Predicate<R> expected;

    public FunctionCompose(Function<V, R> function, String functionName, V params, Predicate<R> expected) {
        this.function = function;
        this.functionName = functionName;
        this.params = params;
        this.expected = expected;
    }

    public Function<V, R> getFunction() {
        return function;
    }

    public String getFunctionName() {
        return functionName;
    }

    public V getParams() {
        return params;
    }

    public Predicate<R> getExpected() {
        return expected;
    }
}
