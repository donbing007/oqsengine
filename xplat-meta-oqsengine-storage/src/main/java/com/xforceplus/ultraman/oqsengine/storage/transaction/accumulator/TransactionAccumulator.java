package com.xforceplus.ultraman.oqsengine.storage.transaction.accumulator;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;

import java.util.Set;

/**
 * 事务累加器.
 *
 * 实现必须并发安全.
 *
 * @author dongbin
 * @version 0.1 2020/12/11 15:23
 * @since 1.8
 */
public interface TransactionAccumulator {

    /**
     * 事务中的创建次数累加1.
     */
    public boolean accumulateBuild(IEntity entity);

    /**
     * 事务中更新次数累加1.
     */
    public boolean accumulateReplace(IEntity newEntity, IEntity oldEntity);

    /**
     * 事务中删除次数累加1.
     */
    public boolean accumulateDelete(IEntity entity);

    /**
     * 获取创建次数.
     *
     * @return 创建次数.
     */
    public long getBuildNumbers();

    /**
     * 获取更新次数.
     *
     * @return 更新次数.
     */
    public long getReplaceNumbers();

    /**
     * 获取删除次数.
     *
     * @return 删除次数.
     */
    public long getDeleteNumbers();

    /**
     * 获取更新的标识列表.
     *
     * @return 更新的标识列表.
     */
    public Set<Long> getUpdateIds();


    /**
     * 重置累加器.
     */
    public void reset();

    /**
     * 当前操作序号,从0开始.保证有序且偏序.
     *
     * @return 当前最大序号.
     */
    public long operationNumber();
}
