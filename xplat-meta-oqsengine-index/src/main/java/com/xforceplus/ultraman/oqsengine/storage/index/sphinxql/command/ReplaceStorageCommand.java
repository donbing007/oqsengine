package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command;

import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.constant.SQLConstant;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.UndoStorageCommand;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpType;
import com.xforceplus.ultraman.oqsengine.storage.undo.transaction.UndoTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.value.LongStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.CommonUtil.toFullString;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/27/2020 4:58 PM
 * 功能描述:
 * 修改历史:
 */
public class ReplaceStorageCommand extends UndoStorageCommand<StorageEntity> {

    final Logger logger = LoggerFactory.getLogger(ReplaceStorageCommand.class);

    private String indexTableName;

    private String replaceSql;

    public ReplaceStorageCommand(String indexTableName) {
        this.indexTableName = indexTableName;

        replaceSql =
                String.format(SQLConstant.WRITER_SQL,
                        "replace", indexTableName,
                        FieldDefine.ID, FieldDefine.ENTITY, FieldDefine.PREF, FieldDefine.CREF,
                        FieldDefine.JSON_FIELDS, FieldDefine.FULL_FIELDS);
    }

    @Override
    public StorageEntity execute(TransactionResource resource, StorageEntity storageEntity) throws SQLException {
        if (!((UndoTransactionResource) resource).isCommitted()) {
            StorageEntity oriStorageEntity = new SelectByIdStorageCommand(indexTableName).execute(resource, storageEntity);
            oriStorageEntity.setFullFields(convertJsonToFull(oriStorageEntity.getJsonFields()));
            super.prepareUndoLog(resource, OpType.REPLACE, oriStorageEntity);
        }
        return this.doExecute(resource, storageEntity);
    }

    StorageEntity doExecute(TransactionResource resource, StorageEntity storageEntity) throws SQLException {
        final String sql = String.format(replaceSql, indexTableName);

        PreparedStatement st = ((Connection) resource.value()).prepareStatement(sql);

        // id, entity, pref, cref, jsonfileds, fullfileds
        st.setLong(1, storageEntity.getId()); // id
        st.setLong(2, storageEntity.getEntity()); // entity
        st.setLong(3, storageEntity.getPref()); // pref
        st.setLong(4, storageEntity.getCref()); // cref
        // jsonfileds
        st.setString(5, SphinxQLHelper.serializableJson(storageEntity.getJsonFields()));
        // fullfileds
        st.setString(6, toFullString(storageEntity.getFullFields()));

        if (logger.isDebugEnabled()) {
            logger.debug(st.toString());
        }

        int size = st.executeUpdate();

        try {
//            // 成功只应该有一条语句影响
//            final int onlyOne = 1;
//            if(size == onlyOne) {
//                throw new SQLException(String.format("Entity{%s} could not be created successfully.", storageEntity.toString()));
//            }
            return null;
        } finally {
            st.close();
        }
    }

    @Override
    public StorageEntity executeUndo(TransactionResource resource, StorageEntity data) throws SQLException {
        return this.execute(resource, data);
    }

    // 转换 json 字段为全文搜索字段.
    private Set<String> convertJsonToFull(Map<String, Object> attributes) {
        Set<String> fullfileds = new HashSet<>();
        Object value;
        StorageValue storageValue = null;
        for (String key : attributes.keySet()) {
            value = attributes.get(key);

            if (Long.class.isInstance(value)) {

                storageValue = new LongStorageValue(key, (Long) value, false);

            } else {

                storageValue = new StringStorageValue(key, (String) value, false);
            }

            fullfileds.add(serializeStorageValueFull(storageValue));
        }

        return fullfileds;
    }

    // 处理成<F123L>F123L 789</F123L> 形式字符串.
    private String serializeStorageValueFull(StorageValue value) {
        StringBuilder buff = new StringBuilder();
        buff.append("<").append(SphinxQLHelper.ATTRIBUTE_FULL_FIELD_PREFIX).append(value.groupStorageName()).append(">");
        buff.append(SphinxQLHelper.ATTRIBUTE_FULL_FIELD_PREFIX).append(value.storageName()).append(' ');
        if (value.type() == StorageType.STRING) {
            buff.append(SphinxQLHelper.encodeSpecialCharset(value.value().toString()));
        } else {
            buff.append(value.value().toString());
        }
        buff.append("</").append(SphinxQLHelper.ATTRIBUTE_FULL_FIELD_PREFIX).append(value.groupStorageName()).append(">");
        return buff.toString();
    }
}
