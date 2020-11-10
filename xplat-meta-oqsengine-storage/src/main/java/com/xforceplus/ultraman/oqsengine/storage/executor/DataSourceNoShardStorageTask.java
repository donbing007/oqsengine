package com.xforceplus.ultraman.oqsengine.storage.executor;

import javax.sql.DataSource;

/**
 * 不进行shard的任务.
 *
 * @author dongbin
 * @version 0.1 2020/11/6 13:55
 * @since 1.8
 */
public abstract class DataSourceNoShardStorageTask implements StorageTask {

    private DataSource dataSource;

    public DataSourceNoShardStorageTask(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 连接池.
     *
     * @return
     */
    @Override
    public DataSource getDataSource() {
        return dataSource;
    }
}
