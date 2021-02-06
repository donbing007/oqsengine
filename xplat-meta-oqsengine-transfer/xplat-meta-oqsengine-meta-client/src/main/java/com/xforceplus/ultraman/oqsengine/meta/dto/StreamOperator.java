package com.xforceplus.ultraman.oqsengine.meta.dto;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import io.grpc.stub.StreamObserver;

/**
 * desc :
 * name : StreamOperator
 *
 * @author : xujia
 * date : 2021/2/5
 * @since : 1.8
 */
public class StreamOperator {
    private String uid;
    private StreamObserver<EntityClassSyncRequest> observer;
    private long heartBeat;

    public StreamOperator(String uid, StreamObserver<EntityClassSyncRequest> observer, long heartBeat) {
        this.uid = uid;
        this.observer = observer;
        this.heartBeat = heartBeat;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public StreamObserver<EntityClassSyncRequest> getObserver() {
        return observer;
    }

    public void setObserver(StreamObserver<EntityClassSyncRequest> observer) {
        this.observer = observer;
    }

    public long getHeartBeat() {
        return heartBeat;
    }

    public void setHeartBeat(long heartBeat) {
        this.heartBeat = heartBeat;
    }
}
