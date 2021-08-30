package com.xforceplus.ultraman.oqsengine.calculation.logic.avg;

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
public class AggregationInitialTask extends AbstractTask implements Serializable {

    private static final int DEFAULT_SIZE = 100;

    /**
     * 聚合任务初始化.
     *
     * @param prefix 聚合任务前缀 (appId-version).
     * @param avgEntity EntityClass信息（包括聚合对象和被聚合对象的信息）.
     * @param relationIds 聚合字段的relation集合(默认是主信息ID).
     */
    public AggregationInitialTask(String prefix, IEntityClass avgEntity, List<Long> relationIds) {
        this.prefix = prefix;
        this.avgEntity = avgEntity;
        this.relationIds = relationIds;
    }

    private String prefix;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public IEntityClass getAvgEntity() {
        return avgEntity;
    }

    public void setAvgEntity(IEntityClass avgEntity) {
        this.avgEntity = avgEntity;
    }

    public List<Long> getRelationIds() {
        return relationIds;
    }

    public void setRelationIds(List<Long> relationIds) {
        this.relationIds = relationIds;
    }

    private IEntityClass avgEntity;

    private List<Long> relationIds;


    @Override
    public Class runnerType() {
        return null;
    }
}
