package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

import java.sql.Types;
import java.util.Optional;

/**
 * 表示一个字段名称.
 *
 * @author dongbin
 * @version 0.1 2022/4/4 12:02
 * @since 1.8
 */
public class EntityFieldName {

    /*
    关系字段的后辍.
     */
    private static final String RELATIONAL_FIELD_NAME_SUFFIX = ".id";
    /*
    关系字段静态字段的替换后辍.
     */
    private static final String RELATIONAL_FIELD_ORIGINAL_NAME_SUFFIX = "_id";

    private IEntityField field;

    public EntityFieldName(IEntityField field) {
        this.field = field;
    }

    /**
     * 获取字段的动态字段名称.
     * 此名称将在OQS内部使用的名称.
     *
     * @return 字段名称.
     */
    public String dynamicName() {
        return this.field.name();
    }

    /**
     * 如果字段所属于的类型为静态类型.
     * 非静态类型对象没有此名称.
     *
     * @return 静态字段名称.
     */
    public Optional<String> originalName() {
        FieldConfig config = field.config();
        if (config != null) {

            if (config.getJdbcType() != Types.NULL) {

                return Optional.of(buildOriginalName());

            } else {

                return Optional.empty();

            }

        } else {
            return Optional.empty();
        }
    }

    private String buildOriginalName() {
        String name = this.field.name();
        if (name.endsWith(RELATIONAL_FIELD_NAME_SUFFIX)) {
            StringBuilder buff = new StringBuilder();
            buff.append(name);
            int replaceIndex = name.length() - RELATIONAL_FIELD_NAME_SUFFIX.length();
            buff.replace(replaceIndex, buff.length(), RELATIONAL_FIELD_ORIGINAL_NAME_SUFFIX);
            return buff.toString();

        } else {

            return name;

        }
    }
}
