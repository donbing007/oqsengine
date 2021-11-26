package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value;

import com.xforceplus.ultraman.oqsengine.storage.value.AnyStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.common.StringsStorageStrategy;

/**
 * 处理 Strings 策略实现.
 * 唯一不同于通用的行为是转换来自主库字段值时,会假定处理的是"[RMB][JPY][USD]"这样的字符串.
 *
 * @author dongbin
 * @version 0.1 2021/05/25 14:36
 * @since 1.8
 */
public class SphinxQLStringsStorageStrategy extends StringsStorageStrategy {

    static final char START = '[';
    static final char END = ']';

    /**
     * 预期为一个"[RMB][JPY][USD]"表示的字符串.
     *
     * @param storageName  物理储存名称.
     * @param storageValue 物理储存值.
     * @return 物理储存实例.
     */
    @Override
    public StorageValue convertIndexStorageValue(String storageName, Object storageValue, boolean attachment) {
        String value = (String) storageValue;

        String logicName = AnyStorageValue.getInstance(storageName).logicName();

        if (attachment) {
            return new StringStorageValue(logicName, value, true);
        }

        StringBuilder buff = new StringBuilder();
        StorageValue head = null;
        int location = 0;
        boolean watch = false;
        for (int i = 0; i < value.length(); i++) {
            char point = value.charAt(i);
            if (START == point) {
                watch = true;
                continue;
            }

            if (END == point) {
                watch = false;

                StorageValue newStorageValue = new StringStorageValue(logicName, buff.toString(), true);
                newStorageValue.locate(location++);

                if (head == null) {
                    head = newStorageValue;
                } else {
                    head.stick(newStorageValue);
                }

                buff.delete(0, buff.length());
                continue;
            }

            if (watch) {
                buff.append(point);
            }
        }

        return head;
    }

}
