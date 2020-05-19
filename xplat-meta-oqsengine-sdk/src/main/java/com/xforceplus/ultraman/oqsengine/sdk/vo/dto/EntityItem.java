package com.xforceplus.ultraman.oqsengine.sdk.vo.dto;

import java.util.Collections;
import java.util.List;

/**
 * entity Item
 */
public class EntityItem {

    List<String> fields;

    List<SubEntityItem> entities;

    public List<String> getFields() {
        if (fields == null) return Collections.emptyList();
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public List<SubEntityItem> getEntities() {
        if (entities == null) return Collections.emptyList();
        return entities;
    }

    public void setEntities(List<SubEntityItem> entities) {
        this.entities = entities;
    }
}
