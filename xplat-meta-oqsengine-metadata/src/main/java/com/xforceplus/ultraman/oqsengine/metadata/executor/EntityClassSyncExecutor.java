package com.xforceplus.ultraman.oqsengine.metadata.executor;

import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IDelayTaskExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ThreadUtils;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.cache.ICacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.dto.EntityClassStorage;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig.SHUT_DOWN_WAIT_TIME_OUT;
import static com.xforceplus.ultraman.oqsengine.meta.common.exception.Code.BUSINESS_HANDLER_ERROR;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.Constant.*;
import static com.xforceplus.ultraman.oqsengine.metadata.utils.EntityClassStorageConvert.protoValuesToLocalStorage;

/**
 * desc :
 * name : EntityClassSyncExecutor
 *
 * @author : xujia
 * date : 2021/2/18
 * @since : 1.8
 */
public class EntityClassSyncExecutor implements SyncExecutor {

    @Resource
    private ICacheExecutor cacheExecutor;

    @Resource
    private IDelayTaskExecutor<ExpireExecutor.DelayCleanEntity> expireExecutor;

    private Thread thread;

    /**
     * 创建监听delayTask的线程
     */
    @PostConstruct
    public void start() {
        thread = ThreadUtils.create(() -> {
            delayCleanTask();
            return true;
        });

        thread.start();
    }

    /**
     * 销毁delayTask的线程
     */
    @PreDestroy
    public void stop() {
        ThreadUtils.shutdown(thread, SHUT_DOWN_WAIT_TIME_OUT);
    }


    @Override
    public boolean sync(String appId, int version, EntityClassSyncRspProto entityClassSyncRspProto) {

        // step1 prepare
        if (cacheExecutor.prepare(appId, version)) {
            try {
                int expiredVersion = version(appId);

                // step2 convert to storage
                List<EntityClassStorage> entityClassStorageList = convert(entityClassSyncRspProto);

                // step3 update new Hash in redis
                if (!cacheExecutor.save(appId, version, entityClassStorageList)) {
                    throw new MetaSyncClientException(
                            String.format("save batches failed, appId : [%s], version : [%d]", appId, version), false
                    );
                }
                //  step4 set into expired to clean after expiredTime
                if (expiredVersion != EXPIRED_VERSION) {
                    expireExecutor.offer(new ExpireExecutor.DelayCleanEntity(COMMON_WAIT_TIME_OUT,
                                                    new ExpireExecutor.Expired(appId, expiredVersion)));
                }

                return true;
            } catch (Exception e) {
                return false;
            } finally {
                cacheExecutor.endPrepare(appId);
            }
        }

        return false;
    }

    @Override
    public int version(String appId) {
        return cacheExecutor.version(appId);
    }

    /**
     * 将protoBuf转为EntityClassStorage列表

     * @param entityClassSyncRspProto
     * @return
     */
    private List<EntityClassStorage> convert(EntityClassSyncRspProto entityClassSyncRspProto) {
        Map<Long, EntityClassStorage> temp = entityClassSyncRspProto.getEntityClassesList().stream().map(
                ecs -> {
                    EntityClassStorage e = protoValuesToLocalStorage(ecs);
                    return e;
                }
        ).collect(Collectors.toMap(EntityClassStorage::getId, s1 -> s1,  (s1, s2) -> s1));

        return temp.values().stream().peek(
                v -> {
                    Long fatherId = v.getFatherId();
                    while (null != fatherId && fatherId >= MIN_ID) {
                        EntityClassStorage entityClassStorage = temp.get(fatherId);
                        if (null == entityClassStorage) {
                            throw new MetaSyncClientException(
                                    String.format("father entityClass : [%d] missed.", fatherId), BUSINESS_HANDLER_ERROR.ordinal());
                        }
                        v.addAncestors(fatherId);
                        fatherId = entityClassStorage.getFatherId();
                    }
                }
        ).collect(Collectors.toList());
    }


    private void delayCleanTask() {
        while (true) {
            ExpireExecutor.DelayCleanEntity task = expireExecutor.take();
            if (null == task) {
                TimeWaitUtils.wakeupAfter(1, TimeUnit.MILLISECONDS);
                continue;
            }

            if (null != task.element()) {
                cacheExecutor.clean(task.element().getAppId(), task.element().getVersion(), false);
            }
        }
    }
}
