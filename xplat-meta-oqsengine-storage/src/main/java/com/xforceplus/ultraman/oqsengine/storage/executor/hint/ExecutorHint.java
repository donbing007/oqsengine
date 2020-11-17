package com.xforceplus.ultraman.oqsengine.storage.executor.hint;

/**
 * 执行器的提示器.
 * @author dongbin
 * @version 0.1 2020/6/23 14:40
 * @since 1.8
 */
public interface ExecutorHint {

    boolean isRollback();

    void setRollback(boolean rollback);

    boolean isReadOnly();

    void setReadOnly(boolean readOnly);

}
