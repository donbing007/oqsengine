package com.xforceplus.ultraman.oqsengine.pojo.dto;


import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 元数据对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
public class Bo implements Serializable {
    /**
     * 对象id.
     */
    private Long id;
    /**
     * 业务对象编码
     */
    private String code;
    /**
     * ApiList列表
     */
    private List<Api> apis;
    /**
     * fieldList列表
     */
    private List<EntityField> fields;
    /**
     * 子对象List列表
     */
    private List<Bo> bos;

    public Bo() {
    }

    public Bo(Long id, String code, List<Api> apis, List<EntityField> fields, List<Bo> bos) {
        this.id = id;
        this.code = code;
        this.apis = apis;
        this.fields = fields;
        this.bos = bos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<Api> getApis() {
        return apis;
    }

    public void setApis(List<Api> apis) {
        this.apis = apis;
    }

    public List<EntityField> getFields() {
        return fields;
    }

    public void setFields(List<EntityField> fields) {
        this.fields = fields;
    }

    public List<Bo> getBos() {
        return bos;
    }

    public void setBos(List<Bo> bos) {
        this.bos = bos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Bo)) {
            return false;
        }
        Bo bo = (Bo) o;
        return Objects.equals(getId(), bo.getId()) &&
                Objects.equals(getCode(), bo.getCode()) &&
                Objects.equals(getApis(), bo.getApis()) &&
                Objects.equals(getFields(), bo.getFields()) &&
                Objects.equals(getBos(), bo.getBos());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getCode(), getApis(), getFields(), getBos());
    }

    @Override
    public String toString() {
        return "Bo{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", apis=" + apis +
                ", fields=" + fields +
                ", bos=" + bos +
                '}';
    }
}
