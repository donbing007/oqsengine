package com.xforceplus.ultraman.oqsengine.storage.master;

import static com.xforceplus.ultraman.oqsengine.storage.master.utils.OriginalEntityUtils.attributesToList;

import com.alibaba.google.common.collect.Lists;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.common.map.MapUtils;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.common.profile.OqsProfile;
import com.xforceplus.ultraman.oqsengine.common.serializable.utils.JacksonDefaultMapper;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.select.BusinessKey;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.utils.MD5Utils;
import com.xforceplus.ultraman.oqsengine.storage.define.OperationType;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.condition.QueryErrorCondition;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.BatchQueryCountExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.BatchQueryExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.BuildExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.BuildUniqueExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.DeleteExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.DeleteUniqueExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.ExistExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.MultipleQueryExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.QueryExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.QueryLimitCommitidByConditionsExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.QueryUniqueExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.UpdateExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.errors.QueryErrorExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.errors.ReplaceErrorExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.ErrorStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.MasterStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.StorageUniqueEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.conditions.SQLJsonConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.master.unique.UniqueIndexValue;
import com.xforceplus.ultraman.oqsengine.storage.master.unique.UniqueKeyGenerator;
import com.xforceplus.ultraman.oqsengine.storage.master.utils.EntityClassRefHelper;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.commit.CommitHelper;
import com.xforceplus.ultraman.oqsengine.storage.value.AnyStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValueFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import io.micrometer.core.annotation.Timed;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 主要储存实现.
 *
 * @author dongbin
 * @version 0.1 2020/2/16 22:11
 * @since 1.8
 */
public class SQLMasterStorage implements MasterStorage {

    private final Logger logger = LoggerFactory.getLogger(SQLMasterStorage.class);

    @Resource(name = "storageJDBCTransactionExecutor")
    private TransactionExecutor transactionExecutor;

    @Resource(name = "masterStorageStrategy")
    private StorageStrategyFactory storageStrategyFactory;

    @Resource(name = "masterConditionsBuilderFactory")
    private SQLJsonConditionsBuilderFactory conditionsBuilderFactory;

    @Resource
    private MetaManager metaManager;

    @Resource
    private UniqueKeyGenerator keyGenerator;

    @Resource(name = "taskThreadPool")
    private ExecutorService asyncErrorExecutor;

    private String tableName;

    private String uniqueTableName;

    private String errorTable;

    private long queryTimeout;

    public void setQueryTimeout(long queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setUniqueTableName(String uniqueTableName) {
        this.uniqueTableName = uniqueTableName;
    }

    public void setErrorTable(String errorTable) {
        this.errorTable = errorTable;
    }

    @Override
    @PostConstruct
    public void init() {

        if (queryTimeout <= 0) {
            setQueryTimeout(3000L);
        }
    }

    @Override
    public DataIterator<OriginalEntity> iterator(IEntityClass entityClass, long startTime, long endTime, long lastStart)
        throws SQLException {
        return new EntityIterator(entityClass, lastStart, startTime, endTime);
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        extraTags = {"initiator", "master", "action", "bussinessKey"}
    )
    @Override
    public Optional<StorageUniqueEntity> select(List<BusinessKey> businessKeys, IEntityClass entityClass)
        throws SQLException {
        if (!containUniqueConfig(businessKeys, entityClass)) {
            return Optional.empty();
        }
        String uniqueKey = buildEntityUniqueKeyByBusinessKey(businessKeys, entityClass);
        return (Optional<StorageUniqueEntity>) transactionExecutor.execute((tx, resource, hint) -> {
            Optional<StorageUniqueEntity> seOP =
                new QueryUniqueExecutor(uniqueTableName, resource, entityClass, queryTimeout).execute(uniqueKey);
            return seOP;
        });
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        extraTags = {"initiator", "master", "action", "condition"}
    )
    @Override
    public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, SelectConfig config)
        throws SQLException {
        return (Collection<EntityRef>) transactionExecutor.execute((tx, resource, hint) -> {
            return QueryLimitCommitidByConditionsExecutor.build(
                tableName,
                resource,
                entityClass,
                config,
                queryTimeout,
                conditionsBuilderFactory,
                storageStrategyFactory).execute(conditions);
        });
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "master", "action", "exist"})
    @Override
    public boolean exist(long id) throws SQLException {
        return (boolean) transactionExecutor.execute(((tx, resource, hint) ->
            ExistExecutor.build(tableName, resource, queryTimeout).execute(id)));
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "master", "action", "one"})
    @Override
    public Optional<IEntity> selectOne(long id) throws SQLException {
        return (Optional<IEntity>) transactionExecutor.execute((tx, resource, hint) -> {
            Optional<MasterStorageEntity> masterStorageEntityOptional =
                QueryExecutor.buildHaveDetail(tableName, resource, queryTimeout).execute(id);
            if (masterStorageEntityOptional.isPresent()) {
                return Optional.ofNullable(buildEntityFromStorageEntity(masterStorageEntityOptional.get()));
            } else {
                return Optional.empty();
            }
        });
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "master", "action", "one"})
    @Override
    public Optional<IEntity> selectOne(long id, IEntityClass entityClass) throws SQLException {
        Optional<IEntity> entityOptional = selectOne(id);
        if (entityOptional.isPresent()) {

            // 校验类型是否正确.
            IEntity e = entityOptional.get();
            Optional<IEntityClass> actualEntityClassOp =
                metaManager.load(e.entityClassRef().getId(), e.entityClassRef().getProfile());
            if (actualEntityClassOp.isPresent()) {

                if (actualEntityClassOp.get().isCompatibility(entityClass.id())) {
                    return entityOptional;
                } else {
                    return Optional.empty();
                }

            } else {
                return Optional.empty();
            }

        }
        return entityOptional;
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        extraTags = {"initiator", "master", "action", "multiple"}
    )
    @Override
    public Collection<IEntity> selectMultiple(long[] ids) throws SQLException {
        // 排重.
        long[] useIds = removeDuplicate(ids);

        Collection<MasterStorageEntity> masterStorageEntities =
            (Collection<MasterStorageEntity>) transactionExecutor.execute(
                (tx, resource, hint) -> {

                    return MultipleQueryExecutor.build(tableName, resource, queryTimeout).execute(useIds);
                }
            );

        List<IEntity> entities = new ArrayList<>(masterStorageEntities.size());
        IEntity entity;
        for (MasterStorageEntity masterStorageEntity : masterStorageEntities) {
            entity = buildEntityFromStorageEntity(masterStorageEntity);
            if (entity != null) {
                entities.add(entity);
            }
        }

        return entities;
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        extraTags = {"initiator", "master", "action", "multiple"}
    )
    @Override
    public Collection<IEntity> selectMultiple(long[] ids, IEntityClass entityClass) throws SQLException {
        Collection<IEntity> entities = selectMultiple(ids);
        return entities.stream().filter(e -> {

            Optional<IEntityClass> actualEntityClassOp =
                metaManager.load(e.entityClassRef().getId(), e.entityClassRef().getProfile());
            if (actualEntityClassOp.isPresent()) {

                return actualEntityClassOp.get().isCompatibility(entityClass.id());

            } else {
                return false;
            }

        }).collect(Collectors.toList());
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "master", "action", "build"})
    @Override
    public int build(IEntity entity, IEntityClass entityClass) throws SQLException {
        checkId(entity);

        return (int) transactionExecutor.execute(
            (tx, resource, hint) -> {

                long createTime = findTime(entity, FieldConfig.FieldSense.CREATE_TIME);
                long updateTime = findTime(entity, FieldConfig.FieldSense.UPDATE_TIME);
                MasterStorageEntity.Builder storageEntityBuilder;
                try {
                    storageEntityBuilder = MasterStorageEntity.Builder.anStorageEntity()
                        .withId(entity.id())
                        /*
                         * optimize: 创建时间和更新时间保证和系统字段同步.
                         */
                        .withCreateTime(createTime > 0 ? createTime : entity.time())
                        .withUpdateTime(updateTime > 0 ? updateTime : entity.time())
                        .withDeleted(false)
                        .withEntityClassVersion(entityClass.version())
                        .withVersion(0)
                        .withAttribute(toJson(entity.entityValue()))
                        .withOp(OperationType.CREATE.getValue())
                        .withProfile(entity.entityClassRef().getProfile());
                } catch (Exception ex) {
                    throw new SQLException(ex.getMessage(), ex);
                }
                fullEntityClassInformation(storageEntityBuilder, entityClass);
                fullTransactionInformation(storageEntityBuilder, resource);
                MasterStorageEntity entityForBuild = storageEntityBuilder.build();
                int ret = BuildExecutor.build(tableName, resource, queryTimeout).execute(entityForBuild);
                String uniqueKey = buildEntityUniqueKeyByEntity(entity, entityClass);
                if (!StringUtils.isBlank(uniqueKey)) {
                    buildUnique(entity, uniqueKey, entityClass, resource);
                }
                return ret;
            });
    }

    @Override
    public void writeError(ErrorStorageEntity errorStorageEntity) {
        asyncErrorExecutor.submit(() -> {
            try {
                transactionExecutor.execute((tx, resource, hint) -> {
                    return new ReplaceErrorExecutor(errorTable, resource, queryTimeout).execute(errorStorageEntity);
                });
            } catch (Exception e) {
                logger.warn("write error record failed, errorStorageEntity [{}]", errorStorageEntity.toString());
            }
        });
    }

    @Override
    public Collection<ErrorStorageEntity> selectErrors(QueryErrorCondition queryErrorCondition) throws SQLException {
        return (Collection<ErrorStorageEntity>) transactionExecutor.execute((tx, resource, hint) -> {
            return new QueryErrorExecutor(errorTable, resource, queryTimeout).execute(queryErrorCondition);
        });
    }

    private String buildEntityUniqueKeyByBusinessKey(List<BusinessKey> businessKeys, IEntityClass entityClass) {
        Map<String, UniqueIndexValue> values = keyGenerator.generator(businessKeys, entityClass);
        Optional<UniqueIndexValue> indexValue = matchUniqueConfig(entityClass, values);
        return indexValue.isPresent() ? MD5Utils.encrypt(indexValue.get().getValue()) : "";
    }

    private boolean containUniqueConfig(List<BusinessKey> businessKeys, IEntityClass entityClass) {
        Map<String, UniqueIndexValue> values = keyGenerator.generator(businessKeys, entityClass);
        return matchUniqueConfig(entityClass, values).isPresent();
    }

    private int buildUnique(IEntity entity, String uniqueKey, IEntityClass entityClass, TransactionResource resource)
        throws SQLException {
        StorageUniqueEntity storageUniqueEntity =
            StorageUniqueEntity.builder().id(entity.id()).key(uniqueKey)
                .entityClasses(getEntityClasses(entityClass)).build();
        logger.debug("entityClasses length : {}, Unique entity : {}",
            storageUniqueEntity.getEntityClasses().length, storageUniqueEntity);
        int result = BuildUniqueExecutor.build(uniqueTableName, resource, queryTimeout)
            .execute(storageUniqueEntity);
        if (result < 1) {
            logger.warn("Failed to build unique index record!");
        }
        return result;
    }

    private long[] getEntityClasses(IEntityClass entityClass) {
        Collection<IEntityClass> family = entityClass.family();
        long[] tileEntityClassesIds = family.stream().mapToLong(ecs -> ecs.id()).toArray();
        return tileEntityClassesIds;
    }

    private String buildEntityUniqueKeyByEntity(IEntity entity, IEntityClass entityClass) throws SQLException {
        Map<String, UniqueIndexValue> values = keyGenerator.generator(entity);
        Optional<UniqueIndexValue> indexValue = matchUniqueConfig(entityClass, values);
        return indexValue.isPresent() ? MD5Utils.encrypt(indexValue.get().getValue()) : "";
    }

    private Optional<UniqueIndexValue> matchUniqueConfig(IEntityClass entityClass, Map<String, UniqueIndexValue> keys) {
        List<String> codes = getAncestorCode(entityClass);
        return keys.values().stream()
            .filter(item -> codes.contains(item.getCode()))
            .findAny();
    }

    private List<String> getAncestorCode(IEntityClass entityClass) {
        List<String> codes = Lists.newArrayList(entityClass.code());
        while (entityClass.father().isPresent()) {
            codes.add(entityClass.father().get().code());
            entityClass = entityClass.father().get();
        }
        return codes;
    }

    private long findTime(IEntity entity, FieldConfig.FieldSense sense) {
        OptionalLong op = entity.entityValue().values().parallelStream()
            .filter(v -> sense == v.getField().config().getFieldSense())
            .mapToLong(v -> v.valueToLong()).findFirst();

        return op.orElse(0);
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        extraTags = {"initiator", "master", "action", "replace"}
    )
    @Override
    public int replace(IEntity entity, IEntityClass entityClass) throws SQLException {
        checkId(entity);

        return (int) transactionExecutor.execute(
            (tx, resource, hint) -> {

                long updateTime = findTime(entity, FieldConfig.FieldSense.UPDATE_TIME);
                /*
                 * 如果从新结果集中查询到更新时间,但是和当前最后更新时间相等那么使用系统时间.
                 */
                if (updateTime == entity.time()) {
                    updateTime = 0;
                }
                MasterStorageEntity.Builder storageEntityBuilder;
                try {
                    storageEntityBuilder = MasterStorageEntity.Builder.anStorageEntity()
                        .withId(entity.id())
                        /*
                         * optimize: 更新时间保证和系统字段同步.
                         */
                        .withUpdateTime(updateTime > 0 ? updateTime : entity.time())
                        .withVersion(entity.version())
                        .withEntityClassVersion(entityClass.version())
                        .withAttribute(toJson(entity.entityValue()))
                        .withOp(OperationType.UPDATE.getValue());
                } catch (Exception ex) {
                    throw new SQLException(ex.getMessage(), ex);
                }

                fullEntityClassInformation(storageEntityBuilder, entityClass);
                fullTransactionInformation(storageEntityBuilder, resource);
                MasterStorageEntity entityForBuild = storageEntityBuilder.build();
                int ret = UpdateExecutor.build(tableName, resource, queryTimeout).execute(entityForBuild);
                String uniqueKey = buildEntityUniqueKeyByEntity(entity, entityClass);
                if (!StringUtils.isBlank(uniqueKey)) {
                    //涉及到分片键的更新这里采用先删除后插入的方式
                    int del = deleteUnique(entity, resource);
                    int buildResult = 0;
                    if (del > 0) {
                        buildResult = buildUnique(entity, uniqueKey, entityClass, resource);
                        if (buildResult < 0) {
                            logger.warn("Failed to build unique index record! id: {}", entity.id());
                        }
                    } else {
                        logger.warn("Failed to delete unique index record! id: {}", entity.id());
                    }
                }
                return ret;
            });

    }

    private int deleteUnique(IEntity entity, TransactionResource resource) throws SQLException {
        StorageUniqueEntity.StorageUniqueEntityBuilder storageEntityBuilder = StorageUniqueEntity.builder();
        storageEntityBuilder.id(entity.id());
        return DeleteUniqueExecutor.build(uniqueTableName, resource, queryTimeout)
            .execute(storageEntityBuilder.build());
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "master", "action", "delete"})
    @Override
    public int delete(IEntity entity, IEntityClass entityClass) throws SQLException {
        checkId(entity);

        return (int) transactionExecutor.execute(
            (tx, resource, hint) -> {
                /*
                 * 删除数据时不再关心字段信息.
                 */
                MasterStorageEntity.Builder storageEntityBuilder = MasterStorageEntity.Builder.anStorageEntity()
                    .withId(entity.id())
                    .withOp(OperationType.DELETE.getValue())
                    .withUpdateTime(entity.time())
                    .withEntityClassVersion(entityClass.version())
                    .withDeleted(true)
                    .withVersion(entity.version());

                fullEntityClassInformation(storageEntityBuilder, entityClass);
                fullTransactionInformation(storageEntityBuilder, resource);
                int del = DeleteExecutor.build(tableName, resource, queryTimeout).execute(storageEntityBuilder.build());
                String uniqueKey = buildEntityUniqueKeyByEntity(entity, entityClass);
                if (!StringUtils.isBlank(uniqueKey)) {
                    int delUnique = deleteUnique(entity, resource);
                    if (delUnique < 1) {
                        logger.warn("Failed to delete the unique index record ! id : {}", entity.id());
                    }
                }
                return del;
            });
    }

    private void checkId(IEntity entity) throws SQLException {
        if (entity.id() == 0) {
            throw new SQLException("Invalid entity`s id.");
        }
    }

    // 储存字段转换为逻辑字段.
    private IEntityValue toEntityValue(MasterStorageEntity masterStorageEntity, IEntityClass entityClass)
        throws SQLException {
        Map<String, Object> object;
        try {
            object = JacksonDefaultMapper.OBJECT_MAPPER.readValue(masterStorageEntity.getAttribute(), Map.class);
        } catch (JsonProcessingException e) {
            throw new SQLException(e.getMessage(), e);
        }

        long fieldId;
        IEntityField field = null;
        Optional<IEntityField> fieldOp = null;
        StorageStrategy storageStrategy;
        StorageValue newStorageValue;
        StorageValue oldStorageValue;
        // key 为物理储存名称,值为构造出的储存值.
        Map<String, EntityValuePack> storageValueCache = new HashMap<>(object.size());

        for (String storageName : object.keySet()) {
            try {
                // 为了找出物理名称中的逻辑字段名称.
                fieldId = Long.parseLong(AnyStorageValue.getInstance(storageName).logicName());
                fieldOp = entityClass.field(fieldId);

                if (!fieldOp.isPresent()) {
                    continue;
                } else {
                    field = fieldOp.get();
                }

                storageStrategy = this.storageStrategyFactory.getStrategy(field.type());
                newStorageValue = StorageValueFactory.buildStorageValue(
                    storageStrategy.storageType(),
                    AnyStorageValue.getInstance(storageName).storageName(),
                    object.get(storageName));

                // 如果是多值.使用 stick 追加.
                if (storageStrategy.isMultipleStorageValue()) {
                    Optional<StorageValue> oldStorageValueOp = Optional.ofNullable(
                        storageValueCache.get(String.valueOf(field.id()))
                    ).map(x -> x.storageValue);

                    if (oldStorageValueOp.isPresent()) {
                        oldStorageValue = oldStorageValueOp.get();
                        storageValueCache.put(
                            String.valueOf(field.id()),
                            new EntityValuePack(field, oldStorageValue.stick(newStorageValue), storageStrategy));
                    } else {
                        storageValueCache.put(
                            String.valueOf(field.id()),
                            new EntityValuePack(field, newStorageValue, storageStrategy));
                    }
                } else {
                    // 单值
                    storageValueCache.put(String.valueOf(field.id()),
                        new EntityValuePack(field, newStorageValue, storageStrategy));
                }

            } catch (Exception ex) {
                throw new SQLException(ex.getMessage(), ex);
            }
        }

        IEntityValue values = EntityValue.build();
        storageValueCache.values().stream().forEach(e -> {
            values.addValue(e.strategy.toLogicValue(e.logicField, e.storageValue));
        });

        return values;

    }

    // 属性名称使用的是属性 F + {id}.
    private String toJson(IEntityValue value) throws JsonProcessingException {

        Map<String, Object> values = new HashMap<>(MapUtils.calculateInitSize(value.size()));

        StorageStrategy storageStrategy;
        StorageValue storageValue;
        for (IValue logicValue : value.values()) {

            if (logicValue != null && logicValue instanceof EmptyTypedValue) {
                continue;
            }

            storageStrategy = storageStrategyFactory.getStrategy(logicValue.getField().type());
            storageValue = storageStrategy.toStorageValue(logicValue);
            while (storageValue != null) {
                values.put(AnyStorageValue.ATTRIBUTE_PREFIX + storageValue.storageName(), storageValue.value());
                storageValue = storageValue.next();
            }
        }

        return JacksonDefaultMapper.OBJECT_MAPPER.writeValueAsString(values);
    }

    // 无法实例化的数据将返回null.
    private IEntity buildEntityFromStorageEntity(MasterStorageEntity se) throws SQLException {
        if (se == null) {
            return null;
        }
        Optional<IEntityClass> entityClassOp;
        if (se.getProfile() == null || se.getProfile().isEmpty()) {
            entityClassOp = metaManager.load(se.getSelfEntityClassId());
        } else {
            entityClassOp = metaManager.load(
                se.getSelfEntityClassId(),
                se.getProfile()
            );
        }

        if (!entityClassOp.isPresent()) {
            return null;
        }

        IEntityClass actualEntityClass = entityClassOp.get();

        Entity.Builder entityBuilder = Entity.Builder.anEntity()
            .withId(se.getId())
            .withTime(se.getUpdateTime())
            .withEntityClassRef(EntityClassRefHelper.fullEntityClassRef(actualEntityClass, se.getProfile()))
            .withVersion(se.getVersion())
            .withEntityValue(toEntityValue(se, actualEntityClass))
            .withMajor(se.getOqsMajor());

        return entityBuilder.build();
    }

    // 填充事务信息.
    private void fullTransactionInformation(MasterStorageEntity.Builder entityBuilder, TransactionResource resource) {
        Optional<Transaction> transactionOptional = resource.getTransaction();
        if (transactionOptional.isPresent()) {
            entityBuilder.withTx(transactionOptional.get().id())
                .withCommitid(CommitHelper.getUncommitId());
        } else {
            logger.warn("With no transaction, unable to get the transaction ID.");
            entityBuilder.withTx(0)
                .withCommitid(0);
        }

    }

    // 填充类型信息
    private void fullEntityClassInformation(MasterStorageEntity.Builder storageEntityBuilder, IEntityClass
        entityClass) {
        Collection<IEntityClass> family = entityClass.family();
        long[] tileEntityClassesIds = family.stream().mapToLong(ecs -> ecs.id()).toArray();
        storageEntityBuilder.withEntityClasses(tileEntityClassesIds);
    }

    /**
     * 数据迭代器,用以迭代出某个entity的实例列表.
     */
    private class EntityIterator implements DataIterator<OriginalEntity> {
        private static final int DEFAULT_PAGE_SIZE = 100;

        private final IEntityClass entityClass;
        private long startId;
        private final long startTime;
        private final long endTime;
        private final int pageSize;
        private final List<OriginalEntity> buffer;

        public EntityIterator(IEntityClass entityClass, long startId, long startTime, long endTime) {
            this(entityClass, startId, startTime, endTime, DEFAULT_PAGE_SIZE);
        }

        public EntityIterator(IEntityClass entityClass, long startId, long startTime, long endTime, int pageSize) {
            this.entityClass = entityClass;
            this.startId = startId;
            this.startTime = startTime;
            this.endTime = endTime;
            this.pageSize = pageSize;
            buffer = new ArrayList<>(pageSize);
        }

        @Override
        public boolean hasNext() {
            try {
                if (buffer.isEmpty()) {
                    load();
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }

            return !buffer.isEmpty();
        }

        @Override
        public OriginalEntity next() {
            if (hasNext()) {
                OriginalEntity originalEntity = buffer.remove(0);
                startId = originalEntity.getId();
                return originalEntity;
            } else {
                return null;
            }
        }

        private void load() throws SQLException {
            transactionExecutor.execute((tx, resource, hint) -> {
                Collection<MasterStorageEntity> storageEntities =
                    BatchQueryExecutor
                        .build(tableName, resource, queryTimeout, entityClass, startTime, endTime, pageSize)
                        .execute(startId);

                Collection<OriginalEntity> originalEntities = new ArrayList<>();
                for (MasterStorageEntity entity : storageEntities) {
                    try {
                        IEntityClass realEntityClass = entityClass;

                        //  这里获取一次带有JOJO的真实entityClass
                        if (null != entity.getAttribute() && !entity.getProfile()
                            .equals(OqsProfile.UN_DEFINE_PROFILE)) {
                            Optional<IEntityClass> entityClassOp =
                                metaManager.load(entityClass.id(), entity.getProfile());
                            if (!entityClassOp.isPresent()) {
                                throw new SQLException(
                                    String.format("entityClass could not be null in meta.[%d]", entityClass.id()));
                            }
                            realEntityClass = entityClassOp.get();
                        }

                        OriginalEntity originalEntity = OriginalEntity.Builder.anOriginalEntity()
                            .withEntityClass(realEntityClass)
                            .withId(entity.getId())
                            .withCreateTime(entity.getCreateTime())
                            .withUpdateTime(entity.getUpdateTime())
                            .withOp(OperationType.UPDATE.getValue())
                            .withTx(entity.getTx())
                            .withDeleted(entity.isDeleted())
                            .withCommitid(entity.getCommitid())
                            .withVersion(entity.getVersion())
                            .withOqsMajor(entity.getOqsMajor())
                            .withAttributes(attributesToList(entity.getAttribute()))
                            .build();

                        originalEntities.add(originalEntity);
                    } catch (JsonProcessingException e) {
                        throw new SQLException(
                            String.format("to originalEntity failed. message : [%s]", e.getMessage()));
                    }
                }

                buffer.addAll(originalEntities);

                return null;
            });
        }

        @Override
        public long size() {
            try {
                return (int) transactionExecutor.execute((tx, resource, hint) -> {
                    return BatchQueryCountExecutor
                        .build(tableName, resource, queryTimeout, entityClass, startTime, endTime)
                        .execute(0L);
                });
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        @Override
        public boolean provideSize() {
            return true;
        }
    }

    // 临时解析结果.
    static class EntityValuePack {
        private final IEntityField logicField;
        private final StorageValue storageValue;
        private final StorageStrategy strategy;

        public EntityValuePack(IEntityField logicField, StorageValue storageValue, StorageStrategy strategy) {
            this.logicField = logicField;
            this.storageValue = storageValue;
            this.strategy = strategy;
        }
    }

    // 删除重复项
    private static long[] removeDuplicate(long[] ids) {
        if (ids.length == 0 || ids.length == 1) {
            return ids;
        }

        long[] newIds = Arrays.copyOf(ids, ids.length);
        Arrays.sort(newIds);

        int fast = 1;
        int slow = 1;
        int len = newIds.length;
        while (fast < len) {
            if (newIds[fast] != newIds[fast - 1]) {
                newIds[slow++] = newIds[fast];
            }
            ++fast;
        }

        return Arrays.copyOf(newIds, slow);
    }
}
