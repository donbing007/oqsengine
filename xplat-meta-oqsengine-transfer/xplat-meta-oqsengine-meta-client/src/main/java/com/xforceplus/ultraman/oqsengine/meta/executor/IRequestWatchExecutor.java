package com.xforceplus.ultraman.oqsengine.meta.executor;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.dto.RequestWatcher;
import io.grpc.stub.StreamObserver;

import java.util.Queue;
import java.util.function.Function;

/**
 * desc :
 * name : IRequestWatchExecutor
 *
 * @author : xujia
 * date : 2021/2/7
 * @since : 1.8
 */
public interface IRequestWatchExecutor extends IWatchExecutor {

    void resetHeartBeat();

    void create(String uid, StreamObserver<EntityClassSyncRequest> observer);

    void add(WatchElement watchElement);

    boolean update(WatchElement watchElement);

    RequestWatcher watcher();

    boolean canAccess(String uid);

    void addForgot(String appId, int version);

    Queue<WatchElement> forgot();

    Function<String, Boolean> accessFunction();
}
