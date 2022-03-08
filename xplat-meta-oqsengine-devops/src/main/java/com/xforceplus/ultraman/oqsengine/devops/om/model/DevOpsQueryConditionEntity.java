package com.xforceplus.ultraman.oqsengine.devops.om.model;

import java.util.List;

/**
 * 统一数据运维请求实体.
 *
 * @copyright: 上海云砺信息科技有限公司
 * @author: youyifan
 * @createTime: 11/8/2021 4:56 PM
 * @description:
 * @history:
 */
public class DevOpsQueryConditionEntity {

    private String code;

    private List<DevOpsQueryConditionItem> fields;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<DevOpsQueryConditionItem> getFields() {
        return fields;
    }

    public void setFields(List<DevOpsQueryConditionItem> fields) {
        this.fields = fields;
    }
}
