package com.xforceplus.ultraman.oqsengine.sdk.vo.dto;

import java.util.List;

/**
 * conditions
 */
public class Conditions {

    private List<FieldCondition> fields;

    private List<SubFieldCondition> entities;

    public List<FieldCondition> getFields() {
        return fields;
    }

    public void setFields(List<FieldCondition> fields) {
        this.fields = fields;
    }

    public List<SubFieldCondition> getEntities() {
        return entities;
    }

    public void setEntities(List<SubFieldCondition> entities) {
        this.entities = entities;
    }
}
