package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value;

import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.common.StringStorageStrategy;

/**
 * Created by justin.xu on 01/2022.
 *
 * @since 1.8
 */
public class SphinxQLStringStorageStrategy extends StringStorageStrategy {

    @Override
    public StorageValue convertIndexStorageValue(String storageName, Object storageValue, boolean attachment, boolean longStrFormat) {

        String sValue = (String) storageValue;

        if (!longStrFormat || sValue.length() <= SphinxQLHelper.MAX_WORLD_SPLIT_LENGTH) {
            return new StringStorageValue(storageName, (String) storageValue, false);
        }

        //  处理超出最大可搜索长度的的逻辑、字段将会被当成strings处理.
        return SphinxQLHelper.stringsStorageConvert(
            storageName, SphinxQLHelper.stringValueFormat(sValue), attachment);
    }
}
