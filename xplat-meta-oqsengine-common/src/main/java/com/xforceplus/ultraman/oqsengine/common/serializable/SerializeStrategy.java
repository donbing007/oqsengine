package com.xforceplus.ultraman.oqsengine.common.serializable;

import java.io.Serializable;

/**
 * 序列化策略接口.
 * 实现须保持线程安全.
 *
 * @author dongbin
 * @version 1.00 2020-07-16
 * @since 1.5
 */
public interface SerializeStrategy {

    /**
     * 将指定的对象进行序列化.
     *
     * @param source 需要序列化的对象。
     * @return 序列化后的字节数组。
     * @throws CanNotBeSerializedException 无法进行序列化。
     */
    public byte[] serialize(Serializable source) throws CanNotBeSerializedException;

    /**
     * 反序列化，将指定的字节反序列化成原始对象.
     *
     * @param datas 需要返序列化的字节数据。
     * @return 原始对象。
     * @throws CanNotBeUnSerializedException 无法进行反序列化。
     */
    public <T> T unserialize(byte[] datas, Class<T> clazz) throws CanNotBeUnSerializedException;
}
