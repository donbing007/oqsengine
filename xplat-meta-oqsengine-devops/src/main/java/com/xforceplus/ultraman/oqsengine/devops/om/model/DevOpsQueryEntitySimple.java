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
public class DevOpsQueryEntitySimple {

    private String code;

    private List<String> fields;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }
}
