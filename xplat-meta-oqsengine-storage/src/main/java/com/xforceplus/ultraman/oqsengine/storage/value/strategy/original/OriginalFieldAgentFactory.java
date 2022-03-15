package com.xforceplus.ultraman.oqsengine.storage.value.strategy.original;

/**
 * 原始类型数据读取器工厂.
 *
 * @author dongbin
 * @version 0.1 2022/3/9 10:50
 * @since 1.8
 */
public interface OriginalFieldAgentFactory<T> {

    /**
     * 获取读取器.
     *
     * @param type 原始数据类型.
     * @return 读取器实例.
     */
    public OriginalFieldAgent getAgent(T type);
}
