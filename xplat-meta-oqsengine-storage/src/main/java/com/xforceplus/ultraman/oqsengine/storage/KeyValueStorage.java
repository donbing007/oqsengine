package com.xforceplus.ultraman.oqsengine.storage;

import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import java.sql.SQLException;
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
public interface KeyValueStorage<T> {

    /**
     * 保存一个key-value,如果key已经存在那么将被更新.
     *
     * @param key   需要保存的key.
     * @param value 需要保存的值.
     */
    public void save(String key, T value) throws SQLException;

    /**
     * 批量保存.
     *
     * @param kvs kv列表.
     * @return 成功的数量.
     */
    public long save(Collection<Map.Entry<String, T>> kvs) throws SQLException;

    /**
     * 判断指定的key是否存在.
     *
     * @param key 目标key.
     * @return true 存在,false不存在.
     */
    public boolean exist(String key) throws SQLException;

    /**
     * 获取指定key的对应value.
     *
     * @param key 目标key.
     * @return 结果.
     */
    public Optional<T> get(String key) throws SQLException;

    /**
     * 删除一个已经存在的key.
     * 如果目标key不存在,将默认为成功.
     *
     * @param key 目标key.
     */
    public void delete(String key) throws SQLException;

    /**
     * 对KEY进行迭代, KEY将按顺序被迭代.
     * KEY可以只输入开始的部份KEY.
     *
     * @param startKey 开始的key.
     * @return 迭代器.
     */
    public DataIterator<String> iterator(String startKey) throws SQLException;
}
