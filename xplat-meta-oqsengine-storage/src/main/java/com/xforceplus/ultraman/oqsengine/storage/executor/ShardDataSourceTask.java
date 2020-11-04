package com.xforceplus.ultraman.oqsengine.storage.executor;

import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;

import javax.sql.DataSource;

/**
 * 支持数据源选择的任务描述.
 * @author dongbin
 * @version 0.1 2020/2/22 21:26
 * @since 1.8
 */
public interface ShardDataSourceTask extends Task<TransactionResource> {

    /**
     * 当前的数据源选择器.
     * @return 选择器.
     */
    Selector<DataSource> getDataSourceSelector();

    /**
     * 分区key.
     * @return 分区 key.
     */
    String getShardKey();
}
