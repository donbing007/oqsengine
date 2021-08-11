package com.xforceplus.ultraman.oqsengine.storage.pojo.kv;

import com.xforceplus.ultraman.oqsengine.common.iterator.AbstractDataIterator;

/**
 * kv储存的key迭代器.
 *
 * @author dongbin
 * @version 0.1 2021/08/06 10:28
 * @since 1.8
 */
public abstract class KeyIterator extends AbstractDataIterator<String> {

    /**
     * 初始化.
     *
     * @param buffSize 缓存大小.
     */
    public KeyIterator(int buffSize) {
        super(buffSize);
    }

    public abstract void seek(String key);
}
