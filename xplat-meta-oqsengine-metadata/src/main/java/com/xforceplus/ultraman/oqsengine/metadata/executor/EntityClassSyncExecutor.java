package com.xforceplus.ultraman.oqsengine.metadata.executor;

import static com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams.SHUT_DOWN_WAIT_TIME_OUT;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.POLL_TIME_OUT_SECONDS;
import static com.xforceplus.ultraman.oqsengine.meta.common.utils.EntityClassStorageBuilderUtils.protoToStorageList;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.Constant.COMMON_WAIT_TIME_OUT;

import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IDelayTaskExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.pojo.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.EntityClassStorageHelper;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ThreadUtils;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.cache.CacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.utils.FileReaderUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 元信息同步执行器.
 *
 * @author xujia 2021/2/18
 * @since 1.8
 */
public class EntityClassSyncExecutor implements SyncExecutor {

    final Logger logger = LoggerFactory.getLogger(EntityClassSyncExecutor.class);

    @Resource
    private CacheExecutor cacheExecutor;

    @Resource
    private IDelayTaskExecutor<ExpireExecutor.DelayCleanEntity> expireExecutor;

    @Resource
    private EventBus eventBus;

    private volatile boolean closed = false;

    private Thread thread;

    private String loadPath;

    public void setLoadPath(String loadPath) {
        this.loadPath = loadPath;
    }

    /**
     * 创建监听delayTask的线程.
     */
    @PostConstruct
    public void start() {
        closed = false;

        //  sync data from file
        if (null != loadPath && !loadPath.isEmpty()) {
            logger.info("start load from local path : {}", loadPath);
            loadFromLocal(loadPath);
            logger.info("success load from local path : {}", loadPath);
        }

        thread = ThreadUtils.create(() -> {
            delayCleanTask();
            return true;
        });

        thread.start();

    }

    /**
     * 销毁delayTask的线程.
     */
    @PreDestroy
    public void stop() {
        closed = true;
        expireExecutor.stop();
        ThreadUtils.shutdown(thread, SHUT_DOWN_WAIT_TIME_OUT);
    }

    /**
     * 同步appId对应的EntityClass package.
     */
    @Override
    public boolean sync(String appId, int version, EntityClassSyncRspProto entityClassSyncRspProto) {

        // step1 prepare
        if (cacheExecutor.prepare(appId, version)) {
            int expiredVersion = -1;
            try {
                List<Event<?>> payloads = new ArrayList<>();
                try {
                    expiredVersion = version(appId);
                } catch (Exception e) {
                    logger.warn("query expiredVersion failed, [{}]", e.toString());
                    return false;
                }

                // step2 convert to storage
                List<EntityClassStorage> entityClassStorageList = protoToStorageList(entityClassSyncRspProto);
                try {
                    // step3 update new Hash in redis
                    if (!cacheExecutor.save(appId, version, entityClassStorageList, payloads)) {
                        throw new MetaSyncClientException(
                            String.format("save batches failed, appId : [%s], version : [%d]", appId, version), false
                        );
                    }

                    //  step4 set into expired clean task
                    if (expiredVersion != NOT_EXIST_VERSION) {
                        expireExecutor.offer(new ExpireExecutor.DelayCleanEntity(COMMON_WAIT_TIME_OUT,
                            new ExpireExecutor.Expired(appId, expiredVersion)));
                    }

                    return true;
                } catch (Exception e) {
                    payloads.clear();
                    logger.warn("sync-error, message[{}]", e.toString());
                    return false;
                } finally {
                    publish(payloads);
                }
            } finally {
                cacheExecutor.endPrepare(appId);
            }
        }
        logger.warn("sync-prepare failed, have another sync job, current [{}]-[{}] will be canceled.",
            appId, version);

        return false;
    }

    @Override
    public boolean dataImport(String appId, int version, String content) {
        int currentVersion = version(appId);

        logger.debug("appId {}, currentVersion {}, update version {}", appId, currentVersion, appId);

        if (version > currentVersion) {
            EntityClassSyncRspProto entityClassSyncRspProto;
            try {
                entityClassSyncRspProto = EntityClassStorageHelper.toEntityClassSyncRspProto(content);
            } catch (Exception e) {
                throw new RuntimeException(String.format("parse data to EntityClassSyncRspProto failed, message [%s]", e.getMessage()));
            }

            if (!sync(appId, version, entityClassSyncRspProto)) {
                throw new RuntimeException("sync data to EntityClassSyncRspProto failed");
            }
            return true;
        } else {
            String message = String.format("appId [%s], current version [%d] greater than update version [%d], ignore...", appId, currentVersion, version);
            logger.warn(message);
            return false;
        }
    }

    /**
     * 获取当前meta的版本信息.
     */
    @Override
    public int version(String appId) {
        return cacheExecutor.version(appId);
    }

    /**
     * 清理过期任务.
     */
    private void delayCleanTask() {
        while (!closed) {
            ExpireExecutor.DelayCleanEntity task = expireExecutor.take();
            if (null == task || null == task.element()) {
                if (!closed) {
                    TimeWaitUtils.wakeupAfter(POLL_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                }
                continue;
            }
            try {
                boolean isClean =
                    cacheExecutor.clean(task.element().getAppId(), task.element().getVersion(), false);

                logger.debug("clean app : {}, version : {}， success : {}",
                    task.element().getAppId(), task.element().getVersion(), isClean);
            } catch (Exception e) {
                //  ignore
                logger.warn("clean app : {}, version : {} catch exception, message : {} , but will ignore...",
                    task.element().getAppId(), task.element().getVersion(), e.getMessage());
            }
        }
    }

    private void publish(List<Event<?>> payloads) {
        payloads.forEach(
            payload -> {
                eventBus.notify(payload);
            }
        );
    }


    private void loadFromLocal(String path) {
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        List<String> files = FileReaderUtils.getFileNamesInOneDir(path);
        for (String file : files) {
            try {
                String[] splitter = EntityClassStorageHelper.splitMetaFromFileName(file);

                int version = Integer.parseInt(splitter[1]);
                String fullPath = path + file;
                String v =
                    EntityClassStorageHelper.initDataFromFilePath(splitter[0], splitter[2], version, fullPath);

                if (dataImport(splitter[0], version, v)) {
                    logger.info("init meta from local path success, path : {}", fullPath);
                } else {
                    logger.warn("init meta from local path failed, less than current oqs use version, path : {}", fullPath);
                }
            } catch (Exception e) {
                logger.warn("load from local-file failed, path : {}, message : {}", path + file, e.getMessage());

                //  ignore current file
            }
        }
    }
}
