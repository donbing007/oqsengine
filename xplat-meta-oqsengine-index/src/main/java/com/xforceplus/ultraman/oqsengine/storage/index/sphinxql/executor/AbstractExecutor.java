package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;

/**
 * 抽像实现.提供了索引名称和操作资源.
 *
 * @author dongbin
 * @version 0.1 2021/3/3 11:43
 * @since 1.8
 */
public abstract class AbstractExecutor<RES, REQ> implements Executor<RES, REQ> {

    private String indexName;
    private TransactionResource transactionResource;

    public AbstractExecutor(String indexName, TransactionResource transactionResource) {
        this.indexName = indexName;
        this.transactionResource = transactionResource;
    }

    public String getIndexName() {
        return indexName;
    }

    public TransactionResource getTransactionResource() {
        return transactionResource;
    }
}
