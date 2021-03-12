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
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.constant.SQLConstant;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.SqlKeywordDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.SphinxQLConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValueFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import io.vavr.Tuple6;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.joining;

/**
 * Query condition Executor
 */
public class QueryConditionExecutor implements Executor<Tuple6<IEntityClass, Conditions, Page, Sort, Set<Long>, Long>, List<EntityRef>> {

    Logger logger = LoggerFactory.getLogger(QueryConditionExecutor.class);

    private String indexTableName;

    private TransactionResource<Connection> resource;

    private StorageStrategyFactory storageStrategyFactory;

    private SphinxQLConditionsBuilderFactory conditionsBuilderFactory;

    private Long maxQueryTimeMs;

    public QueryConditionExecutor(
        String indexTableName
        , TransactionResource<Connection> resource
        , SphinxQLConditionsBuilderFactory conditionsBuilderFactory
        , StorageStrategyFactory storageStrategyFactory
        , Long maxQueryTimeMs
    ) {
        this.indexTableName = indexTableName;
        this.resource = resource;
        this.conditionsBuilderFactory = conditionsBuilderFactory;
        this.storageStrategyFactory = storageStrategyFactory;
        this.maxQueryTimeMs = maxQueryTimeMs;
    }

    public static Executor<Tuple6<IEntityClass, Conditions, Page, Sort, Set<Long>, Long>, List<EntityRef>> build(
        String indexTableName
        , TransactionResource<Connection> resource
        , SphinxQLConditionsBuilderFactory conditionsBuilderFactory
        , StorageStrategyFactory storageStrategyFactory
        , Long maxQueryTimeMs
    ) {
        return new QueryConditionExecutor(indexTableName, resource, conditionsBuilderFactory, storageStrategyFactory, maxQueryTimeMs);
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

            /**
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
        }
        return sortFields;
    }

    // 搜索数量
    private long count(TransactionResource resource) throws SQLException {

        long count = 0;
        Statement statement = null;
        try {
            Connection conn = (Connection) resource.value();
            statement = conn.createStatement();

            ResultSet rs = statement.executeQuery(SQLConstant.SELECT_COUNT_SQL);
            String totalFound = "total_found";
            while (rs.next()) {
                if (totalFound.equals(rs.getString("Variable_name"))) {
                    count = rs.getLong("Value");
                    break;
                }
            }
            rs.close();
        } catch (Exception ex) {
            logger.error("QueryCount error:", ex);
        } finally {
            try {
                statement.close();
            } catch (Exception e) {
                statement = null;
                logger.error("Close rs error:", e);
            }
        }
        return count;
    }

    @Override
    public List<EntityRef> execute(Tuple6<IEntityClass, Conditions, Page, Sort, Set<Long>, Long> queryCondition) throws SQLException {

        Conditions conditions = queryCondition._2();
        IEntityClass entityClass = queryCondition._1();
        Page page = queryCondition._3();
        Sort sort = queryCondition._4();
        Set<Long> filterIds = queryCondition._5();
        Long commitId = queryCondition._6();

        String whereCondition = conditionsBuilderFactory.getBuilder(conditions).build(entityClass, conditions);

        if (filterIds != null && !filterIds.isEmpty()) {
            String ids = filterIds.stream().map(Object::toString).collect(joining(","));
            String filterCondition = String.format(SQLConstant.FILTER_IDS, ids);
            if (StringUtils.isEmpty(whereCondition)) {
                whereCondition = filterCondition;
            } else {
                whereCondition = whereCondition.concat(" and ").concat(filterCondition);
            }
        }

        if (commitId != null && commitId > 0) {
            String commitFilterId = String.format(SQLConstant.FILTER_COMMIT, commitId);

            if (StringUtils.isEmpty(whereCondition)) {
                whereCondition = commitFilterId;
            } else {
                whereCondition = commitFilterId.concat(" AND ").concat(whereCondition);
            }

        }

        if (!page.isSinglePage()) {
            page.setTotalCount(Long.MAX_VALUE);
        }
        PageScope scope = page.getNextPage();

        if (scope == null) {
            return Collections.emptyList();
        }

        Sort useSort = sort;
        StorageStrategy storageStrategy = null;
        String orderBySqlSegment = "";
        String sortSelectValuesSegment = "";
        List<SortField> sortFields = null;
        if (useSort == null) {
            useSort = Sort.buildOutOfSort();
        }

        if (!useSort.isOutOfOrder()) {
            // id 排序处理.
            if (useSort.getField().config().isIdentifie()) {
                StringBuilder buff = new StringBuilder();
                buff.append(SqlKeywordDefine.ORDER);
                buff.append(" ").append(FieldDefine.ID);
                buff.append(" ");
                if (useSort.isAsc()) {
                    buff.append(SqlKeywordDefine.ORDER_TYPE_ASC);
                } else {
                    buff.append(SqlKeywordDefine.ORDER_TYPE_DESC);
                }
                sortFields = Collections.emptyList();
                orderBySqlSegment = buff.toString();
                sortSelectValuesSegment = "";

            } else {
                // 普通属性
                storageStrategy = storageStrategyFactory.getStrategy(useSort.getField().type());
                sortFields = buildSortValues(useSort);
                orderBySqlSegment = buildOrderBySqlSegment(sortFields, useSort.isDes());
                sortSelectValuesSegment = buildSortSelectValuesSegment(sortFields);
            }
        }

        String sql = String.format(SQLConstant.SELECT_SQL, sortSelectValuesSegment, indexTableName, whereCondition, orderBySqlSegment);

        try (PreparedStatement st = resource.value().prepareStatement(sql)) {
            st.setLong(1, 0);
            if (page.isEmptyPage()) {
                st.setLong(2, 0);
            } else {
                st.setLong(2, page.getPageSize() * (page.getIndex() - 1));
            }
            st.setLong(3, page.hasVisibleTotalCountLimit() ?
                page.getVisibleTotalCount()
                : page.getPageSize() * page.getIndex());
            // add max query timeout.
            st.setLong(4, maxQueryTimeMs);

            try (ResultSet rs = st.executeQuery()) {
                List<EntityRef> refs = new ArrayList((int) page.getPageSize());

                while (rs.next()) {
                    EntityRef entityRef = new EntityRef();
                    entityRef.setId(rs.getLong(FieldDefine.ID));
                    entityRef.setMajor(rs.getInt(FieldDefine.OQSMAJOR));

                    if (!useSort.isOutOfOrder()) {
                        if (useSort.getField().config().isIdentifie()) {
                            entityRef.setOrderValue(Long.toString(entityRef.getId()));
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

                                IValue iValue = storageStrategy.toLogicValue(useSort.getField(), reduce.get());

                                if (iValue.compareByString()) {
                                    entityRef.setOrderValue(iValue.valueToString());
                                } else {
                                    entityRef.setOrderValue(Long.toString(iValue.valueToLong()));
                                }

                            }
                        }
                    }
                    refs.add(entityRef);
                }

                if (!page.isSinglePage()) {
                    long count = count(resource);
                    page.setTotalCount(count);
                } else {
                    page.setTotalCount(refs.size());
                }

                return refs;
            }
        }
    }
}
