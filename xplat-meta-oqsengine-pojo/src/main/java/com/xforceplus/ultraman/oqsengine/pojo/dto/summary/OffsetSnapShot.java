package com.xforceplus.ultraman.oqsengine.pojo.dto.summary;

/**
 * desc :
 * name : SimpleSummary
 *
 * @author : xujia
 * date : 2020/9/21
 * @since : 1.8
 */
public class OffsetSnapShot implements Cloneable {
    private String dataSourceName;
    private String tableName;
    private long startId;
    private int left;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return new OffsetSnapShot(dataSourceName, tableName, startId, left);
    }

    public OffsetSnapShot() {

    }

    public OffsetSnapShot(String dataSourceName, String tableName, long startId, int left) {
        this.dataSourceName = dataSourceName;
        this.tableName = tableName;
        this.startId = startId;
        this.left = left;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public String getTableName() {
        return tableName;
    }

    public long getStartId() {
        return startId;
    }

    public int getLeft() {
        return left;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setStartId(long startId) {
        this.startId = startId;
    }

    public void setLeft(int left) {
        this.left = left;
    }
}
