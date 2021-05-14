package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor;

import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.storage.pojo.search.CrossSearchConfig;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import io.vavr.Tuple3;
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
    extends AbstractIndexExecutor<Tuple3<String, String, CrossSearchConfig>, Collection<EntityRef>> {

    public static CrossSearchExecutor build(String indexName, TransactionResource transactionResource, long timeoutMs) {
        return new CrossSearchExecutor(indexName, transactionResource, timeoutMs);
    }

    public CrossSearchExecutor(String indexName, TransactionResource transactionResource) {
        super(indexName, transactionResource);
    }

    public CrossSearchExecutor(String indexName, TransactionResource transactionResource, long timeoutMs) {
        super(indexName, transactionResource, timeoutMs);
    }

    @Override
    public Collection<EntityRef> execute(Tuple3<String, String, CrossSearchConfig> queryParam)
        throws SQLException {
        return null;
    }
}
