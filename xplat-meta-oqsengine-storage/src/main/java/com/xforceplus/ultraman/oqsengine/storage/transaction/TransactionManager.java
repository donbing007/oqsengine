package com.xforceplus.ultraman.oqsengine.storage.transaction;


/**
 * transaction manager.
 *
 * @author dongbin
 * @version 0.1 2020/2/15 16:58
 * @since 1.8
 */
public interface TransactionManager {

    /**
     *
     * @return
     */
    Transaction create();

    Transaction getCurrent();

    void rebind(long id);

    void bind(Transaction tx);

    void unbind(Transaction tx);

}
