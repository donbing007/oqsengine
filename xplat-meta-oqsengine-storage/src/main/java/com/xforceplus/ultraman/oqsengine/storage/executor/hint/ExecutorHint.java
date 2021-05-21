package com.xforceplus.ultraman.oqsengine.storage.executor.hint;

/**
 * 执行器的提示器.
 *
 * @author dongbin
 * @version 0.1 2020/6/23 14:40
 * @since 1.8
 */
public interface ExecutorHint {

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

}
