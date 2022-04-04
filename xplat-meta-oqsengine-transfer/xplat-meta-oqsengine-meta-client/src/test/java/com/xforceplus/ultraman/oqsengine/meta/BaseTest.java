package com.xforceplus.ultraman.oqsengine.meta;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.executor.RequestWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import com.xforceplus.ultraman.oqsengine.meta.utils.ClientIdUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * desc :
 * name : BaseTest.
 *
 * @author : xujia 2021/3/4
 * @since : 1.8
 */
public class BaseTest {

    protected IRequestHandler requestHandler;

    protected GRpcParams grpcParams;

    protected RequestWatchExecutor requestWatchExecutor;

    protected ExecutorService executorService;

    protected static String testClientId;

    protected void baseInit() {
        testClientId = ClientIdUtils.generate();

        grpcParams = grpcParamsConfig();
        requestWatchExecutor = requestWatchExecutor();

        requestHandler = requestHandler();
    }

    protected RequestWatchExecutor requestWatchExecutor() {
        RequestWatchExecutor requestWatchExecutor = new RequestWatchExecutor();
        return requestWatchExecutor;
    }

    protected GRpcParams grpcParamsConfig() {
        GRpcParams grpcParamsConfig = new GRpcParams();
        grpcParamsConfig.setDefaultDelayTaskDuration(30_000);
        grpcParamsConfig.setKeepAliveSendDuration(5_000);
        grpcParamsConfig.setReconnectDuration(5_000);
        grpcParamsConfig.setDefaultHeartbeatTimeout(30_000);
        grpcParamsConfig.setMonitorSleepDuration(1_000);

        return grpcParamsConfig;
    }

    protected IRequestHandler requestHandler() {
        IRequestHandler requestHandler = new SyncRequestHandler();

        SyncExecutor syncExecutor = new SyncExecutor() {
            final Map<String, Integer> stringIntegerMap = new HashMap<>();

            @Override
            public void sync(String appId, String env, int version, EntityClassSyncRspProto entityClassSyncRspProto) {
                stringIntegerMap.put(appId, version);
            }

            @Override
            public int version(String appId) {
                Integer version = stringIntegerMap.get(appId);
                if (null == version) {
                    return NOT_EXIST_VERSION;
                }
                return version;
            }
        };

        executorService = new ThreadPoolExecutor(5, 5, 0,
            TimeUnit.SECONDS, new LinkedBlockingDeque<>(50));

        ReflectionTestUtils.setField(requestHandler, "syncExecutor", syncExecutor);
        ReflectionTestUtils.setField(requestHandler, "requestWatchExecutor", requestWatchExecutor);
        ReflectionTestUtils.setField(requestHandler, "grpcParams", grpcParams);
        ReflectionTestUtils.setField(requestHandler, "executorService", executorService);

        return requestHandler;
    }
}
