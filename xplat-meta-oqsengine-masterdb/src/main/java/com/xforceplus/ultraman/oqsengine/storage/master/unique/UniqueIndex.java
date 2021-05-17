package com.xforceplus.ultraman.oqsengine.storage.master.unique;

import com.xforceplus.ultraman.oqsengine.storage.master.unique.impl.SortedEntityField;

import java.util.List;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 2020/10/27 2:54 PM
 */
public class UniqueIndex {

    private String name;
    private String code;
    private List<SortedEntityField> fields;

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

    public List<SortedEntityField> getFields() {

       return fields;
    }


    public void setFields(List<SortedEntityField> fields) {
        this.fields = fields;
    }

    public void add(SortedEntityField field) {
        this.fields.add(field);
    }
}
