package com.xforceplus.ultraman.oqsengine.storage.executor.hint;

/**
 * 执行器的提示默认实现.
 * @author dongbin
 * @version 0.1 2020/6/23 14:41
 * @since 1.8
 */
public class DefaultExecutorHint implements ExecutorHint {

    private static DefaultExecutorHint EMPTY_HINT = new DefaultExecutorHint();

    public static ExecutorHint buildEmptyHint() {
        return EMPTY_HINT;
    }

    private boolean rollback = false;

    @Override
    public boolean isRollback() {
        return rollback;
    }

    @Override
    public void setRollback(boolean rollback) {
        this.rollback = rollback;
    }
}
