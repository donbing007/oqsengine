package com.xforceplus.ultraman.oqsengine.meta.executor;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse;
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
    /**
     * 新增一个客户端及关注元素,如果当前的uid为一个新的uid，server将持有这个observer
     * @param uid
     * @param observer
     * @param watchElement
     */
    void add(String uid, StreamObserver<EntityClassSyncResponse> observer, WatchElement watchElement);

    /**
     * 更新watchElement
     * @param uid
     * @param watchElement
     * @return
     */
    boolean update(String uid, WatchElement watchElement);

    /**
     * 获取当前watchElement的关注者列表
     * @param watchElement
     * @return
     */
    List<ResponseWatcher> need(WatchElement watchElement);

    /**
     * 获取UID对应的关注者实例
     * @param uid
     * @return
     */
    ResponseWatcher watcher(String uid);

    /**
     * 心跳检查
     * @param heartbeatTimeout
     */
    void keepAliveCheck(long heartbeatTimeout);

    /**
     * 获取当前APP+ENV所对应的关注者UID
     * @param appId
     * @param env
     * @return
     */
    Set<String> appWatchers(String appId, String env);

    /**
     * 获取当前版本
     * @param appId
     * @param env
     * @return
     */
    Integer version(String appId, String env);

    /**
     * 新增版本
     * @param appId
     * @param env
     * @param version
     * @return
     */
    boolean addVersion(String appId, String env, int version);
}
