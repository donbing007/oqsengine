package com.xforceplus.ultraman.oqsengine.storage.master.command;

import com.alibaba.fastjson.JSONObject;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.master.constant.SQLConstant;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.AbstractStorageCommand;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommand;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/27/2020 3:41 PM
 * 功能描述:
 * 修改历史:
 */
public class BuildStorageCommand extends AbstractStorageCommand<IEntity> {

    final Logger logger = LoggerFactory.getLogger(BuildStorageCommand.class);

    private StorageStrategyFactory storageStrategyFactory;

    private Selector<String> tableNameSelector;

    public BuildStorageCommand(StorageStrategyFactory storageStrategyFactory, Selector<String> tableNameSelector){
        this.storageStrategyFactory = storageStrategyFactory;
        this.tableNameSelector = tableNameSelector;
    }

    @Override
    public IEntity execute(TransactionResource resource, IEntity entity) throws SQLException {
        super.recordOriginalData(resource, OpTypeEnum.BUILD, entity);
        return this.doExecute(resource, entity);
    }

    IEntity doExecute(TransactionResource resource, IEntity entity) throws SQLException {
        String tableName = tableNameSelector.select(Long.toString(entity.id()));
        String sql = String.format(SQLConstant.BUILD_SQL, tableName);

        PreparedStatement st = ((Connection)resource.value()).prepareStatement(sql);
        // id, entity, version, time, pref, cref, deleted, attribute,refs
        st.setLong(1, entity.id()); // id
        st.setLong(2, entity.entityClass().id()); // entity
        st.setInt(3, 0); // version
        st.setLong(4, System.currentTimeMillis()); // time
        st.setLong(5, entity.family().parent()); // pref
        st.setLong(6, entity.family().child()); // cref
        st.setBoolean(7, false); // deleted
        st.setString(8, CommonUtil.toJson(storageStrategyFactory, entity.entityValue())); // attribute

        if (logger.isDebugEnabled()) {
            logger.debug(st.toString());
        }

        int size = st.executeUpdate();

        /**
         * 插入影响条件恒定为1.
         */
        final int onlyOne = 1;
        if (size != onlyOne) {
            throw new SQLException(
                    String.format("Entity{%s} could not be created successfully.", entity.toString()));
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
