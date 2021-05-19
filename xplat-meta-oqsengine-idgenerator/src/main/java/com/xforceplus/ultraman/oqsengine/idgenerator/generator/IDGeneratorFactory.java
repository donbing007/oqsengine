package com.xforceplus.ultraman.oqsengine.idgenerator.generator;

/**
 * 用于产生generator根据业务标签
 */
public interface IDGeneratorFactory {

    /**
     * 根据业务类型创建序列号生成器
     * @param bizType
     * @return
     */
    IDGenerator getIdGenerator(String bizType);
}
