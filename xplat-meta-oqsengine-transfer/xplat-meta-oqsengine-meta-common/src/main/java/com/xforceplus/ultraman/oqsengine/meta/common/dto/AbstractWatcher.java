package com.xforceplus.ultraman.oqsengine.meta.common.dto;

import io.grpc.stub.StreamObserver;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象watcher.
 *
 * @author xujia
 * @since 1.8
 */
public abstract class AbstractWatcher<T> implements IWatcher<T> {

    private final Logger logger = LoggerFactory.getLogger(AbstractWatcher.class);
    /**
     * 客户标识.
     */
    protected volatile String clientId;

    /**
     * 注册的uid.
     */
    protected volatile String uid;

    /**
     * 上一次的心跳时间.
     */
    protected volatile long heartBeat;

    /**
     * 注册的streamObserver.
     */
    protected volatile StreamObserver<T> streamObserver;

    /**
     * 当前关注的appId
     * 注意：这个Map对应一个(OQS应用、SDK端)的所有关注列表，
     * 不能出现同一个AppID对应多套环境同时运行在一个(OQS应用、SDK端)
     * 所以这里的Key只是AppId.
     */
    protected Map<String, WatchElement> watches;

    /**
     * 当前是否已被清理状态.
     */
    private volatile boolean isActive = true;

    /**
     * 构造函数.
     */
    public AbstractWatcher(String clientId, String uid, StreamObserver<T> streamObserver) {
        this.clientId = clientId;
        this.uid = uid;
        this.streamObserver = streamObserver;
        this.heartBeat = System.currentTimeMillis();
        this.watches = new ConcurrentHashMap<>();
    }

    @Override
    public String clientId() {
        return clientId;
    }

    @Override
    public void addWatch(WatchElement w, boolean force) {
        if (force) {
            watches.put(w.getAppId(), w);
        } else {
            watches.putIfAbsent(w.getAppId(), w);
        }
    }

    @Override
    public String uid() {
        return uid;
    }

    @Override
    public long heartBeat() {
        return heartBeat;
    }

    @Override
    public void resetHeartBeat() {
        if (isActive) {
            heartBeat = System.currentTimeMillis();
        }
    }

    @Override
    public <S> void release(Supplier<S> supplier) {
        inActive();
        try {
            supplier.get();
        } catch (Exception e) {
            //  ignore

        } finally {
            //  释放当前observer
            release();
        }
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public Map<String, WatchElement> watches() {
        return watches;
    }

    @Override
    public boolean runWithCheck(Function<StreamObserver<T>, Boolean> function) {
        if (isActive) {
            return function.apply(streamObserver);
        }
        logger.warn("uid [{}], offServe...", uid);
        return false;
    }

    @Override
    public StreamObserver<T> observer() {
        return streamObserver;
    }

    protected abstract void reset(String uid, StreamObserver<T> streamObserver);

    /**
     * 释放.
     */
    protected void releaseStreamObserver() {
        try {
            if (null != streamObserver) {
                streamObserver.onCompleted();
            }
        } catch (Exception e) {
            //  ignore
        }
    }

    @Override
    public void active() {
        isActive = true;
        //  打开服务时设置一次heartbeat
        resetHeartBeat();
    }

    @Override
    public void inActive() {
        isActive = false;
    }
}
