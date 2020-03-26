package com.xforceplus.ultraman.oqsengine.sdk.service.flow;

import java.util.function.Function;

@FunctionalInterface
public interface DSLFlow<T , R> extends Function<T, R> {

}
