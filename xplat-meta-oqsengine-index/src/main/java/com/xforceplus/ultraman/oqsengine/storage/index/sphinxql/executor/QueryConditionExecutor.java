package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
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
public class QueryConditionExecutor implements Executor<Tuple6<Long, Conditions, Page, Sort, List<Long>, Long>, List<EntityRef>> {

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

    public static Executor<Tuple6<Long, Conditions, Page, Sort, List<Long>, Long>, List<EntityRef>> build(
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

        public SortField(String fieldName, String alias, boolean number) {
            this.fieldName = fieldName;
            this.alias = alias;
            this.number = number;
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
    // select id,cref,pref, jsonfields.123123L as sort1 from
    private String buildSortSelectValuesSegment(List<SortField> sortFields) {
        StringBuilder buff = new StringBuilder();
        String fieldName;
        for (SortField field : sortFields) {
            if (buff.length() > 0) {
                buff.append(',');
            }

            if (field.isNumber()) {
                fieldName = "bigint(" + field.getFieldName() + ")";
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
            StorageStrategy storageStrategy = storageStrategyFactory.getStrategy(sort.getField().type());
            Collection<String> storageNames = storageStrategy.toStorageNames(sort.getField());

            String fieldName;
            String alias;
            boolean number;
            int aliasIndex = 0;
            sortFields = new ArrayList<>();
            StringBuilder buff = new StringBuilder();
            for (String storageName : storageNames) {
                buff.delete(0, buff.length());
                buff.append(FieldDefine.JSON_FIELDS).append(".").append(storageName);
                fieldName = buff.toString();

                buff.delete(0, buff.length());
                buff.append("sort").append(aliasIndex++);
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

    //TODO
    @Override
    public List<EntityRef> execute(Tuple6<Long, Conditions, Page, Sort, List<Long>, Long> queryCondition) throws SQLException {

        Conditions conditions = queryCondition._2();
        Long entityId = queryCondition._1();
        Page page = queryCondition._3();
        Sort sort = queryCondition._4();
        List<Long> filterIds = queryCondition._5();
        Long commitId = queryCondition._6();

        String whereCondition = conditionsBuilderFactory.getBuilder(conditions).build(conditions);

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
                whereCondition = whereCondition.concat(" and ").concat(commitFilterId);
            }

        }

        if (!whereCondition.isEmpty()) {
            whereCondition = SqlKeywordDefine.AND + " " + whereCondition;
        }

        if (page.isEmptyPage()) {
            return Collections.emptyList();
        }

        page.setTotalCount(Long.MAX_VALUE);
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
        } else {
            storageStrategy = storageStrategyFactory.getStrategy(useSort.getField().type());
            sortFields = buildSortValues(useSort);
            orderBySqlSegment = buildOrderBySqlSegment(sortFields, useSort.isDes());
            sortSelectValuesSegment = buildSortSelectValuesSegment(sortFields);
        }


//        String orderBy = buildOrderBy(useSort);
        String sql = String.format(SQLConstant.SELECT_SQL, sortSelectValuesSegment, indexTableName, whereCondition, orderBySqlSegment);

        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = ((Connection) resource.value()).prepareStatement(sql);
            st.setLong(1, entityId);
            st.setLong(2, 0);
            st.setLong(3, page.getPageSize() * page.getIndex());
            st.setLong(4, page.hasVisibleTotalCountLimit() ?
                    page.getVisibleTotalCount()
                    : page.getPageSize() * page.getIndex());
            // add max query timeout.
            st.setLong(5, maxQueryTimeMs);
            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }

            rs = st.executeQuery();
            List<EntityRef> refs = new ArrayList((int) page.getPageSize());

            while (rs.next()) {
                EntityRef entityRef = new EntityRef();
                entityRef.setId(rs.getLong(FieldDefine.ID));
                entityRef.setCref(rs.getLong(FieldDefine.CREF));
                entityRef.setPref(rs.getLong(FieldDefine.PREF));

                if (sort != null && !sort.isOutOfOrder()) {
                    //TODO generator multi
                    ResultSet finalRs = rs;
                    AtomicInteger index = new AtomicInteger(0);

                    StorageStrategy finalStorageStrategy = storageStrategy;
                    Optional<StorageValue> reduce = sortFields.stream().map(x -> {
                        //get sort value
                        try {
                            switch (finalStorageStrategy.storageType()) {
                                case LONG:
                                    return StorageValueFactory.buildStorageValue(finalStorageStrategy.storageType(), x.fieldName, finalRs.getLong("sort" + index.getAndIncrement()));
                                case STRING:
                                    return StorageValueFactory.buildStorageValue(finalStorageStrategy.storageType(), x.fieldName, finalRs.getString("sort" + index.getAndIncrement()));
                                default:
                                    return null;
                            }
                        } catch (Exception ex) {
                            logger.error("{}", ex);
                            return null;
                        }
                    }).filter(Objects::nonNull).reduce(StorageValue::stick);

                    if (reduce.isPresent()) {

                        IValue iValue = storageStrategy.toLogicValue(useSort.getField(), reduce.get());

                        if(iValue.compareByString()) {
                            entityRef.setOrderValue(iValue.valueToString());
                        }else{
                            entityRef.setOrderValue(Long.toString(iValue.valueToLong()));
                        }

                    }
                }

                refs.add(entityRef);
            }

            if (!page.isSinglePage()) {
                long count = count(resource);
                page.setTotalCount(count);
            }

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
}
