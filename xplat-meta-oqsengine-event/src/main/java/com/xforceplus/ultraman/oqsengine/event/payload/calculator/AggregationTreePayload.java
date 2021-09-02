package com.xforceplus.ultraman.oqsengine.event.payload.calculator;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Aggregation类型的payload.
 *
 * @className: AggregationTreePayload
 * @package: com.xforceplus.ultraman.oqsengine.event.payload.calculator
 * @author: wangzheng
 * @date: 2021/9/1 16:57
 */
public class AggregationTreePayload implements Serializable {
    /**
     * 应用id.
     */
    private String appId;

    /**
     * 应用版本.
     */
    private int version;

    /**
     * 上一版本的结构.
     */
    private List<EntityClass> preEntityList;

    /**
     * 当前版本的结构.
     */
    private List<EntityClass> entityList;

    public AggregationTreePayload(String appId, int version, List<EntityClass> preEntityList, List<EntityClass> entityList) {
        this.appId = appId;
        this.version = version;
        this.preEntityList = preEntityList;
        this.entityList = entityList;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public List<EntityClass> getPreEntityList() {
        return preEntityList;
    }

    public void setPreEntityList(List<EntityClass> preEntityList) {
        this.preEntityList = preEntityList;
    }

    public List<EntityClass> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<EntityClass> entityList) {
        this.entityList = entityList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AggregationTreePayload)) return false;
        AggregationTreePayload that = (AggregationTreePayload) o;
        return getVersion() == that.getVersion() &&
                Objects.equals(getAppId(), that.getAppId()) &&
                Objects.equals(getPreEntityList(), that.getPreEntityList()) &&
                Objects.equals(getEntityList(), that.getEntityList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAppId(), getVersion(), getPreEntityList(), getEntityList());
    }

    @Override
    public String toString() {
        return "AggregationTreePayload{" +
                "appId='" + appId + '\'' +
                ", version=" + version +
                ", preEntityList=" + preEntityList +
                ", entityList=" + entityList +
                '}';
    }
}
