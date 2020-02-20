package com.xforceplus.ultraman.oqsengine.sdk.vo.dto;

public class QuadRow {

    private String id;

    private String code;

    private String name;

    private String desc;

    public QuadRow() {
    }

    public QuadRow(String id, String code, String name, String desc) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
