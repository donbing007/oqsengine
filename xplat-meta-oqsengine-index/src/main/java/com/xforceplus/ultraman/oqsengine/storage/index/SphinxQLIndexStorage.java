package com.xforceplus.ultraman.oqsengine.storage.index;

import com.alibaba.fastjson.JSON;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.interfaces.IConditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.executor.DataSourceShardingTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.helper.StorageTypeHelper;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 基于 SphinxQL 的索引储存实现.
 *
 * @author dongbin
 * @version 0.1 2020/2/17 17:16
 * @since 1.8
 */
public class SphinxQLIndexStorage implements IndexStorage {

    private static final String BUILD_SQL = "insert into oqsindex (id, entity, pref, cref, numerfields, stringfields) values(?,?,?,?,?,?)";
    private static final String REPLACE_SQL = "replace into oqsindex (id, entity, pref, cref, numerfields, stringfields) values(?,?,?,?,?,?)";
    private static final String DELETE_SQL = "delete from oqsindex where id = ?";
    private static final String SELECT_SQL = "select entity, version, time, status, data from oqsindex where id = ?";

    @Resource(name = "indexDataSourceSelector")
    private Selector<DataSource> dataSourceSelector;

    @Resource
    private TransactionExecutor transactionExecutor;

    @Override
    public Collection<EntityRef> select(IConditions conditions, IEntityClass entityClass, Page page) {
        return null;
    }

    @Override
    public void build(IEntity entity) throws SQLException {
        doBuildOrReplace(entity, false);
    }

    @Override
    public void replace(IEntity entity) throws SQLException {
        doBuildOrReplace(entity, true);
    }

    @Override
    public void delete(IEntity entity) throws SQLException {
        checkId(entity);

        transactionExecutor.execute(new DataSourceShardingTask(dataSourceSelector, Long.toString(entity.id())) {

            @Override
            public Object run(TransactionResource resource) throws SQLException {

                PreparedStatement st = ((Connection) resource.value()).prepareStatement(DELETE_SQL);
                st.setLong(1, entity.id()); // id

                int size = st.executeUpdate();
                final int onlyOne = 1;
                if (size != onlyOne) {
                    throw new SQLException(String.format("Entity{%s} could not be delete successfully.", entity.toString()));
                }

                return null;
            }
        });

    }

    private void doBuildOrReplace(IEntity entity, boolean replacement) throws SQLException {
        checkId(entity);
        final String sql = replacement ? REPLACE_SQL : BUILD_SQL;

        transactionExecutor.execute(
            new DataSourceShardingTask(dataSourceSelector, Long.toString(entity.id())) {

                @Override
                public Object run(TransactionResource resource) throws SQLException {
                    PreparedStatement st = ((Connection) resource.value()).prepareStatement(sql);

                    // id, entity, pref, cref, numerfields, stringfields
                    st.setLong(1, entity.id()); // id
                    st.setLong(2, entity.entityClass().id()); // entity
                    st.setLong(3, entity.family().parent()); // pref
                    st.setLong(4, entity.family().child()); // cref
                    st.setString(5, serialize(entity.entityValue(), entity.entityClass(), StorageType.LONG)); // numberfields
                    st.setString(6, serialize(entity.entityValue(), entity.entityClass(), StorageType.STRING)); // stringfields
                    int size = st.executeUpdate();

                    // 成功只应该有一条语句影响
                    final int onlyOne = 1;
                    if (size == onlyOne) {
                        return entity.id();
                    } else {
                        throw new SQLException(
                            String.format(
                                "Entity{%s} could not be %s successfully.",
                                entity.toString(),
                                replacement ? "replace" : "build"
                            ));
                    }
                }
            });
    }

    /**
     * string f{fieldID}{fieldValue|unicode} f{fieldID}{fieldValue|unicode}
     * number
     * {
     * "{fieldId}" : fieldValue
     * }
     */
    private String serialize(IEntityValue values, IEntityClass entityClass, StorageType watchStorageType) {
        // key = fieldId
        Map<String, String> data = values.values().stream().filter(v -> {
            Optional<Field> field = entityClass.field(v.getField().getId());
            if (!field.isPresent()) {
                return false;
            } else {
                StorageType storageType = StorageTypeHelper.findStorageType(field.get().getFieldType());
                return watchStorageType == storageType;
            }
        }).collect(Collectors.toMap(
            v -> Long.toString(v.getField().getId()),
            v -> {
                if (watchStorageType == StorageType.STRING) {
                    return v.valueToString();
                } else {
                    return Long.toString(v.valueToLong());
                }
            },
            (v0, v1) -> v0));

        if (StorageType.LONG == watchStorageType) {
            return JSON.toJSONString(data);
        } else {
            StringBuilder buff = new StringBuilder();
            data.forEach((k, v) -> {
                buff.append("f").append(k).append(unicode(v));
            });

            return buff.toString();
        }
    }

    // 数字,大小写字母除外都将使用 unicode 的十六进制码表示.
    private String unicode(String str) {
        StringBuilder buff = new StringBuilder();
        for (char c : str.toCharArray()) {
            if ((c >= 65 && c <= 90) || (c >= 97 && c <= 122)
            || c >= 48 && c <= 57) {

                buff.append(c);

            } else {
                buff.append(Integer.toHexString(c));
            }
        }

        return buff.toString();
    }

    private void checkId(IEntity entity) throws SQLException {
        if (entity.id() == 0) {
            throw new SQLException("Invalid entity`s id.");
        }
    }
}
