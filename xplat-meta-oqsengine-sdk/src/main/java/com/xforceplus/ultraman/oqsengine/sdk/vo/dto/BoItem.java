package com.xforceplus.ultraman.oqsengine.sdk.vo.dto;

import java.util.List;
import java.util.Map;

public class BoItem implements ResponseItem{

    private Map<String, ApiItem> api;

    private List<FieldItem> fields;

    private String parentEntityId;

    private List<String> subEntities;

    public BoItem() {
    }

    public BoItem(Map<String, ApiItem> api, List<FieldItem> fields, String parentEntityId, List<String> subEntities) {
        this.api = api;
        this.fields = fields;
        this.subEntities = subEntities;
        this.parentEntityId = parentEntityId;
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

    public String getParentEntityId() {
        return parentEntityId;
    }

    public void setParentEntityId(String parentEntityId) {
        this.parentEntityId = parentEntityId;
    }

    public List<String> getSubEntities() {
        return subEntities;
    }

    public void setSubEntities(List<String> subEntities) {
        this.subEntities = subEntities;
    }

    @Override
    public String toString() {
        return "BoItem{" +
                "api=" + api +
                ", fields=" + fields +
                ", parentEntityId='" + parentEntityId + '\'' +
                ", subEntities=" + subEntities +
                '}';
    }
}
