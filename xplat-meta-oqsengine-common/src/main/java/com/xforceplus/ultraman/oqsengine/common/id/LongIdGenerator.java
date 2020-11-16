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
    boolean isContinuous();

    /**
     * 是否偏序的.
     *
     * @return true 偏序,false不偏序.
     */
    boolean isPartialOrder();
}
