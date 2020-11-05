package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.pojo.page.PageScope;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.constant.SQLConstant;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.SqlKeywordDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.SphinxQLConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import io.vavr.Tuple6;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

    /**
     * order by str builder
     *
     * @param sort
     * @return
     */
    private String buildOrderBy(Sort sort) {

        StringBuilder buff = new StringBuilder(SqlKeywordDefine.ORDER).append(" ");
        if (!sort.isOutOfOrder()) {
            StorageStrategy storageStrategy = storageStrategyFactory.getStrategy(sort.getField().type());
            Collection<String> storageNames = storageStrategy.toStorageNames(sort.getField());
            //表示还没有排序字段时的长度.
            int emptyLen = buff.length();

            for (String storageName : storageNames) {
                if (buff.length() > emptyLen) {
                    buff.append(", ");
                }
                if (storageStrategy.storageType() == StorageType.LONG) {
                    buff.append("bigint(").append(FieldDefine.JSON_FIELDS).append(".").append(storageName).append(")");
                } else {
                    buff.append(FieldDefine.JSON_FIELDS).append(".").append(storageName);
                }

                if (sort.isAsc()) {
                    buff.append(" ").append(SqlKeywordDefine.ORDER_TYPE_ASC);
                } else {
                    buff.append(" ").append(SqlKeywordDefine.ORDER_TYPE_DESC);
                }
            }

        } else {
            buff.append("id ").append(SqlKeywordDefine.ORDER_TYPE_ASC);
        }
        return buff.toString();
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

        if ( filterIds != null && !filterIds.isEmpty()) {
            String ids = filterIds.stream().map(Object::toString).collect(joining(","));
            String filterCondition = String.format(SQLConstant.FILTER_IDS, ids);
            if (StringUtils.isEmpty(whereCondition)) {
                whereCondition = filterCondition;
            } else {
                whereCondition = whereCondition.concat(" and ").concat(filterCondition);
            }
        }

        if (commitId != null) {
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
        if (useSort == null) {
            useSort = Sort.buildOutOfSort();
        }

        String orderBy = buildOrderBy(useSort);
        String sql = String.format(SQLConstant.SELECT_SQL, indexTableName, whereCondition, orderBy);

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
                refs.add(new EntityRef(rs.getLong(FieldDefine.ID), rs.getLong(FieldDefine.PREF),
                        rs.getLong(FieldDefine.CREF)));
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
