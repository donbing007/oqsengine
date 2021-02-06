package com.xforceplus.ultraman.oqsengine.meta.common.dto;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
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
public interface IWatcher<T, V> {

    String uid();

    long heartBeat();

    void resetHeartBeat();

    StreamObserver<T> observer();

    boolean onWatch(String appId, Integer version);

    void addWatch(String appId, V v);

    Map<String, V> watches();

    /**
     * 执行supplier后remove
     * @param supplier
     * @param <S>
     */
    <S> void remove(Supplier<S> supplier);

    void remove();

    boolean isRemoved();

    boolean runWithCheck(Function<StreamObserver<T>, Boolean> function);

}
