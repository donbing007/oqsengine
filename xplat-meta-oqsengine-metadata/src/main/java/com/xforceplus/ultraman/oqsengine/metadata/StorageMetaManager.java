package com.xforceplus.ultraman.oqsengine.metadata;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.Constant.COMMON_WAIT_TIME_OUT;

import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.cache.CacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.dto.metrics.MetaLogs;
import com.xforceplus.ultraman.oqsengine.metadata.dto.metrics.MetaMetrics;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.metadata.handler.EntityClassFormatHandler;
import com.xforceplus.ultraman.oqsengine.metadata.utils.EntityClassStorageHelper;
import com.xforceplus.ultraman.oqsengine.metadata.utils.FileReaderUtils;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import io.micrometer.core.annotation.Timed;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Meta管理类实现.
 *
 * @author xujia 2021/2/9
 * @since 1.8
 */
public class StorageMetaManager implements MetaManager {

    final Logger logger = LoggerFactory.getLogger(StorageMetaManager.class);

    @Resource
    private CacheExecutor cacheExecutor;

    @Resource
    private IRequestHandler requestHandler;

    @Resource(name = "grpcSyncExecutor")
    private SyncExecutor syncExecutor;

    @Resource(name = "entityClassFormatHandler")
    private EntityClassFormatHandler entityClassFormatHandler;

    @Resource(name = "taskThreadPool")
    private ExecutorService asyncDispatcher;

    private boolean isOffLineUse = false;

    public void isOffLineUse() {
        this.isOffLineUse = true;
    }

    private String loadPath;

    public void setLoadPath(String loadPath) {
        this.loadPath = loadPath;
    }

    private <T> CompletableFuture<T> async(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, asyncDispatcher);
    }

    @PostConstruct
    public void init() {
        //  sync data from file
        if (null != loadPath && !loadPath.isEmpty()) {
            logger.info("start load from local path : {}", loadPath);
            loadFromLocal(loadPath);
            logger.info("success load from local path : {}", loadPath);
        }
    }

    @Override
    public Optional<IEntityClass> load(long id, String profile) {
        return entityClassFormatHandler.classLoad(id, profile);
    }

    @Override
    public Optional<IEntityClass> loadHistory(long id, int version) {
        return Optional.empty();
    }

    /**
     * 需要关注某个appId.
     * 注意：当前的实现只支持单个appId的单个Env，即appId如果关注了test env，则无法再次关注其他环境.
     *
     * @param appId 应用标识.
     * @param env   环境编码.
     * @return 版本号.
     */
    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "meta", "action", "need"})
    @Override
    public int need(String appId, String env) {
        try {
            cacheExecutor.appEnvSet(appId, env);

            String cacheEnv = cacheExecutor.appEnvGet(appId);
            if (!cacheEnv.equals(env)) {
                logger.warn("appId [{}], param env [{}] not equals to cache's env [{}], will use cache to register.",
                    appId, env, cacheEnv);
                env = cacheEnv;
            }

            int version = cacheExecutor.version(appId);

            if (!isOffLineUse) {
                requestHandler.register(new WatchElement(appId, env, version, WatchElement.ElementStatus.Register));

                if (version < 0) {
                    CompletableFuture<Integer> future = async(() -> {
                        int ver;
                        /*
                         * 这里每10毫秒获取一次当前版本、直到获取到版本或者超时
                         */
                        while (true) {
                            ver = cacheExecutor.version(appId);
                            if (ver < 0) {
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    break;
                                }
                            } else {
                                return ver;
                            }
                        }
                        return NOT_EXIST_VERSION;
                    });

                    try {
                        version = future.get(COMMON_WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e.getMessage());
                    }

                    if (version == NOT_EXIST_VERSION) {
                        throw new RuntimeException(
                            String.format("get version of appId [%s] failed, reach max wait time", appId));
                    }
                }
            } else {
                if (version < 0) {
                    throw new RuntimeException(
                        String.format("local cache has not init this version of appId [%s].", appId));
                }
            }
            return version;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 使本地缓存失效.
     */
    @Override
    public void invalidateLocal() {
        cacheExecutor.invalidateLocal();
    }


    @Override
    public boolean dataImport(String appId, String env, int version, String content) {

        cacheExecutor.appEnvSet(appId, env);

        if (!cacheExecutor.appEnvGet(appId).equals(env)) {
            throw new RuntimeException("appId has been init with another Id, need failed...");
        }

        int currentVersion = cacheExecutor.version(appId);

        if (version > currentVersion) {
            logger.info("execute data import, appId {}, currentVersion {}, update version {}", appId, currentVersion,
                version);

            EntityClassSyncRspProto entityClassSyncRspProto;
            try {
                entityClassSyncRspProto = EntityClassStorageHelper.toEntityClassSyncRspProto(content);
            } catch (Exception e) {
                throw new RuntimeException(
                    String.format("parse data to EntityClassSyncRspProto failed, message [%s]", e.getMessage()));
            }

            if (!syncExecutor.sync(appId, version, entityClassSyncRspProto)) {
                throw new RuntimeException("sync data to EntityClassSyncRspProto failed");
            }
            return true;
        } else {
            String message = String
                .format("appId [%s], current version [%d] greater than update version [%d], ignore...", appId,
                    currentVersion, version);
            logger.warn(message);
            return false;
        }
    }

    @Override
    public Optional<MetaMetrics> showMeta(String appId) throws Exception {

        try {
            int currentVersion = cacheExecutor.version(appId);
            if (currentVersion == NOT_EXIST_VERSION) {
                return Optional.empty();
            }
            String env = cacheExecutor.appEnvGet(appId);

            Collection<Long> ids = cacheExecutor.appEntityIdList(appId, currentVersion);

            Map<Long, EntityClassStorage> metas = cacheExecutor.multiplyRead(ids, currentVersion, false);

            return Optional
                .of(new MetaMetrics(currentVersion, env, appId, null != metas ? metas.values() : new ArrayList<>()));

        } catch (Exception e) {
            logger.warn("show meta error, appId {}, message : {}", appId, e.getMessage());
            throw e;
        }
    }

    @Override
    public Collection<MetaLogs> metaLogs() {
        Map<String, String> result = cacheExecutor.getSyncLog();
        List<MetaLogs> metaLogs = new ArrayList<>();

        if (!result.isEmpty()) {
            result.forEach(
                (k, v) -> {
                    String[] keySplitter = k.split("\\.");
                    if (keySplitter.length == 3) {
                        metaLogs.add(
                            new MetaLogs(keySplitter[0], Integer.parseInt(keySplitter[1]),
                                Long.parseLong(keySplitter[2]), v)
                        );
                    }
                }
            );
        }

        return metaLogs;

    }

    /**
     * 显示当前oqs中所有正在使用的appId.
     *
     * @return appId列表.
     */
    public Map<String, String> showApplications() {
        return cacheExecutor.showAppEnv();
    }

    private void loadFromLocal(String path) {
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        List<String> files = FileReaderUtils.getFileNamesInOneDir(path);
        for (String file : files) {
            try {
                String[] splitter = EntityClassStorageHelper.splitMetaFromFileName(file);

                String appId = splitter[0];
                int version = Integer.parseInt(splitter[1]);
                String fullPath = path + file;

                String v =
                    EntityClassStorageHelper.initDataFromFilePath(appId, splitter[2], version, fullPath);

                if (dataImport(splitter[0], splitter[2], version, v)) {
                    logger
                        .info("init meta from local path success, path : {}, appId : {}, version : {}", fullPath, appId,
                            version);
                } else {
                    logger.warn("init meta from local path failed, less than current oqs use version, path : {}",
                        fullPath);
                }
            } catch (Exception e) {
                logger.warn("load from local-file failed, path : {}, message : {}", path + file, e.getMessage());

                //  ignore current file
            }
        }
    }
}