package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * 字段配置信息.
 * @author dongbin
 * @version 0.1 2020/2/26 14:32
 * @since 1.8
 */
public class FieldConfig implements Serializable {

    /**
     * 是否可搜索.true 可搜索,false 不可搜索.
     */
    private boolean searchable = false;

    /**
     * 字符串:     最大字符个数.
     * 数字:       最大的数字.
     * 日期/时间:   离现在最近的时间日期.
     */
    private long max = Long.MAX_VALUE;

    /**
     * 字符串:     最小字符个数.
     * 数字:       最小的数字.
     * 日期/时间:   离现在最远的时间日期.
     */
    private long min = Long.MIN_VALUE;

    /**
     * 是否为数据标识.
     */
    private boolean identifie = false;

    /**
     * 创建一个新的 FieldConfig.
     * @return 实例.
     */
    public static FieldConfig build() {
        return new FieldConfig();
    }

    /**
     * 设置是否可搜索,默认不搜索.
     * @param searchable true 可搜索, false 不可搜索.
     * @return 当前实例.
     */
    public FieldConfig searchable(boolean searchable) {
        this.searchable = searchable;
        return this;
    }

    /**
     * 字符串:     最大字符个数.
     * 数字:       最大的数字.
     * 日期/时间:   离现在最近的时间日期.
     *
     * @param max 值.
     * @return 当前实例.
     */
    public FieldConfig max(long max) {
        this.max = max;
        return this;
    }

    /**
     * 字符串:     最小字符个数.
     * 数字:       最小的数字.
     * 日期/时间:   离现在最远的时间日期.
     *
     * @param min 值.
     * @return 当前实例.
     */
    public FieldConfig min(long min) {
        this.min = min;
        return this;
    }

    public FieldConfig identifie(boolean identifie) {
        this.identifie = identifie;
        return this;
    }

    /**
     * 是否表示一个数据标识.
     * @return true 数据标识,false 非数据标识.
     */
    public boolean isIdentifie() {
        return identifie;
    }

    /**
     * 是否可搜索.true 可搜索,false 不可搜索.
     * @return 结果.
     */
    public boolean isSearchable() {
        return searchable;
    }

    /**
     * 获取最大值.
     * @return 最大值.
     */
    public long getMax() {
        return max;
    }

    /**
     * 获取最小值.
     * @return
     */
    public long getMin() {
        return min;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FieldConfig)) {
            return false;
        }
        FieldConfig that = (FieldConfig) o;
        return isSearchable() == that.isSearchable() &&
            getMax() == that.getMax() &&
            getMin() == that.getMin() &&
            isIdentifie() == that.isIdentifie();
    }

    @Override
    public int hashCode() {
        return Objects.hash(isSearchable(), getMax(), getMin(), isIdentifie());
    }

    @Override
    public String toString() {
        return "FieldConfig{" +
            "searchable=" + searchable +
            ", max=" + max +
            ", min=" + min +
            ", identifie=" + identifie +
            '}';
    }
}
