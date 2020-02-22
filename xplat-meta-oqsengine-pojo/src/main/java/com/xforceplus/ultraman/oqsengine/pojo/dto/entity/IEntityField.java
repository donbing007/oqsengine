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
}
