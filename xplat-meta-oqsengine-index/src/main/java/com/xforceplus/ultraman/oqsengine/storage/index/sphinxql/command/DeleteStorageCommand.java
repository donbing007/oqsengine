package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command;

import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.constant.SQLConstant;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/27/2020 4:58 PM
 * 功能描述:
 * 修改历史:
 */
public class DeleteStorageCommand implements StorageCommand<Long, Integer> {

    final Logger logger = LoggerFactory.getLogger(DeleteStorageCommand.class);

    private String indexTableName;

    public DeleteStorageCommand(String indexTableName) {
        this.indexTableName = indexTableName;
    }

    @Override
    public Integer execute(TransactionResource resource, Long id) throws SQLException {
        return this.doExecute(resource, id);
    }

    private int doExecute(TransactionResource resource, long id) throws SQLException {
        String sql = String.format(SQLConstant.DELETE_SQL, indexTableName);
        PreparedStatement st = ((Connection) resource.value()).prepareStatement(sql);
        st.setLong(1, id); // id

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
