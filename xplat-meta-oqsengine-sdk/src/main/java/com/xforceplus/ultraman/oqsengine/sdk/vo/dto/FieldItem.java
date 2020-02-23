package com.xforceplus.ultraman.oqsengine.sdk.vo.dto;

public class FieldItem {

    String code;

    String name;

    String type;

    String maxLength;

    String editable;

    String searchable;

    String required;

    String enumCode;

    String displayType;

    SoloItem relationshipEntity;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(String maxLength) {
        this.maxLength = maxLength;
    }

    public String getEditable() {
        return editable;
    }

    public void setEditable(String editable) {
        this.editable = editable;
    }

    public String getSearchable() {
        return searchable;
    }

    public void setSearchable(String searchable) {
        this.searchable = searchable;
    }

    public String getRequired() {
        return required;
    }

    public void setRequired(String required) {
        this.required = required;
    }

    public String getEnumCode() {
        return enumCode;
    }

    public void setEnumCode(String enumCode) {
        this.enumCode = enumCode;
    }

    public String getDisplayType() {
        return displayType;
    }

    public void setDisplayType(String displayType) {
        this.displayType = displayType;
    }

    public SoloItem getRelationshipEntity() {
        return relationshipEntity;
    }

    public void setRelationshipEntity(SoloItem relationshipEntity) {
        this.relationshipEntity = relationshipEntity;
    }
}
