package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql;

import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.executor.DataSourceShardingTask;
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

    @Resource(name = "indexSearchDataSourceSelector")
    private Selector<DataSource> searchDataSourceSelector;

    @Resource(name = "storageSphinxQLTransactionExecutor")
    private TransactionExecutor transactionExecutor;

    @Resource(name = "indexStorageStrategy")
    private StorageStrategyFactory storageStrategyFactory;

    private String indexTableName;

    // 最大查询超时时间,默认为无限.
    private long maxQueryTimeMs = 0;

    public long getMaxQueryTimeMs() {
        return maxQueryTimeMs;
    }

    public void setMaxQueryTimeMs(long maxQueryTimeMs) {
        this.maxQueryTimeMs = maxQueryTimeMs;
    }

    public void setIndexTableName(String indexTableName) {
        this.indexTableName = indexTableName;
    }

    public String getIndexTableName() {
        return indexTableName;
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
    public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, Sort sort, Page page
            , List<Long> filterIds, Long commitId)
            throws SQLException {

        return (Collection<EntityRef>) transactionExecutor
                .execute(new DataSourceShardingTask(searchDataSourceSelector, Long.toString(entityClass.id())) {
                    @Override
                    public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                        return QueryConditionExecutor.build(indexTableName, resource, sphinxQLConditionsBuilderFactory, storageStrategyFactory, maxQueryTimeMs)
                                .execute(Tuple.of(entityClass.id(), conditions, page, sort, filterIds, commitId));
                    }
                });
    }

    @Override
    public void replaceAttribute(IEntityValue attribute) throws SQLException {
        checkId(attribute.id());

        transactionExecutor
                .execute(new DataSourceShardingTask(writerDataSourceSelector, Long.toString(attribute.id())) {

                    @Override
                    public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                        long dataId = attribute.id();

                        Optional<StorageEntity> oldStorageEntityOptional = QueryExecutor.build(resource, indexTableName).execute(dataId);

                        if (oldStorageEntityOptional.isPresent()) {

                            StorageEntity storageEntity = oldStorageEntityOptional.get();

                            /**
                             * 把新的属性插入旧属性集中替换已有,或新增.
                             */
                            Map<String, Object> completeAttribues = storageEntity.getJsonFields();
                            Map<String, Object> modifiedAttributes = serializeToMap(attribute, true);
                            completeAttribues.putAll(modifiedAttributes);

                            // 处理 fulltext
                            storageEntity.setJsonFields(completeAttribues);
                            storageEntity.setFullFields(convertJsonToFull(completeAttribues));

                            doBuildReplaceStorageEntity(storageEntity, true);

                        } else {

                            throw new SQLException(String
                                    .format("Attempt to update a property on a data that does not exist.[%d]", dataId));

                        }

                        return null;
                    }
                });
    }

    @Override
    public int build(IEntity entity) throws SQLException {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public int replace(IEntity entity) throws SQLException {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public int buildOrReplace(StorageEntity storageEntity, IEntityValue entityValue, boolean replacement) throws SQLException {
        checkId(storageEntity.getId());

        //  jsonFields
        storageEntity.setJsonFields(serializeToMap(entityValue, true));
        // fullTexts
        storageEntity.setFullFields(serializeSetFull(entityValue));

        return doBuildReplaceStorageEntity(storageEntity, replacement);
    }

    @Override
    public int delete(long id) throws SQLException {
        checkId(id);

        return (int) transactionExecutor
                .execute(new DataSourceShardingTask(writerDataSourceSelector, Long.toString(id)) {

                    @Override
                    public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                        return DeleteExecutor.build(resource, indexTableName)
                                .execute(id);
                    }
                });
    }

    @Override
    public int delete(IEntity entity) throws SQLException {
        checkId(entity.id());

        return (int) transactionExecutor
                .execute(new DataSourceShardingTask(writerDataSourceSelector, Long.toString(entity.id())) {

                    @Override
                    public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                        return DeleteExecutor.build(resource, indexTableName)
                                .execute(entity.id());
                    }
                });

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
            throw new SQLException("Invalid entity`s id.");
        }
    }


//    // 更新原始数据.
    private int doBuildReplaceStorageEntity(StorageEntity storageEntity, boolean replacement) throws SQLException {
        return (int) transactionExecutor
                .execute(new DataSourceShardingTask(writerDataSourceSelector, Long.toString(storageEntity.getId())) {

                    @Override
                    public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                        if (replacement) {
                            return ReplaceExecutor.build(resource, indexTableName).execute(storageEntity);
                        } else {
                            return BuildExecutor.build(resource, indexTableName).execute(storageEntity);
                        }
                    }
                });
    }


    //    // 查询原始数据.
//    private Optional<StorageEntity> doSelectStorageEntity(long id) throws SQLException {
//        return (Optional<StorageEntity>) transactionExecutor
//                .execute(new DataSourceShardingTask(searchDataSourceSelector, Long.toString(id)) {
//
//                    @Override
//                    public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
//                        StorageEntity storageEntity = new SelectByIdStorageCommand(indexTableName).execute(resource,
//                                id);
//
//                        return Optional.ofNullable(storageEntity);
//                    }
//                });
//    }

//    private int doBuildOrReplace(IEntity entity, boolean replacement) throws SQLException {
//        checkId(entity.id());
//
//        return doBuildReplaceStorageEntity(new StorageEntity(entity.id(), entity.entityClass().id(),
//            entity.family().parent(), entity.family().child(), serializeToMap(entity.entityValue(), true),
//            serializeSetFull(entity.entityValue())), replacement);
//    }

}
