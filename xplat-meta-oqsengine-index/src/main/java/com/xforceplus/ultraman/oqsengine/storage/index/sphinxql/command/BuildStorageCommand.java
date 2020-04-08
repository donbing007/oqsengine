package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.constant.SQLConstant;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommand;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.CommonUtil.toFullString;
import static com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.CommonUtil.toJsonString;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/27/2020 4:58 PM
 * 功能描述:
 * 修改历史:
 */
public class BuildStorageCommand implements StorageCommand {

    final Logger logger = LoggerFactory.getLogger(BuildStorageCommand.class);

    private StorageStrategyFactory storageStrategyFactory;

    private String indexTableName;

    private String buildSql;

    public BuildStorageCommand(StorageStrategyFactory storageStrategyFactory, String indexTableName) {
        this.storageStrategyFactory = storageStrategyFactory;
        this.indexTableName = indexTableName;

        buildSql =
                String.format(SQLConstant.WRITER_SQL,
                        "insert", indexTableName,
                        FieldDefine.ID, FieldDefine.ENTITY, FieldDefine.PREF, FieldDefine.CREF,
                        FieldDefine.JSON_FIELDS, FieldDefine.FULL_FIELDS);
    }

    @Override
    public Object execute(Connection conn, Object data) throws SQLException {
        IEntity entity = (IEntity) data;

        StorageEntity storageEntity = new StorageEntity(
                entity.id(),
                entity.entityClass().id(),
                entity.family().parent(),
                entity.family().child(),
                CommonUtil.serializeToJson(storageStrategyFactory, entity.entityValue(), true),
                CommonUtil.serializeSetFull(storageStrategyFactory, entity.entityValue())
        );

        final String sql = String.format(buildSql, indexTableName);

        PreparedStatement st = conn.prepareStatement(sql);

        // id, entity, pref, cref, jsonfileds, fullfileds
        st.setLong(1, storageEntity.getId()); // id
        st.setLong(2, storageEntity.getEntity()); // entity
        st.setLong(3, storageEntity.getPref()); // pref
        st.setLong(4, storageEntity.getCref()); // cref
        // attribute
        st.setString(5, toJsonString(storageEntity.getJsonFields()));
        // full
        st.setString(6, toFullString(storageEntity.getFullFields()));

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
            st.close();
        }
    }

}
