package com.xforceplus.ultraman.oqsengine.sdk.service.operation;

import java.util.Objects;

/**
 * special function with context only change 2rd params
 *
 * @param <T>
 * @param <U>
 * @param <P>
 * @param <R> some type extend u
 */
@FunctionalInterface
public interface TriFunction<T, U, P, R extends U> {

    R apply(T source, U source2, P token);

    default <V extends U> TriFunction<T, U, P, V> andThen(TriFunction<T, U, P, V> after) {
        Objects.requireNonNull(after);
        return (t, u, p) -> {
            return after.apply(t, this.apply(t, u, p), p);
        };
    }
}
