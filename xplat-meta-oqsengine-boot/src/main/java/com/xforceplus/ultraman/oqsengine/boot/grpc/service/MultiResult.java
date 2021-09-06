package com.xforceplus.ultraman.oqsengine.boot.grpc.service;

import java.util.List;
import java.util.Map;

public class MultiResult {

    private Integer successCount;
    private Integer failedCount;
    private List<String> messages;
    private List<Map<String, String>> failedMapList;

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(Integer failedCount) {
        this.failedCount = failedCount;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public List<Map<String, String>> getFailedMapList() {
        return failedMapList;
    }

    public void setFailedMapList(List<Map<String, String>> failedMapList) {
        this.failedMapList = failedMapList;
    }
}
