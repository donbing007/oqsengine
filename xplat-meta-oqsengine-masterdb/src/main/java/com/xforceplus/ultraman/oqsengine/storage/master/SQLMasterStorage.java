package com.xforceplus.ultraman.oqsengine.storage.master;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityFamily;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.executor.DataSourceShardingTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.value.AnyStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValueFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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


    private static final String BUILD_SQL =
        "insert into %s (id, entity, version, time, pref, cref, deleted, attribute) values(?,?,?,?,?,?,?,?)";
    private static final String REPLACE_SQL =
        "update %s set version = version + 1, time = ?, attribute = ? where id = ? and version = ?";
    private static final String DELETE_SQL =
        "update %s set version = version + 1, deleted = ?, time = ? where id = ? and version = ?";
    private static final String SELECT_SQL =
        "select id, entity, version, time, pref, cref, deleted, attribute from %s where id = ? and deleted = false";
    private static final String SELECT_IN_SQL =
        "select id, entity, version, time, pref, cref, deleted, attribute from %s where id in (%s) and deleted = false";

    @Resource(name = "masterDataSourceSelector")
    private Selector<DataSource> dataSourceSelector;

    @Resource(name = "tableNameSelector")
    private Selector<String> tableNameSelector;

    @Resource(name = "storageJDBCTransactionExecutor")
    private TransactionExecutor transactionExecutor;

    @Resource(name = "masterStorageStrategy")
    private StorageStrategyFactory storageStrategyFactory;

    private long queryTimeout;

    private int workerSize;

    public void setQueryTimeout(long queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public void setWorkerSize(int workerSize) {
        this.workerSize = workerSize;
    }

    /**
     * 工作者线程.
     */
    private ExecutorService worker;

    @PostConstruct
    public void init() {
        if (workerSize <= 0) {
            setWorkerSize(Runtime.getRuntime().availableProcessors());
        }

        if (queryTimeout <= 0) {
            setQueryTimeout(3000L);
        }

        worker = new ThreadPoolExecutor(workerSize, workerSize,
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(500),
            ExecutorHelper.buildNameThreadFactory("Master-worker", false));
    }

    @PreDestroy
    public void destroy() {
        ExecutorHelper.shutdownAndAwaitTermination(worker);
    }


    @Override
    public Optional<IEntity> select(long id, IEntityClass entityClass) throws SQLException {
        return (Optional<IEntity>) transactionExecutor.execute(
            new DataSourceShardingTask(dataSourceSelector, Long.toString(id)) {

                @Override
                public Object run(TransactionResource resource) throws SQLException {
                    String tableName = tableNameSelector.select(Long.toString(id));
                    String sql = String.format(SELECT_SQL, tableName);

                    PreparedStatement st = ((Connection) resource.value()).prepareStatement(sql);
                    st.setLong(1, id); // id

                    if (logger.isDebugEnabled()) {
                        logger.debug(st.toString());
                    }

                    ResultSet rs = st.executeQuery();
                    // entity, version, time, pref, cref, deleted, attribute, refs

                    try {
                        if (rs.next()) {

                            return buildEntityFromResultSet(rs, entityClass);

                        } else {
                            return Optional.empty();
                        }
                    } finally {
                        if (rs != null) {
                            rs.close();
                        }

                        if (st != null) {
                            st.close();
                        }
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
            futures.add(worker.submit(new MultipleSelectCallable(latch, groupedIds, ids)));
        }

        try {
            if (!latch.await(queryTimeout, TimeUnit.MILLISECONDS)) {
                throw new SQLException("Query failed, timeout.");
            }
        } catch (InterruptedException e) {
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
    public void build(IEntity entity) throws SQLException {
        checkId(entity);

        transactionExecutor.execute(
            new DataSourceShardingTask(
                dataSourceSelector, Long.toString(entity.id())) {

                @Override
                public Object run(TransactionResource resource) throws SQLException {
                    String tableName = tableNameSelector.select(Long.toString(entity.id()));
                    String sql = String.format(BUILD_SQL, tableName);

                    PreparedStatement st = ((Connection) resource.value()).prepareStatement(sql);
                    // id, entity, version, time, pref, cref, deleted, attribute,refs
                    st.setLong(1, entity.id()); // id
                    st.setLong(2, entity.entityClass().id()); // entity
                    st.setInt(3, 0); // version
                    st.setLong(4, System.currentTimeMillis()); // time
                    st.setLong(5, entity.family().parent()); // pref
                    st.setLong(6, entity.family().child()); // cref
                    st.setBoolean(7, false); // deleted
                    st.setString(8, toJson(entity.entityValue())); // attribute

                    if (logger.isDebugEnabled()) {
                        logger.debug(st.toString());
                    }

                    int size = st.executeUpdate();

                    /**
                     * 插入影响条件恒定为1.
                     */
                    final int onlyOne = 1;
                    if (size != onlyOne) {
                        throw new SQLException(
                            String.format("Entity{%s} could not be created successfully.", entity.toString()));
                    }

                    try {
                        return null;
                    } finally {
                        if (st != null) {
                            st.close();
                        }
                    }
                }
            });
    }

    @Override
    public void replace(IEntity entity) throws SQLException {
        checkId(entity);

        transactionExecutor.execute(
            new DataSourceShardingTask(
                dataSourceSelector, Long.toString(entity.id())) {

                @Override
                public Object run(TransactionResource resource) throws SQLException {
                    String tableName = tableNameSelector.select(Long.toString(entity.id()));
                    String sql = String.format(REPLACE_SQL, tableName);
                    PreparedStatement st = ((Connection) resource.value()).prepareStatement(sql);

                    // update %s set version = version + 1, time = ?, attribute = ? where id = ? and version = ?";
                    st.setLong(1, System.currentTimeMillis()); // time
                    st.setString(2, toJson(entity.entityValue())); // attribute
                    st.setLong(3, entity.id()); // id
                    st.setInt(4, entity.version()); // version

                    if (logger.isDebugEnabled()) {
                        logger.debug(st.toString());
                    }

                    int size = st.executeUpdate();

                    final int onlyOne = 1;
                    if (size != onlyOne) {
                        throw new SQLException(String.format("Entity{%s} could not be replace successfully.", entity.toString()));
                    }

                    try {
                        return null;
                    } finally {
                        if (st != null) {
                            st.close();
                        }
                    }
                }
            });
    }

    @Override
    public void delete(IEntity entity) throws SQLException {
        checkId(entity);

        transactionExecutor.execute(
            new DataSourceShardingTask(
                dataSourceSelector, Long.toString(entity.id())) {

                @Override
                public Object run(TransactionResource resource) throws SQLException {
                    String tableName = tableNameSelector.select(Long.toString(entity.id()));
                    String sql = String.format(DELETE_SQL, tableName);
                    PreparedStatement st = ((Connection) resource.value()).prepareStatement(sql);

                    // deleted time id version;
                    st.setBoolean(1, true); // deleted
                    st.setLong(2, System.currentTimeMillis()); // time
                    st.setLong(3, entity.id()); // id
                    st.setInt(4, entity.version()); // version

                    if (logger.isDebugEnabled()) {
                        logger.debug(st.toString());
                    }

                    int size = st.executeUpdate();
                    final int onlyOne = 1;
                    if (size != onlyOne) {
                        throw new SQLException(String.format("Entity{%s} could not be delete successfully.", entity.toString()));
                    }

                    try {
                        return null;
                    } finally {
                        if (st != null) {
                            st.close();
                        }
                    }
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

        for (String storageName : object.keySet()) {
            try {

                // 为了找出物理名称中的逻辑字段名称.
                logicName = AnyStorageValue.getInstance(storageName).logicName();
                field = fieldTable.get(logicName);

                if (field == null) {
                    continue;
                }

                fieldType = field.type();

                storageStrategy = this.storageStrategyFactory.getStrategy(fieldType);
                newStorageValue = StorageValueFactory.buildStorageValue(
                    storageStrategy.storageType(), storageName, object.get(storageName));

                // 如果是多值.使用 stick 追加.
                if (storageStrategy.isMultipleStorageValue()) {
                    Optional<StorageValue> oldStorageValueOp = Optional.ofNullable(storageValueCache.get(String.valueOf(field.id()))).map(x -> x.storageValue);

                    if (oldStorageValueOp.isPresent()) {
                        oldStorageValue = oldStorageValueOp.get();
                        storageValueCache.put(String.valueOf(field.id()), new EntityValuePack(field,  oldStorageValue.stick(newStorageValue), storageStrategy));
                    } else {
                        storageValueCache.put(String.valueOf(field.id()), new EntityValuePack(field, newStorageValue, storageStrategy));
                    }
                } else {
                    // 单值
                    storageValueCache.put(String.valueOf(field.id()), new EntityValuePack(field, newStorageValue, storageStrategy));
                }

            } catch (Exception ex) {
                throw new SQLException(
                    String.format("Something wrong has occured.[entity:%d, class: %d, fieldId: %d, msg:%s]",
                        id, entityClass.id(), field.id(), ex.getMessage()));
            }
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

    // 属性名称使用的是属性 id.
    private String toJson(IEntityValue value) {

        JSONObject object = new JSONObject();
        StorageStrategy storageStrategy;
        StorageValue storageValue;
        for (IValue logicValue : value.values()) {
            storageStrategy = storageStrategyFactory.getStrategy(logicValue.getField().type());
            storageValue = storageStrategy.toStorageValue(logicValue);
            while (storageValue != null) {
                object.put(storageValue.storageName(), storageValue.value());
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
                        public Object run(TransactionResource resource) throws SQLException {
                            List<IEntity> entities = new ArrayList(size);
                            for (String table : ids.keySet()) {
                                entities.addAll(select(table, ids.get(table), resource));
                            }

                            return entities;
                        }

                        private Collection<IEntity> select(
                            String tableName, List<Long> partitionTableIds, TransactionResource res)
                            throws SQLException {

                            // 组织成 以逗号分隔的 id 字符串.
                            String inSqlIds = partitionTableIds.stream().map(
                                id -> id.toString()).collect(Collectors.joining(","));

                            String sql = String.format(SELECT_IN_SQL, tableName, inSqlIds);
                            PreparedStatement st = ((Connection) res.value()).prepareStatement(sql);
                            ResultSet rs = st.executeQuery();

                            if (logger.isDebugEnabled()) {
                                logger.debug(st.toString());
                            }

                            List<IEntity> entities = new ArrayList<>(partitionTableIds.size());

                            while (rs.next()) {
                                long id = rs.getLong(FieldDefine.ID);
                                entities.add(buildEntityFromResultSet(rs, entityTable.get(id)).get());
                            }

                            try {
                                return entities;
                            } finally {
                                if (rs != null) {
                                    rs.close();
                                }

                                if (st != null) {
                                    st.close();
                                }
                            }
                        }
                    });
            } finally {
                latch.countDown();
            }
        }
    }

    private Optional<IEntity> buildEntityFromResultSet(ResultSet rs, IEntityClass entityClass) throws SQLException {
        long dataEntityClassId = rs.getLong("entity");
        if (entityClass.id() != dataEntityClassId) {
            throw new SQLException(
                String.format(
                    "The incorrect Entity type is expected to be %d, but the actual data type is %d."
                    , entityClass.id(), dataEntityClassId));
        }

        long id = rs.getLong(FieldDefine.ID);
        Entity entity = new Entity(
            id,
            entityClass,
            toEntityValue(rs.getLong(FieldDefine.ID), entityClass, rs.getString(FieldDefine.ATTRIBUTE)),
            new EntityFamily(rs.getLong(FieldDefine.PREF), rs.getLong(FieldDefine.CREF)),
            rs.getInt(FieldDefine.VERSION)
        );

        return Optional.of(entity);
    }
}
