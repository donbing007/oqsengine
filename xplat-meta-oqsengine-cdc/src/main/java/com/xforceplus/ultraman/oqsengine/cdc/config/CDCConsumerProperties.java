package com.xforceplus.ultraman.oqsengine.cdc.config;

import static com.xforceplus.ultraman.oqsengine.cdc.constant.CDCConstant.*;

/**
 * desc :
 * name : CDCConsumerProperties
 *
 * @author : xujia
 * date : 2020/11/3
 * @since : 1.8
 */
public class CDCConsumerProperties {

    //  ip
    private String cdcConnectString;
    //  port
    private int cdcConnectPort;
    //  cdc destination
    private String cdcDestination;
    //  user
    private String cdcUserName;
    //  pass
    private String cdcPassword;

    private int reconnectWaitInSeconds = DEFAULT_RECONNECT_WAIT_IN_SECONDS;

    private int freeMessageWaitInSeconds = DEFAULT_FREE_MESSAGE_WAIT_IN_SECONDS;

    private int freeMessageMaxReportThreshold = DEFAULT_FREE_MESSAGE_MAX_REPORT_THRESHOLD;

    private String subscribeFilter = DEFAULT_SUBSCRIBE_FILTER;

    private int batchSize = DEFAULT_BATCH_SIZE;

    public CDCConsumerProperties(String cdcConnectString, int cdcConnectPort, String cdcDestination, String cdcUserName, String cdcPassword) {
        this.cdcConnectString = cdcConnectString;
        this.cdcConnectPort = cdcConnectPort;
        this.cdcDestination = cdcDestination;
        this.cdcUserName = cdcUserName;
        this.cdcPassword = cdcPassword;
    }

    public int getReconnectWaitInSeconds() {
        return reconnectWaitInSeconds;
    }

    public void setReconnectWaitInSeconds(int reconnectWaitInSeconds) {
        this.reconnectWaitInSeconds = reconnectWaitInSeconds;
    }

    public int getFreeMessageWaitInSeconds() {
        return freeMessageWaitInSeconds;
    }

    public void setFreeMessageWaitInSeconds(int freeMessageWaitInSeconds) {
        this.freeMessageWaitInSeconds = freeMessageWaitInSeconds;
    }

    public int getFreeMessageMaxReportThreshold() {
        return freeMessageMaxReportThreshold;
    }

    public void setFreeMessageMaxReportThreshold(int freeMessageMaxReportThreshold) {
        this.freeMessageMaxReportThreshold = freeMessageMaxReportThreshold;
    }

    public String getCdcConnectString() {
        return cdcConnectString;
    }

    public void setCdcConnectString(String cdcConnectString) {
        this.cdcConnectString = cdcConnectString;
    }

    public int getCdcConnectPort() {
        return cdcConnectPort;
    }

    public void setCdcConnectPort(int cdcConnectPort) {
        this.cdcConnectPort = cdcConnectPort;
    }

    public String getCdcDestination() {
        return cdcDestination;
    }

    public void setCdcDestination(String cdcDestination) {
        this.cdcDestination = cdcDestination;
    }

    public String getCdcUserName() {
        return cdcUserName;
    }

    public void setCdcUserName(String cdcUserName) {
        this.cdcUserName = cdcUserName;
    }

    public String getCdcPassword() {
        return cdcPassword;
    }

    public void setCdcPassword(String cdcPassword) {
        this.cdcPassword = cdcPassword;
    }

    public String getSubscribeFilter() {
        return subscribeFilter;
    }

    public void setSubscribeFilter(String subscribeFilter) {
        this.subscribeFilter = subscribeFilter;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
