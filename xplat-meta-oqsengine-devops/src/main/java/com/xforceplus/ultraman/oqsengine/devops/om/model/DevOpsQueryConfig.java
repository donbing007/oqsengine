package com.xforceplus.ultraman.oqsengine.devops.om.model;

import java.util.List;

/**
 * @copyright： 上海云砺信息科技有限公司
 * @author: youyifan
 * @createTime: 11/3/2021 4:45 PM
 * @description:
 * @history:
 */
public class DevOpsQueryConfig {

    private Long entityClassId;
    /**
     * 页面大小.
     */
    private Long pageSize;
    /**
     * 页面页数.
     */
    private Long pageNo;

    private DevOpsQueryEntity entity;

    private DevOpsQueryCondition conditions;

    private List<DevOpsQuerySort> sort;

    public Long getEntityClassId() {
        return entityClassId;
    }

    public void setEntityClassId(Long entityClassId) {
        this.entityClassId = entityClassId;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }

    public Long getPageNo() {
        return pageNo;
    }

    public void setPageNo(Long pageNo) {
        this.pageNo = pageNo;
    }

    public DevOpsQueryEntity getEntity() {
        return entity;
    }

    public void setEntity(DevOpsQueryEntity entity) {
        this.entity = entity;
    }

    public DevOpsQueryCondition getConditions() {
        return conditions;
    }

    public void setConditions(DevOpsQueryCondition conditions) {
        this.conditions = conditions;
    }

    public List<DevOpsQuerySort> getSort() {
        return sort;
    }

    public void setSort(List<DevOpsQuerySort> sort) {
        this.sort = sort;
    }
}
