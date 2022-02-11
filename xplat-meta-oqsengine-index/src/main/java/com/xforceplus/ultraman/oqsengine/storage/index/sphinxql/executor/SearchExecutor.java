package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor;

import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.pojo.page.PageScope;
import com.xforceplus.ultraman.oqsengine.storage.executor.jdbc.AbstractJdbcTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.constant.SQLConstant;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.pojo.SphinxQLWhere;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.SphinxQLConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.pojo.search.SearchConfig;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import io.vavr.Tuple2;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 全文搜索执行器.
 *
 * @author dongbin
 * @version 0.1 2021/05/13 17:21
 * @since 1.8
 */
public class SearchExecutor
    extends AbstractJdbcTaskExecutor<Tuple2<SearchConfig, IEntityClass[]>, Collection<EntityRef>> {

    private SphinxQLConditionsBuilderFactory conditionsBuilderFactory;

    public static SearchExecutor build(
        String indexName,
        TransactionResource transactionResource,
        SphinxQLConditionsBuilderFactory conditionsBuilderFactory,
        long timeoutMs) {
        return new SearchExecutor(indexName, transactionResource, conditionsBuilderFactory, timeoutMs);
    }

    public SearchExecutor(
        String indexName,
        TransactionResource transactionResource,
        SphinxQLConditionsBuilderFactory conditionsBuilderFactory) {
        this(indexName, transactionResource, conditionsBuilderFactory, 0);
    }

    public SearchExecutor(
        String indexName,
        TransactionResource transactionResource,
        SphinxQLConditionsBuilderFactory conditionsBuilderFactory,
        long timeoutMs) {
        super(indexName, transactionResource, timeoutMs);
        this.conditionsBuilderFactory = conditionsBuilderFactory;
    }

    @Override
    public Collection<EntityRef> execute(Tuple2<SearchConfig, IEntityClass[]> param)
        throws SQLException {

        SearchConfig config = param._1();
        IEntityClass[] entityClasses = param._2();
        SphinxQLWhere where = conditionsBuilderFactory.getSearchBuilder().build(config, entityClasses);
        if (entityClasses != null) {
            for (IEntityClass entityClass : entityClasses) {
                where.addEntityClass(entityClass);
            }
        }

        String sql = String.format(SQLConstant.SEARCH_SQL, getTableName(), where.toString(), "bm25");

        Page page = config.getPage();
        if (!page.isSinglePage()) {
            page.setTotalCount(Long.MAX_VALUE);
        }
        PageScope scope = page.getNextPage();

        if (scope == null && !page.isEmptyPage()) {
            return Collections.emptyList();
        }

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

            List<EntityRef> refs = new ArrayList((int) page.getPageSize());
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    EntityRef entityRef = EntityRef.Builder.anEntityRef()
                        .withId(rs.getLong(FieldDefine.ID))
                        .build();
                    refs.add(entityRef);
                }
            }

            if (!page.isSinglePage()) {
                // 注意这里会复用 PreparedStatement 之前打开的 ResultSet 会被关闭.
                long count = SphinxQLHelper.count(st);
                page.setTotalCount(count);
            } else {
                page.setTotalCount(refs.size());
            }

            return refs;
        }
    }
}
