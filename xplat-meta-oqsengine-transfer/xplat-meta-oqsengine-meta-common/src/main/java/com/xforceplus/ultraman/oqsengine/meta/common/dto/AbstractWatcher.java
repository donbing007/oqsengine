package com.xforceplus.ultraman.oqsengine.meta.common.dto;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * desc :
 * name : AbstractWatcher
 *
 * @author : xujia
 * date : 2021/2/6
 * @since : 1.8
 */
public abstract class AbstractWatcher<T> implements IWatcher<T> {

    private Logger logger = LoggerFactory.getLogger(AbstractWatcher.class);
    /**
     * 注册的uid;
     */
    protected volatile String uid;

    /**
     * 上一次的心跳时间
     */
    protected volatile long heartBeat;

    /**
     * 注册的streamObserver
     */
    protected volatile StreamObserver<T> streamObserver;

    /**
     * 当前关注的appId
     * 注意：这个Map对应一个(OQS应用、SDK端)的所有关注列表，
     * 不能出现同一个AppID对应多套环境同时运行在一个(OQS应用、SDK端)
     * 所以这里的Key只是AppId
     */
    protected Map<String, WatchElement> watches;

    /**
     * 当前是否已被清理状态
     */
    private volatile boolean onServe = true;


    public AbstractWatcher(String uid, StreamObserver<T> streamObserver) {
        this.uid = uid;
        this.streamObserver = streamObserver;
        this.heartBeat = System.currentTimeMillis();
        this.watches = new ConcurrentHashMap<>();
    }

    @Override
    public void addWatch(WatchElement w) {
        watches.putIfAbsent(w.getAppId(), w);
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
        if (onServe) {
            heartBeat = System.currentTimeMillis();
        }
    }

    @Override
    public <S> void release(Supplier<S> supplier) {
        offServe();
        try {
            supplier.get();
        } catch (Exception e) {
            //  ignore

        } finally {
            /**
             * 释放当前observer
             */
            release();
        }
    }

    @Override
    public boolean isOnServe() {
        return onServe;
    }

    /**
     * @return
     */
    @Override
    public Map<String, WatchElement> watches() {
        return watches;
    }

    @Override
    public boolean runWithCheck(Function<StreamObserver<T>, Boolean> function) {
        if (onServe) {
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

    @Override
    public void release() {
        try {
            if (null != streamObserver) {
                streamObserver.onCompleted();
            }
        } catch (Exception e) {
            //  ignore
        }
    }

    public void onServe() {
        onServe = true;
        /**
         * 打开服务时设置一次heartbeat
         */
        resetHeartBeat();
    }

    public void offServe() {
        onServe = false;
    }
}
