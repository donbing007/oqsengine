package com.xforceplus.ultraman.oqsengine.meta.connect;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncGrpc;
import com.xforceplus.ultraman.oqsengine.meta.mock.MockServer;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;

/**
 * desc :
 * name : MockGRpcClient
 *
 * @author : xujia
 * date : 2021/2/22
 * @since : 1.8
 */
public class MockGRpcClient implements GRpcClient {

    private ManagedChannel channel;
    private EntityClassSyncGrpc.EntityClassSyncStub stub;
    private String serverName;

    private boolean isClientOpen;

    public Server mockServer;

    @Override
    public void start() {

        serverName = InProcessServerBuilder.generateName();

        buildServer();

        buildChannel();

        stub = EntityClassSyncGrpc.newStub(channel);

        isClientOpen = true;
    }

    @Override
    public void stop() {
        isClientOpen = false;
        channel.shutdown();
        mockServer.shutdown();
    }

    @Override
    public boolean opened() {
        return isClientOpen;
    }

    private void buildServer() {
        try {
            mockServer = InProcessServerBuilder
                .forName(serverName).directExecutor().addService(new MockServer()).build().start();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private void buildChannel() {
        channel = InProcessChannelBuilder
            .forName(serverName).directExecutor().maxInboundMessageSize(1024).build();
    }

    @Override
    public EntityClassSyncGrpc.EntityClassSyncStub channelStub() {
        return stub;
    }
}
