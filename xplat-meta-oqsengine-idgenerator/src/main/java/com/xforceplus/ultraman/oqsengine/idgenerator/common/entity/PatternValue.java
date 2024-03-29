package com.xforceplus.ultraman.oqsengine.idgenerator.common.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 5/13/21 4:40 PM
 */
public class PatternValue implements Serializable, Cloneable {

    private static final long serialVersionUID = -8015203769645869019L;

    public PatternValue(@JsonProperty("id") long id, @JsonProperty("value") String value) {
        this.id = id;
        this.value = value;
    }

    public PatternValue() {

    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new RuntimeException("Clone paternValue failed!");
        }
    }

    /**
     * 自增序列中的ID.
     */
    private long id;

    /**
     * 根据自增编号模式处理后自增编号.
     */
    private String value;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "PatternValue{"
            + "id=" + id
            + ", value='" + value + '\''
            + '}';
    }
}
