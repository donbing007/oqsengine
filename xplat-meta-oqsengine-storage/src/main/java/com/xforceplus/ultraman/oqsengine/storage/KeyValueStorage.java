package com.xforceplus.ultraman.oqsengine.storage;

import com.xforceplus.ultraman.oqsengine.storage.pojo.kv.AbstractKeyIterator;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * kv 储存定义.
 *
 * @author dongbin
 * @version 0.1 2021/07/16 10:20
 * @since 1.8
 */
public interface KeyValueStorage {

    /**
     * 保存一个key-value,如果key已经存在那么将被更新.
     *
     * @param key   需要保存的key.
     * @param value 需要保存的值.
     */
    public void save(String key, byte[] value);

    /**
     * 批量保存.
     *
     * @param kvs kv列表.
     * @return 成功的数量.
     */
    public long save(Collection<Map.Entry<String, byte[]>> kvs);

    /**
     * 保存一下key-value,如果key已经存在那么将拒绝写入.
     *
     * @param key   需要保存的key.
     * @param value 需要保存的值.
     * @return true 增加成功, false已经有一个相同的KEY.
     */
    public boolean add(String key, byte[] value);

    /**
     * 判断指定的key是否存在.
     *
     * @param key 目标key.
     * @return true 存在,false不存在.
     */
    public boolean exist(String key);

    /**
     * 获取指定key的对应value.
     *
     * @param key 目标key.
     * @return 结果.
     */
    public Optional<byte[]> get(String key);

    /**
     * 批量获取指定KEY的值.
     *
     * @param keys 目标key列表.
     * @return KEY-VALUE映射列表.
     */
    public Collection<Map.Entry<String, byte[]>> get(String[] keys);

    /**
     * 删除一个已经存在的key.
     * 如果目标key不存在,将默认为成功.
     *
     * @param key 目标key.
     */
    public void delete(String key);

    /**
     * 批量删除已经存在的key.
     *
     * @param keys 需要删除的key列表.
     */
    public void delete(String[] keys);

    /**
     * 对KEY进行迭代, KEY将按顺序被迭代.
     * KEY可以只输入开始的部份KEY.
     *
     * @param keyPrefix 开始的key.
     * @return 迭代器.
     */
    public default AbstractKeyIterator iterator(String keyPrefix) {
        return iterator(keyPrefix, true);
    }

    /**
     * 对KEY进行迭代, KEY将按顺序被迭代.
     * KEY可以只输入开始的部份KEY.
     *
     * @param keyPrefix key前辍.
     * @param asc       true从开头开始迭代,false从尾到开始迭代.
     * @return 迭代器.
     */
    public AbstractKeyIterator iterator(String keyPrefix, boolean asc);
}
