package com.xforceplus.ultraman.oqsengine.metadata.executor;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.IWatcher;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IDelayTaskExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;
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

import static com.xforceplus.ultraman.oqsengine.meta.common.exception.Code.BUSINESS_HANDLER_ERROR;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.Constant.COMMON_WAIT_TIME_OUT;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.Constant.EXPIRED_VERSION;
import static com.xforceplus.ultraman.oqsengine.metadata.utils.EntityClassStorageConvert.fromProtoBuffer;

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
        ThreadUtils.shutdown(thread, COMMON_WAIT_TIME_OUT);
    }


    @Override
    public boolean sync(String appId, int version, EntityClassSyncRspProto entityClassSyncRspProto) {

        // step1 prepare
        if (cacheExecutor.prepare(appId, version)) {
            try {
                int expiredVersion = version(appId);

                // step2 convert to storage
                List<EntityClassStorage> entityClassStorageList = convert(version, entityClassSyncRspProto);

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
     * @param version
     * @param entityClassSyncRspProto
     * @return
     */
    private List<EntityClassStorage> convert(int version, EntityClassSyncRspProto entityClassSyncRspProto) {
        Map<Long, EntityClassStorage> temp = entityClassSyncRspProto.getEntityClassesList().stream().map(
                ecs -> {
                    EntityClassStorage e = fromProtoBuffer(ecs, EntityClassStorage.class);
                    e.setVersion(version);
                    return e;
                }
        ).collect(Collectors.toMap(EntityClassStorage::getId, s1 -> s1,  (s1, s2) -> s1));

        return temp.values().stream().peek(
                v -> {
                    Long fatherId = v.getFatherId();
                    if (null != fatherId) {
                        while (null != fatherId) {
                            EntityClassStorage entityClassStorage = temp.get(v.getFatherId());
                            if (null == entityClassStorage) {
                                throw new MetaSyncClientException(
                                        String.format("father entityClass : [%d] missed.", v.getFatherId()), BUSINESS_HANDLER_ERROR.ordinal());
                            }
                            v.addAncestors(v.getFatherId());
                            fatherId = entityClassStorage.getFatherId();
                        }
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
