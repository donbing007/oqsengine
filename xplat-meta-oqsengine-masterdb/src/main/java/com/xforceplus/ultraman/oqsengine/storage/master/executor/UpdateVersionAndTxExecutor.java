package com.xforceplus.ultraman.oqsengine.storage.master.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.define.OperationType;
import com.xforceplus.ultraman.oqsengine.storage.master.define.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 更新目标数据版本和事务信息.
 *
 * @author dongbin
 * @version 0.1 2020/11/3 12:02
 * @since 1.8
 */
public class UpdateVersionAndTxExecutor implements Executor<StorageEntity, Integer> {

    final Logger logger = LoggerFactory.getLogger(ReplaceExecutor.class);

    private Selector<String> tableNameSelector;
    private TransactionResource<Connection> resource;

    public static Executor<StorageEntity, Integer> build(
        Selector<String> tableNameSelector, TransactionResource<Connection> resource) {
        return new UpdateVersionAndTxExecutor(tableNameSelector, resource);
    }

    public UpdateVersionAndTxExecutor(Selector<String> tableNameSelector, TransactionResource<Connection> resource) {
        this.tableNameSelector = tableNameSelector;
        this.resource = resource;
    }

    @Override
    public Integer execute(StorageEntity storageEntity) throws SQLException {
        String sql = buildSQL(storageEntity);
        PreparedStatement st = resource.value().prepareStatement(sql);
        st.setInt(1, storageEntity.getVersion());
        st.setLong(2, storageEntity.getTime());
        st.setLong(3, storageEntity.getTx());
        st.setLong(4, storageEntity.getCommitid());
        st.setLong(5, OperationType.UPDATE.getValue());
        st.setLong(6, storageEntity.getId());

        if (logger.isDebugEnabled()) {
            logger.debug(st.toString());
        }

        try {
            return st.executeUpdate();
        } finally {
            if (st != null) {
                st.close();
            }
        }
    }

    private String buildSQL(StorageEntity storageEntity) {
        // update table set version=?,time=?,tx=?,commitid=?,op = ? where id=?
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(tableNameSelector.select(Long.toString(storageEntity.getId())))
            .append(" SET ")
            .append(FieldDefine.VERSION).append("=").append("?, ")
            .append(FieldDefine.TIME).append("=").append("?, ")
            .append(FieldDefine.TX).append("=").append("?, ")
            .append(FieldDefine.COMMITID).append("=").append("?, ")
            .append(FieldDefine.OP).append("=").append("?")
            .append(" WHERE ")
            .append(FieldDefine.ID).append("=").append("?");
        return sql.toString();
    }
}
