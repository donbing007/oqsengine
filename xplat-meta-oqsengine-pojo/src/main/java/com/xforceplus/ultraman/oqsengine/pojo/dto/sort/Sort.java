package com.xforceplus.ultraman.oqsengine.pojo.dto.sort;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

import java.util.Objects;

/**
 * 查询排序方式.
 * @author dongbin
 * @version 0.1 2020/2/22 16:25
 * @since 1.8
 */
public class Sort {

    private static final Sort NO_SORT = new Sort(true);

    private IEntityField field;
    private boolean asc;
    private boolean outOfOrder;

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
     * 构造一个不需要排序的实例.实际的顺序将由实现决定.
     * @return 排序实例.
     */
    public static Sort buildOutOfSort() {
        return NO_SORT;
    }

    /**
     * 不允许实例.
     */
    private Sort(boolean outOfOrder) {
        this.outOfOrder = outOfOrder;
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
     * 是否升序.
     * @return true 升序, false 不是升序.
     */
    public boolean isAsc() {
        return asc;
    }

    /**
     * 是否降序.
     * @return true 升序, false 不是降序.
     */
    public boolean isDes() {
        return !asc;
    }

    /**
     * 是否不须排序.
     * @return true 不需要排序,false 需要排序.
     */
    public boolean isOutOfOrder() {
        return outOfOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Sort)) {
            return false;
        }
        Sort sort = (Sort) o;
        return isAsc() == sort.isAsc() &&
            isOutOfOrder() == sort.isOutOfOrder() &&
            Objects.equals(getField(), sort.getField());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getField(), isAsc(), isOutOfOrder());
    }
}
