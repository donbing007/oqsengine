package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor;

import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.pojo.SphinxQLWhere;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.SphinxQLConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.pojo.search.CrossSearchConfig;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import io.vavr.Tuple2;
import java.sql.SQLException;
import java.util.Collection;

/**
 * 全文搜索执行器.
 *
 * @author dongbin
 * @version 0.1 2021/05/13 17:21
 * @since 1.8
 */
public class CrossSearchExecutor
    extends AbstractIndexExecutor<Tuple2<Condition, CrossSearchConfig>, Collection<EntityRef>> {

    private SphinxQLConditionsBuilderFactory conditionsBuilderFactory;

    public static CrossSearchExecutor build(String indexName, TransactionResource transactionResource, long timeoutMs) {
        return new CrossSearchExecutor(indexName, transactionResource, timeoutMs);
    }

    public CrossSearchExecutor(
        String indexName,
        TransactionResource transactionResource,
        SphinxQLConditionsBuilderFactory conditionsBuilderFactory) {
        this(indexName, transactionResource, conditionsBuilderFactory, 0);
    }

    public CrossSearchExecutor(
        String indexName,
        TransactionResource transactionResource,
        SphinxQLConditionsBuilderFactory conditionsBuilderFactory,
        long timeoutMs) {
        super(indexName, transactionResource, timeoutMs);
        this.conditionsBuilderFactory = conditionsBuilderFactory;
    }

    @Override
    public Collection<EntityRef> execute(Tuple2<Condition, CrossSearchConfig> queryParam)
        throws SQLException {
        Condition condition = queryParam._1();
        CrossSearchConfig config = queryParam._2();

        Conditions conditions = Conditions.buildEmtpyConditions().addAnd(condition);
        ConditionsBuilder<SphinxQLWhere> where = conditionsBuilderFactory.getBuilder(conditions);


        return null;
    }
}
