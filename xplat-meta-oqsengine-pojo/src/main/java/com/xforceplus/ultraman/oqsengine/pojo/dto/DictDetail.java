package com.xforceplus.ultraman.oqsengine.pojo.dto;


import java.io.Serializable;
import java.util.Objects;

/**
 * 字典内容对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
public class DictDetail implements Serializable {
    /**
     * 名称
     */
    private String name;

    /**
     * 编码
     */
    private String code;

    public DictDetail() {
    }

    public DictDetail(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DictDetail)) return false;
        DictDetail that = (DictDetail) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getCode(), that.getCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getCode());
    }

    @Override
    public String toString() {
        return "DictDetail{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}