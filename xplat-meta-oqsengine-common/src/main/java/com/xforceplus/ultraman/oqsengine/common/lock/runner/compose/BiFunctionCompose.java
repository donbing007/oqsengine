package com.xforceplus.ultraman.oqsengine.common.lock.runner.compose;

import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * desc :
 * name : BiFunctionCompose
 *
 * @author : xujia
 * date : 2020/10/14
 * @since : 1.8
 * @param <T> params1
 * @param <U> params2
 * @param <R> result
 */
public class BiFunctionCompose<T, U, R> {
    private BiFunction<T, U, R> biFunction;
    private String functionName;
    private T tParams;
    private U uParams;
    private Predicate<R> expected;

    public BiFunctionCompose(BiFunction<T, U, R> biFunction, String functionName, T tParams, U uParams, Predicate<R> expected) {
        this.biFunction = biFunction;
        this.functionName = functionName;
        this.tParams = tParams;
        this.uParams = uParams;
        this.expected = expected;
    }

    public BiFunction<T, U, R> getBiFunction() {
        return biFunction;
    }

    public String getFunctionName() {
        return functionName;
    }

    public T gettParams() {
        return tParams;
    }

    public U getuParams() {
        return uParams;
    }

    public Predicate<R> getExpected() {
        return expected;
    }
}
