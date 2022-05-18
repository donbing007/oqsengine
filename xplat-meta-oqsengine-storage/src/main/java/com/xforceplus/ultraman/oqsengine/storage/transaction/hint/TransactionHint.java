package com.xforceplus.ultraman.oqsengine.storage.transaction.hint;

/**
 * 事务的提示.
 *
 * @author dongbin
 * @version 0.1 2022/5/13 17:38
 * @since 1.8
 */
public interface TransactionHint {

    /**
     * 是否需要rollback.
     *
     * @return true需要, false不需要.
     */
    boolean isRollback();

    /**
     * 设置是否回滚.
     *
     * @param rollback true需要,false不需要.
     */
    void setRollback(boolean rollback);

    /**
     * 是否可以进行提交号的CDC等待.
     * 实际是否进行等待由事务实现决定.
     *
     * @return true 可以,false不可以.
     */
    boolean isCanWaitCommitSync();

    /**
     * 设置当前事务是否需要进行提交号的CDC等待.
     * true表示事务中产生了任何写操作后需要等待CDC同步结束标示提交号淘汰后才可以解除阻塞.
     * 如果当前已经设定为需要同步,那么无法再次设置为false.
     *
     * @param canWaitCommitSync true 等待, false不需要.
     */
    void setCanWaitCommitSync(boolean canWaitCommitSync);
}
