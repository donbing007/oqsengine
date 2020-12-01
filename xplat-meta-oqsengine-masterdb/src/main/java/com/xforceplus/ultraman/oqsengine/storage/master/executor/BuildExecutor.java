package com.xforceplus.ultraman.oqsengine.storage.master.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.define.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;

/**
 * 创建数据执行器.
 *
 * @author dongbin
 * @version 0.1 2020/11/2 14:41
 * @since 1.8
 */
public class BuildExecutor extends AbstractMasterExecutor<StorageEntity, Integer> {

    public static Executor<StorageEntity, Integer> build(
        String tableName, TransactionResource resource, long timeout) {
        return new BuildExecutor(tableName, resource, timeout);
    }

    public BuildExecutor(String tableName, TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public BuildExecutor(String tableName, TransactionResource<Connection> resource, long timeout) {
        super(tableName, resource, timeout);
    }

    @Override
    public Integer execute(StorageEntity storageEntity) throws SQLException {
        String sql = buildSQL(storageEntity);
        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
            st.setLong(1, storageEntity.getId());
            st.setLong(2, storageEntity.getEntity());
            st.setLong(3, storageEntity.getTx());
            st.setLong(4, storageEntity.getCommitid());
            st.setInt(5, storageEntity.getVersion());
            st.setInt(6, storageEntity.getOp());
            st.setLong(7, storageEntity.getTime());
            st.setLong(8, storageEntity.getPref());
            st.setLong(9, storageEntity.getCref());
            st.setBoolean(10, storageEntity.getDeleted());
            st.setString(11, storageEntity.getAttribute());
            st.setString(12, storageEntity.getMeta());
            st.setInt(13, OqsVersion.MAJOR);

            checkTimeout(st);
            return st.executeUpdate();

        }
    }

    private String buildSQL(StorageEntity storageEntity) {
        StringBuilder buff = new StringBuilder();
        // insert into ${table} (id, entity, tx, commitid, version, op, time, pref, cref, deleted, attribute,meta,oqsver) values(?,?,?,?,?,?,?,?,?,?)
        buff.append("INSERT INTO ").append(getTableName())
            .append(' ')
            .append("(").append(String.join(",",
            FieldDefine.ID,
            FieldDefine.ENTITY,
            FieldDefine.TX,
            FieldDefine.COMMITID,
            FieldDefine.VERSION,
            FieldDefine.OP,
            FieldDefine.TIME,
            FieldDefine.PREF,
            FieldDefine.CREF,
            FieldDefine.DELETED,
            FieldDefine.ATTRIBUTE,
            FieldDefine.META,
            FieldDefine.OQS_MAJOR)
        )
            .append(") VALUES (")
            .append(String.join(",", Collections.nCopies(13, "?")))
            .append(")");
        return buff.toString();
    }
}
