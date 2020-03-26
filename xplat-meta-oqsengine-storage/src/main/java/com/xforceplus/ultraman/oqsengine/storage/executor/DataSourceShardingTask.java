package com.xforceplus.ultraman.oqsengine.storage.executor;

import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;

import javax.sql.DataSource;

/**
 * 支持数据源分片任务超类.
 *
 * @author dongbin
 * @version 0.1 2020/2/17 20:07
 * @since 1.8
 */
public abstract class DataSourceShardingTask implements ShardDataSourceTask {

    private Selector<DataSource> dataSourceSelector;
    private String shardKey;
    private OpTypeEnum opTypeEnum;

    public DataSourceShardingTask(
        Selector<DataSource> dataSourceSelector, String shardKey) {
        this.dataSourceSelector = dataSourceSelector;
        this.shardKey = shardKey;
    }

    public DataSourceShardingTask(
            Selector<DataSource> dataSourceSelector, String shardKey, OpTypeEnum opTypeEnum) {
        this.dataSourceSelector = dataSourceSelector;
        this.shardKey = shardKey;
        this.opTypeEnum = opTypeEnum;
    }

    @Override
    public Selector<DataSource> getDataSourceSelector() {
        return dataSourceSelector;
    }

    @Override
    public String getShardKey() {
        return shardKey;
    }

    public OpTypeEnum getOpType(){
        return opTypeEnum;
    }
}
