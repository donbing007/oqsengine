package com.xforceplus.ultraman.oqsengine.metadata;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;
import static java.lang.Thread.State.TERMINATED;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xforceplus.ultraman.oqsengine.common.mock.ReflectionUtils;
import com.xforceplus.ultraman.oqsengine.event.payload.meta.MetaChangePayLoad;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.monitor.MetricsRecorder;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.executor.IRequestWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.cache.CacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.dto.log.UpGradeLog;
import com.xforceplus.ultraman.oqsengine.metadata.dto.metrics.AppSimpleInfo;
import com.xforceplus.ultraman.oqsengine.metadata.dto.model.ClientModel;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import io.grpc.stub.StreamObserver;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 05/2022.
 *
 * @since 1.8
 */
public class TimeOutNeedTest {
    private static StorageMetaManager storageMetaManager;

    @BeforeAll
    public static void before() throws IllegalAccessException {
        storageMetaManager = new StorageMetaManager(new ClientModel());

        Collection<Field> cacheFields = ReflectionUtils.printAllMembers(storageMetaManager);
        ReflectionUtils.reflectionFieldValue(cacheFields, "cacheExecutor", storageMetaManager,
            mockCacheExecutor());
        ReflectionUtils.reflectionFieldValue(cacheFields, "requestHandler", storageMetaManager, mockRequestHandler());
        ReflectionUtils.reflectionFieldValue(cacheFields, "syncExecutor", storageMetaManager,
            mockSyncExecutor());
    }

    private String fakeAppId = "fakeAppId";
    private String fakeEnv = "fakeEnv";

    private static final long miniWait = 60_000;
    private static final long maxWait = 65_000;
    @Test
    public void testTimeoutThreadInterrupted() throws InterruptedException {
        Thread thread = new Thread(
            () -> {
                storageMetaManager.need(fakeAppId, fakeEnv);
            }
        );

        thread.start();

        long start = System.currentTimeMillis();
        try {
            thread.join();
        } catch (Exception e) {

        }
        long duration = System.currentTimeMillis() - start;

        Assertions.assertTrue(duration >= miniWait && duration <= maxWait);
        Assertions.assertEquals(thread.getState(), TERMINATED);
    }

    private static SyncExecutor mockSyncExecutor() {
        return new SyncExecutor() {
            @Override
            public boolean sync(String appId, String env, int version,
                                EntityClassSyncRspProto entityClassSyncRspProto) {
                return false;
            }

            @Override
            public int version(String appId) {
                return 0;
            }
        };
    }

    private static IRequestHandler mockRequestHandler() {
        return new IRequestHandler() {
            @Override
            public boolean register(WatchElement watchElement) {
                return true;
            }

            @Override
            public boolean reRegister() {
                return false;
            }

            @Override
            public void initWatcher(String clientId, String uid,
                                    StreamObserver<EntityClassSyncRequest> streamObserver) {

            }

            @Override
            public IRequestWatchExecutor watchExecutor() {
                return null;
            }

            @Override
            public void notReady() {

            }

            @Override
            public void ready() {

            }

            @Override
            public boolean reset(WatchElement watchElement) {
                return true;
            }

            @Override
            public MetricsRecorder metricsRecorder() {
                return null;
            }

            @Override
            public void invoke(EntityClassSyncResponse entityClassSyncResponse, Void unused) {

            }

            @Override
            public boolean isShutDown() {
                return false;
            }

            @Override
            public void start() {

            }

            @Override
            public void stop() {

            }
        };
    }

    private static Map<String, String> envs = new HashMap<>();

    private static CacheExecutor mockCacheExecutor() {

        return new CacheExecutor() {

            @Override
            public MetaChangePayLoad save(String appId, String env, int version, List<EntityClassStorage> storageList)
                throws JsonProcessingException {
                return null;
            }

            @Override
            public Map<String, String> remoteRead(long entityClassId) throws JsonProcessingException {
                return null;
            }

            @Override
            public Map<String, String> remoteRead(long entityClassId, int version) throws JsonProcessingException {
                return null;
            }

            @Override
            public Map<String, Map<String, String>> multiRemoteRead(Collection<Long> ids, int version)
                throws JsonProcessingException {
                return null;
            }

            @Override
            public boolean clean(String appId, int version, boolean force) {
                return false;
            }

            @Override
            public Collection<Long> appEntityIdList(String appId, Integer version) {
                return null;
            }

            @Override
            public int version(String appId) {
                return NOT_EXIST_VERSION;
            }

            @Override
            public int version(Long entityClassId, boolean withCache) {
                return NOT_EXIST_VERSION;
            }

            @Override
            public Map<Long, Integer> versions(List<Long> entityClassIds, boolean withCache, boolean errorContinue) {
                return new HashMap<>();
            }

            @Override
            public boolean resetVersion(String appId, int version, List<Long> ids) {
                return false;
            }

            @Override
            public boolean prepare(String appId, int version) {
                return false;
            }

            @Override
            public boolean endPrepare(String appId) {
                return false;
            }

            @Override
            public String appEnvGet(String appId) {
                return envs.get(appId);
            }

            @Override
            public boolean appEnvSet(String appId, String env) {
                envs.put(appId, env);
                return true;
            }

            @Override
            public boolean appEnvRemove(String appId) {
                return false;
            }

            @Override
            public void invalidateLocal() {

            }

            @Override
            public List<String> readProfileCodes(long entityClassId, int version) {
                return null;
            }

            @Override
            public Optional<IEntityClass> localRead(long entityClassId, int version, String profile) {
                return Optional.empty();
            }

            @Override
            public void localStorage(long entityClassId, int version, String profile, IEntityClass entityClass) {

            }

            @Override
            public List<AppSimpleInfo> showAppInfo() {
                return null;
            }

            @Override
            public Collection<UpGradeLog> showUpgradeLogs(String appId, String env) throws JsonProcessingException {
                return null;
            }
        };
    }
}
