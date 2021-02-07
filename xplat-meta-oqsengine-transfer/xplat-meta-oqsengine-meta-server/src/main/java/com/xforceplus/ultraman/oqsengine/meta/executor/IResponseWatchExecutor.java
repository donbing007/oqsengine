package com.xforceplus.ultraman.oqsengine.meta.executor;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.IWatcher;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.Optional;

/**
 * desc :
 * name : IResponseWatchExecutor
 *
 * @author : xujia
 * date : 2021/2/7
 * @since : 1.8
 */
public interface IResponseWatchExecutor {

    void heartBeat(String uid);

    void release(String uid);

    void add(String uid, StreamObserver<EntityClassSyncResponse> observer, WatchElement watchElement);

    boolean update(String uid, WatchElement watchElement);

    List<IWatcher<EntityClassSyncResponse>> need(WatchElement watchElement);

    Optional<IWatcher<EntityClassSyncResponse>> watcher(String uid);
}
