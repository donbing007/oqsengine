package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.pojo.page.PageScope;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.executor.jdbc.AbstractJdbcTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.constant.SQLConstant;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.SqlKeywordDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.pojo.SphinxQLWhere;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.SphinxQLConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.commit.CommitHelper;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValueFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import io.vavr.Tuple3;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Query condition Executor.
 */
public class QueryConditionExecutor
    extends AbstractJdbcTaskExecutor<Tuple3<IEntityClass, Conditions, SelectConfig>, List<EntityRef>> {

    Logger logger = LoggerFactory.getLogger(QueryConditionExecutor.class);

    private StorageStrategyFactory storageStrategyFactory;

    private SphinxQLConditionsBuilderFactory conditionsBuilderFactory;

    /**
     * 实例化.
     *
     * @param indexTableName           索引名称.
     * @param resource                 事务资源.
     * @param conditionsBuilderFactory 条件构造器工厂.
     * @param storageStrategyFactory   逻辑物理字段转换器工厂.
     * @param maxQueryTimeMs           最大查询超时毫秒.
     */
    public QueryConditionExecutor(
        String indexTableName,
        TransactionResource<Connection> resource,
        SphinxQLConditionsBuilderFactory conditionsBuilderFactory,
        StorageStrategyFactory storageStrategyFactory,
        long maxQueryTimeMs) {

        super(indexTableName, resource, maxQueryTimeMs);
        this.conditionsBuilderFactory = conditionsBuilderFactory;
        this.storageStrategyFactory = storageStrategyFactory;
    }

    /**
     * 构造方法.
     *
     * @param indexTableName           索引名称.
     * @param resource                 事务资源.
     * @param conditionsBuilderFactory 条件构造器工厂.
     * @param storageStrategyFactory   逻辑物理字段转换器工厂.
     * @param maxQueryTimeMs           最大查询超时毫秒.
     * @return 实例.
     */
    public static Executor<Tuple3<IEntityClass, Conditions, SelectConfig>, List<EntityRef>> build(
        String indexTableName,
        TransactionResource<Connection> resource,
        SphinxQLConditionsBuilderFactory conditionsBuilderFactory,
        StorageStrategyFactory storageStrategyFactory,
        Long maxQueryTimeMs) {

        return new QueryConditionExecutor(indexTableName, resource, conditionsBuilderFactory, storageStrategyFactory,
            maxQueryTimeMs);
    }


    // 排序中间结果.
    private static class SortField {
        private String fieldName;
        private String alias;
        private boolean number;
        private boolean system;

        public SortField(String fieldName, String alias, boolean number) {
            this(fieldName, alias, number, false);
        }

        public SortField(String fieldName, String alias, boolean number, boolean system) {
            this.fieldName = fieldName;
            this.alias = alias;
            this.number = number;
            this.system = system;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getAlias() {
            return alias;
        }

        public boolean isNumber() {
            return number;
        }

        public boolean isSystem() {
            return system;
        }
    }

    // 构造排序查询语句段.
    private String buildOrderBySqlSegment(List<SortField> sortFields, boolean desc) {
        StringBuilder buff = new StringBuilder();

        buff.append(SqlKeywordDefine.ORDER);

        boolean first = true;
        for (SortField field : sortFields) {

            if (!first && buff.length() > 0) {
                buff.append(",");
            }
            first = false;
            buff.append(' ')
                .append(field.getAlias())
                .append(' ')
                .append(desc ? SqlKeywordDefine.ORDER_TYPE_DESC : SqlKeywordDefine.ORDER_TYPE_ASC);

        }
        return buff.toString();
    }

    // 构造排序要用到的字段在select段.
    // select id,cref,pref, attr.123123L as sort1 from
    private String buildSortSelectValuesSegment(List<SortField> sortFields) {
        StringBuilder buff = new StringBuilder();
        String fieldName;
        for (SortField field : sortFields) {
            if (buff.length() > 0) {
                buff.append(',');
            }

            if (field.isNumber()) {
                if (!field.isSystem()) {
                    fieldName = "bigint(" + field.getFieldName() + ")";
                } else {
                    fieldName = field.getFieldName();
                }
            } else {
                fieldName = field.getFieldName();
            }

            buff.append(fieldName)
                .append(' ').append(SqlKeywordDefine.ALIAS_LINK).append(' ')
                .append(field.getAlias());
        }

        if (buff.length() > 0) {
            // 为首个增加一个','分隔.
            buff.insert(0, ',');
        }
        return buff.toString();
    }

    // 构造排序字段信息.
    private List<SortField> buildSortValues(Sort sort) {
        List<SortField> sortFields;
        if (!sort.isOutOfOrder()) {

            /*
             * optimize: 这里进行一个可能的优化,尝试将排序字段优化成原生字段.
             *           如果可以使用原生字段字段,即非 NORMAL 类型的字段.
             */
            sortFields = new ArrayList<>(tryNativeFieldSorting(sort.getField()));
            if (!sortFields.isEmpty()) {
                return sortFields;
            }

            StorageStrategy storageStrategy = storageStrategyFactory.getStrategy(sort.getField().type());
            Collection<String> storageNames = storageStrategy.toStorageNames(sort.getField(), true);

            String fieldName;
            String alias;
            boolean number;
            int aliasIndex = 0;
            StringBuilder buff = new StringBuilder();
            for (String storageName : storageNames) {
                buff.delete(0, buff.length());
                buff.append(FieldDefine.ATTRIBUTE).append(".").append(storageName);
                fieldName = buff.toString();

                buff.delete(0, buff.length());
                buff.append(FieldDefine.SORT_FIELD_ALIAS_PREFIX).append(aliasIndex++);
                alias = buff.toString();

                if (storageStrategy.storageType() == StorageType.LONG) {
                    number = true;
                } else {
                    number = false;
                }

                sortFields.add(new SortField(fieldName, alias, number));
            }
        } else {
            sortFields = Collections.emptyList();
        }

        return sortFields;
    }

    // 尝试使用原生的字段排序.
    private List<SortField> tryNativeFieldSorting(IEntityField field) {
        FieldConfig.FieldSense sence = field.config().getFieldSense();
        // 中会有一个排序字段.
        final int onlyOne = 1;
        List<SortField> sortFields = new ArrayList<>(onlyOne);
        switch (sence) {
            case CREATE_TIME: {
                sortFields.add(
                    new SortField(
                        FieldDefine.CREATE_TIME,
                        String.format("%s0", FieldDefine.SORT_FIELD_ALIAS_PREFIX), true, true));
                break;
            }
            case UPDATE_TIME: {
                sortFields.add(
                    new SortField(
                        FieldDefine.UPDATE_TIME,
                        String.format("%s0", FieldDefine.SORT_FIELD_ALIAS_PREFIX), true, true));
                break;
            }
            default: {
                // do nothing
            }
        }
        return sortFields;
    }

    @Override
    public List<EntityRef> execute(Tuple3<IEntityClass, Conditions, SelectConfig> queryCondition) throws SQLException {

        Conditions conditions = queryCondition._2();
        IEntityClass entityClass = queryCondition._1();
        Set<Long> filterIds = queryCondition._3().getExcludedIds();
        long commitId = queryCondition._3().getCommitId();
        Conditions filterConditions = queryCondition._3().getDataAccessFilterCondtitions();

        SphinxQLWhere where = conditionsBuilderFactory.getBuilder(conditions).build(conditions, entityClass);
        /*
         * 如果有数据过滤条件,那么将默认以OR=true,range=true的方式找到条件构造器.
         * 目的是防止进入全文字段.
         */
        if (!filterConditions.isEmtpy()) {
            SphinxQLWhere filterWhere =
                conditionsBuilderFactory.getBuilder(true, true).build(filterConditions, entityClass);

            where.addWhere(filterWhere, true);

        }


        if (filterIds != null && !filterIds.isEmpty()) {
            for (long id : filterIds) {
                where.addFilterId(id);
            }
        }

        if (commitId > 0 && commitId < CommitHelper.getUncommitId()) {
            where.setCommitId(commitId);
        }

        where.addEntityClass(entityClass);

        Page page = queryCondition._3().getPage();
        if (!page.isSinglePage()) {
            page.setTotalCount(Long.MAX_VALUE);
        }
        PageScope scope = page.getNextPage();

        /*
         * 当没有下一页,并且不是空页要求时直接空返回.
         * 当是空页要求时,会继续计算但不会返回任何数据只是会填充数据总量.
         */
        if (scope == null && !page.isEmptyPage()) {
            return Collections.emptyList();
        }

        Sort sort = queryCondition._3().getSort();
        Sort secondSort = queryCondition._3.getSecondarySort();
        Sort thirdSort = queryCondition._3.getThirdSort();
        StorageStrategy storageStrategy = null;
        String orderBySqlSegment = "";
        String sortSelectValuesSegment = "";
        List<SortField> sortFields = null;

        /*
         * 空页要求时不需要进行排序.
         */
        if (!page.isEmptyPage()) {
            if (!sort.isOutOfOrder()) {
                // id 排序处理.
                if (sort.getField().config().isIdentifie()) {
                    StringBuilder buff = new StringBuilder();
                    buff.append(SqlKeywordDefine.ORDER);
                    buff.append(" ").append(FieldDefine.ID);
                    buff.append(" ");
                    if (sort.isAsc()) {
                        buff.append(SqlKeywordDefine.ORDER_TYPE_ASC);
                    } else {
                        buff.append(SqlKeywordDefine.ORDER_TYPE_DESC);
                    }
                    sortFields = Collections.emptyList();
                    orderBySqlSegment = buff.toString();
                    sortSelectValuesSegment = "";

                } else {
                    // 普通属性
                    storageStrategy = storageStrategyFactory.getStrategy(sort.getField().type());
                    sortFields = buildSortValues(sort);
                    orderBySqlSegment = buildOrderBySqlSegment(sortFields, sort.isDes());
                    sortSelectValuesSegment = buildSortSelectValuesSegment(sortFields);
                }
            }
        }

        String sql = String.format(
            SQLConstant.SELECT_SQL, sortSelectValuesSegment, getTableName(), where.toString(), orderBySqlSegment);

        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
            st.setLong(1, 0);
            if (page.isEmptyPage()) {
                st.setLong(2, 0);
            } else {
                st.setLong(2, page.getPageSize() * (page.getIndex() - 1));
            }
            long maxMatch = page.getPageSize() * (page.getIndex() - 1);
            if (page.hasVisibleTotalCountLimit()) {
                maxMatch = maxMatch > page.getVisibleTotalCount() ? page.getVisibleTotalCount() : maxMatch;
            }
            st.setLong(3, maxMatch <= 0 ? 1 : maxMatch);
            // 设置manticore的查询超时时间.
            st.setLong(4, getTimeoutMs());

            // 设定本地超时时间.
            checkTimeout(st);

            try (ResultSet rs = st.executeQuery()) {
                List<EntityRef> refs = new ArrayList((int) page.getPageSize());

                while (rs.next()) {
                    EntityRef.Builder entityRefBuilder = EntityRef.Builder.anEntityRef();
                    long id = rs.getLong(FieldDefine.ID);
                    entityRefBuilder.withId(id);

                    if (!sort.isOutOfOrder()) {
                        if (sort.getField().config().isIdentifie()) {
                            entityRefBuilder.withOrderValue(Long.toString(id));
                        } else {
                            ResultSet finalRs = rs;
                            AtomicInteger index = new AtomicInteger(0);

                            StorageStrategy finalStorageStrategy = storageStrategy;
                            Optional<StorageValue> reduce = sortFields.stream().map(x -> {
                                //get sort value
                                try {
                                    switch (finalStorageStrategy.storageType()) {
                                        case LONG:
                                            return StorageValueFactory.buildStorageValue(
                                                finalStorageStrategy.storageType(),
                                                x.fieldName,
                                                finalRs.getLong("sort" + index.getAndIncrement()));
                                        case STRING:
                                            return StorageValueFactory.buildStorageValue(
                                                finalStorageStrategy.storageType(),
                                                x.fieldName,
                                                finalRs.getString("sort" + index.getAndIncrement()));
                                        default:
                                            return null;
                                    }
                                } catch (Exception ex) {
                                    logger.error(ex.getMessage(), ex);
                                    return null;
                                }
                            }).filter(Objects::nonNull).reduce(StorageValue::stick);

                            if (reduce.isPresent()) {

                                IValue logicValue = storageStrategy.toLogicValue(sort.getField(), reduce.get());

                                if (logicValue.getValue() == null) {
                                    entityRefBuilder.withOrderValue(null);
                                } else {
                                    if (logicValue.compareByString()) {
                                        entityRefBuilder.withOrderValue(logicValue.valueToString());
                                    } else {
                                        entityRefBuilder.withOrderValue(Long.toString(logicValue.valueToLong()));
                                    }
                                }
                            }
                        }
                    }
                    refs.add(entityRefBuilder.build());
                }

                if (!page.isSinglePage()) {
                    long count = SphinxQLHelper.count(getResource());
                    page.setTotalCount(count);
                } else {
                    page.setTotalCount(refs.size());
                }

                return refs;
            }
        }
    }
}
