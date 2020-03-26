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

import static com.xforceplus.ultraman.oqsengine.storage.master.constant.SQLConstant.*;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/25/2020 11:49 PM
 * 功能描述:
 * 修改历史:
 */
public class SQLMasterAction {

    final Logger logger = LoggerFactory.getLogger(SQLMasterAction.class);

    @Resource(name = "tableNameSelector")
    private Selector<String> tableNameSelector;

    @Resource(name = "masterStorageStrategy")
    private StorageStrategyFactory storageStrategyFactory;

    public Optional<IEntity> select(Connection connection, long id, IEntityClass entityClass) throws SQLException {
        String tableName = tableNameSelector.select(Long.toString(id));
        String sql = String.format(SELECT_SQL, tableName);

        PreparedStatement st = connection.prepareStatement(sql);
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

    public Collection<IEntity> select(Connection connection,
                                       String tableName, List<Long> partitionTableIds,
                                       Map<Long, IEntityClass> entityTable)
            throws SQLException {

        // 组织成 以逗号分隔的 id 字符串.
        String inSqlIds = partitionTableIds.stream().map(
                id -> id.toString()).collect(Collectors.joining(","));

        String sql = String.format(SELECT_IN_SQL, tableName, inSqlIds);
        PreparedStatement st = connection.prepareStatement(sql);
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

    public Map selectVersionTime(Connection connection, long sourceId, long targetId) throws SQLException {
        Map versionTime = new HashMap();

//        // 需要在内部类中修改,所以使用了引用类型.
//        final int[] newVersion = new int[1];
//        final long[] newTime = new long[1];

        String tableName = tableNameSelector.select(Long.toString(sourceId));
        String sql = String.format(SELECT_VERSION_TIME_SQL, tableName);

        PreparedStatement st = connection.prepareStatement(sql);
        st.setLong(1, sourceId);

        if (logger.isDebugEnabled()) {
            logger.debug(st.toString());
        }

        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            versionTime.put("version", rs.getInt(FieldDefine.VERSION));
            versionTime.put("time", rs.getLong(FieldDefine.TIME));
        } else {
            throw new SQLException(
                    String.format("Can not found data %d.", sourceId, targetId));
        }

        try {
            return versionTime;
        } finally {
            if (rs != null) {
                rs.close();
            }

            if (st != null) {
                st.close();
            }
        }
    }

    public Object replaceVersionTime(Connection connection, long sourceId, long targetId, int[] newVersion, long[] newTime) throws SQLException {
        String tableName = tableNameSelector.select(Long.toString(targetId));
        String sql = String.format(REPLACE_VERSION_TIME_SQL, tableName);

        PreparedStatement st = connection.prepareStatement(sql);
        st.setInt(1, newVersion[0]);
        st.setLong(2, newTime[0]);
        st.setLong(3, targetId);

        int size = st.executeUpdate();

        final int onlyOne = 1;
        if (size != onlyOne) {
            throw new SQLException(
                    String.format("Unable to synchronize information from %d to %d.", sourceId, targetId));
        }

        try {
            return null;
        } finally {
            if (st != null) {
                st.close();
            }
        }
    }

    public Object build(Connection connection, IEntity entity) throws SQLException {
        checkId(entity);

        String tableName = tableNameSelector.select(Long.toString(entity.id()));
        String sql = String.format(BUILD_SQL, tableName);

        PreparedStatement st = connection.prepareStatement(sql);
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

    public Object replace(Connection connection, IEntity entity) throws SQLException {
        checkId(entity);

        String tableName = tableNameSelector.select(Long.toString(entity.id()));
        String sql = String.format(REPLACE_SQL, tableName);
        PreparedStatement st = connection.prepareStatement(sql);

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

    public Object delete(Connection connection, IEntity entity) throws SQLException {
        checkId(entity);

        String tableName = tableNameSelector.select(Long.toString(entity.id()));
        String sql = String.format(DELETE_SQL, tableName);
        PreparedStatement st = connection.prepareStatement(sql);

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

    private Optional<IEntity> buildEntityFromResultSet(ResultSet rs, IEntityClass entityClass) throws SQLException {
        long dataEntityClassId = rs.getLong(FieldDefine.ENTITY);
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
