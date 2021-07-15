package com.xforceplus.ultraman.oqsengine.calculation;

import java.util.List;

/**
 * .
 *
 * @author leo
 * @version 0.1 7/14/21 2:36 PM
 * @since 1.8
 */
public interface IDGenerator {
    /**
     * 获取下个序列号.
     * @param bizTag bizTag
     * @param step step
     *
     * @return id
     */
    Long nextId(String bizTag,int step);

    /**
     * 获取下一批序列号.
     *
     * @param batchSize size of batch
     * @param bizTag bizTag
     *
     * @return id list
     */
    List<Long> nextIds(String bizTag,int step,Integer batchSize);
}
