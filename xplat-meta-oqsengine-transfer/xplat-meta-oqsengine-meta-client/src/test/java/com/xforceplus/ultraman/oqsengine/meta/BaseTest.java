package com.xforceplus.ultraman.oqsengine.meta;

import com.google.protobuf.InvalidProtocolBufferException;
import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.EntityClassStorageHelper;
import com.xforceplus.ultraman.oqsengine.meta.executor.RequestWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;

/**
 * desc :
 * name : BaseTest
 *
 * @author : xujia
 * date : 2021/3/4
 * @since : 1.8
 */
public class BaseTest {

    protected IRequestHandler requestHandler;

    protected GRpcParams gRpcParams;

    protected RequestWatchExecutor requestWatchExecutor;

    protected ExecutorService executorService;

    protected void baseInit() {
        gRpcParams = gRpcParamsConfig();
        requestWatchExecutor = requestWatchExecutor();

        requestHandler = requestHandler();
    }

    protected RequestWatchExecutor requestWatchExecutor() {
        RequestWatchExecutor requestWatchExecutor = new RequestWatchExecutor();
        return requestWatchExecutor;
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

    protected IRequestHandler requestHandler() {
        IRequestHandler requestHandler = new SyncRequestHandler();

        SyncExecutor syncExecutor = new SyncExecutor() {
            final Map<String, Integer> stringIntegerMap = new HashMap<>();

            @Override
            public boolean sync(String appId, int version, EntityClassSyncRspProto entityClassSyncRspProto) {
                stringIntegerMap.put(appId, version);
                return true;
            }

            @Override
            public boolean dataImport(String appId, int version, String content) {
                try {
                    EntityClassStorageHelper.toEntityClassSyncRspProto(content);
                    return true;
                } catch (InvalidProtocolBufferException e) {
                    return false;
                }
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
        ReflectionTestUtils.setField(requestHandler, "grpcParams", gRpcParams);
        ReflectionTestUtils.setField(requestHandler, "executorService", executorService);

        return requestHandler;
    }
}
