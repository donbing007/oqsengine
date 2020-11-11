package com.xforceplus.ultraman.oqsengine.storage.transaction;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.status.StatusService;

/**
 * 默认的事务管理器.
 *
 * @author dongbin
 * @version 0.1 2020/2/15 17:49
 * @since 1.8
 */
public class DefaultTransactionManager extends AbstractTransactionManager {

    private LongIdGenerator idGenerator;
    private StatusService statusService;

    public DefaultTransactionManager(LongIdGenerator idGenerator, StatusService statusService) {
        this(3000, idGenerator, statusService);
    }

    public DefaultTransactionManager(int survivalTimeMs, LongIdGenerator idGenerator, StatusService statusService) {
        super(survivalTimeMs);
        this.idGenerator = idGenerator;
        this.statusService = statusService;
    }

    @Override
    public Transaction doCreate() {
        long id = idGenerator.next();

        Transaction tx = new MultiLocalTransaction(id, statusService);

        return tx;
    }
}
