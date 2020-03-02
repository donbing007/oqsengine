package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.BooleanValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.pojo.page.PageScope;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.executor.DataSourceShardingTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.helper.StorageTypeHelper;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.query.QueryOptimizer;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基于 SphinxQL 的索引储存实现.
 *
 * @author dongbin
 * @version 0.1 2020/2/17 17:16
 * @since 1.8
 */
public class SphinxQLIndexStorage implements IndexStorage {

    final Logger logger = LoggerFactory.getLogger(SphinxQLIndexStorage.class);

    private static final String WRITER_SQL = "%s into %s (%s, %s, %s, %s, %s, %s) values(?,?,?,?,?,?)";
    private static final String DELETE_SQL = "delete from %s where id = ?";
    private static final String SELECT_SQL = "select id, pref, cref from %s where entity = ? and %s order by %s limit ?,?";
    private static final String SELECT_COUNT_SQL = "select count(*) as count from %s where entity = ? and %s";
    private static final String SELECT_FROM_ID_SQL = "select id, pref, cref, entity, jsonfields from %s where id = ?";

    private String buildSql;
    private String replaceSql;

    @Resource(name = "indexQueryOptimizer")
    private QueryOptimizer<String> queryOptimizer;

    @Resource(name = "indexWriteDataSourceSelector")
    private Selector<DataSource> writerDataSourceSelector;

    @Resource(name = "indexSearchDataSourceSelector")
    private Selector<DataSource> searchDataSourceSelector;

    @Resource(name = "storageSphinxQLTransactionExecutor")
    private TransactionExecutor transactionExecutor;

    private String indexTableName;

    public void setIndexTableName(String indexTableName) {
        this.indexTableName = indexTableName;
    }

    @PostConstruct
    public void init() {
        buildSql =
            String.format(WRITER_SQL,
                "insert", indexTableName,
                FieldDefine.ID, FieldDefine.ENTITY, FieldDefine.PREF, FieldDefine.CREF,
                FieldDefine.JSON_FIELDS, FieldDefine.FULL_FIELDS);
        replaceSql =
            String.format(WRITER_SQL,
                "replace", indexTableName,
                FieldDefine.ID, FieldDefine.ENTITY, FieldDefine.PREF, FieldDefine.CREF,
                FieldDefine.JSON_FIELDS, FieldDefine.FULL_FIELDS);
    }

    @Override
    public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, Sort sort, Page page) throws SQLException {

        return (Collection<EntityRef>) transactionExecutor.execute(
            new DataSourceShardingTask(searchDataSourceSelector, Long.toString(entityClass.id())) {
                @Override
                public Object run(TransactionResource resource) throws SQLException {
                    String whereCondition = queryOptimizer.optimizeConditions(conditions).build(conditions);
                    PreparedStatement st = null;
                    ResultSet rs = null;
                    if (!page.isSinglePage()) {
                        String countSql = String.format(SELECT_COUNT_SQL, indexTableName, whereCondition);
                        long count = 0;

                        st = ((Connection) resource.value()).prepareStatement(countSql);
                        st.setLong(1, entityClass.id());

                        if (logger.isDebugEnabled()) {
                            logger.debug(st.toString());
                        }

                        rs = st.executeQuery();

                        while (rs.next()) {
                            count = rs.getLong("count");
                            break;
                        }

                        rs.close();
                        st.close();
                        page.setTotalCount(count);
                    }

                    PageScope scope = page.getNextPage();

                    String orderBy = buildOrderBy(sort);

                    String sql = String.format(SELECT_SQL, indexTableName, whereCondition, orderBy);
                    st = ((Connection) resource.value()).prepareStatement(sql);
                    st.setLong(1, entityClass.id());
                    st.setLong(2, scope.getStartLine());
                    st.setLong(3, scope.getEndLine());

                    if (logger.isDebugEnabled()) {
                        logger.debug(st.toString());
                    }

                    rs = st.executeQuery();

                    List<EntityRef> refs = new ArrayList((int) page.getPageSize());
                    while (rs.next()) {
                        refs.add(new EntityRef(
                            rs.getLong(FieldDefine.ID),
                            rs.getLong(FieldDefine.PREF),
                            rs.getLong(FieldDefine.CREF)
                        ));
                    }

                    try {
                        return refs;
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

    // 构造排序.
    private String buildOrderBy(Sort sort) {
        StringBuilder buff = new StringBuilder();
        if (sort != null) {
            StorageType sortFieldStorageType = StorageTypeHelper.findStorageType(sort.getField().type());
            if (sortFieldStorageType == StorageType.LONG) {
                buff.append("integer(")
                    .append(FieldDefine.JSON_FIELDS)
                    .append(".")
                    .append(sort.getField().id())
                    .append(")");
            } else {
                buff.append(FieldDefine.JSON_FIELDS)
                    .append(".")
                    .append(sort.getField().id());
            }

            if (sort.isAsc()) {
                buff.append(" ASC");
            } else {
                buff.append(" DESC");
            }
        } else {
            buff.append("id ASC");
        }
        return buff.toString();
    }

    @Override
    public void replaceAttribute(IEntityValue attribute) throws SQLException {
        transactionExecutor.execute(
            new DataSourceShardingTask(searchDataSourceSelector, Long.toString(attribute.id())) {

                @Override
                public Object run(TransactionResource resource) throws SQLException {
                    IEntity entity = doSelectIndexEntityById(attribute.id(), resource);

                    entity.entityValue().addValues(attribute.values());

                    doBuildOrReplace(entity, true);
                    ;

                    return null;
                }
            });
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

        transactionExecutor.execute(new DataSourceShardingTask(writerDataSourceSelector, Long.toString(entity.id())) {

            @Override
            public Object run(TransactionResource resource) throws SQLException {

                String sql = String.format(DELETE_SQL, indexTableName);
                PreparedStatement st = ((Connection) resource.value()).prepareStatement(sql);
                st.setLong(1, entity.id()); // id

                if (logger.isDebugEnabled()) {
                    logger.debug(st.toString());
                }

                // 在事务状态,返回值恒等于0.
                st.executeUpdate();

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


    private void doBuildOrReplace(IEntity entity, boolean replacement) throws SQLException {
        checkId(entity);
        final String sql = String.format(replacement ? replaceSql : buildSql, indexTableName);

        transactionExecutor.execute(
            new DataSourceShardingTask(writerDataSourceSelector, Long.toString(entity.id())) {

                @Override
                public Object run(TransactionResource resource) throws SQLException {
                    PreparedStatement st = ((Connection) resource.value()).prepareStatement(sql);

                    // id, entity, pref, cref, numerfields, stringfields
                    st.setLong(1, entity.id()); // id
                    st.setLong(2, entity.entityClass().id()); // entity
                    st.setLong(3, entity.family().parent()); // pref
                    st.setLong(4, entity.family().child()); // cref
                    // attribute
                    st.setString(5, serializeJson(entity.entityValue()));
                    // full
                    st.setString(6, serializeFull(entity.entityValue()));

                    if (logger.isDebugEnabled()) {
                        logger.debug(st.toString());
                    }

                    int size = st.executeUpdate();

                    try {
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
                    } finally {
                        st.close();
                    }
                }
            });
    }

    /**
     * fieldId + fieldvalue(unicode) + space + fieldId + fieldvalue(unicode)....n
     */
    private static String serializeFull(IEntityValue entityValue) {
        StringBuilder buff = new StringBuilder();
        entityValue.values().stream().forEach(v -> {
            buff.append(SphinxQLHelper.serializeFull(v));
            buff.append(" ");
        });

        return buff.toString();
    }

    /**
     * {
     * "{fieldId}" : fieldValue
     * }
     */
    private static String serializeJson(IEntityValue values) {
        // key = fieldId
        Map<String, Object> data = values.values().stream().collect(Collectors.toMap(
            v -> Long.toString(v.getField().id()),
            v -> {
                StorageType current = StorageTypeHelper.findStorageType(v.getField().type());
                if (current == StorageType.STRING) {
                    return SphinxQLHelper.escapeString(v.valueToString());
                } else {
                    return v.valueToLong();
                }
            },
            (v0, v1) -> v0));

        return JSON.toJSONString(data);
    }

    /**
     * 这个返序列化是为内部使用,替换部份属性准备的.
     * 其只会将字段处理成 string,boolean 和 long 三种类型.只关心字段的 id,并不处理字段名字.
     * 所以不能做为外部的返回值.
     *
     * @param id   数据 id.
     * @param json 字段属性 json.
     * @return 属性实例.
     */
    private IEntityValue deserialize(long id, String json) {
        JSONObject jsonObject = JSON.parseObject(json);
        IEntityValue entityValue = new EntityValue(id);
        entityValue.addValues(jsonObject.entrySet().stream().map(e -> {
            long fieldId = Long.parseLong(e.getKey());
            Object value = e.getValue();
            if (String.class.isInstance(value)) {

                return new StringValue(new Field(fieldId, "", FieldType.STRING), (String) value);

            } else if (Boolean.class.isInstance(value)) {

                return new BooleanValue(new Field(fieldId, "", FieldType.BOOLEAN), (Boolean) value);

            } else if (Integer.class.isInstance(value)) {

                return new LongValue(new Field(fieldId, "", FieldType.LONG), ((Integer) value).longValue());

            } else if (Long.class.isInstance(value)) {

                return new LongValue(new Field(fieldId, "", FieldType.LONG), (Long) value);
            } else {

                throw new IllegalStateException(
                    String.format("Types that cannot be handled.[%s]", value.getClass().toString()));

            }

        }).collect(Collectors.toList()));

        return entityValue;
    }


    private void checkId(IEntity entity) throws SQLException {
        if (entity.id() == 0) {
            throw new SQLException("Invalid entity`s id.");
        }
    }

    // 索引内部查询.
    private IEntity doSelectIndexEntityById(long id, TransactionResource resource) throws SQLException {
        String sql = String.format(SELECT_FROM_ID_SQL, indexTableName);
        PreparedStatement st = ((Connection) (resource.value())).prepareStatement(sql);
        st.setLong(1, id);
        ResultSet rs = st.executeQuery();
        try {
            if (rs.next()) {
                // long id, IEntityClass entityClass, IEntityValue entityValue, IEntityFamily family, int version
                return new Entity(
                    id,
                    new EntityClass(rs.getLong(FieldDefine.ENTITY)),
                    deserialize(id, rs.getString(FieldDefine.JSON_FIELDS)),
                    new EntityFamily(rs.getLong(FieldDefine.PREF), rs.getLong(FieldDefine.CREF)),
                    rs.getInt(FieldDefine.ID)
                );
            } else {
                throw new SQLException(String.format("No target Entity(%d)", id));
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
}
