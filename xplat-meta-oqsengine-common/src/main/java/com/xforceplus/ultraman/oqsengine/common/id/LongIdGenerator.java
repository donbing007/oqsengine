package com.xforceplus.ultraman.oqsengine.common.id;

/**
 * 生成结果为一个 long 的 ID 生成器.
 *
 * @author dongbin
 * @version 0.1 2020/2/16 22:40
 * @since 1.8
 */
public interface LongIdGenerator extends IdGenerator<Long> {

    /**
     * 是否连续的.
     *
     * @return true 连续的,false 不连续的.
     */
    default boolean isContinuous() {
        return false;
    }

    /**
     * 是否偏序的.
     *
     * @return true 偏序,false不偏序.
     */
    default boolean isPartialOrder() {
        return false;
    }

    /**
     * 重置,从0开始计数.
     */
    default void reset() {

    }

    /**
     * 重置指定namespace的计数.
     *
     * @param ns 目标ns.
     */
    default void reset(String ns) {
        throw new UnsupportedOperationException(
            String.format("The current ID generator(%s) does not support namespaces.",
                this.getClass().getSimpleName()));
    }
}
