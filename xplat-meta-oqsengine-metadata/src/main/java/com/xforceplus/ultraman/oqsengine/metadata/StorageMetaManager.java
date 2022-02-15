package com.xforceplus.ultraman.oqsengine.metadata;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;
import static com.xforceplus.ultraman.oqsengine.metadata.cache.DefaultCacheExecutor.OBJECT_MAPPER;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.Constant.COMMON_WAIT_TIME_OUT;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_CODE;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_FATHER;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_FIELDS;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_ID;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_LEVEL;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_NAME;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_PROFILES;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_RELATIONS;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.EntityClassElements.ELEMENT_VERSION;
import static com.xforceplus.ultraman.oqsengine.metadata.utils.CacheUtils.parseOneKeyFromProfileEntity;
import static com.xforceplus.ultraman.oqsengine.metadata.utils.CacheUtils.parseOneKeyFromProfileRelations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.common.profile.OqsProfile;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.monitor.dto.MetricsLog;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.cache.CacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.cache.DefaultCacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.dto.HealthCheckEntityClass;
import com.xforceplus.ultraman.oqsengine.metadata.dto.metrics.MetaMetrics;
import com.xforceplus.ultraman.oqsengine.metadata.dto.model.AbstractMetaModel;
import com.xforceplus.ultraman.oqsengine.metadata.dto.model.MetaModel;
import com.xforceplus.ultraman.oqsengine.metadata.dto.model.OfflineModel;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.RelationStorage;
import com.xforceplus.ultraman.oqsengine.metadata.utils.CacheUtils;
import com.xforceplus.ultraman.oqsengine.metadata.utils.offline.FileReaderUtils;
import com.xforceplus.ultraman.oqsengine.metadata.utils.offline.OffLineMetaHelper;
import com.xforceplus.ultraman.oqsengine.metadata.utils.storage.CacheToStorageGenerator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import io.micrometer.core.annotation.Timed;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Function;
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

    @Resource(name = "taskThreadPool")
    private ExecutorService asyncDispatcher;

    private AbstractMetaModel metaModel;

    public StorageMetaManager(AbstractMetaModel metaModel) {
        this.metaModel = metaModel;
    }

    private <T> CompletableFuture<T> async(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, asyncDispatcher);
    }

    @PostConstruct
    public void init() {
        //  sync data from file
        if (metaModel.getModel().equals(MetaModel.OFFLINE)) {
            String path = ((OfflineModel) metaModel).getPath();
            logger.info("start load from local path : {}", path);

            offLineInit(path);

            logger.info("success load from local path : {}", path);
        }
    }

    @Override
    public Collection<IEntityClass> appLoad(String appId) {
        try {
            Collection<IEntityClass> collection = new ArrayList<>();
            int currentVersion = cacheExecutor.version(appId);
            if (currentVersion == NOT_EXIST_VERSION) {
                return collection;
            }

            Collection<Long> entityClassIds =
                cacheExecutor.appEntityIdList(appId, currentVersion);
            if (entityClassIds.isEmpty()) {
                return collection;
            }

            entityClassIds.forEach(
                entityClassId -> {
                    collection.addAll(withProfilesLoad(entityClassId, currentVersion));
                }
            );

            return collection;

        } catch (Exception e) {
            logger.warn("load meta by appId error, appId {}, message : {}", appId, e.getMessage());
            throw e;
        }
    }

    @Override
    public Optional<IEntityClass> load(long entityClassId, String profile) {
        //  这里是一次IO操作REDIS获取当前的版本, 并组装结构
        return load(entityClassId, cacheExecutor.version(entityClassId), profile);
    }

    @Override
    public Optional<IEntityClass> load(long entityClassId, int version, String profile) {

        /*
        健康检查使用的
         */
        if (entityClassId == HealthCheckEntityClass.getInstance().id()) {
            return Optional.of(HealthCheckEntityClass.getInstance());
        }

        /*
         * 不存在时抛出异常
         */
        if (NOT_EXIST_VERSION == version) {
            version = cacheExecutor.version(entityClassId);
            if (NOT_EXIST_VERSION == version) {
                throw new RuntimeException(
                    String.format("invalid entityClassId : [%d], no version pair", entityClassId));
            }
        }

        Optional<IEntityClass> ecOp = cacheExecutor.localRead(entityClassId, version, profile);
        if (ecOp.isPresent()) {
            return ecOp;
        } else {
            IEntityClass entityClass = null;
            try {
                //  从cache中读取原始数据
                Map<String, String> keyValues = cacheExecutor.remoteRead(entityClassId, version);

                if (keyValues.isEmpty()) {
                    throw new RuntimeException("entityClassStorage is null, may be delete.");
                }

                entityClass = classLoad(entityClassId, profile, keyValues);

                //  加入本地cache
                if (null != entityClass) {
                    cacheExecutor.localAdd(entityClassId, version, profile, entityClass);
                }
            } catch (Exception e) {
                logger.warn("load entityClass failed, message : {}", e.getMessage());
            }
            return Optional.ofNullable(entityClass);
        }
    }

    private Collection<IEntityClass> withProfilesLoad(long entityClassId, int version) {
        try {
            List<IEntityClass> entityClassList = new ArrayList<>();

            if (version == NOT_EXIST_VERSION) {
                version = cacheExecutor.version(entityClassId);
            }
            Optional<IEntityClass> entityClassOp =
                    load(entityClassId, version, null);

            if (entityClassOp.isPresent()) {
                entityClassList.add(entityClassOp.get());

                List<String> profiles = cacheExecutor.readProfileCodes(entityClassId, version);
                if (!profiles.isEmpty()) {
                    for (String profile : profiles) {
                        Optional<IEntityClass> ecOp =
                                load(entityClassId, version, profile);

                        ecOp.ifPresent(entityClassList::add);
                    }
                }
            }

            return entityClassList;
        } catch (Exception e) {
            logger.warn("load entityClass [{}] error, message [{}]", entityClassId, e.getMessage());
        }
        return new ArrayList<>();

    }

    @Override
    public Collection<IEntityClass> withProfilesLoad(long entityClassId) {
        return withProfilesLoad(entityClassId, NOT_EXIST_VERSION);
    }


    @Override
    public int need(String appId, String env) {
        return need(appId, env, false);
    }

    /**
     * 需要关注某个appId.
     * 注意：当前的实现只支持单个appId的单个Env，即appId如果关注了test env，则无法再次关注其他环境.
     *
     * @param appId 应用标识.
     * @param env   环境编码.
     * @return 版本号.
     */
    @Override
    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "meta", "action", "need"})
    public int need(String appId, String env, boolean overWrite) {
        try {
            cacheExecutor.appEnvSet(appId, env);

            String cacheEnv = cacheExecutor.appEnvGet(appId);
            if (!cacheEnv.equals(env) && !overWrite) {
                if (!overWrite) {
                    logger
                        .warn("appId [{}], param env [{}] not equals to cache's env [{}], will use cache to register.",
                            appId, env, cacheEnv);

                    throw new RuntimeException("appId has been init with another Id, need failed...");
                } else {
                    env = cacheEnv;
                }
            }

            int version = cacheExecutor.version(appId);

            if (metaModel.getModel().equals(MetaModel.CLIENT_SYNC)) {
                requestHandler.register(new WatchElement(appId, env, version, WatchElement.ElementStatus.Register), overWrite);

                if (version < 0) {
                    version = waitForMetaSync(appId);
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
    public boolean metaImport(String appId, String env, int version, String content) {

        //  当缓存中appId不存在时,将会设置一个新的ENV
        cacheExecutor.appEnvSet(appId, env);

        //  判断环境一致
        if (!cacheExecutor.appEnvGet(appId).equals(env)) {
            throw new RuntimeException("appId has been init with another Id, need failed...");
        }

        int currentVersion = cacheExecutor.version(appId);

        if (version > currentVersion) {
            logger.info("execute data import, appId {}, currentVersion {}, update version {}", appId, currentVersion,
                version);

            EntityClassSyncRspProto entityClassSyncRspProto;
            try {
                entityClassSyncRspProto = OffLineMetaHelper.toEntityClassSyncRspProto(content);
            } catch (Exception e) {
                throw new RuntimeException(
                    String.format("parse data to EntityClassSyncRspProto failed, message [%s]", e.getMessage()));
            }

            try {
                syncExecutor.sync(appId, version, entityClassSyncRspProto);
            } catch (Exception e) {
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

            Collection<EntityClassStorage> result = CacheToStorageGenerator.toEntityClassStorages(
                    DefaultCacheExecutor.OBJECT_MAPPER,
                    cacheExecutor.multiRemoteRead(
                        cacheExecutor.appEntityIdList(appId, currentVersion), currentVersion
                    )
                ).values();


            return Optional
                .of(new MetaMetrics(currentVersion, cacheExecutor.appEnvGet(appId), appId, result));

        } catch (Exception e) {
            logger.warn("show meta error, appId {}, message : {}", appId, e.getMessage());
            throw e;
        }
    }

    @Override
    public Collection<MetricsLog> metaLogs(MetricsLog.ShowType showType) {
        return requestHandler.metricsRecorder().showLogs(showType);
    }

    @Override
    public int reset(String appId, String env) {
        String cacheEnv = cacheExecutor.appEnvGet(appId);
        //  重置
        if (null == cacheEnv || cacheEnv.isEmpty()) {
            return need(appId, env);
        } else {
            int version = cacheExecutor.version(appId);

            if (!cacheEnv.equals(env)) {
                if (version > NOT_EXIST_VERSION) {
                    cacheExecutor.clean(appId, version, true);
                }

                requestHandler
                    .reset(new WatchElement(appId, env, NOT_EXIST_VERSION, WatchElement.ElementStatus.Register));

                version = waitForMetaSync(appId);

                cacheExecutor.appEnvSet(appId, env);
            }

            return version;
        }
    }

    @Override
    public boolean remove(String appId) {
        int version = cacheExecutor.version(appId);

        if (version > NOT_EXIST_VERSION) {
            cacheExecutor.clean(appId, version, true);
        }

        cacheExecutor.appEnvRemove(appId);

        return true;
    }

    @Override
    public Map<String, String> showApplications() {
        return cacheExecutor.showAppEnv();
    }

    private void offLineInit(String path) {
        if (OffLineMetaHelper.isValidPath(path)) {

            if (!path.endsWith(File.separator)) {
                path = path + File.separator;
            }

            List<String> files = FileReaderUtils.getFileNamesInOneDir(path);
            for (String file : files) {
                try {
                    String[] splitter = OffLineMetaHelper.splitMetaFromFileName(file);

                    String appId = splitter[0];
                    int version = Integer.parseInt(splitter[1]);
                    String fullPath = path + file;

                    String v =
                        OffLineMetaHelper.initDataFromFilePath(appId, splitter[2], version, fullPath);

                    if (metaImport(splitter[0], splitter[2], version, v)) {
                        logger
                            .info("init meta from local path success, path : {}, appId : {}, version : {}", fullPath,
                                appId,
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
            return;
        }

        logger.warn("load path invalid, nothing would be load from offLine-model.");
    }

    /**
     * 等待SyncClient进行register并返回当前版本.
     *
     * @param appId 应用ID.
     * @return 版本号.
     */
    private int waitForMetaSync(String appId) {
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
            int version = future.get(COMMON_WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
            if (version == NOT_EXIST_VERSION) {
                throw new RuntimeException(
                    String.format("get version of appId [%s] failed, reach max wait time", appId));
            }
            return version;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.warn(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 获取一个完整的EntityClass.
     */
    public IEntityClass classLoad(long entityClassId, String profile, Map<String, String> keyValues) {
        try {
            EntityClass.Builder builder = EntityClass.Builder.anEntityClass();

            //  set id
            String id = keyValues.remove(ELEMENT_ID);
            if (null == id || id.isEmpty()) {
                throw new RuntimeException(
                    String.format("catch id is null from cache, query entityClassId : %d", entityClassId));
            }
            builder.withId(Long.parseLong(id));

            //  code
            String code = keyValues.remove(ELEMENT_CODE);
            if (null == code || code.isEmpty()) {
                throw new RuntimeException(String.format("id : %s, code is null from cache.", id));
            }
            builder.withCode(code);

            //  name
            String name = keyValues.remove(ELEMENT_NAME);
            if (null != name && !name.isEmpty()) {
                builder.withName(name);
            }

            //  level
            String level = keyValues.remove(ELEMENT_LEVEL);
            if (null == level || level.isEmpty()) {
                throw new RuntimeException(String.format("id : %s, level is null from cache.", id));
            }
            builder.withLevel(Integer.parseInt(level));

            //  version
            String vn = keyValues.remove(ELEMENT_VERSION);
            if (null == vn || vn.isEmpty()) {
                throw new RuntimeException(String.format("id : %s, version is null from cache.", id));
            }
            builder.withVersion(Integer.parseInt(vn));

            //  entityFields & profile & relations
            withFieldsRelations(builder, profile, keyValues, this::load, this::withProfilesLoad);

            //  father
            String father = keyValues.remove(ELEMENT_FATHER);
            if (CacheUtils.validBusinessId(father)) {
                Optional<IEntityClass> fatherEntityClassOp = load(Long.parseLong(father), profile);
                if (fatherEntityClassOp.isPresent()) {
                    builder.withFather(fatherEntityClassOp.get());
                } else {
                    throw new RuntimeException(String.format("id : %s, father not get from cache.", id));
                }
            }

            return builder.build();

        } catch (Exception e) {
            logger.warn(e.getMessage());
            return null;
        }
    }

    private void withFieldsRelations(EntityClass.Builder builder, String profile, Map<String, String> keyValues,
                                     BiFunction<Long, String, Optional<IEntityClass>> rightEntityClassLoader,
                                     Function<Long, Collection<IEntityClass>> rightFamilyEntityClassLoader) throws
        JsonProcessingException {

        List<IEntityField> fields = new ArrayList<>();
        List<Relationship> relationships = new ArrayList<>();

        Iterator<Map.Entry<String, String>> iterator = keyValues.entrySet().iterator();

        profile = (null == profile) ? OqsProfile.UN_DEFINE_PROFILE : profile;

        boolean profileFound = false;
        //  entityFields & profile
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            if (entry.getKey().startsWith(ELEMENT_FIELDS + ".")) {
                fields.add(CacheUtils.resetAutoFill(OBJECT_MAPPER.readValue(entry.getValue(), EntityField.class)));
            } else if (entry.getKey().startsWith(ELEMENT_PROFILES + "." + ELEMENT_FIELDS)) {
                String key = parseOneKeyFromProfileEntity(entry.getKey());
                if (key.equals(profile)) {
                    profileFound = true;
                    fields.add(CacheUtils.resetAutoFill(OBJECT_MAPPER.readValue(entry.getValue(), EntityField.class)));
                }
            } else if (entry.getKey().startsWith(ELEMENT_PROFILES + "." + ELEMENT_RELATIONS)) {
                if (!profile.equals(OqsProfile.UN_DEFINE_PROFILE)) {
                    String key = parseOneKeyFromProfileRelations(entry.getKey());
                    if (profile.equals(key)) {
                        profileFound = true;
                        relationships.addAll(toQqsRelation(OBJECT_MAPPER.readValue(keyValues.get(entry.getKey()),
                            OBJECT_MAPPER.getTypeFactory().constructParametricType(
                                List.class, RelationStorage.class)), rightEntityClassLoader, rightFamilyEntityClassLoader));
                    }
                }
            }
        }
        if (profileFound) {
            builder.withProfile(profile);
        }

        builder.withFields(fields);

        //  relations
        String relations = keyValues.remove(ELEMENT_RELATIONS);
        if (null != relations && !relations.isEmpty()) {
            List<RelationStorage> relationStorageList = OBJECT_MAPPER.readValue(
                relations, OBJECT_MAPPER.getTypeFactory().constructParametricType(List.class, RelationStorage.class));
            relationships
                .addAll(toQqsRelation(relationStorageList, rightEntityClassLoader, rightFamilyEntityClassLoader));
        }

        builder.withRelations(relationships);
    }

    /**
     * 加载relation.
     */
    private List<Relationship> toQqsRelation(List<RelationStorage> relationStorageList,
                                             BiFunction<Long, String, Optional<IEntityClass>> rightEntityClassLoader,
                                             Function<Long, Collection<IEntityClass>> rightFamilyEntityClassLoader) {
        List<Relationship> relationships = new ArrayList<>();
        if (null != relationStorageList) {
            relationStorageList.forEach(
                r -> {
                    Relationship.Builder builder = Relationship.Builder.anRelationship()
                        .withId(r.getId())
                        .withCode(r.getCode())
                        .withLeftEntityClassId(r.getLeftEntityClassId())
                        .withLeftEntityClassCode(r.getLeftEntityClassCode())
                        .withRelationType(Relationship.RelationType.getInstance(r.getRelationType()))
                        .withIdentity(r.isIdentity())
                        .withStrong(r.isStrong())
                        .withRightEntityClassId(r.getRightEntityClassId())
                        .withRightEntityClassLoader(rightEntityClassLoader)
                        .withRightFamilyEntityClassLoader(rightFamilyEntityClassLoader)
                        .withEntityField(r.getEntityField())
                        .withBelongToOwner(r.isBelongToOwner());

                    relationships.add(builder.build());
                }
            );
        }
        return relationships;
    }
}