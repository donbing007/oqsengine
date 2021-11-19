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
public class DevOpsQueryEntity {

    private List<String> entities;

    private List<String> fields;

    public List<String> getEntities() {
        return entities;
    }

    public void setEntities(List<String> entities) {
        this.entities = entities;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }
}
