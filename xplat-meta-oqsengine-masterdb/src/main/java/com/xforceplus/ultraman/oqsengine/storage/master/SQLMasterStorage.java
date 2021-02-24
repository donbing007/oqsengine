package com.xforceplus.ultraman.oqsengine.storage.master;

import com.alibaba.fastjson.JSONObject;
import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IEntityClassHelper;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.define.OperationType;
import com.xforceplus.ultraman.oqsengine.storage.master.define.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.*;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.conditions.SQLJsonConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.commit.CommitHelper;
import com.xforceplus.ultraman.oqsengine.storage.utils.IEntityValueBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
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

    @Resource(name = "entityValueBuilder")
    private IEntityValueBuilder<String> entityValueBuilder;

    @Resource(name = "masterConditionsBuilderFactory")
    private SQLJsonConditionsBuilderFactory conditionsBuilderFactory;

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
    public Collection<EntityRef> select(long commitid, Conditions conditions, IEntityClass entityClass, Sort sort)
        throws SQLException {
        long startMs = System.currentTimeMillis();
        try {
            return (Collection<EntityRef>) transactionExecutor.execute((resource, hint) -> {
                return QueryLimitCommitidByConditionsExecutor.build(
                    tableName,
                    resource,
                    entityClass,
                    sort,
                    commitid,
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
                Optional<StorageEntity> seOP = QueryExecutor.buildHaveDetail(tableName, resource, queryTimeout).execute(id);
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
            Collection<StorageEntity> storageEntities = (Collection<StorageEntity>) transactionExecutor.execute(
                (resource, hint) -> {

                    return MultipleQueryExecutor.build(tableName, resource, queryTimeout).execute(ids);
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
                    StorageEntity.Builder storageEntityBuilder = StorageEntity.Builder.aStorageEntity()
                        .withId(entity.id())
                        .withCreateTime(entity.time())
                        .withUpdateTime(entity.time())
                        .withDeleted(false)
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

    @Override
    public int replace(IEntity entity, IEntityClass entityClass) throws SQLException {
        long startMs = System.currentTimeMillis();
        try {
            checkId(entity);

            return (int) transactionExecutor.execute(
                (resource, hint) -> {
                    StorageEntity.Builder storageEntityBuilder = StorageEntity.Builder.aStorageEntity()
                        .withId(entity.id())
                        .withUpdateTime(entity.time())
                        .withVersion(entity.version())
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
                    StorageEntity.Builder storageEntityBuilder = StorageEntity.Builder.aStorageEntity()
                        .withId(entity.id())
                        .withOp(OperationType.DELETE.getValue())
                        .withUpdateTime(entity.time())
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

    private IEntityValue toEntityValue(StorageEntity storageEntity, IEntityClass entityClass) throws SQLException {

        IEntityClass current = entityClass;
        Map<String, IEntityField> fieldTable = new HashMap();
        while (current != null) {
            current.fields().stream().forEach(f -> {
                fieldTable.put(Long.toString(f.id()), f);
            });
            if (current.father().isPresent()) {
                current = current.father().get();
            } else {
                // break
                current = null;
            }
        }

        return entityValueBuilder.build(storageEntity.getId(), fieldTable, storageEntity.getAttribute());

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
                object.put(FieldDefine.ATTRIBUTE_PREFIX + storageValue.storageName(), storageValue.value());
                storageValue = storageValue.next();
            }
        }
        return object;

    }

    private Optional<IEntity> buildEntityFromStorageEntity(StorageEntity se, IEntityClass entityClass) throws SQLException {
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
    private void fullTransactionInformation(StorageEntity.Builder entityBuilder, TransactionResource resource) {
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
    private void fullEntityClassInformation(StorageEntity.Builder storageEntityBuilder, IEntityClass entityClass) {
        IEntityClass[] tileEntityClasses = IEntityClassHelper.paveEntityClass(entityClass);
        long[] tileEntityClassesIds = Arrays.stream(tileEntityClasses).mapToLong(ecs -> ecs.id()).toArray();
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
        private List<StorageEntity> buffer;

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
                Collection<StorageEntity> storageEntities =
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

//    @Override
//    public QueryIterator newIterator(
//        IEntityClass entityClass,
//        long startTimeMs,
//        long endTimeMs,
//        ExecutorService threadPool,
//        int batchTimeout,
//        int pageSize,
//        boolean filterSearchable) throws SQLException {
//
//        List<DataSource> dataSources = new ArrayList<>();
//        dataSources.add(masterDataSource);
//
//        List<DataSourceSummary> dataSourceSummaries = new LinkedList<>();
//        BatchCondition batchCondition = new BatchCondition(startTimeMs, endTimeMs, entityClass);
//        for (int i = 0; i < dataSources.size(); i++) {
//            List<String> tables = new ArrayList<>();
//
//            tables.add(tableName);
//
//            CountDownLatch latch = new CountDownLatch(tables.size());
//            List<Future<TableSummary>> futures = new ArrayList<Future<TableSummary>>(tables.size());
//            for (String tableName : tables) {
//                futures.add(threadPool.submit(
//                    new CountByTableSummaryCallable(latch, batchCondition, tableName, batchTimeout)));
//            }
//
//            try {
//                if (!latch.await(batchTimeout, TimeUnit.MILLISECONDS)) {
//                    for (Future<TableSummary> f : futures) {
//                        f.cancel(true);
//                    }
//
//                    throw new SQLException("Query failed, timeout.");
//                }
//            } catch (InterruptedException e) {
//                throw new SQLException(e.getMessage(), e);
//            }
//            if (futures.size() > 0) {
//                List<TableSummary> tableSummaries = new ArrayList<>();
//                for (Future<TableSummary> f : futures) {
//                    try {
//                        TableSummary tableSummary = f.get();
//                        if (null != tableSummary && tableSummary.getCount() > 0) {
//                            tableSummaries.add(tableSummary);
//                        }
//                    } catch (Exception e) {
//                        throw new SQLException(e.getMessage(), e);
//                    }
//                }
//                if (tableSummaries.size() > 0) {
//                    DataSourceSummary dataSourceSummary = new DataSourceSummary(dataSources.get(i),
//                        String.format("%s-%d", dataSourceName, i), tableSummaries);
//                    dataSourceSummaries.add(dataSourceSummary);
//                }
//            }
//        }
//
//        return 0 < dataSourceSummaries.size() ?
//            new DataQueryIterator(batchCondition, dataSourceSummaries, this, threadPool,
//                batchTimeout, pageSize, filterSearchable) : null;
//    }


//    /**
//     * 统一EntityClass在每一个库->表中的数量
//     */
//    private class CountByTableSummaryCallable implements Callable<TableSummary> {
//
//        private CountDownLatch latch;
//
//        private BatchCondition batchCondition;
//
//        private String tableName;
//
//        private int timeout;
//
//        public CountByTableSummaryCallable(CountDownLatch latch, BatchCondition batchCondition, String tableName, int timeout) {
//            this.latch = latch;
//            this.batchCondition = batchCondition;
//            this.timeout = timeout;
//            this.tableName = tableName;
//        }
//
//        @Override
//        @SuppressWarnings("unchecked")
//        public TableSummary call() throws Exception {
//            try {
//                return (TableSummary) transactionExecutor.execute(
//                    (resource, hint) -> {
//
//                        TableSummary tableSummary = new TableSummary(tableName);
//
//                        Optional<Integer> integer =
//                            BatchQueryCountExecutor.build(
//                                tableName,
//                                resource,
//                                timeout,
//                                batchCondition.getEntityClass().id(),
//                                batchCondition.getStartTime(),
//                                batchCondition.getEndTime()).execute(1L);
//
//                        if (integer.isPresent()) {
//                            tableSummary.setCount(integer.get());
//                            return tableSummary;
//                        }
//                        throw new SQLException("query count failed, empty result.");
//                    });
//            } finally {
//                latch.countDown();
//            }
//        }
//    }
//
//
//    /**
//     *
//     */
//    public class BathQueryByTableSummaryCallable implements Callable<List<IEntity>> {
//
//        private CountDownLatch latch;
//
//        private BatchCondition batchCondition;
//
//        private String tableName;
//
//        private long startId;
//
//        private int pageSize;
//
//        private DataSource dataSource;
//
//        private int timeout;
//
//        public BathQueryByTableSummaryCallable(BatchCondition batchCondition, long startId, int pageSize, DataSource dataSource, String tableName, int timeout) {
//            this.batchCondition = batchCondition;
//            this.startId = startId;
//            this.pageSize = pageSize;
//            this.dataSource = dataSource;
//            this.tableName = tableName;
//            this.timeout = timeout;
//        }
//
//        public void setLatch(CountDownLatch latch) {
//            this.latch = latch;
//        }
//
//        @Override
//        @SuppressWarnings("unchecked")
//        public List<IEntity> call() throws Exception {
//            try {
//                return (List<IEntity>) transactionExecutor.execute(
//                    (resource, hint) -> {
//
//                        Collection<StorageEntity> seCollection = BatchQueryExecutor.build(tableName, resource, timeout,
//                            batchCondition.getEntityClass().id(), batchCondition.getStartTime(), batchCondition.getEndTime(),
//                            startId, pageSize).execute(1L);
//
//                        List<IEntity> entities = new ArrayList<>(seCollection.size());
//                        for (StorageEntity se : seCollection) {
//                            Optional<IEntity> entityOp = buildEntityFromStorageEntity(se, batchCondition.getEntityClass());
//                            if (entityOp.isPresent()) {
//                                entities.add(entityOp.get());
//                            } else {
//                                throw new SQLException("batch query failed, empty result.");
//                            }
//                        }
//                        return entities;
//                    });
//            } finally {
//                latch.countDown();
//            }
//        }
//    }
}
