package com.xforceplus.ultraman.oqsengine.storage.transaction;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.storage.undo.UndoFactory;

/**
 * 默认的事务管理器.
 *
 * @author dongbin
 * @version 0.1 2020/2/15 17:49
 * @since 1.8
 */
public class DefaultTransactionManager extends AbstractTransactionManager {

    private LongIdGenerator idGenerator;

    private UndoFactory undoFactory;

    public DefaultTransactionManager(LongIdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public DefaultTransactionManager(int survivalTimeMs, LongIdGenerator idGenerator, UndoFactory undoFactory) {
        super(survivalTimeMs);
        this.idGenerator = idGenerator;
        this.undoFactory = undoFactory;
    }

    @Override
    public Transaction doCreate() {
        long id = idGenerator.next();

        Transaction tx = new MultiLocalTransaction(id);

        tx.setUndoExecutor(undoFactory.getUndoExecutor());

        return tx;
    }
}
