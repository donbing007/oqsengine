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
    public void resetHeartBeat(String uid) {
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
    public boolean canAccess(String uid) {
        /**
         * 判断是否可用
         */
        if (null != uid && null != requestWatcher && requestWatcher.isOnServe()) {
            try {
                return uid.equals(requestWatcher.uid());
            } catch (Exception e) {
                /**
                 * 兜底瞬间将uid置为null的逻辑.
                 */
                return false;
            }
        }
        return false;
    }

    @Override
    public void addForgot(WatchElement watchElement) {
        forgotQueue.add(watchElement);
    }

    @Override
    public Queue<WatchElement> forgot() {
        return forgotQueue;
    }

    @Override
    public void start() {
        /**
         * 1.添加keepAlive任务线程
         */
        executors.add(ThreadUtils.create(this::keepAliveTask));

        /**
         * 1.添加stream-connect-check任务线程
         */
        executors.add(ThreadUtils.create(this::streamConnectCheckTask));

        /**
         * 3.添加AppCheck任务线程
         */
        executors.add(ThreadUtils.create(this::appCheckTask));

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

    /**
     * 保持和服务端的KeepAlive
     * @return
     */
    private boolean keepAliveTask() {
        logger.info("start keepAlive task ok...");
        while (!isShutDown()) {
            if (requestWatcher.isOnServe()) {
                EntityClassSyncRequest request = EntityClassSyncRequest.newBuilder()
                        .setUid(requestWatcher.uid()).setStatus(HEARTBEAT.ordinal()).build();

                try {
                    sendRequest(requestWatcher, request);
                } catch (Exception e) {
                    //  ignore
                    logger.warn("send keepAlive failed, message [{}], but exception will ignore due to retry...", e.getMessage());
                }

            }
            logger.debug("keepAlive ok, next check after duration ({})ms...", gRpcParamsConfig.getKeepAliveSendDuration());
            TimeWaitUtils.wakeupAfter(gRpcParamsConfig.getKeepAliveSendDuration(), TimeUnit.MILLISECONDS);
        }
        logger.info("keepAlive task has quited due to sync-client shutdown...");
        return true;
    }

    /**
     * 检查当前的KeepAlive是否已超时
     * @return
     */
    private boolean streamConnectCheckTask() {
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
            logger.debug("streamConnect check ok, next check after duration ({})ms...", gRpcParamsConfig.getMonitorSleepDuration());
            TimeWaitUtils.wakeupAfter(gRpcParamsConfig.getMonitorSleepDuration(), TimeUnit.MILLISECONDS);
        }
        logger.info("streamConnect check task has quited due to sync-client shutdown...");
        return true;
    }

    /**
     * 检查当前APP的注册状态，处于INIT、REGISTER的APP将被重新注册到服务端
     * @return
     */
    private boolean appCheckTask() {
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
                        //  ignore & break
                        break;
                    }
                    checkSize--;
                }

                requestWatcher.watches().values().stream().filter(s -> {
                    /**
                     * 过滤出INIT、已经长时间处于REGISTER的watchElement
                     * 时间超长的判定标准为当前时间-最后注册时间 > delay时间
                     */
                    return s.getStatus().ordinal() == WatchElement.AppStatus.Init.ordinal() ||
                            (s.getStatus().ordinal() < WatchElement.AppStatus.Confirmed.ordinal() &&
                                    System.currentTimeMillis() - s.getRegisterTime() > gRpcParamsConfig.getDefaultDelayTaskDuration());
                }).forEach(
                        k -> {
                            EntityClassSyncRequest.Builder builder = EntityClassSyncRequest.newBuilder();

                            EntityClassSyncRequest entityClassSyncRequest =
                                    builder.setUid(requestWatcher.uid()).setAppId(k.getAppId()).setVersion(k.getVersion())
                                            .setStatus(RequestStatus.REGISTER.ordinal()).build();
                            try {
                                sendRequest(requestWatcher, entityClassSyncRequest, accessFunction(), requestWatcher.uid());
                                k.setRegisterTime(System.currentTimeMillis());
                            } catch (Exception e) {
                                k.setStatus(WatchElement.AppStatus.Init);
                            }
                        }
                );

                logger.debug("app check ok, next check after duration ({})ms...", gRpcParamsConfig.getMonitorSleepDuration());
            }
            TimeWaitUtils.wakeupAfter(gRpcParamsConfig.getMonitorSleepDuration(), TimeUnit.MILLISECONDS);
        }
        logger.info("appCheck task has quited due to sync-client shutdown...");

        return true;
    }
}
