package com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper;

import com.xforceplus.ultraman.oqsengine.storage.value.LongStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;

/**
 * 浮点值帮助工具.
 *
 * @author dongbin
 * @version 0.1 2022/3/9 12:05
 * @since 1.8
 */
public class JdbcOriginalFieldHelper {

    /**
     * 浮点类型原生字段处理帮助工具.
     *
     * @param fieldName 字段逻辑名称.
     * @param plainValue 浮点值字符串表示.
     * @return OQS物理值实例.
     */
    public static StorageValue buildDecimalStorageValue(String fieldName, String plainValue) {
        long integerPart = 0;
        long decimalPart = 0;
        if (plainValue.indexOf('.') > 0) {
            String[] plainValues = plainValue.split("\\.");
            integerPart = Long.parseLong(plainValues[0]);
            decimalPart = Long.parseLong(plainValues[1]);
        } else {
            integerPart = Long.parseLong(plainValue);
        }

        LongStorageValue decimalValue = new LongStorageValue(fieldName, integerPart, true);
        return decimalValue.stick(
            new LongStorageValue(fieldName, decimalPart, true)
        );
    }

    /**
     * 构造一个浮点 StorageValue 的字符串表示.
     *
     * @param storageValue oqs物理储存值.
     * @return 字符串表示.
     */
    public static String buildDecimalStorageValuePlainValue(StorageValue storageValue) {
        return String.format("%d.%d", storageValue.value(), storageValue.next().value());
    }
}
