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
 * 更新执行器.
 *
 * @author dongbin
 * @version 0.1 2020/11/2 15:44
 * @since 1.8
 */
public class ReplaceExecutor implements Executor<StorageEntity, Integer> {

    final Logger logger = LoggerFactory.getLogger(ReplaceExecutor.class);

    private Selector<String> tableNameSelector;
    private TransactionResource<Connection> resource;

    public static Executor<StorageEntity, Integer> build(
        Selector<String> tableNameSelector, TransactionResource resource) {
        return new ReplaceExecutor(tableNameSelector, resource);
    }

    public ReplaceExecutor(Selector<String> tableNameSelector, TransactionResource<Connection> resource) {
        this.tableNameSelector = tableNameSelector;
        this.resource = resource;
    }

    @Override
    public Integer execute(StorageEntity storageEntity) throws SQLException {
        String sql = buildSQL(storageEntity);
        PreparedStatement st = resource.value().prepareStatement(sql);
        st.setLong(1, storageEntity.getTime());
        st.setLong(2, storageEntity.getTx());
        st.setLong(3, storageEntity.getCommitid());
        st.setInt(4, OperationType.UPDATE.getValue());
        st.setString(5, storageEntity.getAttribute());
        st.setString(6, storageEntity.getMeta());
        st.setLong(7, storageEntity.getId());
        st.setInt(8, storageEntity.getVersion());

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
        //"update %s set version = version + 1, time = ?, tx = ?, commitid = ?, op = ?, attribute = ?,meta = ? where id = ? and version = ?";
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(tableNameSelector.select(Long.toString(storageEntity.getId())))
            .append(" SET ")
            .append(FieldDefine.VERSION).append("=").append(FieldDefine.VERSION).append(" + 1, ")
            .append(FieldDefine.TIME).append("=").append("?, ")
            .append(FieldDefine.TX).append("=").append("?, ")
            .append(FieldDefine.COMMITID).append("=").append("?, ")
            .append(FieldDefine.OP).append("=").append("?, ")
            .append(FieldDefine.ATTRIBUTE).append("=").append("?, ")
            .append(FieldDefine.META).append("=").append("? ")
            .append(" WHERE ")
            .append(FieldDefine.ID).append("=").append("? AND ")
            .append(FieldDefine.VERSION).append("=").append("?");
        return sql.toString();
    }
}
