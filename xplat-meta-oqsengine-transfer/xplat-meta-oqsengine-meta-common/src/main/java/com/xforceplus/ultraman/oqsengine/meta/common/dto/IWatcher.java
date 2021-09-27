package com.xforceplus.ultraman.oqsengine.meta.common.dto;

import io.grpc.stub.StreamObserver;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * desc :.
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

    boolean isAlive(String uid);

    void addWatch(WatchElement watchElement);

    Map<String, WatchElement> watches();

    boolean runWithCheck(Function<StreamObserver<T>, Boolean> function);

    /**
     * 执行supplier后remove
     * @param supplier
     * @param <S>
     */
    <S> void release(Supplier<S> supplier);

    void release();

    boolean isActive();

    void active();

    void inActive();
}
