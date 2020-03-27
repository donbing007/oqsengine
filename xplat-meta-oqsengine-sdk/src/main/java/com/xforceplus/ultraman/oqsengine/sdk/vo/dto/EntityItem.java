package com.xforceplus.ultraman.oqsengine.sdk.vo.dto;

import java.util.List;

/**
 * entity Item
 */
public class EntityItem {

    List<String> fields;

    List<SubEntityItem> entities;

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public List<SubEntityItem> getEntities() {
        return entities;
    }

    public void setEntities(List<SubEntityItem> entities) {
        this.entities = entities;
    }
}
