package com.xforceplus.ultraman.oqsengine.idgenerator.generator;

/**
 * 用于产生generator根据业务标签.
 *
 * @author leo
 */
public interface IDGeneratorFactory {

    /**
     * 根据业务类型创建序列号生成器.
     *
     * @param bizType bizType
     * @return generator instance
     */
    IDGenerator getIdGenerator(String bizType);
}
