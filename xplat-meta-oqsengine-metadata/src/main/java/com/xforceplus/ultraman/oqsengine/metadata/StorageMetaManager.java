package com.xforceplus.ultraman.oqsengine.metadata;

import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.pojo.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.meta.common.pojo.RelationStorage;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import com.xforceplus.ultraman.oqsengine.metadata.cache.ICacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.dto.HealthCheckEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsRelation;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.MIN_ID;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.Constant.COMMON_WAIT_TIME_OUT;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.Constant.HEALTH_CHECK_ENTITY_ID;

/**
 * desc :
 * name : StorageMetaManager
 * Meta管理类
 *
 * @author : xujia
 * date : 2021/2/9
 * @since : 1.8
 */
public class StorageMetaManager implements MetaManager {

    final Logger logger = LoggerFactory.getLogger(StorageMetaManager.class);

    @Resource
    private ICacheExecutor cacheExecutor;

    @Resource
    private IRequestHandler requestHandler;

    @Resource(name = "waitVersionExecutor")
    private ExecutorService asyncDispatcher;

    private <T> CompletableFuture<T> async(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, asyncDispatcher);
    }

    /**
     * 使用entityClassId获取对应的EntityClass
     * @param id 元信息的标识.
     * @return
     */
    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "meta", "action", "load"})
    @Override
    public Optional<IEntityClass> load(long id) {
        try {
            if (id == HEALTH_CHECK_ENTITY_ID) {
                return Optional.of(HealthCheckEntityClass.getInstance());
            }
            Map<Long, EntityClassStorage> entityClassStorageMaps = cacheExecutor.read(id);
            return Optional.of(toEntityClass(id, entityClassStorageMaps));
        } catch (Exception e) {
            logger.warn(String.format("load entityClass [%d] error, message [%s]", id, e.getMessage()));
            return Optional.empty();
        }
    }

    @Override
    public Optional<IEntityClass> loadHistory(long id, int version) {
        return Optional.empty();
    }

    /**
     * 需要关注某个appId
     * 注意：当前的实现只支持单个appId的单个Env，即appId如果关注了test env，则无法再次关注其他环境
     * @param appId 应用标识.
     * @param env
     * @return
     */
    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "meta", "action", "need"})
    @Override
    public int need(String appId, String env) {
        try {
            cacheExecutor.appEnvSet(appId, env);

            if (!cacheExecutor.appEnvGet(appId).equals(env)) {
                throw new RuntimeException("appId has been init with another Id, need failed...");
            }

            int version = cacheExecutor.version(appId);

            requestHandler.register(new WatchElement(appId, env, version, WatchElement.ElementStatus.Register));

            if (version < 0) {
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
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 使本地缓存失效
     */
    @Override
    public void invalidateLocal() {
        cacheExecutor.invalidateLocal();
    }

    /**
     * 生成IEntityClass
     * @param id
     * @param entityClassStorageMaps
     * @return
     * @throws SQLException
     */
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
                        .withRelations(toQqsRelation(entityClassStorage.getRelations()))
                        .withFields(entityClassStorage.getFields());
        /**
         * 加载父类
         */
        if (null != entityClassStorage.getFatherId() && entityClassStorage.getFatherId() >= MIN_ID) {
            builder.withFather(toEntityClass(entityClassStorage.getFatherId(), entityClassStorageMaps));
        }

        return builder.build();
    }

    /**
     * 加载relation
     *
     * @param relationStorageList
     * @return
     */
    private List<OqsRelation> toQqsRelation(List<RelationStorage> relationStorageList) {
        List<OqsRelation> oqsRelations = new ArrayList<>();
        relationStorageList.forEach(
                r -> {
                    OqsRelation.Builder builder = OqsRelation.Builder.anOqsRelation()
                            .withId(r.getId())
                            .withCode(r.getCode())
                            .withLeftEntityClassId(r.getLeftEntityClassId())
                            .withLeftEntityClassCode(r.getLeftEntityClassCode())
                            .withRelationType(OqsRelation.RelationType.getInstance(r.getRelationType()))
                            .withIdentity(r.isIdentity())
                            .withStrong(r.isStrong())
                            .withRightEntityClassId(r.getRightEntityClassId())
                            .withRightEntityClassLoader(this::load)
                            .withEntityField(r.getEntityField())
                            .withBelongToOwner(r.isBelongToOwner());

                    oqsRelations.add(builder.build());
                }
        );
        return oqsRelations;
    }
}
