package com.xforceplus.ultraman.oqsengine.meta.executor;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.IWatcher;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.Optional;

/**
 * desc :
 * name : IWatchExecutor
 *
 * @author : xujia
 * date : 2021/2/4
 * @since : 1.8
 */
public interface IWatchExecutor<T, V> {

    void heartBeat(String uid);

    boolean update(String appId, int version, String uid);

    void add(String appId, int version, String uid, StreamObserver<T> observer);

    void remove(String uid);

    List<IWatcher<T, V>> need(String appId, int version);

    Optional<IWatcher<T, V>> watcher(String uid);

    void start();

    void stop();
}
