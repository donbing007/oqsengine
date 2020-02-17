package com.xforceplus.ultraman.oqsengine.storage.selector;


import com.xforceplus.ultraman.oqsengine.common.hash.Time33Hash;

/**
 * @author dongbin
 * @version 0.1 2020/2/16 19:18
 * @since 1.8
 */
public class NumberIndexTableNameHashSelector implements Selector<String> {

    private String baseTableName;
    private int size;

    public NumberIndexTableNameHashSelector(String baseTableName, int size) {
        this.baseTableName = baseTableName;
        this.size = size;
    }

    @Override
    public String select(String key) {
        int code = Time33Hash.build().hash(key);
        return baseTableName + (code % size);
    }
}
