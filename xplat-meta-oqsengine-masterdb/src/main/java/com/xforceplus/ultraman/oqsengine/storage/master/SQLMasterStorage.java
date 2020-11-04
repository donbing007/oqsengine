package com.xforceplus.ultraman.oqsengine.storage.master;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityFamily;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.executor.DataSourceShardingTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.hint.ExecutorHint;
import com.xforceplus.ultraman.oqsengine.storage.master.define.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.*;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.value.AnyStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValueFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
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

    private final static String ATTRIBUTE_PREFIX = "F";

    @Resource(name = "masterDataSourceSelector")
    private Selector<DataSource> dataSourceSelector;

    @Resource(name = "tableNameSelector")
    private Selector<String> tableNameSelector;

    @Resource(name = "storageJDBCTransactionExecutor")
    private TransactionExecutor transactionExecutor;

    @Resource(name = "masterStorageStrategy")
    private StorageStrategyFactory storageStrategyFactory;

    @Resource(name = "ioThreadPool")
    private ExecutorService threadPool;

    private long queryTimeout;

    public void setQueryTimeout(long queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    @Override
    @PostConstruct
    public void init() {

        if (queryTimeout <= 0) {
            setQueryTimeout(3000L);
        }
    }

    @Override
    public Optional<IEntity> select(long id, IEntityClass entityClass) throws SQLException {
        return (Optional<IEntity>) transactionExecutor.execute(
            new DataSourceShardingTask(dataSourceSelector, Long.toString(id)) {

                @Override
                public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                    Optional<StorageEntity> seOP = QueryExecutor.buildHaveDetail(tableNameSelector, resource).execute(id);
                    if (seOP.isPresent()) {
                        return buildEntityFromStorageEntity(seOP.get(), entityClass);
                    } else {
                        return Optional.empty();
                    }
                }
            });
    }

    @Override
    public Collection<IEntity> selectMultiple(Map<Long, IEntityClass> ids) throws SQLException {
        Map<DataSource, List<Long>> groupedMap = ids.keySet().stream().collect(
            Collectors.groupingBy(id -> dataSourceSelector.select(Long.toString(id))));

        CountDownLatch latch = new CountDownLatch(groupedMap.keySet().size());
        List<Future> futures = new ArrayList(groupedMap.keySet().size());

        for (List<Long> groupedIds : groupedMap.values()) {
            futures.add(threadPool.submit(new MultipleSelectCallable(latch, groupedIds, ids)));
        }

        try {
            if (!latch.await(queryTimeout, TimeUnit.MILLISECONDS)) {

                for (Future f : futures) {
                    f.cancel(true);
                }

                throw new SQLException("Query failed, timeout.");
            }
        } catch (InterruptedException e) {
            throw new SQLException(e.getMessage(), e);
        }

        List<IEntity> results = new ArrayList<>(ids.size());
        for (Future<Collection<IEntity>> f : futures) {
            try {
                results.addAll(f.get());
            } catch (Exception e) {
                throw new SQLException(e.getMessage(), e);
            }
        }

        return results;
    }

    @Override
    public int synchronize(long sourceId, long targetId) throws SQLException {
        // 需要在内部类中修改,所以使用了引用类型.

        Optional<StorageEntity> oldOp = (Optional<StorageEntity>) transactionExecutor.execute(
            new DataSourceShardingTask(dataSourceSelector, Long.toString(sourceId)) {
                @Override
                public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                    return QueryExecutor.buildNoDetail(tableNameSelector, resource).execute(sourceId);
                }
            }
        );
        if (oldOp.isPresent()) {

            return (int) transactionExecutor.execute(
                new DataSourceShardingTask(dataSourceSelector, Long.toString(targetId)) {
                    @Override
                    public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                        StorageEntity targetEntity = oldOp.get();
                        targetEntity.setId(targetId);

                        return UpdateVersionAndTxExecutor.build(tableNameSelector, resource).execute(targetEntity);
                    }
                }

            );
        } else {

            return 0;
        }

    }

    @Override
    public int build(IEntity entity) throws SQLException {
        checkId(entity);

        return (int) transactionExecutor.execute(
            new DataSourceShardingTask(
                dataSourceSelector, Long.toString(entity.id())) {

                @Override
                public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                    StorageEntity storageEntity = new StorageEntity();
                    storageEntity.setId(entity.id());
                    storageEntity.setEntity(entity.entityClass().id());
                    storageEntity.setPref(entity.family().parent());
                    storageEntity.setCref(entity.family().child());
                    storageEntity.setTime(entity.time());
                    storageEntity.setAttribute(toJson(entity.entityValue()));
                    storageEntity.setMeta(buildSearchAbleSyncMeta(entity.entityClass()));

                    return BuildExecutor.build(tableNameSelector, resource).execute(storageEntity);
                }
            });
    }

    @Override
    public int replace(IEntity entity) throws SQLException {
        checkId(entity);

        return (int) transactionExecutor.execute(
            new DataSourceShardingTask(
                dataSourceSelector, Long.toString(entity.id())) {

                @Override
                public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                    StorageEntity storageEntity = new StorageEntity();
                    storageEntity.setId(entity.id());
                    storageEntity.setEntity(entity.entityClass().id());
                    storageEntity.setVersion(entity.version());
                    storageEntity.setPref(entity.family().parent());
                    storageEntity.setCref(entity.family().child());
                    storageEntity.setTime(entity.time());
                    storageEntity.setAttribute(toJson(entity.entityValue()));
                    storageEntity.setMeta(buildSearchAbleSyncMeta(entity.entityClass()));

                    return ReplaceExecutor.build(tableNameSelector, resource).execute(storageEntity);

                }
            });
    }

    @Override
    public int delete(IEntity entity) throws SQLException {
        checkId(entity);

        return (int) transactionExecutor.execute(
            new DataSourceShardingTask(
                dataSourceSelector, Long.toString(entity.id())) {

                @Override
                public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                    StorageEntity storageEntity = new StorageEntity();
                    storageEntity.setId(entity.id());
                    storageEntity.setVersion(entity.version());
                    storageEntity.setTime(entity.time());
                    /**
                     * 删除数据时不再关心字段信息.
                     */

                    return DeleteExecutor.build(tableNameSelector, resource).execute(storageEntity);
                }
            });
    }

    private void checkId(IEntity entity) throws SQLException {
        if (entity.id() == 0) {
            throw new SQLException("Invalid entity`s id.");
        }
    }

    /**
     * {
     * "numberAttribute": 1, # 普通数字属性
     * "stringAttribute": "value" # 普通字符串属性.
     * }
     */
    private IEntityValue toEntityValue(long id, IEntityClass entityClass, String json) throws SQLException {
        JSONObject object = JSON.parseObject(json);

        // 以字段逻辑名称为 key, 字段信息为 value.
        Map<String, IEntityField> fieldTable = entityClass.fields()
            .stream().collect(Collectors.toMap(f -> Long.toString(f.id()), f -> f, (f0, f1) -> f0));

        String logicName;
        IEntityField field = null;
        FieldType fieldType;
        StorageStrategy storageStrategy;
        StorageValue newStorageValue;
        StorageValue oldStorageValue;
        // key 为物理储存名称,值为构造出的储存值.
        Map<String, EntityValuePack> storageValueCache = new HashMap<>(object.size());

        String sn;
        for (String storageName : object.keySet()) {
            sn = compatibleStorageName(storageName);
//            try {

                // 为了找出物理名称中的逻辑字段名称.
            logicName = AnyStorageValue.getInstance(sn).logicName();
                field = fieldTable.get(logicName);

                if (field == null) {
                    continue;
                }

                fieldType = field.type();

                storageStrategy = this.storageStrategyFactory.getStrategy(fieldType);
                newStorageValue = StorageValueFactory.buildStorageValue(
                    storageStrategy.storageType(), sn, object.get(storageName));

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

//            } catch (Exception ex) {
//                throw new SQLException(
//                    String.format("Something wrong has occured.[entity:%d, class: %d, fieldId: %d, msg:%s]",
//                        id, entityClass.id(), field.id(), ex.getClass().getName() + ":" + ex.getMessage()));
//            }
        }

        IEntityValue values = new EntityValue(id);
        storageValueCache.values().stream().forEach(e -> {
            values.addValue(e.strategy.toLogicValue(e.logicField, e.storageValue));
        });


        return values;
    }

    // toEntity 临时解析结果.
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

    // 属性名称使用的是属性 F + {id}.
    private String toJson(IEntityValue value) {

        JSONObject object = new JSONObject();
        StorageStrategy storageStrategy;
        StorageValue storageValue;
        for (IValue logicValue : value.values()) {
            storageStrategy = storageStrategyFactory.getStrategy(logicValue.getField().type());
            storageValue = storageStrategy.toStorageValue(logicValue);
            while (storageValue != null) {
                object.put(ATTRIBUTE_PREFIX + storageValue.storageName(), storageValue.value());
                storageValue = storageValue.next();
            }
        }
        return object.toJSONString();

    }

    /**
     * 多重 id 查询任务,每一个任务表示一个分库的查询任务.
     */
    private class MultipleSelectCallable implements Callable<Collection<IEntity>> {

        private CountDownLatch latch;
        // 按照表名分区的 id.
        private Map<String, List<Long>> ids;
        // 目标 id总量.
        private int size;
        // id 对应 entityClass 速查表.
        private Map<Long, IEntityClass> entityTable;

        private String dataSourceShardKey;

        public MultipleSelectCallable(CountDownLatch latch, List<Long> ids, Map<Long, IEntityClass> entityTable) {
            this.latch = latch;
            this.entityTable = entityTable;
            // 按表区分.
            this.ids = ids.stream().collect(Collectors.groupingBy(id -> tableNameSelector.select(id.toString())));
            size = ids.size();

            dataSourceShardKey = Long.toString(ids.get(0));
        }

        @Override
        public Collection<IEntity> call() throws Exception {
            try {
                return (Collection<IEntity>) transactionExecutor.execute(
                    new DataSourceShardingTask(
                        dataSourceSelector, dataSourceShardKey) {

                        @Override
                        public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                            List<IEntity> entities = new ArrayList(size);
                            for (String table : ids.keySet()) {
                                entities.addAll(select(table, ids.get(table), resource));
                            }

                            return entities;
                        }

                        private Collection<IEntity> select(
                            String tableName, List<Long> partitionTableIds, TransactionResource res)
                            throws SQLException {

                            List<StorageEntity> storageEntities =
                                (List<StorageEntity>) MultipleQueryExecutor.build(tableName, res).execute(partitionTableIds);
                            List<IEntity> entities = new ArrayList<>(storageEntities.size());
                            Optional<IEntity> enOp;
                            for (StorageEntity storageEntity : storageEntities) {
                                enOp = buildEntityFromStorageEntity(storageEntity, entityTable.get(storageEntity.getId()));
                                if (enOp.isPresent()) {
                                    entities.add(enOp.get());
                                }
                            }

                            return entities;
                        }
                    });
            } finally {
                latch.countDown();
            }
        }
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
            se.getVersion()
        );
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
    private String buildSearchAbleSyncMeta(IEntityClass entityClass) {
        return "[" + entityClass.fields().stream()
            .filter(f -> f.config().isSearchable())
            .map(f -> "\"" + String.join("-", Long.toString(f.id()), f.type().getType()) + "\"")
            .collect(Collectors.joining(",")) + "]";
    }

    /**
     * 兼容旧版本属性名称.
     * 如果首字母是
     *
     * @param name
     * @return
     */
    private String compatibleStorageName(String name) {
        if (name.startsWith(ATTRIBUTE_PREFIX)) {
            return name.substring(1);
        } else {
            return name;
        }
    }
}
