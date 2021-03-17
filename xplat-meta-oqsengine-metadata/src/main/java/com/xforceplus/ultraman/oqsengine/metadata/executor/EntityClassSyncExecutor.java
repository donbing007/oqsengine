package com.xforceplus.ultraman.oqsengine.metadata.executor;

import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IDelayTaskExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.pojo.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ThreadUtils;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.cache.ICacheExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParamsConfig.SHUT_DOWN_WAIT_TIME_OUT;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;
import static com.xforceplus.ultraman.oqsengine.meta.common.utils.EntityClassStorageBuilderUtils.protoToStorageList;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.Constant.*;

/**
 * desc :
 * name : EntityClassSyncExecutor
 *
 * @author : xujia
 * date : 2021/2/18
 * @since : 1.8
 */
public class EntityClassSyncExecutor implements SyncExecutor {

    final Logger logger = LoggerFactory.getLogger(EntityClassSyncExecutor.class);

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
        expireExecutor.stop();
        ThreadUtils.shutdown(thread, SHUT_DOWN_WAIT_TIME_OUT);
    }


    @Override
    public boolean sync(String appId, int version, EntityClassSyncRspProto entityClassSyncRspProto) {

        // step1 prepare
        if (cacheExecutor.prepare(appId, version)) {
            try {
                int expiredVersion = version(appId);

                // step2 convert to storage
                List<EntityClassStorage> entityClassStorageList = protoToStorageList(entityClassSyncRspProto);

                // step3 update new Hash in redis
                if (!cacheExecutor.save(appId, version, entityClassStorageList)) {
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
                logger.warn(e.getMessage());
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

    private void delayCleanTask() {
        while (true) {
            ExpireExecutor.DelayCleanEntity task = expireExecutor.take();
            if (null == task || null == task.element()) {
                TimeWaitUtils.wakeupAfter(1, TimeUnit.MILLISECONDS);
                continue;
            }
            try {
                boolean isClean =
                        cacheExecutor.clean(task.element().getAppId(), task.element().getVersion(), false);

                logger.debug("clean app : {}, version : {}， success : {}"
                        , task.element().getAppId(), task.element().getVersion(), isClean);
            } catch (Exception e) {
                //  ignore
                logger.warn("clean app : {}, version : {} catch exception, message : {} , but will ignore..."
                        , task.element().getAppId(), task.element().getVersion(), e.getMessage());
            }
        }
    }
}
