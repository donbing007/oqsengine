package com.xforceplus.ultraman.oqsengine.storage.master.command;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.constant.SQLConstant;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.AbstractStorageCommand;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommand;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/27/2020 4:12 PM
 * 功能描述:
 * 修改历史:
 */
public class DeleteStorageCommand extends AbstractStorageCommand<IEntity> {

    final Logger logger = LoggerFactory.getLogger(ReplaceStorageCommand.class);

    private Selector<String> tableNameSelector;

    public DeleteStorageCommand(Selector<String> tableNameSelector){
        this.tableNameSelector = tableNameSelector;
    }

    @Override
    public IEntity execute(TransactionResource resource, IEntity entity) throws SQLException {
        super.recordOriginalData(resource, OpTypeEnum.DELETE, entity);
        return this.doExecute(resource, entity);
    }

    IEntity doExecute(TransactionResource resource, IEntity entity) throws SQLException {
        String tableName = tableNameSelector.select(Long.toString(entity.id()));
        String sql = String.format(SQLConstant.DELETE_SQL, tableName);
        PreparedStatement st = ((Connection)resource.value()).prepareStatement(sql);

        // deleted time id version;
        st.setBoolean(1, true); // deleted
        st.setLong(2, System.currentTimeMillis()); // time
        st.setLong(3, entity.id()); // id
        st.setInt(4, entity.version()); // version

        if (logger.isDebugEnabled()) {
            logger.debug(st.toString());
        }

        int size = st.executeUpdate();
        final int onlyOne = 1;
        if (size != onlyOne) {
            throw new SQLException(String.format("Entity{%s} could not be delete successfully.", entity.toString()));
        }

        try {
            return entity;
        } finally {
            if (st != null) {
                st.close();
            }
        }
    }

}
