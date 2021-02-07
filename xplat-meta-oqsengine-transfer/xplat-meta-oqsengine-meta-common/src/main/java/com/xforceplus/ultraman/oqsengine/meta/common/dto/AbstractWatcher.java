package com.xforceplus.ultraman.oqsengine.meta.common.dto;

import io.grpc.stub.StreamObserver;

import java.util.HashMap;
import java.util.Map;
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
    protected String uid;

    /**
     * 上一次的心跳时间
     */
    protected volatile long heartBeat;

    /**
     * 注册的streamObserver
     */
    protected StreamObserver<T> streamObserver;

    /**
     * 当前关注的appId
     */
    protected Map<String, WatchElement> watches;

    /**
     * 当前是否已被清理状态
     */
    private volatile boolean isReleased = false;


    public AbstractWatcher(String uid, StreamObserver<T> streamObserver) {
        this.uid = uid;
        this.streamObserver = streamObserver;
        this.heartBeat = System.currentTimeMillis();
        this.watches = new HashMap<>();
    }

    @Override
    public StreamObserver<T> observer() {
        return streamObserver;
    }

    @Override
    public void addWatch(WatchElement w) {
        watches.put(w.getAppId(), w);
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
        if (!isReleased) {
            heartBeat = System.currentTimeMillis();
        }
    }

    @Override
    public <S> void release(Supplier<S> supplier) {
        canNotServer();
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
    public boolean isReleased() {
        return isReleased;
    }

    @Override
    public Map<String, WatchElement> watches() {
        return watches;
    }

    @Override
    public boolean runWithCheck(Function<StreamObserver<T>, Boolean> function) {
        if (!isReleased()) {
            return function.apply(streamObserver);
        }

        return false;
    }

    public abstract void reset(String uid, StreamObserver<T> streamObserver);

    public void canServer() {
        isReleased = true;
    }

    public void canNotServer() {
        isReleased = false;
    }
}
