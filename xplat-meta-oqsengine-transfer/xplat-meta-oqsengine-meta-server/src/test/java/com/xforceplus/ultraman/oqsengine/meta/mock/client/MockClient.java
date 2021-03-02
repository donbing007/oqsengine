package com.xforceplus.ultraman.oqsengine.meta.mock.client;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;

/**
 * desc :
 * name : MockClient
 *
 * @author : xujia
 * date : 2021/3/2
 * @since : 1.8
 */
public class MockClient {

    private ManagedChannel channel;
    private EntityClassSyncGrpc.EntityClassSyncStub stub;

    private boolean isClientOpen;

    public void start(String host, int port) {

        buildChannel(host, port);

        stub = EntityClassSyncGrpc.newStub(channel);

        isClientOpen = true;
    }

    public void stop() {
        isClientOpen = false;
        channel.shutdown();
    }

    public boolean opened() {
        return isClientOpen;
    }

    private void buildChannel(String host, int port) {
        channel =
                ManagedChannelBuilder.forAddress(host, port)
                        .usePlaintext()
                        .keepAliveTime(30_000, TimeUnit.MILLISECONDS)
                        .keepAliveTimeout(30_000, TimeUnit.MILLISECONDS)
                        .build();
    }

    public EntityClassSyncGrpc.EntityClassSyncStub channelStub() {
        return stub;
    }
}
