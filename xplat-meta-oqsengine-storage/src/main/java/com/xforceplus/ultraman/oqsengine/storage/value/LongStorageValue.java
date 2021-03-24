package com.xforceplus.ultraman.oqsengine.storage.value;

/**
 * 长整形的储存类值类型.
 * @author dongbin
 * @version 0.1 2020/3/4 13:31
 * @since 1.8
 */
public class LongStorageValue extends AbstractStorageValue<Long> {

    public LongStorageValue(String name, long value, boolean logicName) {
        super(name, value, logicName);
    }
}
