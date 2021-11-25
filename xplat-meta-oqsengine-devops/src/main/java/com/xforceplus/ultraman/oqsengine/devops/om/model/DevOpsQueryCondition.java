package com.xforceplus.ultraman.oqsengine.devops.om.model;

import java.util.List;

/**
 * 统一数据运维条件对象.
 *
 * @copyright: 上海云砺信息科技有限公司
 * @author: youyifan
 * @createTime: 11/3/2021 4:45 PM
 * @description:
 * @history:
 */
public class DevOpsQueryCondition {

    private List<DevOpsQueryConditionItem> entities;

    private List<DevOpsQueryConditionItem> fields;

    public List<DevOpsQueryConditionItem> getEntities() {
        return entities;
    }

    public void setEntities(List<DevOpsQueryConditionItem> entities) {
        this.entities = entities;
    }

    public List<DevOpsQueryConditionItem> getFields() {
        return fields;
    }

    public void setFields(List<DevOpsQueryConditionItem> fields) {
        this.fields = fields;
    }
}
