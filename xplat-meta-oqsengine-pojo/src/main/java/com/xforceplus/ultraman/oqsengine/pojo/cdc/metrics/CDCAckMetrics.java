package com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.CDCStatus;
import java.util.ArrayList;
import java.util.List;

/**
 * desc :.
 * name : CDCMetrics
 *
 * @author : xujia 2020/11/3
 * @since : 1.8
 */
public class CDCAckMetrics {

    //  表示当前的cdc消费端连接是否正常(true代表正常)
    private CDCStatus cdcConsumerStatus;

    //  上一次同步的时间(单位为时间戳)
    private long lastUpdateTime;

    //  上一次消费成功binlog的时间(单位为时间戳)
    private long lastConsumerTime;

    //  上一次成功连接的时间(单位为时间戳)
    private long lastConnectedTime;

    //  当前批次的总耗时
    private long totalUseTime;

    //  当前批次落库条数
    private int executeRows;

    //  上一次成功消费后的tx事务列表
    private List<Long> commitList;

    @JsonCreator
    public CDCAckMetrics(@JsonProperty("cdcConsumerStatus")CDCStatus cdcConsumerStatus) {
        this.cdcConsumerStatus = cdcConsumerStatus;
        this.commitList = new ArrayList<>();
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public long getLastConsumerTime() {
        return lastConsumerTime;
    }

    public void setLastConsumerTime(long lastConsumerTime) {
        this.lastConsumerTime = lastConsumerTime;
    }

    public long getLastConnectedTime() {
        return lastConnectedTime;
    }

    public void setLastConnectedTime(long lastConnectedTime) {
        this.lastConnectedTime = lastConnectedTime;
    }

    public CDCStatus getCdcConsumerStatus() {
        return cdcConsumerStatus;
    }

    public void setCdcConsumerStatus(CDCStatus cdcConsumerStatus) {
        this.cdcConsumerStatus = cdcConsumerStatus;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public List<Long> getCommitList() {
        return commitList;
    }

    public void setCommitList(List<Long> commitList) {
        this.commitList = commitList;
    }

    public long getTotalUseTime() {
        return totalUseTime;
    }

    public void setTotalUseTime(long totalUseTime) {
        this.totalUseTime = totalUseTime;
    }

    public int getExecuteRows() {
        return executeRows;
    }

    public void setExecuteRows(int executeRows) {
        this.executeRows = executeRows;
    }
}
