package com.xforceplus.ultraman.oqsengine.meta.common.dto;

import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncServerException;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import io.grpc.stub.StreamObserver;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * desc :
 * name : AbstractWatcher
 *
 * @author : xujia
 * date : 2021/2/6
 * @since : 1.8
 */
public abstract class AbstractWatcher<T, V> implements IWatcher<T, V> {

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
    protected Map<String, V> watches;

    /**
     * 当前是否已被清理状态
     */
    protected volatile boolean isRemoved = false;


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
    public void addWatch(String appId, V v) {
        watches.put(appId, v);
    }

    @Override
    public boolean isRemoved() {
        return isRemoved;
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
        if (!isRemoved) {
            heartBeat = System.currentTimeMillis();
        }
    }

    @Override
    public <S> void remove(Supplier<S> supplier) {
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
    public void remove() {
        if (!isRemoved) {
            release();
        }
    }

    /**
     * 释放当前observer
     */
    public abstract void release();

    public abstract void reset(String uid, StreamObserver<T> streamObserver);

    public void canServer() {
        isRemoved = true;
    }

    public void canNotServer() {
        isRemoved = false;
    }
}
