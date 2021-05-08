package com.xforceplus.ultraman.oqsengine.storage.executor.hint;

/**
 * 执行器的提示默认实现.
 *
 * @author dongbin
 * @version 0.1 2020/6/23 14:41
 * @since 1.8
 */
public class DefaultExecutorHint implements ExecutorHint {

    private boolean rollback = false;

    public DefaultExecutorHint() {
    }

    @Override
    public boolean isRollback() {
        return rollback;
    }

    @Override
    public void setRollback(boolean rollback) {
        this.rollback = rollback;
    }

}
