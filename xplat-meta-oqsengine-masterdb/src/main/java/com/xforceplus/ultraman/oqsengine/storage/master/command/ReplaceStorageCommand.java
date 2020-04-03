package com.xforceplus.ultraman.oqsengine.storage.master.command;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.constant.SQLConstant;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommand;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/27/2020 4:09 PM
 * 功能描述:
 * 修改历史:
 */
public class ReplaceStorageCommand implements StorageCommand<IEntity> {

    final Logger logger = LoggerFactory.getLogger(ReplaceStorageCommand.class);

    private StorageStrategyFactory storageStrategyFactory;

    private Selector<String> tableNameSelector;

    public ReplaceStorageCommand(StorageStrategyFactory storageStrategyFactory, Selector<String> tableNameSelector){
        this.storageStrategyFactory = storageStrategyFactory;
        this.tableNameSelector = tableNameSelector;
    }

    @Override
    public Object execute(Connection conn, IEntity entity) throws SQLException {
        String tableName = tableNameSelector.select(Long.toString(entity.id()));
        String sql = String.format(SQLConstant.REPLACE_SQL, tableName);
        PreparedStatement st = conn.prepareStatement(sql);

        // update %s set version = version + 1, time = ?, attribute = ? where id = ? and version = ?";
        st.setLong(1, System.currentTimeMillis()); // time
        st.setString(2, CommonUtil.toJson(storageStrategyFactory, entity.entityValue())); // attribute
        st.setLong(3, entity.id()); // id
        st.setInt(4, entity.version()); // version

        if (logger.isDebugEnabled()) {
            logger.debug(st.toString());
        }

        int size = st.executeUpdate();

        final int onlyOne = 1;
        if (size != onlyOne) {
            throw new SQLException(String.format("Entity{%s} could not be replace successfully.", entity.toString()));
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
