package com.xforceplus.ultraman.oqsengine.devops.om.model;

/**
 * 统一数据运维响应.
 *
 * @copyright: 上海云砺信息科技有限公司
 * @author: youyifan
 * @createTime: 11/3/2021 4:45 PM
 * @description:
 * @history:
 */
public class DevOpsDataResponse {
    private long txId;
    private long entityId;
    private int version;
    private int eventType;
    private String message;

    /**
     * 构造函数.
     *
     * @param txId  事务ID
     * @param entityId 实体ID
     * @param version 版本
     * @param eventType 事件类型
     * @param message 消息
     */
    public DevOpsDataResponse(long txId, long entityId, int version, int eventType, String message) {
        this.txId = txId;
        this.entityId = entityId;
        this.version = version;
        this.eventType = eventType;
        this.message = message;
    }

    public long getTxId() {
        return txId;
    }

    public void setTxId(long txId) {
        this.txId = txId;
    }

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
