package com.xforceplus.ultraman.oqsengine.pojo.dto.summary;

import javax.sql.DataSource;
import java.util.List;

/**
 * desc :
 * name : DataSourceSummary
 *
 * @author : xujia
 * date : 2020/8/19
 * @since : 1.8
 */
public class DataSourceSummary {
    /**
     * dataSourceName 数据源
     */
    private DataSource dataSource;

    private String poolName;

    /**
     * tableSummaries 分布的表概述
     */
    private List<TableSummary> tableSummaries;

    public DataSourceSummary(DataSource ds, String poolName, List<TableSummary> tableSummaries) {
        this.dataSource = ds;
        this.tableSummaries = tableSummaries;
        this.poolName = poolName;
    }

    public String getPoolName() {
        return poolName;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public List<TableSummary> getTableSummaries() {
        return tableSummaries;
    }

    public int getTotal() {
        return null == tableSummaries ? 0 :
                tableSummaries.stream().mapToInt(TableSummary::getCount).sum();
    }

    public TableSummary next() {
        return tableSummaries.iterator().next();
    }
}
