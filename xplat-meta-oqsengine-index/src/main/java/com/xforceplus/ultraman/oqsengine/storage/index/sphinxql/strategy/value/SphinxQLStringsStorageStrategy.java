package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value;

import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
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

    /**
     * 预期为一个"[RMB][JPY][USD]"表示的字符串.
     *
     * @param storageName  物理储存名称.
     * @param storageValue 物理储存值.
     * @param longStrFormat 在这个实现类中没有作用.
     * @return 物理储存实例.
     */
    @Override
    public StorageValue convertIndexStorageValue(String storageName, Object storageValue, boolean attachment, boolean longStrFormat) {
        return SphinxQLHelper.stringsStorageConvert(storageName, (String) storageValue, attachment);
    }
}
