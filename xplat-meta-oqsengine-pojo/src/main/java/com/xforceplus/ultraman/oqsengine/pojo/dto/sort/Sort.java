package com.xforceplus.ultraman.oqsengine.pojo.dto.sort;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 * 查询排序方式.
 * @author dongbin
 * @version 0.1 2020/2/22 16:25
 * @since 1.8
 */
public class Sort {

    private IEntityField field;
    private boolean asc;

    /**
     * 构造一个降序排序实例.
     * @param field 目标字段.
     * @return 排序实例.
     */
    public static Sort buildAscSort(IEntityField field) {
        return new Sort(field, true);
    }

    /**
     * 构造一个降序实例.
     * @param field 目标字段.
     * @return 排序实例.
     */
    public static Sort buildDescSort(IEntityField field) {
        return new Sort(field, false);
    }

    /**
     * 不允许外部实例化.
     */
    private Sort(IEntityField field, boolean asc) {
        this.field = field;
        this.asc = asc;
    }

    /**
     * 得到排序的字段.
     * @return 目标排序字段.
     */
    public IEntityField getField() {
        return field;
    }

    /**
     * 是否升序.否则为降序.
     * @return true 升序, false 降序.
     */
    public boolean isAsc() {
        return asc;
    }
}
