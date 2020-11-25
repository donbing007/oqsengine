package com.xforceplus.ultraman.oqsengine.pojo.dto.summary;

/**
 * desc :
 * name : TableSummary
 *
 * @author : xujia
 * date : 2020/8/19
 * @since : 1.8
 */
public class TableSummary {
    /**
     * tableName 表名
     */
    private String tableName;

    /**
     * count 数量
     */
    private int count;

    public TableSummary(String tableName) {
        this.tableName = tableName;
        this.count = 0;
    }

    public String getTableName() {
        return tableName;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
