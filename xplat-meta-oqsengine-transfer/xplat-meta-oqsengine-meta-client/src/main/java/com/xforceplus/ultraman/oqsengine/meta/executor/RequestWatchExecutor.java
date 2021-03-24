package com.xforceplus.ultraman.oqsengine.meta.executor;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.dto.RequestWatcher;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.concurrent.TimeUnit;

import static com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams.SHUT_DOWN_WAIT_TIME_OUT;

/**
 * desc :
 * name : RequestWatchExecutor
 *
 * @author : xujia
 * date : 2021/2/7
 * @since : 1.8
 */
public class RequestWatchExecutor implements IRequestWatchExecutor {

    final Logger logger = LoggerFactory.getLogger(RequestWatchExecutor.class);

    private RequestWatcher requestWatcher;

    @Override
    public void resetHeartBeat(String uid) {
        requestWatcher.resetHeartBeat();
    }

    @Override
    public void release(String uid) {
        if (null != requestWatcher) {
            requestWatcher.release();
        }
    }

    @Override
    public void create(String uid, StreamObserver<EntityClassSyncRequest> observer) {
        if (null == requestWatcher) {
            requestWatcher = new RequestWatcher(uid, observer);
        } else {
            requestWatcher.reset(uid, observer);
        }
    }

    @Override
    public void add(WatchElement watchElement) {
        if (null != requestWatcher) {
            requestWatcher.addWatch(watchElement);
        }
    }

    @Override
    public synchronized boolean update(WatchElement watchElement) {
        if (null != requestWatcher && requestWatcher.onWatch(watchElement)) {
            requestWatcher.watches().put(watchElement.getAppId(), watchElement);
            return true;
        }
        return false;
    }

    @Override
    public RequestWatcher watcher() {
        return requestWatcher;
    }

    @Override
    public void active() {
        if (null != requestWatcher) {
            requestWatcher.active();
        }
    }

    @Override
    public void inActive() {
        if (null != requestWatcher) {
            requestWatcher.inActive();
        }
    }

    @Override
    public boolean isAlive(String uid) {
        if (null == requestWatcher) {
            return false;
        }

        return requestWatcher.isAlive(uid);
    }

    @Override
    public void start() {
        logger.debug("requestWatchExecutor start.");
    }

    @Override
    public void stop() {
        if (null != requestWatcher) {
            /**
             * 这里分开设置、等待3S如果有正在进行中的任务
             */
            requestWatcher.inActive();

            TimeWaitUtils.wakeupAfter(SHUT_DOWN_WAIT_TIME_OUT, TimeUnit.SECONDS);

            requestWatcher.release();
        }
        logger.info("requestWatchExecutor stop.");
    }
}
