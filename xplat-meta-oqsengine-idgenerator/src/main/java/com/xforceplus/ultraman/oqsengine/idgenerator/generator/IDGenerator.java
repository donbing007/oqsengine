package com.xforceplus.ultraman.oqsengine.idgenerator.generator;

import java.util.List;

/**
 * 业务编号生成器.
 *
 * @author leo
 */
public interface IDGenerator {
    /**
     * 获取下个序列号.
     *
     * @return id
     */
    String nextId();

    /**
     * 获取下一批序列号.
     *
     * @param batchSize size of batch
     * @return id list
     */
    List<String> nextIds(Integer batchSize);
}
