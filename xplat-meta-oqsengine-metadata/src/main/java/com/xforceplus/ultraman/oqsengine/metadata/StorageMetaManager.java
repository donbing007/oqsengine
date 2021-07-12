package com.xforceplus.ultraman.oqsengine.metadata;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.MIN_ID;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.Constant.COMMON_WAIT_TIME_OUT;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.Constant.HEALTH_CHECK_ENTITY_ID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.common.profile.OqsProfile;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.ProfileStorage;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.RelationStorage;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import com.xforceplus.ultraman.oqsengine.metadata.cache.CacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.dto.HealthCheckEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsRelation;
import io.micrometer.core.annotation.Timed;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
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

    @Resource(name = "taskThreadPool")
    private ExecutorService asyncDispatcher;

    private boolean isOffLineUse = false;

    public void isOffLineUse() {
        this.isOffLineUse = true;
    }

    private <T> CompletableFuture<T> async(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, asyncDispatcher);
    }

    /**
     * 使用entityClassId获取对应的EntityClass.
     *
     * @param id 元信息的标识.
     * @return 元信息实现.
     */
    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "meta", "action", "load"})
    @Override
    public Optional<IEntityClass> load(long id) {
        try {
            return innerLoad(id, null);
        } catch (Exception e) {
            logger.warn("load entityClass [{}] error, message [{}]", id, e.toString());
            return Optional.empty();
        }
    }

    @Override
    public Optional<IEntityClass> load(long id, String profile) {

        try {
            return innerLoad(id, profile);
        } catch (Exception e) {
            logger.warn(String.format("load entityClass [%d]-[%s] error, message [%s]", id, profile, e.getMessage()));
            return Optional.empty();
        }
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

            if (!cacheExecutor.appEnvGet(appId).equals(env)) {
                throw new RuntimeException("appId has been init with another Id, need failed...");
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

    private Optional<IEntityClass> innerLoad(long id, String profileCode)
        throws SQLException, JsonProcessingException {
        if (id == HEALTH_CHECK_ENTITY_ID) {
            return Optional.of(HealthCheckEntityClass.getInstance());
        }
        Map<Long, EntityClassStorage> entityClassStorageMaps = cacheExecutor.read(id);
        return Optional.of(toEntityClass(id, profileCode, entityClassStorageMaps));
    }

    /**
     * 生成IEntityClass.
     */
    private IEntityClass toEntityClass(long id, String profileCode,
                                       Map<Long, EntityClassStorage> entityClassStorageMaps) throws SQLException {
        EntityClassStorage entityClassStorage = entityClassStorageMaps.get(id);
        if (null == entityClassStorage) {
            throw new SQLException(String.format("entity class [%d] not found.", id));
        }

        List<OqsRelation> oqsRelations = toQqsRelation(entityClassStorage.getRelations());

        List<IEntityField> entityFields = new ArrayList<>();
        if (null != entityClassStorage.getFields()) {
            entityClassStorage.getFields()
                .forEach(
                    e -> {
                        IEntityField entityField = cloneEntityField(e);
                        if (null != entityField) {
                            entityFields.add(entityField);
                        }
                    }
                );
        }

        //  加载profile
        if (null != profileCode && !profileCode.equals(OqsProfile.UN_DEFINE_PROFILE) && null != entityClassStorage.getProfileStorageMap()) {
            ProfileStorage profileStorage = entityClassStorage.getProfileStorageMap().get(profileCode);
            if (null != profileStorage) {
                if (null != profileStorage.getEntityFieldList()) {
                    profileStorage.getEntityFieldList().forEach(
                        ps -> {
                            IEntityField entityField = cloneEntityField(ps);
                            if (null != entityField) {
                                entityFields.add(entityField);
                            }
                        }
                    );
                }

                if (null != profileStorage.getRelationStorageList()) {
                    oqsRelations.addAll(toQqsRelation(profileStorage.getRelationStorageList()));
                }
            }
        }

        OqsEntityClass.Builder builder =
            OqsEntityClass.Builder.anEntityClass()
                .withId(entityClassStorage.getId())
                .withCode(entityClassStorage.getCode())
                .withName(entityClassStorage.getName())
                .withLevel(entityClassStorage.getLevel())
                .withVersion(entityClassStorage.getVersion())
                .withRelations(oqsRelations)
                .withFields(entityFields);
        //   加载父类.
        if (null != entityClassStorage.getFatherId() && entityClassStorage.getFatherId() >= MIN_ID) {
            builder.withFather(toEntityClass(entityClassStorage.getFatherId(), profileCode, entityClassStorageMaps));
        }

        return builder.build();
    }


    /**
     * 加载relation.
     */
    private List<OqsRelation> toQqsRelation(List<RelationStorage> relationStorageList) {
        List<OqsRelation> oqsRelations = new ArrayList<>();
        if (null != relationStorageList) {
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
                        .withEntityField(cloneEntityField(r.getEntityField()))
                        .withBelongToOwner(r.isBelongToOwner());

                    oqsRelations.add(builder.build());
                }
            );
        }
        return oqsRelations;
    }

    private IEntityField cloneEntityField(IEntityField entityField) {
        if (null != entityField) {
            EntityField.Builder builder = EntityField.Builder.anEntityField()
                .withName(entityField.name())
                .withCnName(entityField.cnName())
                .withFieldType(entityField.type())
                .withDictId(entityField.dictId())
                .withId(entityField.id())
                .withDefaultValue(entityField.defaultValue());

            if (null != entityField.config()) {
                builder.withConfig(entityField.config().clone());
            }

            return builder.build();
        }
        return null;
    }
}
