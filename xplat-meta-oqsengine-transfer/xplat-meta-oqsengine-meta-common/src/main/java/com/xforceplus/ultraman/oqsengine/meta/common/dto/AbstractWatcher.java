package com.xforceplus.ultraman.oqsengine.meta.common.dto;

import io.grpc.stub.StreamObserver;

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
    public StreamObserver<T> observer() {
        return streamObserver;
    }

    @Override
    public synchronized void addWatch(WatchElement w) {
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
        notServer();
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

    @Override
    public Map<String, WatchElement> watches() {
        return watches;
    }

    @Override
    public boolean runWithCheck(Function<StreamObserver<T>, Boolean> function) {
        if (onServe) {
            return function.apply(streamObserver);
        }

        return false;
    }

    public abstract void reset(String uid, StreamObserver<T> streamObserver);

    public void onServe() {
        onServe = true;
    }

    public void notServer() {
        onServe = false;
    }
}
