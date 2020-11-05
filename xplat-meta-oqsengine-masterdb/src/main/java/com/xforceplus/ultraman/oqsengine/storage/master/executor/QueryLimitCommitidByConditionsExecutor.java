package com.xforceplus.ultraman.oqsengine.storage.master.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * 条件查询.
 * 只会查询大于指定提交号的符合条件的数据.
 *
 * @author dongbin
 * @version 0.1 2020/11/4 15:36
 * @since 1.8
 */
public class QueryLimitCommitidByConditionsExecutor implements Executor<Conditions, Collection<EntityRef>> {

    final Logger logger = LoggerFactory.getLogger(QueryLimitCommitidByConditionsExecutor.class);

    private long commitid;
    private IEntityClass entityClass;
    private TransactionResource<Connection> resource;

    public QueryLimitCommitidByConditionsExecutor(
        long commitid, IEntityClass entityClass, TransactionResource<Connection> resource) {
        this.commitid = commitid;
        this.entityClass = entityClass;
        this.resource = resource;
    }

    @Override
    public Collection<EntityRef> execute(Conditions conditions) throws SQLException {
        return null;
    }
}
