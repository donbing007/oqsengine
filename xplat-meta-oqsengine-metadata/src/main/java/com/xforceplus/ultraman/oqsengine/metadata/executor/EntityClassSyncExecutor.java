package com.xforceplus.ultraman.oqsengine.metadata.executor;

import static com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams.SHUT_DOWN_WAIT_TIME_OUT;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.POLL_TIME_OUT_SECONDS;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.Constant.COMMON_WAIT_TIME_OUT;
import static com.xforceplus.ultraman.oqsengine.metadata.utils.storage.EntityClassStorageBuilderUtils.protoToStorageList;

import com.xforceplus.ultraman.oqsengine.event.ActualEvent;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.event.payload.meta.MetaChangePayLoad;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IDelayTaskExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ThreadUtils;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.cache.CacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.dto.SyncStep;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.EntityClassStorage;
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

    /**
     * 创建监听delayTask的线程.
     */
    @PostConstruct
    public void start() {
        closed = false;

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
     *
     */
    @Override
    public boolean sync(String appId, String env, int version, EntityClassSyncRspProto entityClassSyncRspProto) {
        int expiredVersion = -1;
        MetaChangePayLoad metaChangePayLoad = null;

        //  初始化SyncStep
        SyncStep step = SyncStep.failed(SyncStep.StepDefinition.UNKNOWN, "");
        //  设置是否锁定
        boolean openPrepare = false;

        //  return false的情况代表本地没有准备好.
        try {
            //  准备,是否可以加锁更新，不成功直接返回失败
            step = prepared(appId, version);
            if (!step.getStepDefinition().equals(SyncStep.StepDefinition.SUCCESS)) {
                logger.warn("ignore data sync! another job is on processing, raw info : {}", step.messageFormat());
                return false;
            }
            openPrepare = true;

            //  查询当前版本
            step = querySyncVersion(appId);
            if (!step.getStepDefinition().equals(SyncStep.StepDefinition.SUCCESS)) {
                throw new MetaSyncClientException(step.messageFormat(), false);
            }
            expiredVersion = (int) step.getData();

            //  转换protobuf结构
            step = parserProto(entityClassSyncRspProto);
            //  同步数据失败的情况下需要抛出异常，而不是直接返回false.
            if (!step.getStepDefinition().equals(SyncStep.StepDefinition.SUCCESS)) {
                throw new MetaSyncClientException(step.messageFormat(), false);
            }

            // step3 update new Hash in redis
            step = save(appId, env, version, (List<EntityClassStorage>) step.getData());
            if (!step.getStepDefinition().equals(SyncStep.StepDefinition.SUCCESS)) {
                throw new MetaSyncClientException(step.messageFormat(), false);
            }

            metaChangePayLoad = (MetaChangePayLoad) step.getData();

            return true;
        } finally {
            //  如果成功、执行publish
            if (step.getStepDefinition().equals(SyncStep.StepDefinition.SUCCESS)) {

                //  set into expired clean task
                if (expiredVersion != NOT_EXIST_VERSION) {
                    expireExecutor.offer(new ExpireExecutor.DelayCleanEntity(COMMON_WAIT_TIME_OUT,
                        new ExpireExecutor.Expired(appId, expiredVersion)));
                }
                eventPublish(metaChangePayLoad);
            }

            if (openPrepare) {
                cacheExecutor.endPrepare(appId);
            }
        }
    }

    private SyncStep<Boolean> prepared(String appId, int version) {
        return cacheExecutor.prepare(appId, version) ? SyncStep.ok(true) : SyncStep.failed(SyncStep.StepDefinition.DUPLICATE_PREPARE_FAILED,
            String.format("sync-prepare failed, have another sync job, current [%s]-[%d] will be canceled.", appId, version));
    }

    private SyncStep<Integer> querySyncVersion(String appId) {
        try {
            return SyncStep.ok(version(appId));
        } catch (Exception e) {
            String message = String.format("query expiredVersion failed, appId : %s, %s", appId, e.getMessage());
            return SyncStep.failed(SyncStep.StepDefinition.QUERY_VERSION_FAILED, message);
        }
    }

    private SyncStep<List<EntityClassStorage>> parserProto(EntityClassSyncRspProto entityClassSyncRspProto) {
        // step2 convert to storage
        try {
            return SyncStep.ok(protoToStorageList(entityClassSyncRspProto));
        } catch (Exception e) {
            return SyncStep.failed(SyncStep.StepDefinition.PARSER_PROTO_BUF_FAILED, String.format("parser meta proto failed, [%s]", e.getMessage()));
        }
    }

    private SyncStep<MetaChangePayLoad> save(String appId, String env, int version, List<EntityClassStorage> entityClassStorages) {
        try {
            MetaChangePayLoad metaChangePayLoad =
                cacheExecutor.save(appId, env, version, entityClassStorages);
            return SyncStep.ok(metaChangePayLoad);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return SyncStep.failed(SyncStep.StepDefinition.SAVE_ENTITY_CLASS_STORAGE_FAILED, e.getMessage());
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

                if (!isClean) {
                    expireExecutor.offer(task);
                }
            } catch (Exception e) {
                //  ignore
                logger.warn("clean app : {}, version : {} catch exception, message : {} , but will ignore...",
                    task.element().getAppId(), task.element().getVersion(), e.getMessage());
            }
        }
    }

    private void eventPublish(MetaChangePayLoad metaChangePayLoad) {
        if (null != metaChangePayLoad) {
            logger.info("ready for publish event on appId:[{}]", metaChangePayLoad.getAppId());
            //  publish event
            if (!metaChangePayLoad.getEntityChanges().isEmpty()) {
                eventBus.notify(new ActualEvent<>(EventType.META_DATA_CHANGE, metaChangePayLoad));
                logger.info("publish event ok.");
            } else {
                logger.info("empty event change, nothing to publish.");
            }
        }
    }
}
