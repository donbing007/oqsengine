package com.xforceplus.ultraman.oqsengine.sdk.vo.dto;

import java.util.List;

public class SubFieldCondition {

    private String code;

    private List<FieldCondition> fields;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<FieldCondition> getFields() {
        return fields;
    }

    public void setFields(List<FieldCondition> fields) {
        this.fields = fields;
    }
}
