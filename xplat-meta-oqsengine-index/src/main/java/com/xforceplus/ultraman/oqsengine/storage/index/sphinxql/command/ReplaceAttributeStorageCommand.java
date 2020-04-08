package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.constant.SQLConstant;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommand;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/27/2020 5:00 PM
 * 功能描述:
 * 修改历史:
 */
public class ReplaceAttributeStorageCommand implements StorageCommand {

    final Logger logger = LoggerFactory.getLogger(ReplaceAttributeStorageCommand.class);

    private StorageStrategyFactory storageStrategyFactory;

    private String indexTableName;

    private String replaceSql;

    public ReplaceAttributeStorageCommand(StorageStrategyFactory storageStrategyFactory, String indexTableName){
        this.storageStrategyFactory = storageStrategyFactory;
        this.indexTableName = indexTableName;

        replaceSql =
                String.format(SQLConstant.WRITER_SQL,
                        "replace", indexTableName,
                        FieldDefine.ID, FieldDefine.ENTITY, FieldDefine.PREF, FieldDefine.CREF,
                        FieldDefine.JSON_FIELDS, FieldDefine.FULL_FIELDS);
    }

    @Override
    public Object execute(Connection conn, Object data) throws SQLException {
        IEntityValue attribute = (IEntityValue) data;

        long dataId = attribute.id();
        Optional<StorageEntity> oldStorageEntityOptional = doSelectStorageEntity(conn, dataId);
        if (oldStorageEntityOptional.isPresent()) {

            StorageEntity storageEntity = oldStorageEntityOptional.get();

            /**
             * 把新的属性插入旧属性集中替换已有,或新增.
             */
            JSONObject completeJson = storageEntity.getJsonFields();
            JSONObject modifiedJson = CommonUtil.serializeToJson(storageStrategyFactory, attribute, true);
            for (String key : modifiedJson.keySet()) {
                completeJson.put(key, modifiedJson.get(key));
            }

            //处理 fulltext
            Set<String> completeFull = CommonUtil.convertJsonToFull(completeJson);
            storageEntity.setJsonFields(completeJson);
            storageEntity.setFullFields(completeFull);

            doReplaceStorageEntity(storageEntity, conn);
        } else {
            throw new SQLException(
                    String.format("Attempt to update a property on a data that does not exist.[%d]", dataId)
            );
        }

        return null;
    }

    private Optional<StorageEntity> doSelectStorageEntity(Connection conn, long id) throws SQLException {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            String sql = String.format(SQLConstant.SELECT_FROM_ID_SQL, indexTableName);
            st = conn.prepareStatement(sql);
            st.setLong(1, id);

            rs = st.executeQuery();
            StorageEntity storageEntity = null;
            if (rs.next()) {
                storageEntity = new StorageEntity(
                        id,
                        rs.getLong(FieldDefine.ENTITY),
                        rs.getLong(FieldDefine.PREF),
                        rs.getLong(FieldDefine.CREF),
                        JSON.parseObject(rs.getString(FieldDefine.JSON_FIELDS)),
                        null
                );
            }

            return Optional.ofNullable(storageEntity);
        } finally {
            if (rs != null) {
                rs.close();
            }

            if (st != null) {
                st.close();
            }
        }
    }

    private boolean doReplaceStorageEntity(StorageEntity storageEntity, Connection conn) throws SQLException{
        final String sql = String.format(replaceSql, indexTableName);

        PreparedStatement st = conn.prepareStatement(sql);

        // id, entity, pref, cref, jsonfileds, fullfileds
        st.setLong(1, storageEntity.getId()); // id
        st.setLong(2, storageEntity.getEntity()); // entity
        st.setLong(3, storageEntity.getPref()); // pref
        st.setLong(4, storageEntity.getCref()); // cref
        // attribute
        st.setString(5, CommonUtil.toJsonString(storageEntity.getJsonFields()));
        // full
        st.setString(6, CommonUtil.toFullString(storageEntity.getFullFields()));

        if (logger.isDebugEnabled()) {
            logger.debug(st.toString());
        }

        int size = st.executeUpdate();

        try {
            // 成功只应该有一条语句影响
            final int onlyOne = 1;
            return size == onlyOne;

        } finally {
            st.close();
        }
    }
}
