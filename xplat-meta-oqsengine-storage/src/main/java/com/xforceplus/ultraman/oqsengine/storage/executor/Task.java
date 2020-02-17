package com.xforceplus.ultraman.oqsengine.storage.executor;

import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author dongbin
 * @version 0.1 2020/2/17 15:22
 * @since 1.8
 */
public interface Task {

    Selector<DataSource> getDataSourceSelector();
    String getShardKey();
    Object run(TransactionResource resource) throws SQLException;
}
