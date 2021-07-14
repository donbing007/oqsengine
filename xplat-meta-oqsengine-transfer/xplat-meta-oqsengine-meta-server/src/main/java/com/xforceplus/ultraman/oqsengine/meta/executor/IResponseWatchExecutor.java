package com.xforceplus.ultraman.oqsengine.meta.executor;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.dto.ResponseWatcher;
import com.xforceplus.ultraman.oqsengine.meta.dto.ServerMetricsInfo;
import io.grpc.stub.StreamObserver;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * response executor interface.
 *
 * @author xujia
 * @since 1.8
 */
public interface IResponseWatchExecutor extends IWatchExecutor {
    /**
     * 新增一个客户端及关注元素,如果当前的uid为一个新的uid，server将持有这个observer.
     */
    void add(String clientId, String uid, StreamObserver<EntityClassSyncResponse> observer, WatchElement watchElement);

    /**
     * 更新watchElement.
     */
    boolean update(String uid, WatchElement watchElement);

    /**
     * 获取当前watchElement的关注者列表.
     */
    List<ResponseWatcher> need(WatchElement watchElement);

    /**
     * 获取UID对应的关注者实例.
     */
    ResponseWatcher watcher(String uid);

    /**
     * 心跳检查.
     */
    void keepAliveCheck(long heartbeatTimeout);

    /**
     * 获取当前APP+ENV所对应的关注者UID.
     */
    Set<String> appWatchers(String appId, String env);

    /**
     * 获取当前版本.
     */
    Integer version(String appId, String env);

    /**
     * 新增版本.
     */
    boolean addVersion(String appId, String env, int version);

    /**
     * 获取当前监控指标
     * @return
     */
    Optional<ServerMetricsInfo> showMetrics();
}
