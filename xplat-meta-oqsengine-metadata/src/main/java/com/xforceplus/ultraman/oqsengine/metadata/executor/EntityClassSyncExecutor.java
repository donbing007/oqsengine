package com.xforceplus.ultraman.oqsengine.metadata.executor;

import static com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams.SHUT_DOWN_WAIT_TIME_OUT;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.POLL_TIME_OUT_SECONDS;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.Constant.COMMON_WAIT_TIME_OUT;
import static com.xforceplus.ultraman.oqsengine.metadata.utils.EntityClassStorageBuilderUtils.protoToStorageList;

import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IDelayTaskExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ThreadUtils;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.cache.AggregationEventBuilder;
import com.xforceplus.ultraman.oqsengine.metadata.cache.CacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.dto.SyncStep;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.EntityClassStorage;
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
     */
    @Override
    public boolean sync(String appId, int version, EntityClassSyncRspProto entityClassSyncRspProto) {
        int expiredVersion = -1;
        List<Event<?>> payloads = new ArrayList<>();
        List<Event<?>> aggPayloads = new ArrayList<>();

        //  初始化SyncStep
        SyncStep step = SyncStep.failed(SyncStep.StepDefinition.UNKNOWN, "");
        //  设置是否锁定
        boolean openPrepare = false;

        //  return false的情况代表本地没有准备好.
        try {
            //  准备,是否可以加锁更新，不成功直接返回失败
            step = prepared(appId, version);
            if (!step.getStepDefinition().equals(SyncStep.StepDefinition.SUCCESS)) {
                return false;
            }
            openPrepare = true;

            //  查询当前版本
            step = querySyncVersion(appId);
            if (!step.getStepDefinition().equals(SyncStep.StepDefinition.SUCCESS)) {
                return false;
            }
            expiredVersion = (int) step.getData();

            //  转换protobuf结构
            step = parserProto(entityClassSyncRspProto);
            //  同步数据失败的情况下需要抛出异常，而不是直接返回false.
            if (!step.getStepDefinition().equals(SyncStep.StepDefinition.SUCCESS)) {
                throw new MetaSyncClientException(step.getMessage(), false);
            }

            // step3 update new Hash in redis
            logger.info("================start  update new Hash in redis==============");
            List<EntityClassStorage> data = (List<EntityClassStorage>) step.getData();
            step = save(appId, version, data, payloads);
            if (!step.getStepDefinition().equals(SyncStep.StepDefinition.SUCCESS)) {
                return false;
            }

            //  step4 build agg event
            logger.info(String.format("start buildAggEvent : %s"), appId + "-" + version);
            step = buildAggEvent(appId, version, data, aggPayloads);
            if (!step.getStepDefinition().equals(SyncStep.StepDefinition.SUCCESS)) {
                return false;
            }

            step = SyncStep.ok();

        } finally {
            //  如果成功、执行publish
            if (step.getStepDefinition().equals(SyncStep.StepDefinition.SUCCESS)) {

                //  set into expired clean task
                if (expiredVersion != NOT_EXIST_VERSION) {
                    expireExecutor.offer(new ExpireExecutor.DelayCleanEntity(COMMON_WAIT_TIME_OUT,
                        new ExpireExecutor.Expired(appId, expiredVersion)));
                }

                publish(payloads);
                publish(aggPayloads);
            } else {
                payloads.clear();
                aggPayloads.clear();
            }

            if (openPrepare) {
                cacheExecutor.endPrepare(appId);
            }

            //  record sync logs to redis
            cacheExecutor.addSyncLog(appId, version, step.toPersistentMessage());
        }

        return step.getStepDefinition().equals(SyncStep.StepDefinition.SUCCESS);
    }

    @Override
    public void recordSyncFailed(String appId, Integer version, String message) {
        cacheExecutor.addSyncLog(appId, version,  SyncStep.StepDefinition.SYNC_CLIENT_FAILED + ":" + message);
    }


    private SyncStep<Boolean> prepared(String appId, int version) {
        return cacheExecutor.prepare(appId, version) ? SyncStep.ok(true) : SyncStep.failed(SyncStep.StepDefinition.DUPLICATE_PREPARE_FAILED,
            String.format("sync-prepare failed, have another sync job, current [%s]-[%d] will be canceled.", appId, version));
    }

    private SyncStep<Integer> querySyncVersion(String appId) {
        try {
            return SyncStep.ok(version(appId));
        } catch (Exception e) {
            String message = String.format("query expiredVersion failed, [%s]", e.getMessage());
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

    private SyncStep<Boolean> save(String appId, int version, List<EntityClassStorage> entityClassStorages, List<Event<?>> payloads) {
        try {
            return cacheExecutor.save(appId, version, entityClassStorages, payloads) ? SyncStep.ok(true)
                : SyncStep.failed(SyncStep.StepDefinition.SAVE_ENTITY_CLASS_STORAGE_FAILED, "storage entity class failed.");

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return SyncStep.failed(SyncStep.StepDefinition.SAVE_ENTITY_CLASS_STORAGE_FAILED, e.getMessage());
        }
    }

    private SyncStep<Boolean> buildAggEvent(String appId, int version, List<EntityClassStorage> entityClassStorages, List<Event<?>> payloads) {
        try {
            logger.info(String.format("start buildAggEvent : %s, List<EntityClassStorage> is : %s "), appId + "-" + version, entityClassStorages.toString());
            new AggregationEventBuilder().buildAggEvent(appId, version, entityClassStorages, payloads);
            return SyncStep.ok(true);
        } catch (Exception e) {
            logger.error(String.format("failed buildAggEvent : %s, List<EntityClassStorage> is : %s "), appId + "-" + version, entityClassStorages.toString());
            logger.error(e.getMessage(), e);
            return SyncStep.failed(SyncStep.StepDefinition.BUILD_EVENT_FAILED, String.format("build agg event failed, [%s]", e.getMessage()));
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
}
