package com.xforceplus.ultraman.oqsengine.idgenerator.generator;

import java.util.List;

/**
 * @author leo
 * 业务编号生成器
 */
public interface IDGenerator {
    /**
     * 获取下个序列号
     * @return
     */
    String nextId();

    /**
     * 获取下一批序列号
     * @param batchSize
     * @return
     */
    List<String> nextIds(Integer batchSize);
}
