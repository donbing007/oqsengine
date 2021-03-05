package com.xforceplus.ultraman.oqsengine.meta.connect;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncGrpc;
import com.xforceplus.ultraman.oqsengine.meta.mock.MockServer;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Rule;

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

    @Rule
    public GrpcCleanupRule gRpcCleanup = new GrpcCleanupRule();

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
    }

    @Override
    public boolean opened() {
        return isClientOpen;
    }

    private void buildServer() {
        MockServer mockServer = new MockServer();
        try {
            gRpcCleanup.register(InProcessServerBuilder
                    .forName(serverName).directExecutor().addService(mockServer).build().start());
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private void buildChannel() {
        channel = gRpcCleanup.register(
                InProcessChannelBuilder
                        .forName(serverName).directExecutor().maxInboundMessageSize(1024).build()
        );
    }

    @Override
    public EntityClassSyncGrpc.EntityClassSyncStub channelStub() {
        return stub;
    }
}
