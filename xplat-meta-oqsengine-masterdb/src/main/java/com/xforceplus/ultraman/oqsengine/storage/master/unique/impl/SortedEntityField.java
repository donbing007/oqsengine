package com.xforceplus.ultraman.oqsengine.storage.master.unique.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 2020/10/27 3:06 PM
 */
public class SortedEntityField {
    private int sort;
    private IEntityField field;

    public SortedEntityField(IEntityField field, int sort) {
        this.sort = sort;
        this.field = field;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public IEntityField getField() {
        return field;
    }

    public void setField(IEntityField field) {
        this.field = field;
    }
}
