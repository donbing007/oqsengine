package com.xforceplus.ultraman.oqsengine.storage.master;

import static com.xforceplus.ultraman.oqsengine.storage.master.utils.OriginalEntityUtils.attributesToList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.common.map.MapUtils;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
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
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.define.OperationType;
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
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.MasterStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.conditions.SQLJsonConditionsBuilderFactory;
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
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
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

    private final ObjectMapper objectMapper = JsonMapper.builder()
        .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
        .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
        .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
        .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
        .enable(JsonReadFeature.ALLOW_MISSING_VALUES)
        .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
        .enable(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS)
        .enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS)
        .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
        .enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
        .enable(JsonReadFeature.ALLOW_YAML_COMMENTS).build();

    @Resource(name = "storageJDBCTransactionExecutor")
    private TransactionExecutor transactionExecutor;

    @Resource(name = "masterStorageStrategy")
    private StorageStrategyFactory storageStrategyFactory;

    @Resource(name = "masterConditionsBuilderFactory")
    private SQLJsonConditionsBuilderFactory conditionsBuilderFactory;

    @Resource
    private MetaManager metaManager;

    private String tableName;

    private long queryTimeout;

    public void setQueryTimeout(long queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
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

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "master", "action",
        "condition"})
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
    public Optional<IEntity> selectOne(long id, IEntityClass entityClass) throws SQLException {
        return (Optional<IEntity>) transactionExecutor.execute((tx, resource, hint) -> {
            Optional<MasterStorageEntity> masterStorageEntityOptional =
                QueryExecutor.buildHaveDetail(tableName, resource, entityClass, queryTimeout).execute(id);
            if (masterStorageEntityOptional.isPresent()) {
                return buildEntityFromStorageEntity(masterStorageEntityOptional.get(), entityClass);
            } else {
                return Optional.empty();
            }
        });
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        extraTags = {"initiator", "master", "action", "multiple"}
    )
    @Override
    public Collection<IEntity> selectMultiple(long[] ids, IEntityClass entityClass) throws SQLException {

        Collection<MasterStorageEntity> storageEntities = (Collection<MasterStorageEntity>) transactionExecutor.execute(
            (tx, resource, hint) -> {

                return MultipleQueryExecutor.build(tableName, resource, entityClass, queryTimeout).execute(ids);
            }
        );


        Map<Long, IEntity> entityMap = storageEntities.stream().map(se -> {
            Optional<IEntity> op;
            try {
                op = buildEntityFromStorageEntity(se, entityClass);
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }

            return op.get();
        }).collect(Collectors.toMap(e -> e.id(), e -> e, (e0, e1) -> e0));

        return Arrays.stream(ids).mapToObj(id -> entityMap.get(id)).collect(Collectors.toList());

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
                        .withOp(OperationType.CREATE.getValue());
                } catch (Exception ex) {
                    throw new SQLException(ex.getMessage(), ex);
                }
                fullEntityClassInformation(storageEntityBuilder, entityClass);
                fullTransactionInformation(storageEntityBuilder, resource);

                return BuildExecutor.build(tableName, resource, queryTimeout).execute(storageEntityBuilder.build());
            });

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

                return UpdateExecutor.build(tableName, resource, queryTimeout).execute(storageEntityBuilder.build());

            });

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

                return DeleteExecutor.build(tableName, resource, queryTimeout).execute(storageEntityBuilder.build());
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
            object = objectMapper.readValue(masterStorageEntity.getAttribute(), Map.class);
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

            if (logicValue != null && EmptyTypedValue.class.isInstance(logicValue)) {
                continue;
            }

            storageStrategy = storageStrategyFactory.getStrategy(logicValue.getField().type());
            storageValue = storageStrategy.toStorageValue(logicValue);
            while (storageValue != null) {
                values.put(AnyStorageValue.ATTRIBUTE_PREFIX + storageValue.storageName(), storageValue.value());
                storageValue = storageValue.next();
            }
        }

        return objectMapper.writeValueAsString(values);
    }

    private Optional<IEntity> buildEntityFromStorageEntity(MasterStorageEntity se, IEntityClass entityClass) throws
        SQLException {
        if (se == null) {
            return Optional.empty();
        }
        /*
         * 校验当前数据是否和预期类型匹配.
         */
        long[] dataEntityClassIds = se.getEntityClasses();
        int level = entityClass.level();
        long dataEntityClassId = dataEntityClassIds[level];
        if (entityClass.id() != dataEntityClassId) {
            throw new SQLException(
                String.format(
                    "The incorrect Entity type is expected to be %d, but the actual data type is %d.",
                    entityClass.id(), dataEntityClassId));
        }

        /*
         * 实际的类型
         */
        long actualEntityClassId = 0;
        for (int i = dataEntityClassIds.length - 1; i >= 0; i--) {
            if (dataEntityClassIds[i] > 0) {
                actualEntityClassId = dataEntityClassIds[i];
                break;
            }
        }

        Optional<IEntityClass> actualEntityClassOp = metaManager.load(actualEntityClassId);
        if (!actualEntityClassOp.isPresent()) {
            throw new SQLException(
                String.format("Unable to find the expected EntityClass.[%d]", actualEntityClassId));
        }
        IEntityClass actualEntityClass = actualEntityClassOp.get();

        Entity.Builder entityBuilder = Entity.Builder.anEntity()
            .withId(se.getId())
            .withTime(se.getUpdateTime())
            .withEntityClassRef(actualEntityClass.ref())
            .withVersion(se.getVersion())
            .withEntityValue(toEntityValue(se, actualEntityClass))
            .withMajor(se.getOqsMajor());

        return Optional.of(entityBuilder.build());
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

        private IEntityClass entityClass;
        private long startId;
        private long startTime;
        private long endTime;
        private int pageSize;
        private List<OriginalEntity> buffer;

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
                        OriginalEntity originalEntity = OriginalEntity.Builder.anOriginalEntity()
                            .withEntityClass(entityClass)
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
        public int size() {
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
    }

    // 临时解析结果.
    static class EntityValuePack {
        private IEntityField logicField;
        private StorageValue storageValue;
        private StorageStrategy strategy;

        public EntityValuePack(IEntityField logicField, StorageValue storageValue, StorageStrategy strategy) {
            this.logicField = logicField;
            this.storageValue = storageValue;
            this.strategy = strategy;
        }
    }
}
