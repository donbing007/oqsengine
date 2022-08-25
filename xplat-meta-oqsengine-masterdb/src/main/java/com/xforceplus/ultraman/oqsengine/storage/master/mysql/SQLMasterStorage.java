package com.xforceplus.ultraman.oqsengine.storage.master.mysql;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.common.map.MapUtils;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.common.profile.OqsProfile;
import com.xforceplus.ultraman.oqsengine.common.serializable.utils.JacksonDefaultMapper;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.define.OperationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.ReservedFieldNameWord;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.dynamic.DynamicBuildExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.dynamic.DynamicDeleteExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.dynamic.DynamicExistExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.dynamic.DynamicMultipleQueryExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.dynamic.DynamicQueryByConditionsExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.dynamic.DynamicQueryExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.dynamic.DynamicUpdateExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.original.OriginalBuildExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.original.OriginalDeleteExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.original.OriginalUpdateExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.pojo.BaseMasterStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.pojo.JsonAttributeMasterStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.pojo.MapAttributeMasterStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.strategy.conditions.SQLJsonConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.master.utils.EntityClassRefHelper;
import com.xforceplus.ultraman.oqsengine.storage.master.utils.EntityUpdateTimeRangeIterator;
import com.xforceplus.ultraman.oqsengine.storage.pojo.EntityPackage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OqsEngineEntity;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
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

    private static final JsonAttributeMasterStorageEntity EMPTY_JSON_STORAGE_ENTITY;
    private static final MapAttributeMasterStorageEntity EMPTY_MAP_STORAGE_ENTITY;

    static {
        EMPTY_JSON_STORAGE_ENTITY = new JsonAttributeMasterStorageEntity();
        EMPTY_JSON_STORAGE_ENTITY.setId(-1);

        EMPTY_MAP_STORAGE_ENTITY = new MapAttributeMasterStorageEntity();
        EMPTY_MAP_STORAGE_ENTITY.setId(-1);
    }

    @Resource(name = "storageJDBCTransactionExecutor")
    private TransactionExecutor transactionExecutor;

    @Resource(name = "masterStorageStrategy")
    private StorageStrategyFactory storageStrategyFactory;

    @Resource(name = "masterConditionsBuilderFactory")
    private SQLJsonConditionsBuilderFactory conditionsBuilderFactory;

    @Resource
    private MetaManager metaManager;

    @Resource(name = "masterDataSource")
    private DataSource masterDataSource;

    private String dynamicTableName;

    private long queryTimeout;

    public void setTimeoutMs(long queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public void setDynamicTableName(String dynamicTableName) {
        this.dynamicTableName = dynamicTableName;
    }

    @Override
    @PostConstruct
    public void init() {

    }

    @Override
    public DataIterator<OqsEngineEntity> iterator(
        IEntityClass entityClass, long startTime, long endTime, long lastId, boolean useSelfClass)
        throws SQLException {
        return EntityUpdateTimeRangeIterator.Builder.anEntityIterator()
            .withDataSource(this.masterDataSource)
            .withMetaManager(this.metaManager)
            .withEntityClass(entityClass)
            .withStartTime(startTime)
            .withEndTime(endTime)
            .witherTableName(dynamicTableName)
            .build();
    }

    @Override
    public DataIterator<OqsEngineEntity> iterator(
        IEntityClass entityClass, long startTime, long endTime, long lastId, int size, boolean useSelfClass)
        throws SQLException {
        return EntityUpdateTimeRangeIterator.Builder.anEntityIterator()
            .withDataSource(this.masterDataSource)
            .withMetaManager(this.metaManager)
            .withEntityClass(entityClass)
            .withStartTime(startTime)
            .withEndTime(endTime)
            .witherTableName(dynamicTableName)
            .witherBuffSize(size)
            .build();
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        percentiles = {0.5, 0.9, 0.99},
        extraTags = {"initiator", "master", "action", "condition"}
    )
    @Override
    public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, SelectConfig config)
        throws SQLException {

        SelectConfig useConfig = optimizeToOrder(config);

        return (Collection<EntityRef>) transactionExecutor.execute((tx, resource) -> {
            return DynamicQueryByConditionsExecutor.build(
                dynamicTableName,
                resource,
                entityClass,
                useConfig,
                queryTimeout,
                conditionsBuilderFactory,
                storageStrategyFactory).execute(conditions);
        });
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        percentiles = {0.5, 0.9, 0.99},
        extraTags = {"initiator", "master", "action", "exist"})
    @Override
    public int exist(long id) throws SQLException {
        return (int) transactionExecutor.execute(((tx, resource) ->
            DynamicExistExecutor.build(dynamicTableName, resource, queryTimeout).execute(id)));
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        percentiles = {0.5, 0.9, 0.99},
        extraTags = {"initiator", "master", "action", "one"})
    @Override
    public Optional<IEntity> selectOne(long id) throws SQLException {
        return (Optional<IEntity>) transactionExecutor.execute((tx, resource) -> {
            Optional<JsonAttributeMasterStorageEntity> masterStorageEntityOptional =
                DynamicQueryExecutor.buildHaveDetail(dynamicTableName, resource, queryTimeout).execute(id);
            if (masterStorageEntityOptional.isPresent()) {
                IEntity entity = buildEntityFromJsonStorageEntity(masterStorageEntityOptional.get());
                entity.neat();
                return Optional.ofNullable(entity);
            } else {
                return Optional.empty();
            }
        });
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        percentiles = {0.5, 0.9, 0.99},
        extraTags = {"initiator", "master", "action", "one"})
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
        percentiles = {0.5, 0.9, 0.99},
        extraTags = {"initiator", "master", "action", "multiple"}
    )
    @Override
    public Collection<IEntity> selectMultiple(long[] ids) throws SQLException {
        // 排重.
        long[] useIds = removeDuplicate(ids);

        if (useIds.length == 0) {
            return Collections.emptyList();
        }

        Collection<JsonAttributeMasterStorageEntity> masterStorageEntities =
            (Collection<JsonAttributeMasterStorageEntity>) transactionExecutor.execute(
                (tx, resource) -> {
                    return DynamicMultipleQueryExecutor.build(dynamicTableName, resource, queryTimeout).execute(useIds);
                }
            );

        List<IEntity> entities = new ArrayList<>(masterStorageEntities.size());
        IEntity entity;
        for (JsonAttributeMasterStorageEntity masterStorageEntity : masterStorageEntities) {
            entity = buildEntityFromJsonStorageEntity(masterStorageEntity);
            entity.neat();
            if (entity != null) {
                entities.add(entity);
            }
        }

        return entities;
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        percentiles = {0.5, 0.9, 0.99},
        extraTags = {"initiator", "master", "action", "multiple"}
    )
    @Override
    public Collection<IEntity> selectMultiple(long[] ids, IEntityClass entityClass) throws SQLException {
        Collection<IEntity> entities = selectMultiple(ids);
        if (entities.isEmpty()) {
            return entities;
        }

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

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        percentiles = {0.5, 0.9, 0.99},
        extraTags = {"initiator", "master", "action", "build"})
    @Override
    public boolean build(IEntity entity, IEntityClass entityClass) throws SQLException {
        return doBuildOrReplace(entity, entityClass, true);
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        percentiles = {0.5, 0.9, 0.99},
        extraTags = {"initiator", "master", "action", "builds"})
    @Override
    public void build(EntityPackage entityPackage) throws SQLException {
        doBatchBuildOrReplace(entityPackage, true);
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        percentiles = {0.5, 0.9, 0.99},
        extraTags = {"initiator", "master", "action", "replace"}
    )
    @Override
    public boolean replace(IEntity entity, IEntityClass entityClass) throws SQLException {
        return doBuildOrReplace(entity, entityClass, false);
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        percentiles = {0.5, 0.9, 0.99},
        extraTags = {"initiator", "master", "action", "replaces"}
    )
    @Override
    public void replace(EntityPackage entityPackage) throws SQLException {
        doBatchBuildOrReplace(entityPackage, false);
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        percentiles = {0.5, 0.9, 0.99},
        extraTags = {"initiator", "master", "action", "delete"})
    @Override
    public boolean delete(IEntity entity, IEntityClass entityClass) throws SQLException {
        checkId(entity);

        if (entity.isDeleted()) {
            return true;
        }

        boolean result = (boolean) transactionExecutor.execute(
            (tx, resource) -> {

                BaseMasterStorageEntity storageEntity = buildDeleteMasterStorageEntity(entity, entityClass, resource);
                BaseMasterStorageEntity[] storageEntities = new BaseMasterStorageEntity[] {
                    storageEntity
                };

                DynamicDeleteExecutor.build(dynamicTableName, resource, queryTimeout).execute(storageEntities);

                if (storageEntity.isDynamicSuccess()) {
                    if (storageEntity.isOriginal()) {
                        new OriginalDeleteExecutor(dynamicTableName, resource, queryTimeout).execute(storageEntities);
                    }
                }

                if (storageEntity.isOriginal()) {
                    return storageEntity.isDynamicSuccess() && storageEntity.isOriginalSucess();
                } else {
                    return storageEntity.isDynamicSuccess();
                }
            });

        if (result) {
            entity.delete();
        }

        return result;
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        percentiles = {0.5, 0.9, 0.99},
        extraTags = {"initiator", "master", "action", "deletes"}
    )
    @Override
    public void delete(EntityPackage entityPackage) throws SQLException {
        checkId(entityPackage);

        int originalCount =
            (int) entityPackage.stream().filter(e -> e.getValue().type() == EntityClassType.ORIGINAL).count();
        List<BaseMasterStorageEntity> originalEntities = new ArrayList<>(originalCount);

        transactionExecutor.execute((tx, resource) -> {

            BaseMasterStorageEntity[] storageEntities = entityPackage.stream()
                .filter(er -> !er.getKey().isDeleted())
                .map(e -> buildDeleteMasterStorageEntity(e.getKey(), e.getValue(), resource))
                .toArray(BaseMasterStorageEntity[]::new);

            DynamicDeleteExecutor.build(dynamicTableName, resource, queryTimeout).execute(storageEntities);

            for (BaseMasterStorageEntity storageEntity : storageEntities) {
                if (storageEntity.isDynamicSuccess() && storageEntity.isOriginal()) {
                    originalEntities.add(storageEntity);
                }
            }

            new OriginalDeleteExecutor(dynamicTableName, resource, queryTimeout).execute(
                originalEntities.stream().toArray(BaseMasterStorageEntity[]::new)
            );

            for (BaseMasterStorageEntity storageEntity : storageEntities) {
                if (storageEntity.isOriginal()) {
                    if (storageEntity.isDynamicSuccess() && storageEntity.isOriginalSucess()) {
                        storageEntity.getSourceEntity().delete();
                    }
                } else {
                    if (storageEntity.isDynamicSuccess()) {
                        storageEntity.getSourceEntity().delete();
                    }
                }
            }

            return null;
        });
    }

    /**
     * 批量更新或者删除.
     *
     * @param entity      目标对象.
     * @param entityClass 目标元信息.
     * @param build       true 创建, false 更新.
     * @return true 成功, false 失败.
     */
    private boolean doBuildOrReplace(IEntity entity, IEntityClass entityClass, boolean build) throws SQLException {
        checkId(entity);

        if (!entity.isDirty()) {
            return true;
        }

        boolean result = (boolean) transactionExecutor.execute(
            (tx, resource) -> {

                MapAttributeMasterStorageEntity<IEntityField, StorageValue> storageEntity =
                    build
                        ? buildNewMasterStorageEntity(entity, entityClass, resource)
                        : buildReplaceMasterStorageEntity(entity, entityClass, resource);

                MapAttributeMasterStorageEntity<IEntityField, StorageValue>[] storageEntities =
                    new MapAttributeMasterStorageEntity[] {storageEntity};

                if (build) {
                    DynamicBuildExecutor.build(dynamicTableName, resource, queryTimeout).execute(storageEntities);
                } else {
                    DynamicUpdateExecutor.build(dynamicTableName, resource, queryTimeout).execute(storageEntities);
                }

                if (storageEntity.isDynamicSuccess()) {
                    if (storageEntity.isOriginal()) {
                        if (build) {
                            new OriginalBuildExecutor(dynamicTableName, resource, queryTimeout)
                                .execute(storageEntities);
                        } else {
                            new OriginalUpdateExecutor(dynamicTableName, resource, queryTimeout)
                                .execute(storageEntities);
                        }
                    }
                }

                if (storageEntity.isOriginal()) {
                    return storageEntity.isOriginalSucess() && storageEntity.isDynamicSuccess();
                } else {
                    return storageEntity.isDynamicSuccess();
                }
            });

        if (result) {
            entity.neat();
        }

        return result;
    }

    /**
     * 批量创建或者更新.
     *
     * @param entityPackage 目标实例包.
     * @param build         ture 创建, false 更新.
     */
    private void doBatchBuildOrReplace(EntityPackage entityPackage, boolean build) throws SQLException {
        checkId(entityPackage);

        // 这个计算只是防止ArrayList进行扩张.
        int originalCount =
            (int) entityPackage.stream().filter(e -> e.getValue().type() == EntityClassType.ORIGINAL).count();
        List<MapAttributeMasterStorageEntity<IEntityField, StorageValue>> originalEntities
            = new ArrayList<>(originalCount);

        transactionExecutor.execute(
            (tx, resource) -> {

                MapAttributeMasterStorageEntity<IEntityField, StorageValue>[] storageEntities = entityPackage.stream()
                    .filter(er -> er.getKey().isDirty())
                    .map(er -> {
                        if (build) {
                            return buildNewMasterStorageEntity(er.getKey(), er.getValue(), resource);
                        } else {
                            return buildReplaceMasterStorageEntity(er.getKey(), er.getValue(), resource);
                        }
                    })
                    .toArray(MapAttributeMasterStorageEntity[]::new);

                if (build) {
                    DynamicBuildExecutor.build(dynamicTableName, resource, queryTimeout)
                        .execute(storageEntities);
                } else {
                    DynamicUpdateExecutor.build(dynamicTableName, resource, queryTimeout)
                        .execute(storageEntities);
                }

                // 将静态实体动态部份完成后移入静态列表.
                for (int i = 0; i < storageEntities.length; i++) {
                    if (storageEntities[i].isOriginal() && storageEntities[i].isDynamicSuccess()) {
                        originalEntities.add(storageEntities[i]);
                    }
                }

                // 如果含有静态对象.
                if (!originalEntities.isEmpty()) {
                    if (build) {
                        new OriginalBuildExecutor(dynamicTableName, resource, queryTimeout).execute(
                            originalEntities.stream().toArray(MapAttributeMasterStorageEntity[]::new)
                        );
                    } else {
                        new OriginalUpdateExecutor(dynamicTableName, resource, queryTimeout).execute(
                            originalEntities.stream().toArray(MapAttributeMasterStorageEntity[]::new)
                        );
                    }
                }

                // 设置对象状态
                for (MapAttributeMasterStorageEntity storageEntity : storageEntities) {
                    if (storageEntity.isOriginal()) {
                        if (storageEntity.isDynamicSuccess() && storageEntity.isOriginalSucess()) {
                            storageEntity.getSourceEntity().neat();
                        }
                    } else {
                        if (storageEntity.isDynamicSuccess()) {
                            storageEntity.getSourceEntity().neat();
                        }
                    }
                }

                return null;
            }
        );
    }

    private void checkId(EntityPackage entityPackage) throws SQLException {
        Iterator<Map.Entry<IEntity, IEntityClass>> iter = entityPackage.iterator();
        while (iter.hasNext()) {
            checkId(iter.next().getKey());
        }
    }

    private void checkId(IEntity entity) throws SQLException {
        if (entity.id() == 0) {
            throw new SQLException("Invalid entity`s id.");
        }
    }

    // 储存字段转换为逻辑字段.
    private void toEntityValue(JsonAttributeMasterStorageEntity masterStorageEntity,
                               IEntityClass entityClass,
                               IEntityValue entityValue)
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
        Map<String, String> valueAttachmentCache = new HashMap<>(object.size());

        for (String storageName : object.keySet()) {
            try {

                if (AnyStorageValue.isAttachemntStorageName(storageName)) {

                    valueAttachmentCache.put(
                        AnyStorageValue.compatibleStorageName(storageName),
                        (String) object.get(storageName));

                } else {

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
                }

            } catch (Exception ex) {

                throw new SQLException(
                    String.format(
                        "Unexpected error occurred in field resolution with field name %s and error message %s.[%s]",
                        storageName, ex.getMessage(), masterStorageEntity.getId()), ex);
            }
        }

        StorageStrategy attachmentStorageStrategy = this.storageStrategyFactory.getStrategy(FieldType.STRING);
        storageValueCache.values().stream().forEach(e -> {

            String attachment = valueAttachmentCache.get(attachmentStorageStrategy.toFirstStorageName(e.logicField));
            entityValue.addValue(
                e.strategy.toLogicValue(e.logicField, e.storageValue, attachment));

        });

    }

    // 构造字段的物理储存表示.
    private Map<IEntityField, StorageValue> toStorageValues(IEntityValue value) {

        // 值为可能的多个物理储存值.
        Map<IEntityField, StorageValue> values = new HashMap<>(MapUtils.calculateInitSize(value.size()));

        StorageStrategy storageStrategy = null;
        StorageValue storageValue = null;

        for (IValue logicValue : value.values()) {

            storageValue = null;

            if (!logicValue.isDirty()) {
                continue;
            }

            // 保留字的属性将被过滤.
            if (ReservedFieldNameWord.isReservedWorkd(logicValue.getField().name())) {
                continue;
            }

            storageStrategy = storageStrategyFactory.getStrategy(logicValue.getField().type());

            if (logicValue != null && logicValue instanceof EmptyTypedValue) {

                storageValue = storageStrategy.toEmptyStorageValue(logicValue.getField());

            } else {

                storageValue = storageStrategy.toStorageValue(logicValue);

            }

            values.put(logicValue.getField(), storageValue);

        }

        return values;
    }

    // 无法实例化的数据将返回null.
    private IEntity buildEntityFromJsonStorageEntity(JsonAttributeMasterStorageEntity se) throws SQLException {
        if (se == null) {
            return null;
        }
        Optional<IEntityClass> entityClassOp;
        if (se.getProfile() == null || se.getProfile().isEmpty()) {
            entityClassOp = metaManager.load(se.getSelfEntityClassId(), "");
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
            .withMajor(se.getOqsMajor());

        IEntity entity = entityBuilder.build();
        toEntityValue(se, actualEntityClass, entity.entityValue());
        entity.neat();

        return entity;
    }

    // 填充事务信息.
    private void fullTransactionInformation(BaseMasterStorageEntity storageEntity, TransactionResource resource) {
        Optional<Transaction> transactionOptional = resource.getTransaction();
        if (transactionOptional.isPresent()) {
            storageEntity.setTx(transactionOptional.get().id());
            storageEntity.setCommitid(CommitHelper.getUncommitId());
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("With no transaction, unable to get the transaction ID.");
            }
            storageEntity.setTx(0);
            storageEntity.setCommitid(0);
        }

    }

    // 填充类型信息
    private void fullEntityClassInformation(BaseMasterStorageEntity storageEntity, IEntityClass entityClass) {
        Collection<IEntityClass> family = entityClass.family();
        long[] tileEntityClassesIds = family.stream().mapToLong(ecs -> ecs.id()).toArray();
        storageEntity.setEntityClasses(tileEntityClassesIds);
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

    /**
     * 一个查询排序优化. 1. 如果只按ID排序,且只有ID排序那么整个排序将被去掉.
     */
    private SelectConfig optimizeToOrder(SelectConfig config) {
        if (config.getSecondarySort().isOutOfOrder() && config.getThirdSort().isOutOfOrder()) {
            if (!config.getSort().isOutOfOrder()) {
                if (config.getSort().getField() == EntityField.ID_ENTITY_FIELD) {
                    return SelectConfig.Builder.anSelectConfig()
                        .withCommitId(config.getCommitId())
                        .withPage(config.getPage())
                        .withDataAccessFitlerCondtitons(config.getDataAccessFilterCondtitions())
                        .withExcludedIds(config.getExcludedIds())
                        .withFacet(config.getFacet())
                        .withSort(Sort.buildOutOfSort())
                        .withSecondarySort(config.getSecondarySort())
                        .withThirdSort(config.getThirdSort()).build();
                }
            }
        }
        return config;
    }

    // 更新时构造.
    private MapAttributeMasterStorageEntity<IEntityField, StorageValue> buildReplaceMasterStorageEntity(
        IEntity entity, IEntityClass entityClass,
        TransactionResource resource) {
        long updateTime = findTime(entity, FieldConfig.FieldSense.UPDATE_TIME);
        /*
         * 如果从新结果集中查询到更新时间,但是和当前最后更新时间相等那么使用系统时间.
         */
        if (updateTime == entity.time()) {
            updateTime = 0;
        }

        MapAttributeMasterStorageEntity<IEntityField, StorageValue> storageEntity =
            new MapAttributeMasterStorageEntity();
        storageEntity.setId(entity.id());
        storageEntity.setUpdateTime(updateTime > 0 ? updateTime : entity.time());
        storageEntity.setVersion(entity.version());
        storageEntity.setEntityClassVersion(entityClass.version());
        storageEntity.setOp(OperationType.UPDATE.getValue());
        storageEntity.setAttributes(toStorageValues(entity.entityValue()));
        storageEntity.setOriginal(entityClass.type() == EntityClassType.ORIGINAL);
        fullEntityClassInformation(storageEntity, entityClass);
        fullTransactionInformation(storageEntity, resource);

        if (entityClass.type() == EntityClassType.ORIGINAL) {
            storageEntity.setOriginal(true);
            storageEntity.setOriginalTableName(buildOriginalTableName(entityClass));
        }

        storageEntity.setSourceEntity(entity);

        return storageEntity;
    }

    // 新建时构造.
    private MapAttributeMasterStorageEntity<IEntityField, StorageValue> buildNewMasterStorageEntity(
        IEntity entity, IEntityClass entityClass, TransactionResource resource) {

        long createTime = findTime(entity, FieldConfig.FieldSense.CREATE_TIME);
        if (createTime == 0) {
            createTime = entity.time();
        }

        long updateTime = findTime(entity, FieldConfig.FieldSense.UPDATE_TIME);
        if (updateTime == 0) {
            updateTime = entity.time();
        }

        MapAttributeMasterStorageEntity<IEntityField, StorageValue> storageEntity =
            new MapAttributeMasterStorageEntity();
        storageEntity.setId(entity.id());
        storageEntity.setCreateTime(createTime);
        storageEntity.setUpdateTime(updateTime);
        storageEntity.setOriginal(entityClass.type() == EntityClassType.ORIGINAL);
        storageEntity.setDeleted(false);
        storageEntity.setEntityClassVersion(entityClass.version());
        storageEntity.setVersion(entity.version());
        storageEntity.setAttributes(toStorageValues(entity.entityValue()));
        storageEntity.setOp(OperationType.CREATE.getValue());
        storageEntity.setProfile(entity.entityClassRef().getProfile());

        if (entityClass.type() == EntityClassType.ORIGINAL) {
            storageEntity.setOriginal(true);
            storageEntity.setOriginalTableName(buildOriginalTableName(entityClass));
        }

        storageEntity.setSourceEntity(entity);

        fullEntityClassInformation(storageEntity, entityClass);
        fullTransactionInformation(storageEntity, resource);
        return storageEntity;
    }

    // 删除时构造.
    private BaseMasterStorageEntity buildDeleteMasterStorageEntity(
        IEntity entity, IEntityClass entityClass, TransactionResource resource) {

        BaseMasterStorageEntity storageEntity = new BaseMasterStorageEntity();
        storageEntity.setId(entity.id());
        storageEntity.setOp(OperationType.DELETE.getValue());
        storageEntity.setUpdateTime(entity.time());
        storageEntity.setEntityClassVersion(entityClass.version());
        storageEntity.setDeleted(true);
        storageEntity.setVersion(entity.version());
        storageEntity.setOriginal(entityClass.type() == EntityClassType.ORIGINAL);

        if (entityClass.type() == EntityClassType.ORIGINAL) {
            storageEntity.setOriginal(true);
            storageEntity.setOriginalTableName(buildOriginalTableName(entityClass));
        }

        storageEntity.setSourceEntity(entity);

        fullEntityClassInformation(storageEntity, entityClass);
        fullTransactionInformation(storageEntity, resource);
        return storageEntity;
    }

    /**
     * 查找系统时间.
     *
     * @param entity 目标对象.
     * @param sense  字段风格.
     * @return 时间戳, 如果没有找到返回0.
     */
    private long findTime(IEntity entity, FieldConfig.FieldSense sense) {
        OptionalLong op = entity.entityValue().values().stream()
            .filter(v -> sense == v.getField().config().getFieldSense())
            .mapToLong(v -> v.valueToLong()).findFirst();

        return op.orElse(0);
    }

    /**
     * 构造静态表名, oqs_{应用code}_{对象code}_{定制}.
     */
    private static String buildOriginalTableName(IEntityClass entityClass) {

        StringBuilder buff = new StringBuilder();
        buff.append("oqs_");
        buff.append(entityClass.appCode())
            .append('_')
            .append(entityClass.code());
        if (!entityClass.profile().equals(OqsProfile.UN_DEFINE_PROFILE)) {
            buff.append('_')
                .append(entityClass.profile());
        }

        return buff.toString();
    }
}
