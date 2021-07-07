package com.xforceplus.ultraman.oqsengine.meta.handler;

import com.xforceplus.ultraman.oqsengine.meta.BaseTest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ExecutorHelper;
import io.grpc.stub.StreamObserver;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
public class SyncRequestHandlerTest extends BaseTest {

    @BeforeEach
    public void before() {
        baseInit();
    }

    @AfterEach
    public void after() {
        requestHandler.stop();

        ExecutorHelper.shutdownAndAwaitTermination(executorService, 3600);
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
        Assertions.assertTrue(ret);

        EntityClassSyncResponse notExpected =
                entityClassSyncResponseGenerator(appId, version, true, mockSelfFatherAncestorsGenerate(System.currentTimeMillis()));

        ret = (boolean) m0.invoke(requestHandler, entityClassSyncResponse.getMd5(), notExpected.getEntityClassSyncRspProto());
        Assertions.assertFalse(ret);
    }

    private void check(String appId, int version, int status, EntityClassSyncResponse entityClassSyncResponse) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method m0 = requestHandler.getClass()
                .getDeclaredMethod("execute", new Class[]{EntityClassSyncResponse.class});
        m0.setAccessible(true);

        EntityClassSyncRequest.Builder builder = (EntityClassSyncRequest.Builder) m0.invoke(requestHandler, entityClassSyncResponse);

        Assertions.assertNotNull(builder);
        EntityClassSyncRequest entityClassSyncRequest = builder.build();
        Assertions.assertEquals(appId, entityClassSyncRequest.getAppId());
        Assertions.assertEquals(version + 1, entityClassSyncRequest.getVersion());
        Assertions.assertEquals(status, entityClassSyncRequest.getStatus());
    }
}
