package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql;

import com.alibaba.fastjson.JSON;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
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
    private static final String SELECT_COUNT_SQL = "select count(id) as count from %s where entity = ? and %s";

    private String buildSql;
    private String replaceSql;

    @Resource(name = "indexQueryOptimizer")
    private QueryOptimizer<String> queryOptimizer;

    @Resource(name = "indexWriteDataSourceSelector")
    private Selector<DataSource> writerDataSourceSelector;

    @Resource(name = "indexSearchDataSourceSelector")
    private Selector<DataSource> searchDataSourceSelector;

    @Resource(name = "storageTransactionExecutor")
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
                        rs = st.executeQuery();

                        if (logger.isDebugEnabled()) {
                            logger.debug(st.toString());
                        }

                        while (rs.next()) {
                            count = rs.getLong("count");
                            break;
                        }

                        rs.close();
                        st.close();
                        page.setTotalCount(count);
                    }

                    PageScope scope = page.getNextPage();

                    String orderBy;
                    if (sort != null) {
                        orderBy = FieldDefine.JSON_FIELDS + "." + sort.getField().id();
                        if (sort.isAsc()) {
                            orderBy += " ASC";
                        } else {
                            orderBy += " DESC";
                        }
                    } else {
                        orderBy = "id DESC";
                    }

                    String sql = String.format(SELECT_SQL, indexTableName, whereCondition, orderBy);
                    st = ((Connection) resource.value()).prepareStatement(sql);
                    st.setLong(1, entityClass.id());
                    st.setLong(2, scope.startLine);
                    st.setLong(3, scope.endLine);

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


    private void checkId(IEntity entity) throws SQLException {
        if (entity.id() == 0) {
            throw new SQLException("Invalid entity`s id.");
        }
    }
}
