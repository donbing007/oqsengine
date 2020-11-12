package com.xforceplus.ultraman.oqsengine.storage.master.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.conditions.SQLJsonConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 条件查询.
 * 只会查询大于指定提交号的符合条件的数据.
 *
 * @author dongbin
 * @version 0.1 2020/11/4 15:36
 * @since 1.8
 */
public class QueryLimitCommitidByConditionsExecutor extends AbstractMasterExecutor<Conditions, Collection<EntityRef>> {

    private static final String SELECT_SORT_COLUMN = "sort";

    private long commitid;
    private IEntityClass entityClass;
    private Sort sort;
    private SQLJsonConditionsBuilderFactory conditionsBuilderFactory;
    private StorageStrategyFactory storageStrategyFactory;

    public static Executor<Conditions, Collection<EntityRef>> build(
        String tableName,
        TransactionResource<Connection> resource,
        IEntityClass entityClass,
        Sort sort,
        long commitid,
        long timeoutMs,
        SQLJsonConditionsBuilderFactory conditionsBuilderFactory,
        StorageStrategyFactory storageStrategyFactory) {
        QueryLimitCommitidByConditionsExecutor executor =
            new QueryLimitCommitidByConditionsExecutor(tableName, resource, timeoutMs);
        executor.setCommitid(commitid);
        executor.setEntityClass(entityClass);
        executor.setSort(sort);
        executor.setConditionsBuilderFactory(conditionsBuilderFactory);
        executor.setStorageStrategyFactory(storageStrategyFactory);
        return executor;
    }

    public QueryLimitCommitidByConditionsExecutor(String tableName, TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public QueryLimitCommitidByConditionsExecutor(String tableName, TransactionResource<Connection> resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    public void setCommitid(long commitid) {
        this.commitid = commitid;
    }

    public void setEntityClass(IEntityClass entityClass) {
        this.entityClass = entityClass;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public void setConditionsBuilderFactory(SQLJsonConditionsBuilderFactory conditionsBuilderFactory) {
        this.conditionsBuilderFactory = conditionsBuilderFactory;
    }

    public void setStorageStrategyFactory(StorageStrategyFactory storageStrategyFactory) {
        this.storageStrategyFactory = storageStrategyFactory;
    }

    @Override
    public Collection<EntityRef> execute(Conditions conditions) throws SQLException {
        String where = conditionsBuilderFactory.getBuilder().build(conditions);
        String sql = buildSQL(where);

        PreparedStatement st = getResource().value().prepareStatement(
            sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        st.setLong(1, entityClass.id());
        st.setLong(2, commitid);

        checkTimeout(st);

        ResultSet rs = st.executeQuery();
        Collection<EntityRef> refs = new ArrayList<>();
        while (rs.next()) {
            refs.add(buildEntityRef(rs, sort));
        }

        return refs;
    }

    private EntityRef buildEntityRef(ResultSet rs, Sort sort) throws SQLException {
        EntityRef ref = new EntityRef();
        ref.setId(rs.getLong(FieldDefine.ID));
        ref.setPref(rs.getLong(FieldDefine.PREF));
        ref.setCref(rs.getLong(FieldDefine.CREF));
        ref.setOp(rs.getInt(FieldDefine.OP));
        if (sort != null && !sort.isOutOfOrder()) {
            ref.setOrderValue(rs.getString(SELECT_SORT_COLUMN));
        }
        return ref;
    }

    private String buildSQL(String where) {
        StringBuilder sql = new StringBuilder();

        // select id,pref,cref,op,JSON_UNQUOTE(JSON_EXTRACT(attribute, '$.sort')) as sort from oqsbigentity where (entity = ? and commitid = ?) and (where)
        sql.append("SELECT ")
            .append(String.join(",", FieldDefine.ID, FieldDefine.PREF, FieldDefine.CREF, FieldDefine.OP));
        if (sort != null && !sort.isOutOfOrder()) {
            /**
             * 这里没有使用mysql的->>符号原因是,shard-jdbc解析会出现错误.
             * JSON_UNQUOTE(JSON_EXTRACT())
             * 两个函数的连用结果和->>符号是等价的.
             */
            StorageStrategy storageStrategy = storageStrategyFactory.getStrategy(sort.getField().type());
            sql.append(", JSON_UNQUOTE(JSON_EXTRACT(")
                .append(FieldDefine.ATTRIBUTE)
                .append(", '$.")
                .append(FieldDefine.ATTRIBUTE_PREFIX)
                .append(storageStrategy.toStorageNames(sort.getField()).stream().findFirst().get())
                .append("')) AS ").append(SELECT_SORT_COLUMN);
        }
        sql.append(" FROM ").append(getTableName())
            .append(" WHERE (")
            .append(FieldDefine.ENTITY).append(" = ").append("?")
            .append(" AND ")
            .append(FieldDefine.COMMITID).append(" >= ").append("?")
            .append(")");
        if (where.length() > 0 && !where.isEmpty()) {
            sql.append(" AND (").append(where).append(")");
        }
        return sql.toString();
    }
}
