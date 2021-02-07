package com.xforceplus.ultraman.oqsengine.meta.common.dto;

import io.grpc.stub.StreamObserver;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * desc :
 * name : IWatch
 *
 * @author : xujia
 * date : 2021/2/4
 * @since : 1.8
 */
public interface IWatcher<T> {

    String uid();

    long heartBeat();

    void resetHeartBeat();

    StreamObserver<T> observer();

    boolean onWatch(WatchElement watchElement);

    void addWatch(WatchElement watchElement);

    Map<String, WatchElement> watches();

    /**
     * 执行supplier后remove
     * @param supplier
     * @param <S>
     */
    <S> void release(Supplier<S> supplier);

    void release();

    boolean isReleased();

    boolean runWithCheck(Function<StreamObserver<T>, Boolean> function);

}
