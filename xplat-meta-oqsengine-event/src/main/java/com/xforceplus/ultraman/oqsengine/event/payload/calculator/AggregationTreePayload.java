package com.xforceplus.ultraman.oqsengine.event.payload.calculator;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Aggregation类型的payload.
 *
 * @className: AggregationTreePayload.
 * @author: wangzheng.
 * @date: 2021/9/1 16:57.
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
     * 当前版本的结构.
     */
    private List<IEntityClass> entityList;

    /**
     * 初始化.
     *
     * @param appId appid
     * @param version 版本
     * @param entityList entity集合
     */
    /**
     * 聚合树构建事件.
     *
     * @param appId 应用id.
     * @param version 应用版本.
     * @param entityList 对象列表.
     */
    public AggregationTreePayload(String appId, int version, List<IEntityClass> entityList) {
        this.appId = appId;
        this.version = version;
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

    public List<IEntityClass> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<IEntityClass> entityList) {
        this.entityList = entityList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AggregationTreePayload)) {
            return false;
        }
        AggregationTreePayload that = (AggregationTreePayload) o;
        return getVersion() == that.getVersion()
                && Objects.equals(getAppId(), that.getAppId())
                && Objects.equals(getEntityList(), that.getEntityList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAppId(), getVersion(), getEntityList());
    }

}
