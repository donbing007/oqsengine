package com.xforceplus.ultraman.oqsengine.storage.master;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xforceplus.ultraman.oqsengine.pojo.dto.enums.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityFamily;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.ValueFactory;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.executor.DataSourceShardingTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.helper.StorageTypeHelper;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
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
        "insert into %s (id, entity, version, time, pref, cref, deleted, attribute,refs) values(?,?,?,?,?,?,?,?,?)";
    private static final String REPLACE_SQL =
        "update %s set version = version + 1, time = ?, attribute = ? where id = ? and version = ?";
    private static final String DELETE_SQL =
        "update %s set version = version + 1, deleted = ?, time = ? where id = ? and version = ?";
    private static final String SELECT_SQL =
        "select entity, version, time, pref, cref, deleted, attribute, refs from %s where id = ?";
    private static final String SELECT_IN_SQL =
        "select entity, version, time, pref, cref, deleted, attribute, refs from %s where id in (%s)";


    @Resource(name = "masterDataSourceSelector")
    private Selector<DataSource> dataSourceSelector;

    @Resource(name = "masterTableSelector")
    private Selector<String> tableNameSelector;

    @Resource
    private TransactionExecutor transactionExecutor;


    /**
     *
     * {
     *     "numberAttribute": 1, # 普通数字属性
     *     "stringAttribute": "value" # 普通字符串属性.
     * }
     */
    private IEntityValue toEntityValue(long id, IEntityClass entityClass, String json, FieldType rowFieldType) throws SQLException {
        JSONObject object = JSON.parseObject(json);

        Map<Long, Field> fieldMap = null;
        if (rowFieldType == null) {
             fieldMap = entityClass.fields()
                .stream().collect(Collectors.toMap(Field::getId, f -> f, (f0, f1) -> f0));
        }

        Field field = null;
        StorageType storageType;
        long jsonlongValue;
        String jsonStringValue;
        IEntityValue values = new EntityValue(id);
        FieldType fieldType;
        for (String fieldId : object.keySet()) {

            if (rowFieldType != null) {

                fieldType = rowFieldType;

            } else {

                field = fieldMap.get(fieldId);

                if (field == null) {
                    continue;
                }

                fieldType = field.getFieldType();

            }

            storageType = StorageTypeHelper.findStorageType(fieldType);
            switch(storageType) {
                case LONG: {
                    jsonlongValue = object.getLongValue(fieldId);
                    values.addValue(ValueFactory.buildValue(field, jsonlongValue));
                    break;
                }
                case STRING: {
                    jsonStringValue = object.getString(fieldId);
                    values.addValue(ValueFactory.buildValue(field, jsonStringValue));
                    break;
                }
                default: {
                    logger.warn("Unsupported storage properties.[entity:{}, class:{}, fieldId:{}]"
                        , id, entityClass.id(), field.getId());
                }
            }

        }

        return values;
    }

    // 属性名称使用的是属性 id.
    private String toJson(IEntityValue values) {

        JSONObject object = new JSONObject(values.values().stream().collect(
            Collectors.toMap(
                v -> Long.toString(v.getField().getId()),
                IValue::getValue,
                (v0, v1) -> v0)));
        return object.toJSONString();

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

                    ResultSet rs = st.executeQuery();
                    // entity, version, time, pref, cref, deleted, attribute, refs
                    if (rs.next()) {
                        long dataEntityClassId = rs.getLong("entity");
                        if (entityClass.id() != dataEntityClassId) {
                            throw new SQLException(
                                String.format(
                                    "The incorrect Entity type is expected to be %d, but the actual data type is %d."
                                    ,entityClass.id(), dataEntityClassId));
                        }

                        Entity entity = new Entity(
                            id,
                            entityClass,
                            toEntityValue(id, entityClass, rs.getString("attribute"), null),
                            toEntityValue(id, entityClass, rs.getString("refs"), FieldType.LONG),
                            new EntityFamily(rs.getLong("pref"), rs.getLong("cref")),
                            rs.getInt("version")
                        );

                        return Optional.of(entity);
                    } else {
                        return Optional.empty();
                    }
                }
            });
    }

    @Override
    public Collection<IEntity> selectMultiple(Map<IEntityClass, int[]> ids) throws SQLException {
        //TODO: 还未实现. by dongbin 2020/02/19
        return Collections.emptyList();
    }


    @Override
    public void build(IEntity entity) throws SQLException {
        checkId(entity);

        transactionExecutor.execute(
            new DataSourceShardingTask(dataSourceSelector, Long.toString(entity.id())) {

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
                    st.setString(8,toJson(entity.entityValue())); // attribute
                    st.setString(9, toJson(entity.refs())); // refs

                    int size = st.executeUpdate();

                    /**
                     * 插入影响条件恒定为1.
                     */
                    final int onlyOne = 1;
                    if (size != onlyOne) {
                        throw new SQLException(
                            String.format("Entity{%s} could not be created successfully.", entity.toString()));
                    }

                    return null;
                }
            });
    }

    @Override
    public void replace(IEntity entity) throws SQLException {
        checkId(entity);

        transactionExecutor.execute(new DataSourceShardingTask(dataSourceSelector, Long.toString(entity.id())) {

            @Override
            public Object run(TransactionResource resource) throws SQLException {
                String tableName = tableNameSelector.select(Long.toString(entity.id()));
                String sql = String.format(REPLACE_SQL, tableName);
                PreparedStatement st = ((Connection) resource.value()).prepareStatement(sql);

                // time attribute id version";
                st.setLong(1, System.currentTimeMillis()); // time
                st.setString(2, toJson(entity.entityValue())); // attribute
                st.setLong(3, entity.id()); // id
                st.setInt(4, entity.version()); // version

                int size = st.executeUpdate();
                final int onlyOne = 1;
                if (size != onlyOne) {
                    throw new SQLException(String.format("Entity{%s} could not be replace successfully.", entity.toString()));
                }

                return null;
            }
        });
    }

    @Override
    public void delete(IEntity entity) throws SQLException {
        checkId(entity);

        transactionExecutor.execute(new DataSourceShardingTask(dataSourceSelector, Long.toString(entity.id())) {

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

                int size = st.executeUpdate();
                final int onlyOne = 1;
                if (size != onlyOne) {
                    throw new SQLException(String.format("Entity{%s} could not be delete successfully.", entity.toString()));
                }

                return null;
            }
        });
    }

    private void checkId(IEntity entity) throws SQLException {
        if (entity.id() == 0) {
            throw new SQLException("Invalid entity`s id.");
        }
    }
}
