package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.constant.SQLConstant;
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
public class DeleteStorageCommand implements StorageCommand<IEntity> {

    final Logger logger = LoggerFactory.getLogger(DeleteStorageCommand.class);

    private String indexTableName;

    public DeleteStorageCommand(String indexTableName) {
        this.indexTableName = indexTableName;
    }

    @Override
    public Object execute(Connection conn, IEntity entity) throws SQLException {
        String sql = String.format(SQLConstant.DELETE_SQL, indexTableName);
        PreparedStatement st = conn.prepareStatement(sql);
        st.setLong(1, entity.id()); // id

        if (logger.isDebugEnabled()) {
            logger.debug(st.toString());
        }

        // 在事务状态,返回值恒等于0.
        st.executeUpdate();

        try {
            return entity;
        } finally {
            if (st != null) {
                st.close();
            }
        }
    }

}
