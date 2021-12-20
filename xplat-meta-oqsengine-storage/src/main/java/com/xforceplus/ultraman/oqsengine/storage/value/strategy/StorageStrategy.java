package com.xforceplus.ultraman.oqsengine.storage.value.strategy;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.value.AnyStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.AttachmentStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.LongStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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
     * 将储存类型转换成逻辑类型, 不含附件.
     *
     * @param field        目标字段描述.
     * @param storageValue 目标储存类型.
     * @return 逻辑类型.
     */
    default IValue toLogicValue(IEntityField field, StorageValue storageValue) {
        return toLogicValue(field, storageValue, null);
    }

    /**
     * 将储存类型转换成逻辑类型.
     *
     * @param field        目标字段.
     * @param storageValue 目标物理储存值.
     * @param attachemnt   附件.
     * @return 逻辑类型.
     */
    IValue toLogicValue(IEntityField field, StorageValue storageValue, String attachemnt);

    /**
     * 将逻辑类型转换成储存类型.
     *
     * @param value 逻辑类型.
     * @return 储存类型.
     */
    StorageValue toStorageValue(IValue value);

    /**
     * 将逻辑类型的附件转换成储存类型.
     *
     * @param value 目标逻辑值逻辑类型.
     * @return 储存类型.
     */
    default Optional<StorageValue> toAttachmentStorageValue(IValue value) {
        Optional<String> attachmentOp = value.getAttachment();
        if (!attachmentOp.isPresent()) {
            return Optional.empty();
        } else {
            StringStorageValue sv =
                new StringStorageValue(Long.toString(value.getField().id()), attachmentOp.get(), true);
            return Optional.ofNullable(sv);
        }
    }

    /**
     * 通过离散的物理储存来构造本地的StorageValue.
     *
     * @param storageName  物理储存名称.
     * @param storageValue 物理储存值.
     * @return 实例.
     */
    default StorageValue convertIndexStorageValue(String storageName, Object storageValue, boolean attachment) {
        StorageValue anyStorageValue = AnyStorageValue.getInstance(storageName);
        if (!attachment) {
            switch (anyStorageValue.type()) {
                case STRING:
                    return new StringStorageValue(storageName, (String) storageValue, false);
                case LONG: {
                    long value = 0;
                    if (Integer.class.isInstance(storageValue)) {
                        value = ((Integer) storageValue).longValue();
                    } else if (Long.class.isInstance(storageValue)) {
                        value = ((Long) storageValue).longValue();
                    } else {
                        throw new IllegalArgumentException(
                            String.format("The expectation is an int or a long, but the actual type is %s.",
                                storageValue.getClass().toString()));
                    }
                    return new LongStorageValue(storageName, value, false);
                }
                default:
                    throw new IllegalArgumentException(
                        String.format("Unrecognized physical storage type.[%d]", storageName));
            }
        } else {

            return new AttachmentStorageValue(storageName, (String) storageValue, false);

        }
    }

    /**
     * 获取第一个储存名称.
     *
     * @param field 目标字段.
     * @return 物理储存名称.
     */
    default String toFirstStorageName(IEntityField field) {
        List<String> names = (List<String>) toStorageNames(field);
        return names.get(0);
    }

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
