package com.xforceplus.ultraman.oqsengine.meta.connect;

import static com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams.SHUT_DOWN_WAIT_TIME_OUT;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * grpc client implements.
 *
 * @author xujia
 * @since 1.8
 */
public class MetaSyncGRpcClient implements GRpcClient {

    private final Logger logger = LoggerFactory.getLogger(MetaSyncGRpcClient.class);

    @Resource
    private GRpcParams grpcParams;

    private ManagedChannel channel;
    private EntityClassSyncGrpc.EntityClassSyncStub stub;

    private final String host;
    private final int port;
    private boolean isClientOpen;

    public MetaSyncGRpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void start() {

        channel = ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext()
            .keepAliveTime(grpcParams.getDefaultHeartbeatTimeout(), TimeUnit.MILLISECONDS)
            .keepAliveTimeout(grpcParams.getDefaultHeartbeatTimeout(), TimeUnit.MILLISECONDS)
                .build();

        stub = EntityClassSyncGrpc.newStub(channel);

        logger.info("gRpc-client successfully connects to {}:{}!", host, port);

        isClientOpen = true;
    }

    @Override
    public void stop() {
        try {
            channel.shutdown().awaitTermination(SHUT_DOWN_WAIT_TIME_OUT, TimeUnit.SECONDS);

            logger.info("gRpc-client destroy!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            isClientOpen = false;
        }
    }

    @Override
    public boolean opened() {
        return isClientOpen;
    }

    @Override
    public EntityClassSyncGrpc.EntityClassSyncStub channelStub() {
        return stub;
    }
}
