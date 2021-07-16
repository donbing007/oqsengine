package com.xforceplus.ultraman.oqsengine.meta;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams;
import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IDelayTaskExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.meta.connect.GRpcServer;
import com.xforceplus.ultraman.oqsengine.meta.executor.ResponseWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.executor.RetryExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncResponseHandler;
import com.xforceplus.ultraman.oqsengine.meta.mock.MockEntityClassGenerator;
import com.xforceplus.ultraman.oqsengine.meta.mock.client.MockClient;
import com.xforceplus.ultraman.oqsengine.meta.mock.client.MockerSyncClient;
import com.xforceplus.ultraman.oqsengine.meta.provider.metrics.ServerMetrics;
import com.xforceplus.ultraman.oqsengine.meta.provider.metrics.impl.DefaultServerMetrics;
import io.grpc.stub.StreamObserver;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * desc :
 * name : BaseInit
 *
 * @author : xujia
 * date : 2021/3/2
 * @since : 1.8
 */
public class BaseInit {

    private Logger logger = LoggerFactory.getLogger(BaseInit.class);

    protected EntityClassSyncServer entityClassSyncServer;

    protected ResponseWatchExecutor responseWatchExecutor;
    protected IDelayTaskExecutor<RetryExecutor.DelayTask> retryExecutor;
    protected MockEntityClassGenerator entityClassGenerator;

    protected GRpcParams gRpcParamsConfig;

    protected SyncResponseHandler syncResponseHandler;

    private GRpcServer gRpcServer;

    protected ServerMetrics serverMetrics;

    protected ExecutorService grpcExecutor;
    protected ExecutorService taskExecutor;

    protected void stopServer() {
        gRpcServer.stop();
        ExecutorHelper.shutdownAndAwaitTermination(grpcExecutor, 10);
        ExecutorHelper.shutdownAndAwaitTermination(taskExecutor, 10);
        logger.info("baseInit -> stop server ok.");
    }

    protected void initServer(int port) {
        logger.info("baseInit -> init server.");
        grpcExecutor = new ThreadPoolExecutor(5, 5, 0,
            TimeUnit.SECONDS, new LinkedBlockingDeque<>(50));

        entityClassSyncServer = entityClassSyncServer();

        serverMetrics = new DefaultServerMetrics();
        ReflectionTestUtils.setField(serverMetrics, "responseWatchExecutor", responseWatchExecutor);

        gRpcServer = new GRpcServer(port);
        ReflectionTestUtils.setField(gRpcServer, "entityClassSyncServer", entityClassSyncServer);
        ReflectionTestUtils.setField(gRpcServer, "configuration", gRpcParamsConfig);
        ReflectionTestUtils.setField(gRpcServer, "grpcExecutor", grpcExecutor);

        gRpcServer.start();

        logger.info("baseInit -> init server ok.");
    }

    protected EntityClassSyncServer entityClassSyncServer() {
        syncResponseHandler = syncResponseHandler();
        EntityClassSyncServer syncServer = new EntityClassSyncServer();
        ReflectionTestUtils.setField(syncServer, "responseHandler", syncResponseHandler);
        return syncServer;
    }

    protected SyncResponseHandler syncResponseHandler() {
        responseWatchExecutor = new ResponseWatchExecutor();
        retryExecutor = new RetryExecutor();

        entityClassGenerator = new MockEntityClassGenerator();

        gRpcParamsConfig = gRpcParamsConfig();

        taskExecutor = new ThreadPoolExecutor(5, 5, 0,
            TimeUnit.SECONDS, new LinkedBlockingDeque<>(50));

        SyncResponseHandler syncResponseHandler = new SyncResponseHandler();
        ReflectionTestUtils.setField(syncResponseHandler, "responseWatchExecutor", responseWatchExecutor);
        ReflectionTestUtils.setField(syncResponseHandler, "retryExecutor", retryExecutor);
        ReflectionTestUtils.setField(syncResponseHandler, "entityClassGenerator", entityClassGenerator);
        ReflectionTestUtils.setField(syncResponseHandler, "taskExecutor", taskExecutor);
        ReflectionTestUtils.setField(syncResponseHandler, "grpcParams", gRpcParamsConfig);

        return syncResponseHandler;
    }

    protected GRpcParams gRpcParamsConfig() {
        GRpcParams gRpcParamsConfig = new GRpcParams();
        gRpcParamsConfig.setDefaultDelayTaskDuration(30_000);
        gRpcParamsConfig.setKeepAliveSendDuration(5_000);
        gRpcParamsConfig.setReconnectDuration(5_000);
        gRpcParamsConfig.setDefaultHeartbeatTimeout(30_000);
        gRpcParamsConfig.setMonitorSleepDuration(1_000);

        return gRpcParamsConfig;
    }

    protected MockerSyncClient initClient(String host, int port) {
        MockerSyncClient mockerSyncClient = new MockerSyncClient(new MockClient());
        mockerSyncClient.start(host, port);

        return mockerSyncClient;
    }


    protected EntityClassSyncRequest buildRequest(WatchElement w, String clientId, String uid, RequestStatus requestStatus) {
        return EntityClassSyncRequest.newBuilder()
                .setClientId(clientId)
                .setEnv(w.getEnv())
                .setUid(uid)
                .setAppId(w.getAppId())
                .setVersion(w.getVersion())
                .setStatus(requestStatus.ordinal())
                .build();
    }

    public static class WatchElementVisitor {
        private WatchElement watchElement;
        private Set<Integer> visitors;

        public WatchElementVisitor(WatchElement watchElement) {
            this.watchElement = watchElement;
            visitors = new HashSet<>();
        }

        public WatchElement getWatchElement() {
            return watchElement;
        }

        public Set<Integer> getVisitors() {
            return visitors;
        }

        public void setVisitors(Integer visitors) {
            this.visitors.add(visitors);
        }
    }

    public static class StreamEvent {
        private MockerSyncClient mockerSyncClient;
        private StreamObserver<EntityClassSyncRequest> streamObserver;
        private String uid;

        public StreamEvent(MockerSyncClient mockerSyncClient, StreamObserver<EntityClassSyncRequest> streamObserver, String uid) {
            this.mockerSyncClient = mockerSyncClient;
            this.streamObserver = streamObserver;
            this.uid = uid;
        }

        public MockerSyncClient getMockerSyncClient() {
            return mockerSyncClient;
        }

        public StreamObserver<EntityClassSyncRequest> getStreamObserver() {
            return streamObserver;
        }

        public String getUid() {
            return uid;
        }

        public Map<String, WatchElement> getWatchElements() {
            return mockerSyncClient.getWatchElementMap();
        }
    }
}
