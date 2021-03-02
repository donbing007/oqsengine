package com.xforceplus.ultraman.oqsengine.meta.mock.client;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncGrpc;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;

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
                InProcessChannelBuilder
                        .forAddress(host, port)
                        .directExecutor()
                        .maxInboundMessageSize(1024)
                        .build();
    }

    public EntityClassSyncGrpc.EntityClassSyncStub channelStub() {
        return stub;
    }
}
