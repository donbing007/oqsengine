package com.xforceplus.ultraman.oqsengine.meta.mock.client;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;


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
        isClientOpen = true;
    }

    public void stop() throws InterruptedException {
        isClientOpen = false;
        channel.shutdown();
        if (!channel.isShutdown()) {
            Thread.sleep(2_000);
            channel.shutdownNow();
        }
        channel = null;
        stub = null;
    }

    public boolean opened() {
        return isClientOpen;
    }

    private void buildChannel(String host, int port) {
        channel =
                ManagedChannelBuilder.forAddress(host, port)
                        .usePlaintext()
                        .build();

        stub = EntityClassSyncGrpc.newStub(channel);
    }

    public EntityClassSyncGrpc.EntityClassSyncStub channelStub() {
        return stub;
    }
}
