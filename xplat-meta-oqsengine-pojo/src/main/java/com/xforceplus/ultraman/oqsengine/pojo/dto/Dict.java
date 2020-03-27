package com.xforceplus.ultraman.oqsengine.pojo.dto;


import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 字典对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
public class Dict implements Serializable {
    /**
     * 名称
     */
    private String name;

    /**
     * 编码
     */
    private String code;

    /**
     * 字典详情
     */
    private List<DictDetail> dictDetails;

    public Dict() {
    }

    public Dict(String name, String code, List<DictDetail> dictDetails) {
        this.name = name;
        this.code = code;
        this.dictDetails = dictDetails;
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

    public List<DictDetail> getDictDetails() {
        return dictDetails;
    }

    public void setDictDetails(List<DictDetail> dictDetails) {
        this.dictDetails = dictDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dict)) return false;
        Dict dict = (Dict) o;
        return Objects.equals(getName(), dict.getName()) &&
                Objects.equals(getCode(), dict.getCode()) &&
                Objects.equals(getDictDetails(), dict.getDictDetails());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getCode(), getDictDetails());
    }

    @Override
    public String toString() {
        return "Dict{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", dictDetails=" + dictDetails +
                '}';
    }
}
