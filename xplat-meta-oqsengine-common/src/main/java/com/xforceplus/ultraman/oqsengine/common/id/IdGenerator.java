package com.xforceplus.ultraman.oqsengine.common.id;

/**
 * 标识生成.
 * @param <V> id 类型.
 * @author dongbin
 * @version 0.1 2020/2/16 22:39
 * @since 1.8
 */
public interface IdGenerator<V> {

    /**
     * 获取一个 ID 表示.
     * @return ID.
     */
    V next();
}
