package com.xforceplus.ultraman.oqsengine.sdk.vo.dto;

import java.util.List;
import java.util.Map;

public class BoItem implements ResponseItem{

    private Map<String, ApiItem> api;

    private List<FieldItem> fields;

    private List<Long> subEntities;

    public BoItem() {
    }

    public BoItem(Map<String, ApiItem> api, List<FieldItem> fields, List<Long> subEntities) {
        this.api = api;
        this.fields = fields;
        this.subEntities = subEntities;
    }

    public Map<String, ApiItem> getApi() {
        return api;
    }

    public void setApi(Map<String, ApiItem> api) {
        this.api = api;
    }

    public List<FieldItem> getFields() {
        return fields;
    }

    public void setFields(List<FieldItem> fields) {
        this.fields = fields;
    }

    public List<Long> getSubEntities() {
        return subEntities;
    }

    public void setSubEntities(List<Long> subEntities) {
        this.subEntities = subEntities;
    }
}
