package com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.storage.executor.jdbc.AbstractJdbcTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.pojo.StorageUniqueEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明: 新增业务主键
 * 作者(@author): liwei
 * 创建时间: 2021/3/19 4:55 PM
 */
public class BuildUniqueExecutor extends AbstractJdbcTaskExecutor<StorageUniqueEntity, Integer> {

    public static Executor<StorageUniqueEntity, Integer> build(
        String tableName, TransactionResource resource, long timeout) {
        return new BuildUniqueExecutor(tableName, resource, timeout);
    }

    public BuildUniqueExecutor(String tableName, TransactionResource resource) {
        super(tableName, resource);
    }

    public BuildUniqueExecutor(String tableName, TransactionResource resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }


    @Override
    public Integer execute(StorageUniqueEntity storageUniqueEntity) throws Exception {
        String sql = buildSQL(storageUniqueEntity.getEntityClasses().length);
        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
            int pos = 1;
            st.setLong(pos++, storageUniqueEntity.getId());
            st.setString(pos++, storageUniqueEntity.getKey());
            fullEntityClass(pos, st, storageUniqueEntity);
            checkTimeout(st);
            return st.executeUpdate();
        }
    }


    private int fullEntityClass(int startPos, PreparedStatement st, StorageUniqueEntity storageUniqueEntity)
        throws SQLException {
        int pos = startPos;
        for (int i = 0; i < storageUniqueEntity.getEntityClasses().length; i++) {
            st.setLong(pos++, storageUniqueEntity.getEntityClasses()[i]);
        }
        return pos;
    }

    private String buildSQL(int entityClassSize) {
        StringBuilder buff = new StringBuilder();
        // insert into ${table}
        buff.append("INSERT INTO ").append(getTableName())
            .append(" (").append(String.join(",",
            FieldDefine.ID,
            FieldDefine.UNIQUE_KEY
            )
        );
        for (int i = 0; i < entityClassSize; i++) {
            buff.append(",")
                .append(FieldDefine.ENTITYCLASS_LEVEL_LIST[i]);
        }

        final int baseColumnSize = 2;

        buff.append(") VALUES (")
            .append(String.join(",", Collections.nCopies(baseColumnSize + entityClassSize, "?")))
            .append(")");
        return buff.toString();
    }
}
