package com.xforceplus.ultraman.oqsengine.storage.pojo.kv;

import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;

/**
 * kv储存的key迭代器.
 *
 * @author dongbin
 * @version 0.1 2021/08/06 10:28
 * @since 1.8
 */
public interface KeyIterator extends DataIterator<String> {

    /**
     * 移动到指定的KEY,下次迭代从此key后面开始.
     *
     * @param key 目标key.
     */
    public abstract void seek(String key);

    /**
     * 得到当前指向的key.
     *
     * @return 当前的key.
     */
    public abstract String currentKey();
}
