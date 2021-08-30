package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.task.AbstractTask;
import java.io.Serializable;
import java.util.List;

/**
 * 聚合初始化任务.
 *
 * @author weikai
 * @version 1.0 2021/8/26 15:21
 * @since 1.8
 */
public class AggregationTask extends AbstractTask implements Serializable {

    private static final int DEFAULT_SIZE = 100;

    /**
     * 聚合任务初始化.
     *
     * @param prefix 聚合任务前缀 (appId-version).
     * @param avgEntity EntityClass信息（包括聚合对象和被聚合对象的信息）.
     */
    public AggregationTask(String prefix, List<IEntityClass> avgEntity) {
        this.prefix = prefix;
        this.avgEntitys = avgEntity;
    }

    private String prefix;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public List<IEntityClass> getAvgEntity() {
        return avgEntitys;
    }

    public void setAvgEntity(List<IEntityClass> avgEntity) {
        this.avgEntitys = avgEntity;
    }

    public List<Long> getRelationIds() {
        return relationIds;
    }

    public void setRelationIds(List<Long> relationIds) {
        this.relationIds = relationIds;
    }

    private List<IEntityClass> avgEntitys;

    private List<Long> relationIds;


    @Override
    public Class runnerType() {
        return null;
    }
}
