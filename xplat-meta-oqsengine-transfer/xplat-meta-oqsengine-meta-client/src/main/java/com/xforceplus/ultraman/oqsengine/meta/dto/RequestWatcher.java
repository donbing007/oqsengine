package com.xforceplus.ultraman.oqsengine.meta.dto;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.AbstractWatcher;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * request watcher.
 *
 * @author xujia
 * @since 1.8
 */
public class RequestWatcher extends AbstractWatcher<EntityClassSyncRequest> {

    private final Logger logger = LoggerFactory.getLogger(RequestWatcher.class);


    public RequestWatcher(String clientId, String uid, StreamObserver<EntityClassSyncRequest> observer) {
        super(clientId, uid, observer);
    }

    /**
     * 重置RequestWatcher.
     */
    public void reset(String uid, StreamObserver<EntityClassSyncRequest> streamObserver) {
        super.uid = uid;
        super.streamObserver = streamObserver;
        super.heartBeat = System.currentTimeMillis();
        watches().values().forEach(WatchElement::reset);
    }

    /**
     * param中的watchElement是否被关注.
     */
    @Override
    public boolean onWatch(WatchElement watchElement) {
        WatchElement v = watches.get(watchElement.getAppId());
        if (null == v) {
            return false;
        } else if (!watchElement.getEnv().equals(v.getEnv())) {
            logger.warn("current appId {}, onWatch-env {} not equals to params-env {}",
                v.getAppId(), v.getEnv(), watchElement.getEnv());
            return false;
        }

        //  当前版本小于输入版本或当前版本
        return v.getVersion() < watchElement.getVersion()
            || (v.getVersion() == watchElement.getVersion() &&
            v.getStatus().ordinal() < watchElement.getStatus().ordinal());
    }

    /**
     * 当前requestWatcher是否处于活动状态.
     */
    @Override
    public boolean isAlive(String uid) {
        //  判断是否可用
        if (null != uid && isActive()) {
            try {
                return uid.equals(this.uid());
            } catch (Exception e) {
                //  兜底瞬间将uid置为null的逻辑.
                return false;
            }
        }
        return false;
    }

    /**
     * 释放.
     */
    @Override
    public void release() {
        uid = null;
        releaseStreamObserver();
    }
}
