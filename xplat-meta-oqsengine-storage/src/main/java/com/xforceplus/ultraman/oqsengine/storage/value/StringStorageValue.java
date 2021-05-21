package com.xforceplus.ultraman.oqsengine.storage.value;

/**
 * 字符串储存类型.
 *
 * @author dongbin
 * @version 0.1 2020/3/4 13:34
 * @since 1.8
 */
public class StringStorageValue extends AbstractStorageValue<String> {

    public StringStorageValue(String name, String value, boolean logicName) {
        super(name, value, logicName);
    }
}
