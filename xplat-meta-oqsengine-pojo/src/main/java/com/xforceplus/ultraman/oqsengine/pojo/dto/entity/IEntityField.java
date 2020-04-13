package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

/**
 * 表示entity 的字段信息.
 * @author dongbin
 * @version 0.1 2020/2/22 16:29
 * @since 1.8
 */
public interface IEntityField {

    /**
     * 字段标识.
     * @return 标识.
     */
    long id();

    /**
     * 字段的名称.
     * @return 名称.
     */
    String name();

    /**
     * 字段的类型.
     * @return 类型.
     */
    FieldType type();

    /**
     * 获取字段配置.
     * @return 配置.
     */
    FieldConfig config();

    /**
     * 获取枚举类型的字典id
     * @return 配置
     */
    String dictId();

    /**
     * 获取默认值信息
     * @return 配置
     */
    String defaultValue();

    default Boolean acceptName(String name){
        return name().equals(name);
    }
}
