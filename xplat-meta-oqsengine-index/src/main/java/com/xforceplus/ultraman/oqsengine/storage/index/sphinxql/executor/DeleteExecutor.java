package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.constant.SQLConstant;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * delete executor
 */
public class DeleteExecutor implements Executor<Long, Integer> {

    final Logger logger = LoggerFactory.getLogger(DeleteExecutor.class);

    private String indexTableName;

    private TransactionResource resource;

    public DeleteExecutor(String indexTableName, TransactionResource resource) {
        this.indexTableName = indexTableName;
        this.resource = resource;
    }

    @Override
    public Integer execute(Long id) throws SQLException {
        String sql = String.format(SQLConstant.DELETE_SQL, indexTableName);
        PreparedStatement st = ((Connection) resource.value()).prepareStatement(sql);
        st.setLong(1, id);

        if (logger.isDebugEnabled()) {
            logger.debug(st.toString());
        }

        // 在事务状态,返回值恒等于0.
        st.executeUpdate();

        try {
            // 不做版本控制.没有异常即为成功.
            return 1;
        } finally {
            if (st != null) {
                st.close();
            }
        }
    }
}
