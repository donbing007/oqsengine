package com.xforceplus.ultraman.oqsengine.meta.executor;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.dto.ResponseWatcher;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.Set;

/**
 * desc :
 * name : IResponseWatchExecutor
 *
 * @author : xujia
 * date : 2021/2/7
 * @since : 1.8
 */
public interface IResponseWatchExecutor extends IWatchExecutor {

    void release(String uid);

    void add(String uid, StreamObserver<EntityClassSyncResponse> observer, WatchElement watchElement);

    boolean update(String uid, WatchElement watchElement);

    List<ResponseWatcher> need(WatchElement watchElement);

    ResponseWatcher watcher(String uid);

    void keepAliceCheck(long heartbeatTimeout);

    Set<String> appWatchers(String appId, String env);

    Integer version(String appId, String env);

    boolean addVersion(String appId, String env, int version);
}
