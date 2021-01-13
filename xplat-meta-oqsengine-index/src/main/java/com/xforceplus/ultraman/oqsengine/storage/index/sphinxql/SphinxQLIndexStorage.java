package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql;

import com.alibaba.fastjson.JSON;
import com.alibaba.google.common.collect.Lists;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.AnyEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.executor.ResourceTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.hint.ExecutorHint;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor.*;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.SphinxQLConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.value.LongStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactoryAble;
import io.micrometer.core.instrument.Metrics;
import io.vavr.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.xforceplus.ultraman.oqsengine.common.error.CommonErrors.INVALID_ENTITY_ID;
import static com.xforceplus.ultraman.oqsengine.common.error.CommonErrors.PARSE_COLUMNS_ERROR;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.SECOND;

/**
 * 基于 SphinxQL 的索引储存实现. 注意: 这里交所有的 单引号 双引号和斜杠都进行了替换. 此实现并不会进行属性的返回,只会进行查询.
 * <p>
 * 同时使用了一个 json 的字段格式和全文搜索格式储存属性. id, entity, pref, cref, jsonfields,
 * fullfields. 基中 jsonfields 储存的如果是字符串,那会对其中的字符串进行转义.
 *
 * @author dongbin
 * @version 0.1 2020/2/17 17:16
 * @since 1.8
 */
public class SphinxQLIndexStorage implements IndexStorage, StorageStrategyFactoryAble {

    final Logger logger = LoggerFactory.getLogger(SphinxQLIndexStorage.class);

    @Resource(name = "indexConditionsBuilderFactory")
    private SphinxQLConditionsBuilderFactory sphinxQLConditionsBuilderFactory;

    @Resource(name = "indexWriteDataSourceSelector")
    private Selector<DataSource> writerDataSourceSelector;

    @Resource(name = "sphinxQLSearchTransactionExecutor")
    private TransactionExecutor searchTransactionExecutor;

    @Resource(name = "sphinxQLWriteTransactionExecutor")
    private TransactionExecutor writeTransactionExecutor;

    @Resource(name = "indexStorageStrategy")
    private StorageStrategyFactory storageStrategyFactory;

    @Resource(name = "indexWriteIndexNameSelector")
    private Selector<String> indexWriteIndexNameSelector;

    private int maxBatchSize = 50;

    private String searchIndexName;

    // 最大查询超时时间,默认为无限.
    private long maxSearchTimeoutMs = 0;

    public long getMaxSearchTimeoutMs() {
        return maxSearchTimeoutMs;
    }

    public void setMaxSearchTimeoutMs(long maxSearchTimeoutMs) {
        this.maxSearchTimeoutMs = maxSearchTimeoutMs;
    }

    public String getSearchIndexName() {
        return searchIndexName;
    }

    public void setSearchIndexName(String searchIndexName) {
        this.searchIndexName = searchIndexName;
    }

    public void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }

    /**
     * query by condition
     *
     * @param conditions  搜索条件.
     * @param entityClass 搜索目标的 entityClass.
     * @param sort        搜索结果排序.
     * @param page        搜索结果分页信息.
     * @return
     * @throws SQLException
     */
    @Override
    public Collection<EntityRef> select(
        Conditions conditions, IEntityClass entityClass, Sort sort, Page page, Set<Long> filterIds, long commitId)
        throws SQLException {

        long startMs = System.currentTimeMillis();

        try {
            return (Collection<EntityRef>) searchTransactionExecutor.execute((resource, hint) -> {
                Set<Long> useFilterIds = null;

                if (resource.getTransaction().isPresent()) {
                    Transaction tx = (Transaction) resource.getTransaction().get();
                    Set<Long> updateIds = tx.getAccumulator().getUpdateIds();
                    if (updateIds.size() > 0) {
                        useFilterIds = new HashSet();
                        useFilterIds.addAll(updateIds);
                        useFilterIds.addAll(filterIds);
                    }
                } else {
                    useFilterIds = filterIds;
                }

                return QueryConditionExecutor.build(
                    searchIndexName,
                    resource,
                    sphinxQLConditionsBuilderFactory,
                    storageStrategyFactory,
                    maxSearchTimeoutMs).execute(
                    Tuple.of(entityClass, conditions, page, sort, useFilterIds, commitId));
            });
        } finally {
            Metrics.timer(MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, "initiator", "index", "action", "condition")
                .record(System.currentTimeMillis() - startMs, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void replaceAttribute(IEntityValue attribute) throws SQLException {
        checkId(attribute.id());

        long dataId = attribute.id();
        final StorageEntity oldStorageEntity;
        oldStorageEntity = (StorageEntity) writeTransactionExecutor.execute(new ResourceTask() {
            @Override
            public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {

                Optional<StorageEntity> oldStorageEntityOptional =
                    QueryExecutor.build(resource, searchIndexName).execute(dataId);
                if (oldStorageEntityOptional.isPresent()) {
                    return oldStorageEntityOptional.get();
                } else {
                    return null;
                }
            }

            @Override
            public String key() {
                return Long.toString(dataId);
            }
        });

        if (oldStorageEntity != null) {
            /**
             * 把新的属性插入旧属性集中替换已有,或新增.
             */
            Map<String, Object> completeAttribues = oldStorageEntity.getJsonFields();
            Map<String, Object> modifiedAttributes = serializeToMap(attribute, true);
            completeAttribues.putAll(modifiedAttributes);
            // 处理 fulltext
            oldStorageEntity.setJsonFields(completeAttribues);
            oldStorageEntity.setFullFields(convertJsonToFull(completeAttribues));
            doBuildReplaceStorageEntity(oldStorageEntity, true);
        } else {
            throw new SQLException(String
                .format("Attempt to update a property on a data that does not exist.[%d]", attribute.id()));
        }
    }

    @Override
    public int build(IEntity entity) throws SQLException {
        throw new UnsupportedOperationException("Deprecated");
    }

    @Override
    public int replace(IEntity entity) throws SQLException {
        throw new UnsupportedOperationException("Deprecated");
    }

    @Override
    public void entityValueToStorage(StorageEntity storageEntity, IEntityValue entityValue) {
        //  jsonFields
        storageEntity.setJsonFields(serializeToMap(entityValue, true));
        // fullTexts
        storageEntity.setFullFields(serializeSetFull(entityValue));
    }

    @Override
    public int buildOrReplace(StorageEntity storageEntity, IEntityValue entityValue, boolean replacement) throws SQLException {
        long startMs = System.currentTimeMillis();
        try {
            checkId(storageEntity.getId());
            try {
                entityValueToStorage(storageEntity, entityValue);
            } catch (Exception e) {
                throw new SQLException(
                    String.format("serialize entityValue to jsonFields/fullTexts failed, message : %s", e.getMessage()), PARSE_COLUMNS_ERROR.name());
            }

            return doBuildReplaceStorageEntity(storageEntity, replacement);
        } finally {
            Metrics.timer(MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, "initiator", "index", "action", "save")
                .record(System.currentTimeMillis() - startMs, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public int batchSave(Collection<StorageEntity> storageEntities, boolean replacement, boolean forceRetry) throws SQLException {

        long startMs = System.currentTimeMillis();
        //  database key -> table key -> List
        Map<String, List<StorageEntity>> shardingStorageEntities = new LinkedHashMap<>();
        //  分类storageEntity
        for (StorageEntity storageEntity : storageEntities) {
            //  写入到shardingStorageEntities中
            String shardKey = Long.toString(storageEntity.getId());
            String dataSourceKey = writerDataSourceSelector.select(shardKey).toString();
            String tableShardKey = indexWriteIndexNameSelector.select(shardKey);

            //  写入StorageEntity, key使用 dataSourceKey_tableShardKey 这样的结构
            shardingStorageEntities.computeIfAbsent(dataSourceKey + "_" + tableShardKey, k -> new ArrayList<>())
                .add(storageEntity);
        }

        //  开始按照数据库/表进行分片插入
        int executed = 0;
        for (Map.Entry<String, List<StorageEntity>> entities : shardingStorageEntities.entrySet()) {
            Collection<List<StorageEntity>> partitions = Lists.partition(entities.getValue(), maxBatchSize);
            for (List<StorageEntity> storageEntityList : partitions) {
                try {
                    int batchExecuted = doBatchSave(storageEntityList, replacement);
                    if (batchExecuted != storageEntityList.size()) {
                        throw new SQLException(
                            String.format("batchExecute size [%d] not match storageEntities size [%d], will retry by single executor.",
                                batchExecuted, storageEntities.size()));
                    }
                    executed += batchExecuted;
                } catch (Exception ex1) {
                        /*
                            批量执行失败，将进行降级处理，实际为按每一条进行插入或替换处理，如果依然处理失败，则根据forceRetry判断是否需要无限重试与否
                         */
                    logger.error("batch job failed, will be retry by single operation, message : {}", ex1.getMessage());
                    for (StorageEntity se : storageEntityList) {
                        try {
                            executed += doBuildReplaceStorageEntity(se, replacement);
                        } catch (Exception ex2) {
                                /*
                                    是否进行重试
                                 */
                            if (forceRetry) {
                                while (true) {
                                    try {
                                        executed += doBuildReplaceStorageEntity(se, replacement);
                                        break;
                                    } catch (Exception ex3) {
                                        //  delete error
                                        logger.error("replace error, will retry..., id : {}, commitId : {}, message : {}",
                                            se.getId(), se.getCommitId(), ex3.getMessage());
                                        try {
                                            Thread.sleep(SECOND);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            } else {
                                //  delete error
                                logger.error("replace error, id : {}, commitId : {}, message : {}",
                                    se.getId(), se.getCommitId(), ex2.getMessage());
                                throw ex2;
                            }
                        }
                    }
                }
            }
        }

        try {
            return executed;
        } finally {
            Metrics.timer(MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, "initiator", "index", "action", "save")
                .record(System.currentTimeMillis() - startMs, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public int delete(long id) throws SQLException {
        long startMs = System.currentTimeMillis();
        try {
            checkId(id);

            String shardKey = Long.toString(id);
            return (int) writeTransactionExecutor.execute(new ResourceTask() {

                @Override
                public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                    return DeleteExecutor.build(resource, indexWriteIndexNameSelector.select(shardKey))
                        .execute(id);
                }

                @Override
                public String key() {
                    return shardKey;
                }
            });
        } finally {
            Metrics.timer(MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, "initiator", "index", "action", "delete")
                .record(System.currentTimeMillis() - startMs, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public int delete(IEntity entity) throws SQLException {
        return delete(entity.id());
    }

    @Override
    public void setStorageStrategy(StorageStrategyFactory storageStrategyFactory) {
        this.storageStrategyFactory = storageStrategyFactory;
    }


    /**
     * <f>fieldId + fieldvalue(unicode)</f> + <f>fieldId +
     * fieldvalue(unicode)</f>....n
     */
    public Set<String> serializeSetFull(IEntityValue entityValue) {
        Set<String> fullSet = new HashSet<>();

        entityValue.values().stream().forEach(v -> {

            StorageValue storageValue = storageStrategyFactory.getStrategy(v.getField().type()).toStorageValue(v);

            while (storageValue != null) {
                // 这里已经处理成了索引的储存样式.
                fullSet.add(SphinxQLHelper.serializeStorageValueFull(storageValue));

                storageValue = storageValue.next();
            }
        });

        return fullSet;
    }

    /**
     * { "{fieldId}" : fieldValue }
     */
    private Map<String, Object> serializeToMap(IEntityValue values, boolean encodeString) {
        Map<String, Object> data = new HashMap<>(values.values().size());
        values.values().stream().forEach(v -> {
            StorageValue storageValue = storageStrategyFactory.getStrategy(v.getField().type()).toStorageValue(v);

            while (storageValue != null) {
                if (storageValue.type() == StorageType.STRING) {
                    data.put(storageValue.storageName(),
                        encodeString ? SphinxQLHelper.encodeSpecialCharset((String) storageValue.value())
                            : storageValue.value());
                } else {
                    data.put(storageValue.storageName(), storageValue.value());
                }
                storageValue = storageValue.next();
            }
        });

        return data;
    }

    // 转换 json 字段为全文搜索字段.
    private Set<String> convertJsonToFull(Map<String, Object> attributes) {
        Set<String> fullfileds = new HashSet<>();
        Object value;
        StorageValue storageValue = null;
        for (String key : attributes.keySet()) {
            value = attributes.get(key);

            if (Long.class.isInstance(value)) {

                storageValue = new LongStorageValue(key, (Long) value, false);

            } else if (Integer.class.isInstance(value)) {

                storageValue = new LongStorageValue(key, ((Integer) value).longValue(), false);

            } else {

                storageValue = new StringStorageValue(key, (String) value, false);
            }

            fullfileds.add(SphinxQLHelper.serializeStorageValueFull(storageValue));
        }

        return fullfileds;
    }

    private void checkId(long id) throws SQLException {
        if (id == 0) {
            throw new SQLException("Invalid entity`s id.", INVALID_ENTITY_ID.name());
        }
    }

    //    // 更新原始数据.
    private int doBatchSave(List<StorageEntity> storageEntities, boolean replacement) throws SQLException {

        String shardKey = Long.toString(storageEntities.get(0).getId());

        return (int) writeTransactionExecutor.execute(new ResourceTask() {

            @Override
            public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {

                return BatchHandleExecutor.build(
                    resource, indexWriteIndexNameSelector.select(shardKey), replacement ? "replace" : "insert").execute(storageEntities);
            }

            @Override
            public String key() {
                return shardKey;
            }
        });
    }

    //    // 更新原始数据.
    private int doBuildReplaceStorageEntity(StorageEntity storageEntity, boolean replacement) throws SQLException {
        String shardKey = Long.toString(storageEntity.getId());
        return (int) writeTransactionExecutor.execute(new ResourceTask() {

            @Override
            public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                if (replacement) {
                    return ReplaceExecutor.build(
                        resource, indexWriteIndexNameSelector.select(shardKey)).execute(storageEntity);
                } else {
                    return BuildExecutor.build(
                        resource, indexWriteIndexNameSelector.select(shardKey)).execute(storageEntity);
                }
            }

            @Override
            public String key() {
                return shardKey;
            }
        });
    }

    @Override
    public boolean clean(long entityId, long maintainId, long start, long end) throws SQLException {
        checkId(entityId);
        List<EntityRef> entityRefs = new ArrayList<>();
        for (DataSource dataSource : writerDataSourceSelector.selects()) {
            for (String indexName : indexWriteIndexNameSelector.selects()) {
                Collection<EntityRef> refs =
                    MaintainTimeBetweenQueryExecutor.build(dataSource, indexName, entityId, maintainId, start, end).execute(1L);

                if (!refs.isEmpty()) {
                    entityRefs.addAll(refs);
                }
            }
        }

        /*
            考虑到重建索引后出现不一致的记录数量不会太多，目前采用逐个删除的策略
         */
        if (0 < entityRefs.size()) {
            logger.info("do function clean, some surplus indexes have been deleted, ids : {}", JSON.toJSON(entityRefs));
            for (EntityRef entityRef : entityRefs) {
                delete(new Entity(entityRef.getId(),
                    new AnyEntityClass(), new EntityValue(entityRef.getId()), OqsVersion.MAJOR));

                if (0 < entityRef.getCref()) {
                    delete(new Entity(entityRef.getCref(),
                        new AnyEntityClass(), new EntityValue(entityRef.getCref()), OqsVersion.MAJOR));
                } else if (0 < entityRef.getPref()) {
                    delete(new Entity(entityRef.getPref(),
                        new AnyEntityClass(), new EntityValue(entityRef.getPref()), OqsVersion.MAJOR));
                }
            }
        } else {
            logger.info("do function clean, no more surplus indexes have been deleted");
        }

        return true;
    }
}
