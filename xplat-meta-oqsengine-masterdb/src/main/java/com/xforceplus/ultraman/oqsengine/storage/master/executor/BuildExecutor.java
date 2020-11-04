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
import java.util.Collections;

/**
 * 创建数据执行器.
 *
 * @author dongbin
 * @version 0.1 2020/11/2 14:41
 * @since 1.8
 */
public class BuildExecutor implements Executor<StorageEntity, Integer> {

    final Logger logger = LoggerFactory.getLogger(BuildExecutor.class);

    private Selector<String> tableNameSelector;
    private TransactionResource<Connection> resource;

    public static Executor<StorageEntity, Integer> build(
        Selector<String> tableNameSelector, TransactionResource resource) {
        return new BuildExecutor(tableNameSelector, resource);
    }

    public BuildExecutor(Selector<String> tableNameSelector, TransactionResource resource) {
        this.tableNameSelector = tableNameSelector;
        this.resource = resource;
    }

    @Override
    public Integer execute(StorageEntity storageEntity) throws SQLException {
        String sql = buildSQL(storageEntity);
        PreparedStatement st = resource.value().prepareStatement(sql);
        st.setLong(1, storageEntity.getId());
        st.setLong(2, storageEntity.getEntity());
        st.setLong(3, storageEntity.getTx());
        st.setLong(4, storageEntity.getCommitid());
        st.setInt(5, storageEntity.getVersion());
        st.setInt(6, OperationType.CREATE.getValue());
        st.setLong(7, storageEntity.getTime());
        st.setLong(8, storageEntity.getPref());
        st.setLong(9, storageEntity.getCref());
        st.setBoolean(10, false);
        st.setString(11, storageEntity.getAttribute());
        st.setString(12, storageEntity.getMeta());

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
        StringBuilder buff = new StringBuilder();
        // insert into ${table} (id, entity, tx, commitid, version, op, time, pref, cref, deleted, attribute,meta) values(?,?,?,?,?,?,?,?,?,?)
        buff.append("INSERT INTO ").append(tableNameSelector.select(Long.toString(storageEntity.getId())))
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
            FieldDefine.META)
        )
            .append(") VALUES (")
            .append(String.join(",", Collections.nCopies(12, "?")))
            .append(")");
        return buff.toString();
    }
}
