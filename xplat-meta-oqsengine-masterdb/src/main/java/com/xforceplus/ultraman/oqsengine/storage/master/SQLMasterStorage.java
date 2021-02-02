package com.xforceplus.ultraman.oqsengine.storage.master;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityFamily;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.summary.BatchCondition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.summary.DataSourceSummary;
import com.xforceplus.ultraman.oqsengine.pojo.dto.summary.TableSummary;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.define.OperationType;
import com.xforceplus.ultraman.oqsengine.storage.master.define.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.*;
import com.xforceplus.ultraman.oqsengine.storage.master.iterator.DataQueryIterator;
import com.xforceplus.ultraman.oqsengine.storage.master.iterator.QueryIterator;
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
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.storage.master.utils.EntityFieldBuildUtils.metaToFieldTypeMap;

/**
 * 主要储存实现.
 *
 * @author dongbin
 * @version 0.1 2020/2/16 22:11
 * @since 1.8
 */
public class SQLMasterStorage implements MasterStorage {

    final Logger logger = LoggerFactory.getLogger(SQLMasterStorage.class);

    @Resource(name = "masterDataSource")
    private DataSource masterDataSource;

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

    private static final String dataSourceName = "master";

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
    public QueryIterator newIterator(
        IEntityClass entityClass,
        long startTimeMs,
        long endTimeMs,
        ExecutorService threadPool,
        int batchTimeout,
        int pageSize,
        boolean filterSearchable) throws SQLException {

        List<DataSource> dataSources = new ArrayList<>();
        dataSources.add(masterDataSource);

        List<DataSourceSummary> dataSourceSummaries = new LinkedList<>();
        BatchCondition batchCondition = new BatchCondition(startTimeMs, endTimeMs, entityClass);
        for (int i = 0; i < dataSources.size(); i++) {
            List<String> tables = new ArrayList<>();

            tables.add(tableName);

            CountDownLatch latch = new CountDownLatch(tables.size());
            List<Future<TableSummary>> futures = new ArrayList<Future<TableSummary>>(tables.size());
            for (String tableName : tables) {
                futures.add(threadPool.submit(
                    new CountByTableSummaryCallable(latch, batchCondition, tableName, batchTimeout)));
            }

            try {
                if (!latch.await(batchTimeout, TimeUnit.MILLISECONDS)) {
                    for (Future<TableSummary> f : futures) {
                        f.cancel(true);
                    }

                    throw new SQLException("Query failed, timeout.");
                }
            } catch (InterruptedException e) {
                throw new SQLException(e.getMessage(), e);
            }
            if (futures.size() > 0) {
                List<TableSummary> tableSummaries = new ArrayList<>();
                for (Future<TableSummary> f : futures) {
                    try {
                        TableSummary tableSummary = f.get();
                        if (null != tableSummary && tableSummary.getCount() > 0) {
                            tableSummaries.add(tableSummary);
                        }
                    } catch (Exception e) {
                        throw new SQLException(e.getMessage(), e);
                    }
                }
                if (tableSummaries.size() > 0) {
                    DataSourceSummary dataSourceSummary = new DataSourceSummary(dataSources.get(i),
                        String.format("%s-%d", dataSourceName, i), tableSummaries);
                    dataSourceSummaries.add(dataSourceSummary);
                }
            }
        }

        return 0 < dataSourceSummaries.size() ?
            new DataQueryIterator(batchCondition, dataSourceSummaries, this, threadPool,
                    batchTimeout, pageSize, filterSearchable) : null;
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
    public Optional<IEntityValue> selectEntityValue(long id) throws SQLException {
        return (Optional<IEntityValue>) transactionExecutor.execute((resource, hint) -> {
            Optional<StorageEntity> seOP =
                QueryExecutor.buildHaveAllDetail(tableName, resource, queryTimeout).execute(id);
            if (seOP.isPresent()) {
                return Optional.ofNullable(
                    entityValueBuilder.build(id, metaToFieldTypeMap(seOP.get().getMeta()), seOP.get().getAttribute()));
            } else {
                return Optional.empty();
            }
        });
    }

    @Override
    public Collection<IEntity> selectMultiple(Map<Long, IEntityClass> ids) throws SQLException {
        long startMs = System.currentTimeMillis();

        try {
            Collection<StorageEntity> storageEntities = (Collection<StorageEntity>) transactionExecutor.execute(
                (resource, hint) -> {

                    return MultipleQueryExecutor.build(tableName, resource, queryTimeout).execute(ids.keySet());
                }
            );

            return storageEntities.parallelStream().map(se -> {
                IEntityClass entityClass = ids.get(se.getId());
                Optional<IEntity> op;
                try {
                    op = buildEntityFromStorageEntity(se, entityClass);
                } catch (SQLException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }

                // 不可能为空.
                return op.get();

            }).collect(Collectors.toList());
        } finally {
            Metrics.timer(MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, "initiator", "master", "action", "multiple")
                .record(System.currentTimeMillis() - startMs, TimeUnit.MILLISECONDS);
        }

    }

    @Override
    public int synchronize(long sourceId, long targetId) throws SQLException {
        Optional<StorageEntity> oldOp = (Optional<StorageEntity>) transactionExecutor.execute(
            (resource, hint) -> QueryExecutor.buildNoDetail(tableName, resource, queryTimeout).execute(sourceId)
        );
        if (oldOp.isPresent()) {

            return (int) transactionExecutor.execute((resource, hint) -> {
                StorageEntity targetEntity = oldOp.get();
                targetEntity.setId(targetId);

                // 累加器更新次+1.
                hint.getAccumulator().accumulateReplace(targetId);
                return UpdateVersionAndTxExecutor.build(tableName, resource, queryTimeout).execute(targetEntity);
                }

            );
        } else {

            return 0;
        }

    }

    @Override
    public int synchronizeToChild(IEntity entity) throws SQLException {
        Optional<StorageEntity> childEntityOp = (Optional<StorageEntity>) transactionExecutor.execute(
            (resource, hint) ->
                QueryExecutor.buildHaveAllDetail(tableName, resource, queryTimeout).execute(entity.family().child())
        );

        if (childEntityOp.isPresent()) {

            return (int) transactionExecutor.execute(
                (resource, hint) -> {
                    StorageEntity childEntity = childEntityOp.get();
                    childEntity.setOp(OperationType.UPDATE.getValue());
                    childEntity.setOqsMajor(OqsVersion.MAJOR);
                    childEntity.setTime(entity.time());

                    fullTransactionInformation(childEntity, resource);

                    //继承自父类属性
                    childEntity.setTime(entity.time());
                    JSONObject fatherAttribute = toJson(entity.entityValue());
                    JSONObject childAttribute;
                    if (childEntity.getAttribute() == null) {
                        childAttribute = JSON.parseObject("{}");
                    } else {
                        childAttribute = JSON.parseObject(childEntity.getAttribute());
                    }
                    childAttribute.putAll(fatherAttribute);
                    childEntity.setAttribute(childAttribute.toJSONString());

                    //合并自父类的meta信息.
                    JSONArray fatherMeta = buildSearchAbleSyncMeta(entity.entityClass());
                    JSONArray childMeta;
                    if (childEntity.getMeta() == null) {
                        childMeta = JSON.parseArray("[]");
                    } else {
                        childMeta = JSON.parseArray(childEntity.getMeta());
                    }
                    childMeta.addAll(fatherMeta);
                    // 去重
                    Set<String> duplicateSet = new HashSet(childMeta.toJavaList(String.class));
                    childMeta.clear();
                    childMeta.addAll(duplicateSet);
                    childEntity.setMeta(childMeta.toJSONString());

                    // 累加器更新次+1.
                    hint.getAccumulator().accumulateReplace(childEntity.getId());
                    return ReplaceExecutor.build(tableName, resource, queryTimeout).execute(childEntity);
                });

        } else {
            throw new SQLException(String.format("Unable to synchronize, unable to find subclass {} with object {}.",
                entity.entityClass().code(), entity.family().child()));
        }
    }

    @Override
    public Optional<Long> maxCommitId() throws SQLException {
        return (Optional<Long>) transactionExecutor.execute(
                (resource, hint) -> {
                    return MaxColumnExecutor.build(tableName, resource, queryTimeout).execute(FieldDefine.COMMITID);
                });
    }

    @Override
    public int build(IEntity entity) throws SQLException {
        long startMs = System.currentTimeMillis();
        try {
            checkId(entity);

            return (int) transactionExecutor.execute(
                (resource, hint) -> {
                    StorageEntity storageEntity = new StorageEntity();
                    storageEntity.setId(entity.id());
                    storageEntity.setEntity(entity.entityClass().id());
                    storageEntity.setPref(entity.family().parent());
                    storageEntity.setCref(entity.family().child());
                    storageEntity.setTime(entity.time());
                    storageEntity.setDeleted(false);
                    storageEntity.setAttribute(toJson(entity.entityValue()).toJSONString());
                    storageEntity.setMeta(buildSearchAbleSyncMeta(entity.entityClass()).toJSONString());

                    storageEntity.setOp(OperationType.CREATE.getValue());
                    fullTransactionInformation(storageEntity, resource);

                    // 累加器创建次+1.
                    hint.getAccumulator().accumulateBuild(entity.id());
                    return BuildExecutor.build(tableName, resource, queryTimeout).execute(storageEntity);
                });
        } finally {
            Metrics.timer(MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, "initiator", "master", "action", "build")
                .record(System.currentTimeMillis() - startMs, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public int replace(IEntity entity) throws SQLException {
        long startMs = System.currentTimeMillis();
        try {
            checkId(entity);

            return (int) transactionExecutor.execute(
                (resource, hint) -> {
                    StorageEntity storageEntity = new StorageEntity();
                    storageEntity.setId(entity.id());
                    storageEntity.setEntity(entity.entityClass().id());
                    storageEntity.setVersion(entity.version());
                    storageEntity.setPref(entity.family().parent());
                    storageEntity.setCref(entity.family().child());
                    storageEntity.setTime(entity.time());
                    storageEntity.setAttribute(toJson(entity.entityValue()).toJSONString());
                    storageEntity.setMeta(buildSearchAbleSyncMeta(entity.entityClass()).toJSONString());

                    storageEntity.setOp(OperationType.UPDATE.getValue());
                    fullTransactionInformation(storageEntity, resource);

                    // 累加器更新次数+1.
                    hint.getAccumulator().accumulateReplace(entity.id());
                    return ReplaceExecutor.build(tableName, resource, queryTimeout).execute(storageEntity);

                });
        } finally {
            Metrics.timer(MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, "initiator", "master", "action", "replace")
                .record(System.currentTimeMillis() - startMs, TimeUnit.MILLISECONDS);
        }
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "master", "action", "delete"})
    @Override
    public int delete(IEntity entity) throws SQLException {
        long startMs = System.currentTimeMillis();
        try {
            checkId(entity);

            return (int) transactionExecutor.execute(
                (resource, hint) -> {
                    /**
                     * 删除数据时不再关心字段信息.
                     */
                    StorageEntity storageEntity = new StorageEntity();
                    storageEntity.setId(entity.id());
                    storageEntity.setVersion(entity.version());
                    storageEntity.setTime(entity.time());

                    storageEntity.setOp(OperationType.DELETE.getValue());
                    fullTransactionInformation(storageEntity, resource);

                    // 累加器删除次数+1.
                    hint.getAccumulator().accumulateDelete(entity.id());
                    return DeleteExecutor.build(tableName, resource, queryTimeout).execute(storageEntity);
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

    private IEntityValue toEntityValue(long id, IEntityClass entityClass, String json) throws SQLException {

        // 以字段逻辑名称为 key, 字段信息为 value.
        Map<String, IEntityField> fieldTable = entityClass.fields()
            .stream().collect(Collectors.toMap(f -> Long.toString(f.id()), f -> f, (f0, f1) -> f0));

        if (entityClass.father() != null) {
            fieldTable.putAll(
                entityClass.father()
                    .fields()
                    .stream()
                    .collect(Collectors.toMap(f -> Long.toString(f.id()), f -> f, (f0, f1) -> f0))
            );
        }

        return entityValueBuilder.build(id, fieldTable, json);

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
        long dataEntityClassId = se.getEntity();
        if (entityClass.id() != dataEntityClassId) {
            throw new SQLException(
                String.format(
                    "The incorrect Entity type is expected to be %d, but the actual data type is %d."
                    , entityClass.id(), dataEntityClassId));
        }

        long id = se.getId();
        Entity entity = new Entity(
            id,
            entityClass,
            toEntityValue(se.getId(), entityClass, se.getAttribute()),
            new EntityFamily(se.getPref(), se.getCref()),
            se.getVersion(),
            se.getOqsMajor()
        );
        entity.markTime(se.getTime());
        return Optional.of(entity);
    }

    /**
     * 构造提示同步数据时的元信息.
     * 只会包含可搜索字段信息.
     * ["{ID}-{type}",.....]
     *
     * @param entityClass entity信息.
     * @return 字符串表示.
     */
    private JSONArray buildSearchAbleSyncMeta(IEntityClass entityClass) {
        JSONArray jsonArray = new JSONArray();
        entityClass.fields().stream().filter(f -> f.config().isSearchable())
            .map(f -> String.join("-", Long.toString(f.id()), f.type().getType()))
            .forEach(s -> jsonArray.add(0, s));

        if (entityClass.father() != null) {
            entityClass.father().fields().stream().filter(f -> f.config().isSearchable())
                .map(f -> String.join("-", Long.toString(f.id()), f.type().getType()))
                .forEach(s -> jsonArray.add(0, s));
        }
        return jsonArray;
    }

    /**
     * 统一EntityClass在每一个库->表中的数量
     */
    private class CountByTableSummaryCallable implements Callable<TableSummary> {

        private CountDownLatch latch;

        private BatchCondition batchCondition;

        private String tableName;

        private int timeout;

        public CountByTableSummaryCallable(CountDownLatch latch, BatchCondition batchCondition, String tableName, int timeout) {
            this.latch = latch;
            this.batchCondition = batchCondition;
            this.timeout = timeout;
            this.tableName = tableName;
        }

        @Override
        @SuppressWarnings("unchecked")
        public TableSummary call() throws Exception {
            try {
                return (TableSummary) transactionExecutor.execute(
                    (resource, hint) -> {

                        TableSummary tableSummary = new TableSummary(tableName);

                        Optional<Integer> integer =
                            BatchQueryCountExecutor.build(
                                tableName,
                                resource,
                                timeout,
                                batchCondition.getEntityClass().id(),
                                batchCondition.getStartTime(),
                                batchCondition.getEndTime()).execute(1L);

                        if (integer.isPresent()) {
                            tableSummary.setCount(integer.get());
                            return tableSummary;
                        }
                        throw new SQLException("query count failed, empty result.");
                    });
            } finally {
                latch.countDown();
            }
        }
    }


    /**
     *
     */
    public class BathQueryByTableSummaryCallable implements Callable<List<IEntity>> {

        private CountDownLatch latch;

        private BatchCondition batchCondition;

        private String tableName;

        private long startId;

        private int pageSize;

        private DataSource dataSource;

        private int timeout;

        public BathQueryByTableSummaryCallable(BatchCondition batchCondition, long startId, int pageSize, DataSource dataSource, String tableName, int timeout) {
            this.batchCondition = batchCondition;
            this.startId = startId;
            this.pageSize = pageSize;
            this.dataSource = dataSource;
            this.tableName = tableName;
            this.timeout = timeout;
        }

        public void setLatch(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<IEntity> call() throws Exception {
            try {
                return (List<IEntity>) transactionExecutor.execute(
                    (resource, hint) -> {

                        Collection<StorageEntity> seCollection = BatchQueryExecutor.build(tableName, resource, timeout,
                            batchCondition.getEntityClass().id(), batchCondition.getStartTime(), batchCondition.getEndTime(),
                            startId, pageSize).execute(1L);

                        List<IEntity> entities = new ArrayList<>(seCollection.size());
                        for (StorageEntity se : seCollection) {
                            Optional<IEntity> entityOp = buildEntityFromStorageEntity(se, batchCondition.getEntityClass());
                            if (entityOp.isPresent()) {
                                entities.add(entityOp.get());
                            } else {
                                throw new SQLException("batch query failed, empty result.");
                            }
                        }
                        return entities;
                    });
            } finally {
                latch.countDown();
            }
        }
    }

    // 填充事务信息.
    private void fullTransactionInformation(StorageEntity entity, TransactionResource resource) {
        Optional<Transaction> tOp = resource.getTransaction();
        if (tOp.isPresent()) {
            entity.setTx(tOp.get().id());
            entity.setCommitid(CommitHelper.getUncommitId());
        } else {
            logger.warn("With no transaction, unable to get the transaction ID.");
            entity.setTx(0);
            entity.setCommitid(0);
        }

    }
}
