package com.xforceplus.ultraman.oqsengine.meta.handler;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.executor.RequestWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import io.grpc.stub.StreamObserver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.SYNC_FAIL;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus.SYNC_OK;
import static com.xforceplus.ultraman.oqsengine.meta.utils.EntityClassSyncResponseBuilder.entityClassSyncResponseGenerator;
import static com.xforceplus.ultraman.oqsengine.meta.utils.EntityClassSyncResponseBuilder.mockSelfFatherAncestorsGenerate;

/**
 * desc :
 * name : SyncRequestHandlerTest
 *
 * @author : xujia
 * date : 2021/2/24
 * @since : 1.8
 */
public class SyncRequestHandlerTest {

    private IRequestHandler requestHandler;

    private RequestWatchExecutor requestWatchExecutor;

    private GRpcParamsConfig gRpcParamsConfig;

    @Before
    public void before() {
        gRpcParamsConfig = gRpcParamsConfig();
        requestWatchExecutor = requestWatchExecutor();
        requestHandler = requestHandler();
    }

    @After
    public void after() {
        requestWatchExecutor.stop();
    }

    private GRpcParamsConfig gRpcParamsConfig() {
        GRpcParamsConfig gRpcParamsConfig = new GRpcParamsConfig();
        gRpcParamsConfig.setDefaultDelayTaskDuration(30_000);
        gRpcParamsConfig.setKeepAliveSendDuration(5_000);
        gRpcParamsConfig.setReconnectDuration(5_000);
        gRpcParamsConfig.setDefaultHeartbeatTimeout(30_000);
        gRpcParamsConfig.setMonitorSleepDuration(1_000);

        return gRpcParamsConfig;
    }

    private RequestWatchExecutor requestWatchExecutor() {
        RequestWatchExecutor requestWatchExecutor = new RequestWatchExecutor();
        ReflectionTestUtils.setField(requestWatchExecutor, "gRpcParamsConfig", gRpcParamsConfig);
        return requestWatchExecutor;
    }

    private IRequestHandler requestHandler() {
        IRequestHandler requestHandler = new SyncRequestHandler();

        SyncExecutor syncExecutor = new SyncExecutor() {
            Map<String, Integer> stringIntegerMap = new HashMap<>();

            @Override
            public boolean sync(String appId, int version, EntityClassSyncRspProto entityClassSyncRspProto) {
                stringIntegerMap.put(appId, version);
                return true;
            }

            @Override
            public int version(String appId) {
                Integer version = stringIntegerMap.get(appId);
                if (null == version) {
                    return -1;
                }
                return version;
            }
        };

        ReflectionTestUtils.setField(requestHandler, "syncExecutor", syncExecutor);
        ReflectionTestUtils.setField(requestHandler, "requestWatchExecutor", requestWatchExecutor);


        return requestHandler;
    }


    @Test
    public void executorTest() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        requestWatchExecutor.create(UUID.randomUUID().toString(), new StreamObserver<EntityClassSyncRequest>() {
            @Override
            public void onNext(EntityClassSyncRequest entityClassSyncRequest) {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {

            }
        });
        String appId = "testExecutor";
        int version = 1;
        check(appId, version, SYNC_FAIL.ordinal(),
                entityClassSyncResponseGenerator(appId, version, false, mockSelfFatherAncestorsGenerate(System.currentTimeMillis())));

        check(appId, version, SYNC_OK.ordinal(),
                entityClassSyncResponseGenerator(appId, version, true, mockSelfFatherAncestorsGenerate(System.currentTimeMillis())));
    }

    @Test
    public void md5CheckTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String appId = "testExecutor";
        int version = 1;
        EntityClassSyncResponse entityClassSyncResponse =
                entityClassSyncResponseGenerator(appId, version, true, mockSelfFatherAncestorsGenerate(System.currentTimeMillis()));

        Method m0 = requestHandler.getClass()
                .getDeclaredMethod("md5Check", new Class[]{String.class, EntityClassSyncRspProto.class});
        m0.setAccessible(true);

        boolean ret = (boolean) m0.invoke(requestHandler, entityClassSyncResponse.getMd5(), entityClassSyncResponse.getEntityClassSyncRspProto());
        Assert.assertTrue(ret);

        EntityClassSyncResponse notExpected =
                entityClassSyncResponseGenerator(appId, version, true, mockSelfFatherAncestorsGenerate(System.currentTimeMillis()));

        ret = (boolean) m0.invoke(requestHandler, entityClassSyncResponse.getMd5(), notExpected.getEntityClassSyncRspProto());
        Assert.assertFalse(ret);
    }

    private void check(String appId, int version, int status, EntityClassSyncResponse entityClassSyncResponse) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method m0 = requestHandler.getClass()
                .getDeclaredMethod("execute", new Class[]{EntityClassSyncResponse.class});
        m0.setAccessible(true);

        EntityClassSyncRequest.Builder builder = (EntityClassSyncRequest.Builder) m0.invoke(requestHandler, entityClassSyncResponse);

        Assert.assertNotNull(builder);
        EntityClassSyncRequest entityClassSyncRequest = builder.build();
        Assert.assertEquals(appId, entityClassSyncRequest.getAppId());
        Assert.assertEquals(version + 1, entityClassSyncRequest.getVersion());
        Assert.assertEquals(status, entityClassSyncRequest.getStatus());
    }
}
