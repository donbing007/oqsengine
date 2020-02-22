package com.xforceplus.ultraman.oqsengine.storage.executor;

import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;

import javax.sql.DataSource;

/**
 * @author dongbin
 * @version 0.1 2020/2/17 20:07
 * @since 1.8
 */
public abstract class DataSourceShardingTask implements ShardDataSourceTask {

    private Selector<DataSource> dataSourceSelector;
    private String shardKey;

    public DataSourceShardingTask(Selector<DataSource> dataSourceSelector, String shardKey) {
        this.dataSourceSelector = dataSourceSelector;
        this.shardKey = shardKey;
    }

    @Override
    public Selector<DataSource> getDataSourceSelector() {
        return dataSourceSelector;
    }

    @Override
    public String getShardKey() {
        return shardKey;
    }
}
