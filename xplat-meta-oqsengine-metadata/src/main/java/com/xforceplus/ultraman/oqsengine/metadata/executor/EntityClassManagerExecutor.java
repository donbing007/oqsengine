package com.xforceplus.ultraman.oqsengine.metadata.executor;

import com.xforceplus.ultraman.oqsengine.meta.executor.IEntityClassExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.cache.ICacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.dto.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.oqs.OqsEntityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static com.xforceplus.ultraman.oqsengine.metadata.constant.Constant.COMMON_WAIT_TIME_OUT;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.Constant.NOT_EXIST_VERSION;

/**
 * desc :
 * name : EntityClassManagerExecutor
 *
 * @author : xujia
 * date : 2021/2/9
 * @since : 1.8
 */
public class EntityClassManagerExecutor implements MetaManager {

    final Logger logger = LoggerFactory.getLogger(EntityClassManagerExecutor.class);

    @Resource
    private ICacheExecutor cacheExecutor;

    @Resource
    private IEntityClassExecutor entityClassExecutor;

    @Resource(name = "waitVersionExecutor")
    private ExecutorService asyncDispatcher;

    private <T> CompletableFuture<T> async(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, asyncDispatcher);
    }

    @Override
    public IEntityClass load(long id) {
        try {
            Map<Long, EntityClassStorage> entityClassStorageMaps = cacheExecutor.read(id);
            return toEntityClass(id, entityClassStorageMaps);
        } catch (Exception e) {
            logger.warn(e.getMessage());
            throw new RuntimeException(String.format("load entityClass [%d] error, message [%s]", id, e.getMessage()));
        }
    }

    @Override
    public IEntityClass loadHistory(long id, int version) {
        return null;
    }

    @Override
    public int need(String appId) {

        int version = cacheExecutor.version(appId);

        boolean ret = entityClassExecutor.register(appId, NOT_EXIST_VERSION);

        if (version < 0) {
            if (!ret) {
                throw new RuntimeException("register failed, gRpc sync client not read.");
            }
            CompletableFuture<Integer> future = async(() -> {
                int ver;
                /**
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
                throw new RuntimeException(String.format("get version of appId [%s] failed, reach max wait time", appId));
            }

        }
        return version;
    }

    private IEntityClass toEntityClass(long id, Map<Long, EntityClassStorage> entityClassStorageMaps) throws SQLException {
        EntityClassStorage entityClassStorage = entityClassStorageMaps.get(id);
        if (null == entityClassStorage) {
            throw new SQLException(String.format("entity class [%d] not found.", id));
        }

        OqsEntityClass.Builder builder =
                OqsEntityClass.Builder.anEntityClass()
                        .withId(entityClassStorage.getId())
                        .withCode(entityClassStorage.getCode())
                        .withName(entityClassStorage.getName())
                        .withLevel(entityClassStorage.getLevel())
                        .withVersion(entityClassStorage.getVersion())
                        .withRelations(entityClassStorage.getRelations())
                        .withFields(entityClassStorage.getFields());
        /**
         * 加载父类
         */
        if (null != entityClassStorage.getFatherId()) {
            builder.withFather(toEntityClass(entityClassStorage.getFatherId(), entityClassStorageMaps));
        }

        return builder.build();
    }
}
