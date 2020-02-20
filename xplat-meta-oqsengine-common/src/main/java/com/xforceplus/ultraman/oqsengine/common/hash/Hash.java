package com.xforceplus.ultraman.oqsengine.common.hash;

/**
 * hash 算法实现.
 * @author dongbin
 * @version 0.1 2020/2/20 11:40
 * @since 1.8
 */
public interface Hash {

    /**
     * hash 实现.
     * @param key 目标 key.
     * @return hashcode.
     */
    int hash(String key);
}
