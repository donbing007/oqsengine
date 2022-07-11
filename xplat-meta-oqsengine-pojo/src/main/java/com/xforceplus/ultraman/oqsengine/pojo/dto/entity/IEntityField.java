package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

import java.util.Optional;

/**
 * 表示entity 的字段信息.
 *
 * @author dongbin
 * @version 0.1 2020/2/22 16:29
 * @since 1.8
 */
public interface IEntityField extends Comparable<IEntityField> {

    /**
     * 字段标识.
     *
     * @return 标识.
     */
    long id();

    /**
     * 字符串表示的字段标识.
     *
     * @return 字段标识.
     */
    default String idString() {
        return Long.toString(id());
    }

    /**
     * 字段的名称.
     *
     * @return 名称.
     */
    String name();

    /**
     * 获取字段的名称.
     *
     * @return 字段名称表示.
     */
    default EntityFieldName fieldName() {
        return new EntityFieldName(this);
    }

    /**
     * 字段的中文名.
     *
     * @return 中文名称.
     */
    String cnName();

    /**
     * 字段的类型.
     *
     * @return 类型.
     */
    FieldType type();

    /**
     * 获取字段配置.
     *
     * @return 配置.
     */
    FieldConfig config();

    /**
     * 获取枚举类型的字典id.
     *
     * @return 配置
     */
    String dictId();

    /**
     * 获取默认值信息.
     *
     * @return 配置
     */
    Optional<String> defaultValue();

    /**
     * 字段计算类型.
     *
     * @return 计算类型.
     */
    CalculationType calculationType();

    /**
     * 是否需要在创建索引时索引附件信息.
     *
     * @return true/false.
     */
    default boolean indexAttachment() {
        return true;
    }

    /**
     * 名称是否等于当前名称.
     *
     * @param name 被检测的名称.
     * @return true 一致, false不一致.
     */
    default boolean acceptName(String name) {
        return name().equals(name);
    }

    /**
     * 比较.
     *
     * @param target 比较目标.
     * @return 小于0 当前小于target, 大于0 当前大于target,等于0两者相等.
     */
    @Override
    default int compareTo(IEntityField target) {
        if (this.id() < target.id()) {
            return -1;
        } else if (this.id() > target.id()) {
            return 1;
        } else {
            return 0;
        }
    }
}
