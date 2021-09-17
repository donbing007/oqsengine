package com.xforceplus.ultraman.oqsengine.storage.master.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.common.version.VersionHelp;
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
 * 删除执行器.
 *
 * @author dongbin
 * @version 0.1 2020/11/2 16:03
 * @since 1.8
 */
public class DeleteExecutor extends AbstractJdbcTaskExecutor<MasterStorageEntity[], Integer> {

    public static Executor<MasterStorageEntity[], Integer> build(
        String tableName, TransactionResource resource, long timeout) {
        return new DeleteExecutor(tableName, resource, timeout);
    }

    public DeleteExecutor(String tableName, TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public DeleteExecutor(String tableName, TransactionResource<Connection> resource, long timeout) {
        super(tableName, resource, timeout);
    }

    @Override
    public Integer execute(MasterStorageEntity[] masterStorageEntity) throws Exception {
        final int onlyOne = 1;
        if (masterStorageEntity.length == onlyOne) {
            MasterStorageEntity entity = masterStorageEntity[0];

            if (VersionHelp.isOmnipotence(entity.getVersion())) {

                String sql = buildForceSQL();
                try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
                    checkTimeout(st);

                    setForceParam(entity, st);

                    return st.executeUpdate();
                }
            } else {

                String sql = buildSQL();
                try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
                    checkTimeout(st);

                    setParam(entity, st);

                    return st.executeUpdate();
                }
            }
        } else {
            // 批量全部强制删除
            String sql = buildForceSQL();
            try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
                for (MasterStorageEntity entity : masterStorageEntity) {
                    setForceParam(entity, st);

                    st.addBatch();
                }

                int[] flags = st.executeBatch();
                return Math.toIntExact(Arrays.stream(flags).filter(flag -> {
                    // 表示成功
                    if (flag > 0) {
                        return true;
                    } else if (flag == Statement.SUCCESS_NO_INFO) {
                        return true;
                    }
                    return false;
                }).count());
            }
        }
    }

    private void setForceParam(MasterStorageEntity entity, PreparedStatement st) throws SQLException {
        st.setInt(1, VersionHelp.OMNIPOTENCE_VERSION);
        st.setBoolean(2, true);
        st.setLong(3, entity.getUpdateTime());
        st.setLong(4, entity.getTx());
        st.setLong(5, entity.getCommitid());
        st.setInt(6, entity.getOp());
        st.setInt(7, entity.getEntityClassVersion());
        st.setLong(8, entity.getId());
    }

    private void setParam(MasterStorageEntity entity, PreparedStatement st) throws SQLException {
        st.setBoolean(1, true);
        st.setLong(2, entity.getUpdateTime());
        st.setLong(3, entity.getTx());
        st.setLong(4, entity.getCommitid());
        st.setInt(5, entity.getOp());
        st.setInt(6, entity.getEntityClassVersion());
        st.setLong(7, entity.getId());
        st.setInt(8, entity.getVersion());
    }

    private String buildForceSQL() {
        //"update %s set version = ?, deleted = ?, time = ?, tx = ?, commitid = ?, op = ? where id = ?";
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(getTableName())
            .append(" SET ")
            .append(FieldDefine.VERSION).append("=").append("?, ")
            .append(FieldDefine.DELETED).append("=").append("?, ")
            .append(FieldDefine.UPDATE_TIME).append("=").append("?, ")
            .append(FieldDefine.TX).append("=").append("?, ")
            .append(FieldDefine.COMMITID).append("=").append("?, ")
            .append(FieldDefine.OP).append("=").append("?, ")
            .append(FieldDefine.ENTITYCLASS_VERSION).append("=").append("? ")
            .append("WHERE ")
            .append(FieldDefine.ID).append("=").append('?');

        return sql.toString();
    }

    private String buildSQL() {
        //"update %s set version = version + 1, deleted = ?, time = ?, tx = ?, commitid = ?, op = ? where id = ? and version = ?";
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(getTableName())
            .append(" SET ")
            .append(FieldDefine.VERSION).append("=").append(FieldDefine.VERSION).append(" + 1, ")
            .append(FieldDefine.DELETED).append("=").append("?, ")
            .append(FieldDefine.UPDATE_TIME).append("=").append("?, ")
            .append(FieldDefine.TX).append("=").append("?, ")
            .append(FieldDefine.COMMITID).append("=").append("?, ")
            .append(FieldDefine.OP).append("=").append("?, ")
            .append(FieldDefine.ENTITYCLASS_VERSION).append("=").append("? ")
            .append("WHERE ")
            .append(FieldDefine.ID).append("=").append('?')
            .append(" AND ")
            .append(FieldDefine.VERSION).append("=").append('?');

        return sql.toString();
    }
}
