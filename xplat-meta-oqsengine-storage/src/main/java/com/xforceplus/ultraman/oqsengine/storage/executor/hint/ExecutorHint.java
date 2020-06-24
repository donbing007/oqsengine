package com.xforceplus.ultraman.oqsengine.storage.executor.hint;

/**
 * 执行器的提示器.
 * @author dongbin
 * @version 0.1 2020/6/23 14:40
 * @since 1.8
 */
public interface ExecutorHint {

    public boolean isRollback();

    public void setRollback(boolean rollback);

}
