package com.xforceplus.ultraman.oqsengine.meta.connect;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * desc :
 * name : GRpcClient
 *
 * @author : xujia
 * date : 2021/2/2
 * @since : 1.8
 */
public class MetaSyncGRpcClient implements GRpcClient {

    private Logger logger = LoggerFactory.getLogger(MetaSyncGRpcClient.class);

    private ManagedChannel channel;
    private EntityClassSyncGrpc.EntityClassSyncStub stub;

    private String host;
    private int port;
    private static final long heartbeatIntervalSeconds = 30;
    private static final long heartbeatTimeoutSeconds = 30;
    private static final long destroySeconds = 30;
    boolean isClientOpen;

    public MetaSyncGRpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void create() {

        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .keepAliveTime(heartbeatIntervalSeconds, TimeUnit.SECONDS)
                .keepAliveTimeout(heartbeatTimeoutSeconds, TimeUnit.SECONDS)
                .build();

        stub = EntityClassSyncGrpc.newStub(channel);

        logger.info("oqs-engine successfully connects to {}:{}!", host, port);

        isClientOpen = true;
    }

    @Override
    public void destroy() {
        try {
            channel.shutdown().awaitTermination(destroySeconds, TimeUnit.SECONDS);
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
