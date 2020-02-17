package com.xforceplus.ultraman.oqsengine.storage.transaction;

import java.sql.SQLException;
import java.util.Optional;

/**
 * A transaction instance definition.
 *
 * @author dongbin
 * @version 0.1 2020/2/13 20:37
 * @since 1.8
 */
public interface Transaction {

    /**
     * The unique ID number of the transaction.
     * @return transaction`s id.
     */
    long id();

    /**
     * All operations were successful. Commit operation.
     */
    void commit() throws SQLException;

    /**
     * Operation failed, all operations are rolled back to ensure atomicity.
     */
    void rollback() throws SQLException;

    /**
     * Determines whether the current transaction is committed.
     * @return true Have committed, false not commit.
     */
    boolean isCommitted();

    /**
     * Determine whether or not a rollback has occurred.
     * @return true rollback, false not rollback.
     */
    boolean isRollback();

    /**
     * Determines whether the transaction has ended, whether it is a commit or a rollback.
     * @return true completed, false not.
     */
    boolean isCompleted();

    /**
     * A new connection joins the transaction.
     * @param transactionResource target resource.
     */
    void join(TransactionResource transactionResource);

    /**
     * Determine if resource have joined.
     * @param key resource`s key.
     * @return true Has joined. false not.
     */
    Optional<TransactionResource> query(Object key);

}
