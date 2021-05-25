package com.xforceplus.ultraman.oqsengine.storage.value.strategy;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import java.util.Arrays;
import java.util.Collection;

/**
 * 逻辑类型和储存类型转换策略.
 *
 * @author dongbin
 * @version 0.1 2020/3/4 13:55
 * @since 1.8
 */
public interface StorageStrategy {

    /**
     * 提示当前支持的逻辑字段类型.
     *
     * @return 逻辑字段类型.
     */
    FieldType fieldType();

    /**
     * 支持的物理储存类型.
     *
     * @return 物理字段类型.
     */
    StorageType storageType();

    /**
     * 将储存类型转换成逻辑类型.
     *
     * @param field        目标字段描述.
     * @param storageValue 目标储存类型.
     * @return 逻辑类型.
     */
    IValue toLogicValue(IEntityField field, StorageValue storageValue);

    /**
     * 将逻辑类型转换成储存类型.
     *
     * @param value 逻辑类型.
     * @return 储存类型.
     */
    StorageValue toStorageValue(IValue value);

    /**
     * 根据逻辑类型得到物理储存名称.
     *
     * @param field 目标逻辑字段.
     * @return 物理储存名称.
     */
    default Collection<String> toStorageNames(IEntityField field) {
        return toStorageNames(field, false);
    }

    /**
     * 根据逻辑类型得到物理储存名称.
     *
     * @param field     目标字段.
     * @param shortName true 需要的为短名称,false不需要.
     * @return 物理储存名称.
     */
    default Collection<String> toStorageNames(IEntityField field, boolean shortName) {
        String logicName = shortName ? Long.toString(field.id(), 36) : Long.toString(field.id());

        return Arrays.asList(
            logicName + storageType().getType()
        );
    }

    /**
     * 判断当前策略预期的值类型是否为多值类型.
     *
     * @return true 多值类型,false 单值类型.
     */
    boolean isMultipleStorageValue();

    /**
     * 表示是否可排序.
     *
     * @return true 可排序,false不可排序.
     */
    default boolean isSortable() {
        return true;
    }

}
