package com.xforceplus.ultraman.oqsengine.storage.value.strategy;

/**
 * 一个提示接口,表示可以接收储存策略.
 *
 * @author dongbin
 * @version 0.1 2020/3/5 21:00
 * @since 1.8
 */
public interface StorageStrategyFactoryAble {

    /**
     * 注入方法.
     *
     * @param storageStrategyFactory 工厂实例.
     */
    void setStorageStrategyFactory(StorageStrategyFactory storageStrategyFactory);
}
