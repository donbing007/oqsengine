package com.xforceplus.ultraman.oqsengine.storage.transaction.resource;

import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * 基于 java.sql.Connection 的资源.
 *
 * @author dongbin
 * @version 0.1 2020/5/19 10:41
 * @since 1.8
 */
public abstract class AbstractConnectionTransactionResource extends AbstractTransactionResource<Connection> {

    final Logger logger = LoggerFactory.getLogger(AbstractConnectionTransactionResource.class);

    private boolean autoCommit;

    public AbstractConnectionTransactionResource(String key, Connection value, boolean autoCommit) throws SQLException {
        super(key, value);
        this.autoCommit = autoCommit;
        if (value().getAutoCommit() != autoCommit) {
            if (logger.isDebugEnabled()) {
                logger.debug("The current auto-commit status of the resource is {}, set to {}.",
                    value().getAutoCommit(), autoCommit);
            }
            value().setAutoCommit(autoCommit);
        }
    }

    @Override
    public void commit(long commitId) throws SQLException {
        commit();

        Optional<Transaction> transactionOp = getTransaction();
        if (transactionOp.isPresent()) {
            if (logger.isDebugEnabled()) {
                logger.debug("The transaction resource ({}) commits in the transaction ({}) using the ({}) commit id.",
                    key(), transactionOp.get().id(), commitId);
            }
        }
    }

    @Override
    public void commit() throws SQLException {
        if (!isAutoCommit()) {
            value().commit();

            Optional<Transaction> transactionOp = getTransaction();
            if (transactionOp.isPresent()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("The transaction resource ({}) commits in the transaction.",
                        key(), transactionOp.get().id());
                }
            }
        }
    }

    @Override
    public void rollback() throws SQLException {
        if (!isAutoCommit()) {
            value().rollback();

            Optional<Transaction> transactionOp = getTransaction();
            if (transactionOp.isPresent()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("The transaction resource ({}) rollback in the transaction ({}).",
                        key(), transactionOp.get().id());
                }
            }
        }
    }

    @Override
    public void destroy() throws SQLException {
        value().close();

        if (logger.isDebugEnabled()) {
            logger.debug("Resource {} destroy!", key());
        }
    }

    @Override
    public boolean isDestroyed() throws SQLException {
        return value().isClosed();
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }
}
