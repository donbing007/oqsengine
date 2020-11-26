package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql;

import com.alibaba.fastjson.JSON;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
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
import com.xforceplus.ultraman.oqsengine.storage.executor.DataSourceShardingResourceTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.ResourceTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.hint.ExecutorHint;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor.*;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.SphinxQLConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.value.LongStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactoryAble;
import io.vavr.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

import static com.xforceplus.ultraman.oqsengine.common.error.CommonErrors.INVALID_ENTITY_ID;
import static com.xforceplus.ultraman.oqsengine.common.error.CommonErrors.PARSE_COLUMNS_ERROR;

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

    @Resource(name = "indexSearchDataSource")
    private DataSource searchDataSource;

    @Resource(name = "storageSphinxQLTransactionExecutor")
    private TransactionExecutor transactionExecutor;

    @Resource(name = "indexStorageStrategy")
    private StorageStrategyFactory storageStrategyFactory;

    @Resource(name = "indexWriteIndexNameSelector")
    private Selector<String> indexWriteIndexNameSelector;

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
        Conditions conditions, IEntityClass entityClass, Sort sort, Page page, List<Long> filterIds, Long commitId)
        throws SQLException {

        return (Collection<EntityRef>) transactionExecutor.execute(new ResourceTask() {
            @Override
            public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                return QueryConditionExecutor.build(
                    searchIndexName,
                    resource,
                    sphinxQLConditionsBuilderFactory,
                    storageStrategyFactory,
                    maxSearchTimeoutMs).execute(
                    Tuple.of(entityClass, conditions, page, sort, filterIds, commitId));
            }

            @Override
            public DataSource getDataSource() {
                return searchDataSource;
            }
        });
    }

    @Override
    public void replaceAttribute(IEntityValue attribute) throws SQLException {
        checkId(attribute.id());

        final StorageEntity oldStorageEntity;
        oldStorageEntity = (StorageEntity) transactionExecutor.execute(new ResourceTask() {
            @Override
            public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                long dataId = attribute.id();
                Optional<StorageEntity> oldStorageEntityOptional =
                    QueryExecutor.build(resource, searchIndexName).execute(dataId);
                if (oldStorageEntityOptional.isPresent()) {
                    return oldStorageEntityOptional.get();
                } else {
                    return null;
                }
            }

            @Override
            public DataSource getDataSource() {
                return searchDataSource;
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
    public int buildOrReplace(StorageEntity storageEntity, IEntityValue entityValue, boolean replacement) throws SQLException {
        checkId(storageEntity.getId());
        try {
            //  jsonFields
            storageEntity.setJsonFields(serializeToMap(entityValue, true));
            // fullTexts
            storageEntity.setFullFields(serializeSetFull(entityValue));
        } catch (Exception e) {
            throw new SQLException(
                    String.format("serialize entityValue to jsonFields/fullTexts failed, message : %s", e.getMessage()), PARSE_COLUMNS_ERROR.name());
        }

        return doBuildReplaceStorageEntity(storageEntity, replacement);
    }

    @Override
    public int delete(long id) throws SQLException {
        checkId(id);

        String shardKey = Long.toString(id);
        return (int) transactionExecutor
            .execute(new DataSourceShardingResourceTask(writerDataSourceSelector, shardKey) {

                @Override
                public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                    return DeleteExecutor.build(resource, indexWriteIndexNameSelector.select(shardKey))
                        .execute(id);
                }
            });
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
    public Map<String, Object> serializeToMap(IEntityValue values, boolean encodeString) {
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
    private int doBuildReplaceStorageEntity(StorageEntity storageEntity, boolean replacement) throws SQLException {
        String shardKey = Long.toString(storageEntity.getId());
        return (int) transactionExecutor
            .execute(new DataSourceShardingResourceTask(writerDataSourceSelector, shardKey) {

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
            });
    }

    @Override
    public boolean clean(long entityId, long maintainId, long start, long end) throws SQLException {
        checkId(entityId);
        List<EntityRef> entityRefs = new ArrayList<>();
        for (DataSource dataSource : writerDataSourceSelector.selects()) {
            for (String indexName : indexWriteIndexNameSelector.selects()) {
                Collection<EntityRef> refs =
                        BatchQueryExecutor.build(dataSource, indexName, entityId, maintainId, start, end).execute(1L);

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
                        new AnyEntityClass(), new EntityValue(entityRef.getId())));

                if (0 < entityRef.getCref()) {
                    delete(new Entity(entityRef.getCref(),
                            new AnyEntityClass(), new EntityValue(entityRef.getCref())));
                } else if (0 < entityRef.getPref()) {
                    delete(new Entity(entityRef.getPref(),
                            new AnyEntityClass(), new EntityValue(entityRef.getPref())));
                }
            }
        } else {
            logger.info("do function clean, no more surplus indexes have been deleted");
        }

        return true;
    }
}
