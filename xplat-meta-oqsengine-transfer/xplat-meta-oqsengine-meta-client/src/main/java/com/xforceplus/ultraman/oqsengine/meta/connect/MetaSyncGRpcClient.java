package com.xforceplus.ultraman.oqsengine.meta.connect;

import static com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams.SHUT_DOWN_WAIT_TIME_OUT;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncGrpc;
import com.xforceplus.ultraman.oqsengine.meta.utils.ClientIdUtils;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
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

    private final String clientId = ClientIdUtils.generate();

    @Override
    public void start() {

        channel = ManagedChannelBuilder.forAddress(host, port)
            .intercept(new ClientInterceptor() {
                Metadata.Key<String> metaClientId = Metadata.Key.of("clientId", Metadata.ASCII_STRING_MARSHALLER);

                @Override
                public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
                                                                           CallOptions callOptions, Channel next) {

                    return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

                        @Override
                        public void start(Listener<RespT> responseListener, Metadata headers) {
                            headers.put(metaClientId, clientId);

                            super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                                @Override
                                public void onHeaders(Metadata headers) {
                                    super.onHeaders(headers);
                                }
                            }, headers);
                        }
                    };

                }
            })
            .usePlaintext()
            .keepAliveTime(grpcParams.getDefaultHeartbeatTimeout(), TimeUnit.MILLISECONDS)
            .keepAliveTimeout(grpcParams.getDefaultHeartbeatTimeout(), TimeUnit.MILLISECONDS)
                .build();

        stub = EntityClassSyncGrpc.newStub(channel);

        logger.info("gRpc-client successfully connects to {}:{}, grpc-clientId : {}!", host, port, clientId);

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
