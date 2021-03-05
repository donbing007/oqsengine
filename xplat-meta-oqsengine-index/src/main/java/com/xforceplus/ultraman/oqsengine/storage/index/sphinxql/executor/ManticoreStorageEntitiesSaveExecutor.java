package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor;

import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.pojo.ManticoreStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

/**
 * manticore entity的保存操作.
 *
 * @author dongbin
 * @version 0.1 2021/3/3 11:48
 * @since 1.8
 */
public class ManticoreStorageEntitiesSaveExecutor extends AbstractExecutor<Collection<ManticoreStorageEntity>, Integer> {

    private static final String VALUES_TEMPLATE = "(?,?,?,?,?,?,?,?,?,?)";

    public static ManticoreStorageEntitiesSaveExecutor buildCreate(String indexName, TransactionResource transactionResource) {
        return new ManticoreStorageEntitiesSaveExecutor(true, indexName, transactionResource);
    }

    public static ManticoreStorageEntitiesSaveExecutor buildReplace(String indexName, TransactionResource transactionResource) {
        return new ManticoreStorageEntitiesSaveExecutor(false, indexName, transactionResource);
    }

    private boolean create;

    public ManticoreStorageEntitiesSaveExecutor(boolean create, String indexName, TransactionResource transactionResource) {
        super(indexName, transactionResource);
        this.create = create;
    }

    @Override
    public Integer execute(Collection<ManticoreStorageEntity> manticoreStorageEntities) throws SQLException {
        if (manticoreStorageEntities.isEmpty()) {
            return 0;
        }
        final String sql = buildSql(manticoreStorageEntities.size());

        int point = 1;
        try (PreparedStatement st = ((Connection) getTransactionResource().value()).prepareStatement(sql)) {
            for (ManticoreStorageEntity entity : manticoreStorageEntities) {
                st.setLong(point++, entity.getId());
                st.setString(point++, entity.getAttributeF());
                st.setString(point++, entity.getEntityClassF());
                st.setLong(point++, entity.getTx());
                st.setLong(point++, entity.getCommitId());
                st.setLong(point++, entity.getCreateTime());
                st.setLong(point++, entity.getUpdateTime());
                st.setLong(point++, entity.getMaintainId());
                st.setInt(point++, entity.getOqsmajor());
                st.setString(point++, entity.getAttribute());
            }

            st.executeUpdate();
        }

        return manticoreStorageEntities.size();
    }

    private String buildSql(int size) {
        StringBuilder sql = new StringBuilder();
        if (create) {
            sql.append("INSERT ");
        } else {
            sql.append("REPLACE ");
        }
        sql.append("INTO ")
            .append(getIndexName())
            .append(" (")
            .append(String.join(",",
                FieldDefine.ID,
                FieldDefine.ATTRIBUTEF,
                FieldDefine.ENTITYCLASSF,
                FieldDefine.TX,
                FieldDefine.COMMITID,
                FieldDefine.CREATE_TIME,
                FieldDefine.UPDATE_TIME,
                FieldDefine.MAINTAIN_ID,
                FieldDefine.OQSMAJOR,
                FieldDefine.ATTRIBUTE
            ))
            .append(") VALUES ")
            .append(String.join(",", Collections.nCopies(size, VALUES_TEMPLATE)));

        return sql.toString();
    }
}
