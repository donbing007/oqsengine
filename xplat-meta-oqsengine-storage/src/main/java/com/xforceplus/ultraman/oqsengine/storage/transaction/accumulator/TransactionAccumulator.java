package com.xforceplus.ultraman.oqsengine.storage.transaction.accumulator;

/**
 * 事务累加器.
 *
 * @author dongbin
 * @version 0.1 2020/12/11 15:23
 * @since 1.8
 */
public interface TransactionAccumulator {

    /**
     * 事务中的创建次数累加1.
     */
    public void accumulateBuild();

    /**
     * 事务中更新次数累加1.
     */
    public void accumulateReplace();

    /**
     * 事务中删除次数累加1.
     */
    public void accumulateDelete();

    /**
     * 获取创建次数.
     *
     * @return 创建次数.
     */
    public long getBuildTimes();

    /**
     * 获取更新次数.
     *
     * @return 更新次数.
     */
    public long getReplaceTimes();

    /**
     * 获取删除次数.
     *
     * @return 删除次数.
     */
    public long getDeleteTimes();

    /**
     * 重置累加器.
     */
    public void reset();
}
