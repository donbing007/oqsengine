package com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.dynamic;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.AbstractMasterTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.strategy.conditions.SQLJsonConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.master.utils.EntityClassHelper;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.value.AnyStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 条件查询.
 * 只会查询大于指定提交号的符合条件的数据.
 *
 * @author dongbin
 * @version 0.1 2020/11/4 15:36
 * @since 1.8
 */
public class DynamicQueryByConditionsExecutor
    extends AbstractMasterTaskExecutor<Conditions, Collection<EntityRef>> {

    private static final String SELECT_SORT_COLUMN0 = "sort0";
    private static final String SELECT_SORT_COLUMN1 = "sort1";
    private static final String SELECT_SORT_COLUMN2 = "sort2";

    private SelectConfig config;
    private IEntityClass entityClass;
    private SQLJsonConditionsBuilderFactory conditionsBuilderFactory;
    private StorageStrategyFactory storageStrategyFactory;

    /**
     * 构造实例.
     *
     * @param tableName                表名.
     * @param resource                 事务资源.
     * @param entityClass              元信息.
     * @param config                   查询配置.
     * @param timeoutMs                超时毫秒.
     * @param conditionsBuilderFactory 条件查询构造器工厂.
     * @param storageStrategyFactory   逻辑物理转换策略工厂.
     * @return 实例.
     */
    public static Executor<Conditions, Collection<EntityRef>> build(
        String tableName,
        TransactionResource<Connection> resource,
        IEntityClass entityClass,
        SelectConfig config,
        long timeoutMs,
        SQLJsonConditionsBuilderFactory conditionsBuilderFactory,
        StorageStrategyFactory storageStrategyFactory) {
        DynamicQueryByConditionsExecutor executor =
            new DynamicQueryByConditionsExecutor(tableName, resource, timeoutMs);
        executor.setConfig(config);
        executor.setEntityClass(entityClass);
        executor.setConditionsBuilderFactory(conditionsBuilderFactory);
        executor.setStorageStrategyFactory(storageStrategyFactory);
        return executor;
    }

    public DynamicQueryByConditionsExecutor(String tableName, TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public DynamicQueryByConditionsExecutor(String tableName, TransactionResource<Connection> resource,
                                            long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    public void setConfig(SelectConfig config) {
        this.config = config;
    }

    public void setEntityClass(IEntityClass entityClass) {
        this.entityClass = entityClass;
    }

    public void setConditionsBuilderFactory(SQLJsonConditionsBuilderFactory conditionsBuilderFactory) {
        this.conditionsBuilderFactory = conditionsBuilderFactory;
    }

    public void setStorageStrategyFactory(StorageStrategyFactory storageStrategyFactory) {
        this.storageStrategyFactory = storageStrategyFactory;
    }

    @Override
    public Collection<EntityRef> execute(Conditions conditions) throws Exception {
        Conditions useConditions;
        if (!config.getDataAccessFilterCondtitions().isEmtpy()) {
            useConditions = Conditions.buildEmtpyConditions();
            if (!conditions.isEmtpy()) {
                useConditions.addAnd(conditions, false);
                useConditions.addAnd(config.getDataAccessFilterCondtitions(), true);
            } else {
                useConditions.addAnd(config.getDataAccessFilterCondtitions(), false);
            }
        } else {
            useConditions = conditions;
        }
        // 当前查询条件.
        String where = conditionsBuilderFactory.getBuilder().build(useConditions, entityClass);

        String sql = buildSQL(where);

        try (PreparedStatement st = getResource().value().prepareStatement(
            sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {

            st.setLong(1, config.getCommitId());

            checkTimeout(st);

            try (ResultSet rs = st.executeQuery()) {
                Collection<EntityRef> refs = new ArrayList<>();
                while (rs.next()) {
                    refs.add(buildEntityRef(rs, config.getSort(), config.getSecondarySort(), config.getThirdSort()));
                }

                return refs;
            }
        }
    }

    private EntityRef buildEntityRef(ResultSet rs, Sort sort, Sort secondSort, Sort thridSort) throws SQLException {
        long id = rs.getLong(FieldDefine.ID);
        EntityRef.Builder refBuilder = EntityRef.Builder.anEntityRef();
        refBuilder.withId(id)
            .withOp(rs.getInt(FieldDefine.OP))
            .withMajor(rs.getInt(FieldDefine.OQS_MAJOR));

        if (!sort.isOutOfOrder()) {
            if (sort.getField().config().isIdentifie()) {
                refBuilder.withOrderValue(Long.toString(id));
            } else {
                refBuilder.withOrderValue(rs.getString(SELECT_SORT_COLUMN0));
            }
        }

        if (!secondSort.isOutOfOrder()) {
            if (secondSort.getField().config().isIdentifie()) {
                refBuilder.withSecondOrderValue(Long.toString(id));
            } else {
                refBuilder.withSecondOrderValue(rs.getString(SELECT_SORT_COLUMN1));
            }
        }

        if (!thridSort.isOutOfOrder()) {
            if (thridSort.getField().config().isIdentifie()) {
                refBuilder.withThridOrderValue(Long.toString(id));
            } else {
                refBuilder.withThridOrderValue(rs.getString(SELECT_SORT_COLUMN2));
            }
        }
        return refBuilder.build();
    }

    private String buildSQL(String where) {
        StringBuilder sql = new StringBuilder();

        /*
         * select id,op,oqsmajor,JSON_UNQUOTE(JSON_EXTRACT(attribute, '$.sort')) as sort from oqsbigentity
         * where ((entityclassl0 = ? and entityclassl1 = ? ...) and commitid = ?) and (where)
         */
        sql.append("SELECT ")
            .append(
                String.join(
                    ",",
                    FieldDefine.ID,
                    FieldDefine.OP,
                    FieldDefine.OQS_MAJOR));

        buildSortSelect(sql, config.getSort(), SELECT_SORT_COLUMN0);
        buildSortSelect(sql, config.getSecondarySort(), SELECT_SORT_COLUMN1);
        buildSortSelect(sql, config.getThirdSort(), SELECT_SORT_COLUMN2);

        /*
        要注意,这里依赖一个索引 commitid_entity_class (commitid, entityclass0....)
        这样一个多级索引.
         */
        sql.append(" FROM ").append(getTableName())
            .append(" WHERE (")
            .append(FieldDefine.COMMITID).append(" >= ?")
            .append(" AND ")
            .append(EntityClassHelper.buildEntityClassQuerySql(entityClass))
            .append(")");
        if (where.length() > 0 && !where.isEmpty()) {
            sql.append(" AND (").append(where).append(")");
        }
        if (config.getIgnoredOperations() != null && config.getIgnoredOperations().length > 0) {
            sql.append(" AND (").append(FieldDefine.OP);
            sql.append(" NOT IN (").append(
                Arrays.stream(
                        config.getIgnoredOperations())
                    .map(o -> Integer.toString(o.getValue()))
                    .collect(Collectors.joining(",")
                    )
            ).append(")");
            sql.append(")");
        }

        return sql.toString();
    }

    private void buildSortSelect(StringBuilder buff, Sort sort, String sortFieldName) {
        if (!sort.isOutOfOrder() && !sort.getField().config().isIdentifie()) {

            /*
             * 这里没有使用mysql的->>符号原因是,shard-jdbc解析会出现错误.
             * JSON_UNQUOTE(JSON_EXTRACT())
             * 两个函数的连用结果和->>符号是等价的.
             */
            StorageStrategy storageStrategy = storageStrategyFactory.getStrategy(sort.getField().type());
            buff.append(", JSON_UNQUOTE(JSON_EXTRACT(")
                .append(FieldDefine.ATTRIBUTE)
                .append(", '$.")
                .append(AnyStorageValue.ATTRIBUTE_PREFIX)
                .append(storageStrategy.toStorageNames(sort.getField()).stream().findFirst().get())
                .append("')) AS ").append(sortFieldName);
        }
    }
}
