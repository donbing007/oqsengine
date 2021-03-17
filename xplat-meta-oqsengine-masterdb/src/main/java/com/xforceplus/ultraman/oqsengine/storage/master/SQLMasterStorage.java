package com.xforceplus.ultraman.oqsengine.storage.master;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.select.SelectConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.define.OperationType;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.*;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.MasterStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.conditions.SQLJsonConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.commit.CommitHelper;
import com.xforceplus.ultraman.oqsengine.storage.value.AnyStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValueFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactory;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 主要储存实现.
 *
 * @author dongbin
 * @version 0.1 2020/2/16 22:11
 * @since 1.8
 */
public class SQLMasterStorage implements MasterStorage {

    final Logger logger = LoggerFactory.getLogger(SQLMasterStorage.class);

    @Resource(name = "storageJDBCTransactionExecutor")
    private TransactionExecutor transactionExecutor;

    @Resource(name = "masterStorageStrategy")
    private StorageStrategyFactory storageStrategyFactory;

    @Resource(name = "masterConditionsBuilderFactory")
    private SQLJsonConditionsBuilderFactory conditionsBuilderFactory;

    @Resource(name = "tokenizerFactory")
    private TokenizerFactory tokenizerFactory;

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
    public DataIterator<IEntity> iterator(IEntityClass entityClass, long startTime, long endTime, long lastStart) throws SQLException {
        return new IEntityIterator(entityClass, lastStart, startTime, endTime);
    }

    @Override
    public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, SelectConfig config)
        throws SQLException {
        long startMs = System.currentTimeMillis();
        try {
            return (Collection<EntityRef>) transactionExecutor.execute((resource, hint) -> {
                return QueryLimitCommitidByConditionsExecutor.build(
                    tableName,
                    resource,
                    entityClass,
                    config.getSort(),
                    config.getCommitId(),
                    queryTimeout,
                    conditionsBuilderFactory,
                    storageStrategyFactory).execute(conditions);
            });
        } finally {
            Metrics.timer(MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, "initiator", "master", "action", "condition")
                .record(System.currentTimeMillis() - startMs, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public boolean exist(long id) throws SQLException {
        return (boolean) transactionExecutor.execute(((resource, hint) ->
            ExistExecutor.build(tableName, resource, queryTimeout).execute(id)));
    }

    @Override
    public Optional<IEntity> selectOne(long id, IEntityClass entityClass) throws SQLException {
        long startMs = System.currentTimeMillis();
        try {
            return (Optional<IEntity>) transactionExecutor.execute((resource, hint) -> {
                Optional<MasterStorageEntity> seOP =
                    QueryExecutor.buildHaveDetail(tableName, resource, entityClass, queryTimeout).execute(id);
                if (seOP.isPresent()) {
                    return buildEntityFromStorageEntity(seOP.get(), entityClass);
                } else {
                    return Optional.empty();
                }
            });
        } finally {
            Metrics.timer(MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, "initiator", "master", "action", "one")
                .record(System.currentTimeMillis() - startMs, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public Collection<IEntity> selectMultiple(long[] ids, IEntityClass entityClass) throws SQLException {
        long startMs = System.currentTimeMillis();

        try {
            Collection<MasterStorageEntity> storageEntities = (Collection<MasterStorageEntity>) transactionExecutor.execute(
                (resource, hint) -> {

                    return MultipleQueryExecutor.build(tableName, resource, entityClass, queryTimeout).execute(ids);
                }
            );


            return storageEntities.parallelStream().map(se -> {
                Optional<IEntity> op;
                try {
                    op = buildEntityFromStorageEntity(se, entityClass);
                } catch (SQLException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }

                return op.get();
            }).collect(Collectors.toList());

        } finally {
            Metrics.timer(MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, "initiator", "master", "action", "multiple")
                .record(System.currentTimeMillis() - startMs, TimeUnit.MILLISECONDS);
        }

    }

    @Override
    public int build(IEntity entity, IEntityClass entityClass) throws SQLException {
        long startMs = System.currentTimeMillis();
        try {
            checkId(entity);

            return (int) transactionExecutor.execute(
                (resource, hint) -> {

                    long createTime = findTime(entity, FieldConfig.FieldSense.CREATE_TIME);
                    long updateTIme = findTime(entity, FieldConfig.FieldSense.UPDATE_TIME);
                    MasterStorageEntity.Builder storageEntityBuilder = MasterStorageEntity.Builder.aStorageEntity()
                        .withId(entity.id())
                        /**
                         * optimize: 创建时间和更新时间保证和系统字段同步.
                         */
                        .withCreateTime(createTime > 0 ? createTime : entity.time())
                        .withUpdateTime(updateTIme > 0 ? updateTIme : entity.time())
                        .withDeleted(false)
                        .withEntityClassVersion(entityClass.version())
                        .withVersion(0)
                        .withAttribute(toJson(entity.entityValue()).toJSONString())
                        .withOp(OperationType.CREATE.getValue());
                    fullEntityClassInformation(storageEntityBuilder, entityClass);
                    fullTransactionInformation(storageEntityBuilder, resource);

                    try {
                        return BuildExecutor.build(tableName, resource, queryTimeout).execute(storageEntityBuilder.build());
                    } finally {
                        // 累加器创建次+1.
                        hint.getAccumulator().accumulateBuild(entity.id());
                    }
                });
        } finally {
            Metrics.timer(MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, "initiator", "master", "action", "build")
                .record(System.currentTimeMillis() - startMs, TimeUnit.MILLISECONDS);
        }
    }

    private long findTime(IEntity entity, FieldConfig.FieldSense sense) {
        OptionalLong op = entity.entityValue().values().parallelStream()
            .filter(iValue -> sense == iValue.getField().config().getFieldSense())
            .mapToLong(iValue -> iValue.valueToLong()).findFirst();

        return op.orElse(0);
    }

    @Override
    public int replace(IEntity entity, IEntityClass entityClass) throws SQLException {
        long startMs = System.currentTimeMillis();
        try {
            checkId(entity);

            return (int) transactionExecutor.execute(
                (resource, hint) -> {

                    long updateTime = findTime(entity, FieldConfig.FieldSense.UPDATE_TIME);
                    /**
                     * 如果从新结果集中查询到更新时间,但是和当前最后更新时间相等那么使用系统时间.
                     */
                    if (updateTime == entity.time()) {
                        updateTime = 0;
                    }
                    MasterStorageEntity.Builder storageEntityBuilder = MasterStorageEntity.Builder.aStorageEntity()
                        .withId(entity.id())
                        /**
                         * optimize: 更新时间保证和系统字段同步.
                         */
                        .withUpdateTime(updateTime > 0 ? updateTime : entity.time())
                        .withVersion(entity.version())
                        .withEntityClassVersion(entityClass.version())
                        .withAttribute(toJson(entity.entityValue()).toJSONString())
                        .withOp(OperationType.UPDATE.getValue());

                    fullEntityClassInformation(storageEntityBuilder, entityClass);
                    fullTransactionInformation(storageEntityBuilder, resource);

                    try {
                        return UpdateExecutor.build(tableName, resource, queryTimeout).execute(storageEntityBuilder.build());
                    } finally {
                        // 累加器更新次数+1.
                        hint.getAccumulator().accumulateReplace(entity.id());
                    }

                });
        } finally {
            Metrics.timer(MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, "initiator", "master", "action", "replace")
                .record(System.currentTimeMillis() - startMs, TimeUnit.MILLISECONDS);
        }
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "master", "action", "delete"})
    @Override
    public int delete(IEntity entity, IEntityClass entityClass) throws SQLException {
        long startMs = System.currentTimeMillis();
        try {
            checkId(entity);

            return (int) transactionExecutor.execute(
                (resource, hint) -> {
                    /**
                     * 删除数据时不再关心字段信息.
                     */
                    MasterStorageEntity.Builder storageEntityBuilder = MasterStorageEntity.Builder.aStorageEntity()
                        .withId(entity.id())
                        .withOp(OperationType.DELETE.getValue())
                        .withUpdateTime(entity.time())
                        .withEntityClassVersion(entityClass.version())
                        .withDeleted(true)
                        .withVersion(entity.version());

                    fullEntityClassInformation(storageEntityBuilder, entityClass);
                    fullTransactionInformation(storageEntityBuilder, resource);

                    try {
                        return DeleteExecutor.build(tableName, resource, queryTimeout).execute(storageEntityBuilder.build());
                    } finally {
                        // 累加器删除次数+1.
                        hint.getAccumulator().accumulateDelete(entity.id());
                    }
                });
        } finally {
            Metrics.timer(MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, "initiator", "master", "action", "delete")
                .record(System.currentTimeMillis() - startMs, TimeUnit.MILLISECONDS);
        }
    }

    private void checkId(IEntity entity) throws SQLException {
        if (entity.id() == 0) {
            throw new SQLException("Invalid entity`s id.");
        }
    }

    // 储存字段转换为逻辑字段.
    private IEntityValue toEntityValue(MasterStorageEntity masterStorageEntity, IEntityClass entityClass) throws SQLException {
        JSONObject object = JSON.parseObject(masterStorageEntity.getAttribute());

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
    private JSONObject toJson(IEntityValue value) {

        JSONObject object = new JSONObject();
        StorageStrategy storageStrategy;
        StorageValue storageValue;
        for (IValue logicValue : value.values()) {
            storageStrategy = storageStrategyFactory.getStrategy(logicValue.getField().type());
            storageValue = storageStrategy.toStorageValue(logicValue);
            while (storageValue != null) {
                object.put(AnyStorageValue.ATTRIBUTE_PREFIX + storageValue.storageName(), storageValue.value());
                storageValue = storageValue.next();
            }
        }
        return object;

    }

    private Optional<IEntity> buildEntityFromStorageEntity(MasterStorageEntity se, IEntityClass entityClass) throws SQLException {
        if (se == null) {
            return Optional.empty();
        }
        /**
         * 校验当前数据是否和预期类型匹配.
         */
        long[] dataEntityClassIds = se.getEntityClasses();
        int level = entityClass.level();
        long dataEntityClassId = dataEntityClassIds[level];
        if (entityClass.id() != dataEntityClassId) {
            throw new SQLException(
                String.format(
                    "The incorrect Entity type is expected to be %d, but the actual data type is %d."
                    , entityClass.id(), dataEntityClassId));
        }

        Entity.Builder entityBuilder = Entity.Builder.anEntity()
            .withId(se.getId())
            .withTime(se.getUpdateTime())
            .withEntityClassRef(
                EntityClassRef.Builder.anEntityClassRef()
                    .withEntityClassId(entityClass.id())
                    .withEntityClassCode(entityClass.code()).build())
            .withVersion(se.getVersion())
            .withEntityValue(toEntityValue(se, entityClass))
            .withMajor(se.getOqsMajor());

        return Optional.of(entityBuilder.build());
    }

    // 填充事务信息.
    private void fullTransactionInformation(MasterStorageEntity.Builder entityBuilder, TransactionResource resource) {
        Optional<Transaction> tOp = resource.getTransaction();
        if (tOp.isPresent()) {
            entityBuilder.withTx(tOp.get().id())
                .withCommitid(CommitHelper.getUncommitId());
        } else {
            logger.warn("With no transaction, unable to get the transaction ID.");
            entityBuilder.withTx(0)
                .withCommitid(0);
        }

    }

    // 填充类型信息
    private void fullEntityClassInformation(MasterStorageEntity.Builder storageEntityBuilder, IEntityClass entityClass) {
        Collection<IEntityClass> family = entityClass.family();
        long[] tileEntityClassesIds = family.stream().mapToLong(ecs -> ecs.id()).toArray();
        storageEntityBuilder.withEntityClasses(tileEntityClassesIds);
    }

    /**
     * 数据迭代器,用以迭代出某个entity的实例列表.
     */
    private class IEntityIterator implements DataIterator<IEntity> {
        private static final int DEFAULT_PAGE_SIZE = 100;

        private IEntityClass entityClass;
        private long startId;
        private long startTime;
        private long endTime;
        private int pageSize;
        private List<MasterStorageEntity> buffer;

        public IEntityIterator(IEntityClass entityClass, long startId, long startTime, long endTime) {
            this(entityClass, startId, startTime, endTime, DEFAULT_PAGE_SIZE);
        }

        public IEntityIterator(IEntityClass entityClass, long startId, long startTime, long endTime, int pageSize) {
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
        public IEntity next() {
            if (hasNext()) {
                try {
                    return buildEntityFromStorageEntity(buffer.remove(0), entityClass).get();
                } catch (SQLException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            } else {
                return null;
            }
        }

        private void load() throws SQLException {

            transactionExecutor.execute((resource, hint) -> {
                Collection<MasterStorageEntity> storageEntities =
                    BatchQueryExecutor.build(tableName, resource, queryTimeout, entityClass, startTime, endTime, pageSize)
                        .execute(startId);

                buffer.addAll(storageEntities);

                return null;
            });
        }

        @Override
        public long size() {
            try {
                return (int) transactionExecutor.execute((resource, hint) -> {
                    return BatchQueryCountExecutor.build(tableName, resource, queryTimeout, entityClass, startTime, endTime)
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
