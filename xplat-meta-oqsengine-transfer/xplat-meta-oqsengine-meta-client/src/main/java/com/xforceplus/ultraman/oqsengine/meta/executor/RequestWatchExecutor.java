package com.xforceplus.ultraman.oqsengine.meta.executor;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig;
import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ThreadUtils;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.dto.RequestWatcher;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.xforceplus.ultraman.oqsengine.meta.EntityClassSyncClient.isShutDown;
import static com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig.SHUT_DOWN_WAIT_TIME_OUT;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.HEARTBEAT;
import static com.xforceplus.ultraman.oqsengine.meta.constant.ClientConstant.CLIENT_TASK_COUNT;
import static com.xforceplus.ultraman.oqsengine.meta.utils.SendUtils.sendRequest;

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

    private Queue<WatchElement> forgotQueue = new ConcurrentLinkedDeque<>();

    private List<Thread> executors = new ArrayList<>(CLIENT_TASK_COUNT);

    @Resource
    private GRpcParamsConfig gRpcParamsConfig;

    @Override
    public void resetHeartBeat() {
        requestWatcher.resetHeartBeat();
    }

    @Override
    public void create(String uid, StreamObserver<EntityClassSyncRequest> observer) {
        if (null == requestWatcher) {
            requestWatcher = new RequestWatcher(uid, observer);
            this.start();
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
        if (requestWatcher.onWatch(watchElement)) {
            WatchElement we = requestWatcher.watches().get(watchElement.getAppId());
            if (null != we) {
                we.setStatus(watchElement.getStatus());
                we.setVersion(watchElement.getVersion());
            } else {
                requestWatcher.addWatch(watchElement);
            }
            return true;
        }
        return false;
    }


    @Override
    public void release() {
        if (null != requestWatcher && requestWatcher.isOnServe()) {
            requestWatcher.release();
        }
    }

    @Override
    public RequestWatcher watcher() {
        return requestWatcher;
    }

    @Override
    public boolean canAccess(String uid) {
        /**
         * 判断是否可用
         */
        boolean status = (null != requestWatcher && requestWatcher.isOnServe());
        if (null != uid && status) {
            try {
                return uid.equals(requestWatcher.uid());
            } catch (Exception e) {
                /**
                 * 兜底瞬间将uid置为null的逻辑.
                 */
                return false;
            }
        }
        return status;
    }

    @Override
    public void addForgot(String appId, int version) {
        forgotQueue.add(new WatchElement(appId, version, WatchElement.AppStatus.Init));
    }

    @Override
    public void start() {
        /**
         * 1.添加keepAlive任务线程
         */
        executors.add(ThreadUtils.create(() -> {
            logger.info("start keepAlive task ok...");
            while (!isShutDown()) {
                if (requestWatcher.isOnServe()) {
                    EntityClassSyncRequest request = EntityClassSyncRequest.newBuilder()
                            .setUid(requestWatcher.uid()).setStatus(HEARTBEAT.ordinal()).build();

                    try {
                        sendRequest(requestWatcher, request);
                        logger.debug("send keepAlive ok...");
                    } catch (Exception e) {
                        //  ignore
                        logger.warn("send keepAlive failed, but exception will ignore due to retry...");
                    }

                }
                TimeWaitUtils.wakeupAfter(gRpcParamsConfig.getKeepAliveSendDuration(), TimeUnit.MILLISECONDS);
            }
            logger.info("keepAlive task has quited due to sync-client shutdown...");
            return true;
        }));

        /**
         * 1.添加stream-connect-check任务线程
         */
        executors.add(ThreadUtils.create(() -> {
            logger.info("start stream-connect-check task ok...");
            while (!isShutDown()) {
                if (requestWatcher.isOnServe()) {
                    if (System.currentTimeMillis() - requestWatcher.heartBeat() >
                            gRpcParamsConfig.getDefaultHeartbeatTimeout()) {
                        try {
                            requestWatcher.observer().onCompleted();
                            logger.warn("last heartbeat time [{}] reaches max timeout [{}]"
                                    , System.currentTimeMillis() - requestWatcher.heartBeat(),
                                    gRpcParamsConfig.getDefaultHeartbeatTimeout());
                        } catch (Exception e) {
                            //ignore
                        }

                    }
                }
                TimeWaitUtils.wakeupAfter(gRpcParamsConfig.getMonitorSleepDuration(), TimeUnit.MILLISECONDS);
            }
            logger.info("stream-connect-check task has quited due to sync-client shutdown...");
            return true;
        }));

        /**
         * 3.添加AppCheck任务线程
         */
        executors.add(ThreadUtils.create(() -> {
            logger.info("start appCheck task ok...");
            while (!isShutDown()) {
                if (requestWatcher.isOnServe()) {
                    /**
                     * 将forgetQueue中的数据添加到watch中
                     */
                    int checkSize = forgotQueue.size();
                    while (checkSize > 0) {
                        try {
                            WatchElement w = forgotQueue.remove();
                            if (!requestWatcher.watches().containsKey(w.getAppId())) {
                                requestWatcher.watches().put(w.getAppId(), w);
                            }
                        } catch (Exception e) {
                            //  ignore
                        }
                        checkSize--;
                    }

                    requestWatcher.watches().values().stream().filter(s -> {
                        return s.getStatus().ordinal() == WatchElement.AppStatus.Init.ordinal() ||
                                (s.getStatus().ordinal() < WatchElement.AppStatus.Confirmed.ordinal() &&
                                        System.currentTimeMillis() - s.getRegisterTime() > gRpcParamsConfig.getDefaultDelayTaskDuration());
                    }).forEach(
                            k -> {
                                EntityClassSyncRequest.Builder builder = EntityClassSyncRequest.newBuilder();

                                EntityClassSyncRequest entityClassSyncRequest =
                                        builder.setUid(requestWatcher.uid()).setAppId(k.getAppId()).setVersion(k.getVersion())
                                                .setStatus(RequestStatus.REGISTER.ordinal()).build();

                                sendRequest(requestWatcher, entityClassSyncRequest, accessFunction(), requestWatcher.uid());
                            }
                    );

                    logger.debug("app check ok...");
                }
                TimeWaitUtils.wakeupAfter(gRpcParamsConfig.getMonitorSleepDuration(), TimeUnit.MILLISECONDS);
            }
            logger.info("appCheck task has quited due to sync-client shutdown...");

            return true;
        }));

        /**
         * 启动所有任务线程
         */
        executors.forEach(Thread::start);
    }

    @Override
    public void stop() {
        if (null != requestWatcher) {
            /**
             * 这里分开设置、等待3S如果有正在进行中的任务
             */
            requestWatcher.notServer();

            TimeWaitUtils.wakeupAfter(SHUT_DOWN_WAIT_TIME_OUT, TimeUnit.SECONDS);

            requestWatcher.release();

            executors.forEach(s -> {
                ThreadUtils.shutdown(s, SHUT_DOWN_WAIT_TIME_OUT);
            });
        }
    }

    public Function<String, Boolean> accessFunction() {
        return this::canAccess;
    }
}
