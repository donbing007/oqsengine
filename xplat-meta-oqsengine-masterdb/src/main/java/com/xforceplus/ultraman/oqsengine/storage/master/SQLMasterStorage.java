package com.xforceplus.ultraman.oqsengine.storage.master;


import static com.xforceplus.ultraman.oqsengine.storage.master.utils.OriginalEntityUtils.attributesToMap;

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
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.ValueWithEmpty;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.BatchQueryCountExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.BatchQueryExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.BuildExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.DeleteExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.ExistExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.MultipleQueryExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.QueryExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.QueryLimitCommitidByConditionsExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.UpdateExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.rebuild.DevOpsRebuildExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.BaseMasterStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.JsonAttributeMasterStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.MapAttributeMasterStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.MasterStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.conditions.SQLJsonConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.master.unique.UniqueKeyGenerator;
import com.xforceplus.ultraman.oqsengine.storage.master.utils.EntityClassRefHelper;
import com.xforceplus.ultraman.oqsengine.storage.pojo.EntityPackage;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.ExecutorService;
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

    @Resource
    private UniqueKeyGenerator keyGenerator;

    @Resource(name = "taskThreadPool")
    private ExecutorService asyncErrorExecutor;

    @Resource
    private KeyValueStorage kv;

    @Resource(name = "masterDataSource")
    private DataSource masterDataSource;

    private String tableName;

    private long queryTimeout;

    public void setTimeoutMs(long queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    @PostConstruct
    public void init() {

    }

    @Override
    public DataIterator<OriginalEntity> iterator(IEntityClass entityClass, long startTime, long endTime, long lastStart)
        throws SQLException {
        return new EntityIterator(entityClass, lastStart, startTime, endTime);
    }

    @Override
    public DataIterator<OriginalEntity> iterator(IEntityClass entityClass, long startTime, long endTime, long lastId,
                                                 int size) throws SQLException {
        return new EntityIterator(entityClass, lastId, startTime, endTime, size);
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        extraTags = {"initiator", "master", "action", "condition"}
    )
    @Override
    public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, SelectConfig config)
        throws SQLException {

        SelectConfig useConfig = optimizeToOrder(config);

        return (Collection<EntityRef>) transactionExecutor.execute((tx, resource, hint) -> {
            return QueryLimitCommitidByConditionsExecutor.build(
                tableName,
                resource,
                entityClass,
                useConfig,
                queryTimeout,
                conditionsBuilderFactory,
                storageStrategyFactory).execute(conditions);
        });
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "master", "action", "exist"})
    @Override
    public int exist(long id) throws SQLException {
        return (int) transactionExecutor.execute(((tx, resource, hint) ->
            ExistExecutor.build(tableName, resource, queryTimeout).execute(id)));
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "master", "action", "one"})
    @Override
    public Optional<IEntity> selectOne(long id) throws SQLException {
        return (Optional<IEntity>) transactionExecutor.execute((tx, resource, hint) -> {
            Optional<JsonAttributeMasterStorageEntity> masterStorageEntityOptional =
                QueryExecutor.buildHaveDetail(tableName, resource, queryTimeout).execute(id);
            if (masterStorageEntityOptional.isPresent()) {
                IEntity entity = buildEntityFromJsonStorageEntity(masterStorageEntityOptional.get());
                entity.neat();
                return Optional.ofNullable(entity);
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

        if (useIds.length == 0) {
            return Collections.emptyList();
        }

        Collection<JsonAttributeMasterStorageEntity> masterStorageEntities =
            (Collection<JsonAttributeMasterStorageEntity>) transactionExecutor.execute(
                (tx, resource, hint) -> {

                    return MultipleQueryExecutor.build(tableName, resource, queryTimeout).execute(useIds);
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

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "master", "action", "build"})
    @Override
    public boolean build(IEntity entity, IEntityClass entityClass) throws SQLException {
        checkId(entity);

        if (!entity.isDirty()) {
            return true;
        }

        boolean result = (boolean) transactionExecutor.execute(
            (tx, resource, hint) -> {

                JsonAttributeMasterStorageEntity
                    entityForBuild = buildNewMasterStorageEntity(entity, entityClass, resource);

                boolean[] results = BuildExecutor.build(tableName, resource, queryTimeout)
                    .execute(new JsonAttributeMasterStorageEntity[] {entityForBuild});

                final int first = 0;
                return results[first];
            });

        if (result) {
            entity.neat();
        }

        return result;
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "master", "action", "builds"})
    @Override
    public void build(EntityPackage entityPackage) throws SQLException {
        checkId(entityPackage);

        JsonAttributeMasterStorageEntity[] masterStorageEntities =
            new JsonAttributeMasterStorageEntity[entityPackage.size()];

        boolean[] results = (boolean[]) transactionExecutor.execute(
            (tx, resource, hint) -> {

                Map.Entry<com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity, IEntityClass> entry;
                IEntity entity;
                IEntityClass entityClass;
                int size = entityPackage.size();
                for (int i = 0; i < size; i++) {
                    entry = entityPackage.getNotSafe(i);
                    entity = entry.getKey();

                    if (entity.isDirty()) {

                        entityClass = entry.getValue();

                        masterStorageEntities[i] = buildNewMasterStorageEntity(entity, entityClass, resource);

                    } else {

                        masterStorageEntities[i] = EMPTY_JSON_STORAGE_ENTITY;

                    }
                }

                return BuildExecutor.build(tableName, resource, queryTimeout).execute(
                    Arrays.stream(masterStorageEntities)
                        .filter(se -> EMPTY_JSON_STORAGE_ENTITY != se).toArray(JsonAttributeMasterStorageEntity[]::new)
                );
            }
        );

        int resultsCursor = 0;
        for (int i = 0; i < masterStorageEntities.length; i++) {
            if (EMPTY_JSON_STORAGE_ENTITY != masterStorageEntities[i]) {
                if (results[resultsCursor++]) {
                    entityPackage.getNotSafe(i).getKey().neat();
                }
            }
        }
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        extraTags = {"initiator", "master", "action", "replace"}
    )
    @Override
    public boolean replace(IEntity entity, IEntityClass entityClass) throws SQLException {
        checkId(entity);

        if (!entity.isDirty()) {
            return true;
        }

        boolean result = (boolean) transactionExecutor.execute(
            (tx, resource, hint) -> {

                MapAttributeMasterStorageEntity masterStorageEntity =
                    buildReplaceMasterStorageEntity(entity, entityClass, resource);

                boolean[] results = UpdateExecutor.build(tableName, resource, queryTimeout)
                    .execute(new MapAttributeMasterStorageEntity[] {masterStorageEntity});

                final int first = 0;
                return results[first];
            });

        if (result) {
            entity.neat();
        }

        return result;
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        extraTags = {"initiator", "master", "action", "replaces"}
    )
    @Override
    public void replace(EntityPackage entityPackage) throws SQLException {
        checkId(entityPackage);

        MapAttributeMasterStorageEntity[] masterStorageEntities =
            new MapAttributeMasterStorageEntity[entityPackage.size()];

        boolean[] results = (boolean[]) transactionExecutor.execute(
            (tx, resource, hint) -> {

                Map.Entry<com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity, IEntityClass> entry;
                IEntity entity;
                IEntityClass entityClass;
                int size = entityPackage.size();
                for (int i = 0; i < size; i++) {
                    entry = entityPackage.getNotSafe(i);
                    entity = entry.getKey();

                    if (entity.isDirty()) {

                        entityClass = entry.getValue();

                        masterStorageEntities[i] = buildReplaceMasterStorageEntity(entity, entityClass, resource);

                    } else {

                        masterStorageEntities[i] = EMPTY_MAP_STORAGE_ENTITY;

                    }
                }

                return UpdateExecutor.build(tableName, resource, queryTimeout).execute(
                    Arrays.stream(masterStorageEntities)
                        .filter(se -> EMPTY_MAP_STORAGE_ENTITY != se).toArray(MapAttributeMasterStorageEntity[]::new)
                );
            });

        int resultsCursor = 0;
        for (int i = 0; i < masterStorageEntities.length; i++) {
            if (EMPTY_MAP_STORAGE_ENTITY != masterStorageEntities[i]) {
                if (results[resultsCursor++]) {
                    entityPackage.getNotSafe(i).getKey().neat();
                }
            }
        }
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "master", "action", "delete"})
    @Override
    public boolean delete(IEntity entity, IEntityClass entityClass) throws SQLException {
        checkId(entity);

        if (entity.isDeleted()) {
            return true;
        }

        boolean result = (boolean) transactionExecutor.execute(
            (tx, resource, hint) -> {

                BaseMasterStorageEntity
                    masterStorageEntity = buildDeleteMasterStorageEntity(entity, entityClass, resource);
                boolean[] results = DeleteExecutor.build(tableName, resource, queryTimeout)
                    .execute(new BaseMasterStorageEntity[] {masterStorageEntity});

                final int first = 0;
                return results[first];
            });

        if (result) {
            entity.delete();
        }

        return result;
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        extraTags = {"initiator", "master", "action", "deletes"}
    )
    @Override
    public void delete(EntityPackage entityPackage) throws SQLException {
        checkId(entityPackage);

        boolean[] results = (boolean[]) transactionExecutor.execute(
            (tx, resource, hint) -> {

                BaseMasterStorageEntity[] masterStorageEntities = entityPackage.stream()
                    .map(e -> buildDeleteMasterStorageEntity(e.getKey(), e.getValue(), resource))
                    .toArray(BaseMasterStorageEntity[]::new);

                return DeleteExecutor.build(tableName, resource, queryTimeout).execute(masterStorageEntities);
            });

        IEntity entity;
        for (int i = 0; i < results.length; i++) {
            if (results[i]) {
                entity = entityPackage.get(i).get().getKey();
                entity.delete();
            }
        }
    }

    @Override
    public int rebuild(long entityClassId, long maintainId, long startTime, long endTime) throws Exception {

        Optional<IEntityClass> actualEntityClassOp = metaManager.load(entityClassId, "");

        if (actualEntityClassOp.isPresent()) {
            return DevOpsRebuildExecutor
                .build(tableName, masterDataSource, maintainId, startTime, endTime)
                .execute(actualEntityClassOp.get());
        }

        return 0;
    }

    private void checkId(EntityPackage entityPackage) throws SQLException {
        Iterator<Map.Entry<com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity, IEntityClass>> iter =
            entityPackage.iterator();
        while (iter.hasNext()) {
            checkId(iter.next().getKey());
        }
    }

    private void checkId(IEntity entity) throws SQLException {
        if (entity.id() == 0) {
            throw new SQLException("Invalid entity`s id.");
        }
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
            } catch (Exception ex) {
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

        private void load() throws Exception {
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
                            .withAttributes(attributesToMap(entity.getAttribute()))
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
                throw new SQLException(ex.getMessage(), ex);
            }
        }

        StorageStrategy attachmentStorageStrategy = this.storageStrategyFactory.getStrategy(FieldType.STRING);
        storageValueCache.values().stream().forEach(e -> {

            String attachment = valueAttachmentCache.get(attachmentStorageStrategy.toFirstStorageName(e.logicField));
            entityValue.addValue(
                e.strategy.toLogicValue(e.logicField, e.storageValue, attachment));

        });

    }

    // 构造储存字段哈希.
    private Map<String, Object> toStorageValues(IEntityValue value) {

        Map<String, Object> values = new HashMap<>(MapUtils.calculateInitSize(value.size()));

        StorageStrategy storageStrategy = null;
        StorageValue storageValue = null;
        StorageValue attachmentSv = null;
        for (IValue logicValue : value.values()) {

            /*
            只有状态为"脏"的字段才会需要处理.
             */
            if (!logicValue.isDirty()) {
                continue;
            }

            storageStrategy = storageStrategyFactory.getStrategy(logicValue.getField().type());

            if (logicValue != null && logicValue instanceof EmptyTypedValue) {

                storageValue = storageStrategy.toEmptyStorageValue(logicValue.getField());

                values.put(String.format("%s%s", AnyStorageValue.ATTRIBUTE_PREFIX, storageValue.storageName()),
                    ValueWithEmpty.EMPTY_VALUE);
                values.put(String.format("%s%s", AnyStorageValue.ATTACHMENT_PREFIX, storageValue.storageName()),
                    ValueWithEmpty.EMPTY_VALUE);

            } else {

                storageValue = storageStrategy.toStorageValue(logicValue);

                while (true) {
                    values.put(
                        String.format("%s%s", AnyStorageValue.ATTRIBUTE_PREFIX, storageValue.storageName()),
                        storageValue.value());

                    if (storageValue.next() != null) {
                        storageValue = storageValue.next();
                    } else {
                        break;
                    }
                }

                // 处理附件.
                Optional<StorageValue> attachmentSvOp = storageStrategy.toAttachmentStorageValue(logicValue);
                if (attachmentSvOp.isPresent()) {
                    attachmentSv = attachmentSvOp.get();
                    values.put(String.format("%s%s", AnyStorageValue.ATTACHMENT_PREFIX, attachmentSv.storageName()),
                        logicValue.getAttachment().get());
                }
            }
        }

        return values;
    }

    // 构造创建实例属性JSON字符串,名称使用的是属性 F + {id}.
    private String toBuildJson(IEntityValue value) {

        Map<String, Object> values = toStorageValues(value);

        try {
            return JacksonDefaultMapper.OBJECT_MAPPER.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
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
    private MapAttributeMasterStorageEntity buildReplaceMasterStorageEntity(
        IEntity entity, IEntityClass entityClass,
        TransactionResource resource) {
        long updateTime = findTime(entity, FieldConfig.FieldSense.UPDATE_TIME);
        /*
         * 如果从新结果集中查询到更新时间,但是和当前最后更新时间相等那么使用系统时间.
         */
        if (updateTime == entity.time()) {
            updateTime = 0;
        }

        MapAttributeMasterStorageEntity storageEntity = new MapAttributeMasterStorageEntity();
        storageEntity.setId(entity.id());
        storageEntity.setUpdateTime(updateTime > 0 ? updateTime : entity.time());
        storageEntity.setVersion(entity.version());
        storageEntity.setEntityClassVersion(entityClass.version());
        storageEntity.setOp(OperationType.UPDATE.getValue());
        storageEntity.setAttributes(toStorageValues(entity.entityValue()));
        fullEntityClassInformation(storageEntity, entityClass);
        fullTransactionInformation(storageEntity, resource);

        return storageEntity;
    }

    // 新建时构造.
    private JsonAttributeMasterStorageEntity buildNewMasterStorageEntity(
        IEntity entity, IEntityClass entityClass, TransactionResource resource) {

        long createTime = findTime(entity, FieldConfig.FieldSense.CREATE_TIME);
        if (createTime == 0) {
            createTime = entity.time();
        }

        long updateTime = findTime(entity, FieldConfig.FieldSense.UPDATE_TIME);
        if (updateTime == 0) {
            updateTime = entity.time();
        }

        JsonAttributeMasterStorageEntity storageEntity = new JsonAttributeMasterStorageEntity();
        storageEntity.setId(entity.id());
        storageEntity.setCreateTime(createTime);
        storageEntity.setUpdateTime(updateTime);
        storageEntity.setDeleted(false);
        storageEntity.setEntityClassVersion(entityClass.version());
        storageEntity.setVersion(0);
        try {
            storageEntity.setAttribute(toBuildJson(entity.entityValue()));
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
        storageEntity.setOp(OperationType.CREATE.getValue());
        storageEntity.setProfile(entity.entityClassRef().getProfile());

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
}
