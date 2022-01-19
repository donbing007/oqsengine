package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value;

import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.common.StringStorageStrategy;

/**
 * Created by justin.xu on 01/2022.
 *
 * @since 1.8
 */
public class SphinxQLStringStorageStrategy extends StringStorageStrategy {

    /**
     * 通过离散的物理储存来构造本地的StorageValue.
     *
     * @param storageName  物理储存名称.
     * @param storageValue 物理储存值.
     * @param attrF 是否为attrF，默认会对attrF中非attachment、却长度 > MAX_WORLD_SPLIT_LENGTH 字节的String进行切割,比如:
     *              假如MAX_WORLD_SPLIT_LENGTH长度为5，则AAAAAABBBBBBCCCC将会转为[AAAAAABBBBBBCCCC].
     * @return 实例.
     */
    @Override
    public StorageValue convertIndexStorageValue(String storageName, Object storageValue, boolean attachment, boolean attrF) {

        String stringValue = (String) storageValue;

        if (attrF && stringValue.length() > SphinxQLHelper.MAX_WORLD_SPLIT_LENGTH) {
            //  处理超出最大可搜索长度的的逻辑、字段将会被当成strings处理.
            return SphinxQLHelper.stringsStorageConvert(
                storageName, SphinxQLHelper.stringValueFormat(stringValue), attachment, false);
        }

        return super.convertIndexStorageValue(storageName, storageValue, attachment, attrF);

    }
}
