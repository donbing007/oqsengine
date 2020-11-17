package com.xforceplus.ultraman.oqsengine.storage.transaction;

import java.sql.SQLException;
import java.util.Optional;

/**
 * 事务实现接口.
 * 需要注意实现中需要保证
 * isCommitted,
 * isRollback,
 * 方法的多线程可见性.
 *
 * @author dongbin
 * @version 0.1 2020/2/13 20:37
 * @since 1.8
 */
public interface Transaction {

    /**
     * Not attached to anything.
     */
    long NOT_ATTACHMENT = -1;

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
    void join(TransactionResource transactionResource) throws SQLException;

    /**
     * Determine if resource have joined.
     * @param key resource`s key.
     * @return true Has joined. false not.
     */
    Optional<TransactionResource> query(String key);

    /**
     * Attachment id. Usually a thread id.
     * @return attachment id.
     */
    long attachment();

    /**
     * Set attachment.Usually a thread id.
     * @param id attachment id.
     */
    void attach(long id);

    /**
     * Is it a read-only transaction?
     *
     * @return true readOnly,false not readOnly.
     */
    boolean isReadOnley();

    /**
     * Declare a write transaction.
     */
    void declareWriteTransaction();

    /**
     * Exclusive actions ensure that only one thread executes per transaction.
     *
     * @param action Actions that need to be performed.
     */
    void exclusiveAction(TransactionExclusiveAction action) throws SQLException;


}
