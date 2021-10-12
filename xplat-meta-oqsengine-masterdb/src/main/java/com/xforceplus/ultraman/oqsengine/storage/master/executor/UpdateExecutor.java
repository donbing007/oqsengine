package com.xforceplus.ultraman.oqsengine.storage.master.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.storage.executor.jdbc.AbstractJdbcTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.MasterStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

/**
 * 更新执行器.
 *
 * @author dongbin
 * @version 0.1 2020/11/2 15:44
 * @since 1.8
 */
public class UpdateExecutor extends AbstractJdbcTaskExecutor<MasterStorageEntity[], int[]> {

    public static Executor<MasterStorageEntity[], int[]> build(
        String tableName, TransactionResource resource, long timeoutMs) {
        return new UpdateExecutor(tableName, resource, timeoutMs);
    }

    public UpdateExecutor(String tableName, TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public UpdateExecutor(String tableName, TransactionResource<Connection> resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    @Override
    public int[] execute(MasterStorageEntity[] masterStorageEntity) throws Exception {
        String sql = buildSQL();
        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {

            checkTimeout(st);

            final int onlyOne = 1;
            if (masterStorageEntity.length == onlyOne) {
                MasterStorageEntity entity = masterStorageEntity[0];

                setParam(entity, st);

                return new int[] {st.executeUpdate()};

            } else {

                for (MasterStorageEntity entity : masterStorageEntity) {

                    setParam(entity, st);

                    st.addBatch();
                }

                int[] flags = st.executeBatch();
                return Arrays.stream(flags).map(f -> {
                    if (f > 0 || f == Statement.SUCCESS_NO_INFO) {
                        return 1;
                    } else {
                        return 0;
                    }
                }).toArray();
            }

        }
    }

    private void setParam(MasterStorageEntity entity, PreparedStatement st) throws SQLException {
        st.setLong(1, entity.getUpdateTime());
        st.setLong(2, entity.getTx());
        st.setLong(3, entity.getCommitid());
        st.setInt(4, entity.getOp());
        st.setInt(5, OqsVersion.MAJOR);
        st.setInt(6, entity.getEntityClassVersion());
        st.setString(7, entity.getAttribute());
        st.setLong(8, entity.getId());
        st.setInt(9, entity.getVersion());
    }

    private String buildSQL() {
        //"update %s set version = version + 1, updatetime = ?, tx = ?, commitid = ?, op = ?, attribute = ? where id = ? and version = ?";
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(getTableName())
            .append(" SET ")
            .append(FieldDefine.VERSION).append("=").append(FieldDefine.VERSION).append(" + 1, ")
            .append(FieldDefine.UPDATE_TIME).append("=?, ")
            .append(FieldDefine.TX).append("=?, ")
            .append(FieldDefine.COMMITID).append("=?, ")
            .append(FieldDefine.OP).append("=?, ")
            .append(FieldDefine.OQS_MAJOR).append("=?, ")
            .append(FieldDefine.ENTITYCLASS_VERSION).append("=?, ")
            .append(FieldDefine.ATTRIBUTE).append("=?")
            .append(" WHERE ")
            .append(FieldDefine.ID).append("=").append("?")
            .append(" AND ")
            .append(FieldDefine.VERSION).append("=?");
        return sql.toString();
    }
}
