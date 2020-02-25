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

    public FieldItem(){
    }

    public FieldItem(String code, String name, String type, String maxLength, String editable
            , String searchable, String required, String enumCode, String displayType, SoloItem relationshipEntity) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.maxLength = maxLength;
        this.editable = editable;
        this.searchable = searchable;
        this.required = required;
        this.enumCode = enumCode;
        this.displayType = displayType;
        this.relationshipEntity = relationshipEntity;
    }

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

    @Override
    public String toString() {
        return "FieldItem{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", maxLength='" + maxLength + '\'' +
                ", editable='" + editable + '\'' +
                ", searchable='" + searchable + '\'' +
                ", required='" + required + '\'' +
                ", enumCode='" + enumCode + '\'' +
                ", displayType='" + displayType + '\'' +
                ", relationshipEntity=" + relationshipEntity +
                '}';
    }
}
