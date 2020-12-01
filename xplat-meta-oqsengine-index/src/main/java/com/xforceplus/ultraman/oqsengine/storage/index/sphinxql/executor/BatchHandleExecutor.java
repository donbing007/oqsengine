package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.constant.SQLConstant;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.CommonUtil.toFullString;

/**
 * desc :
 * name : BatchReplaceExecutor
 *
 * @author : xujia
 * date : 2020/12/1
 * @since : 1.8
 */
public class BatchHandleExecutor implements Executor<List<StorageEntity>, Integer> {

    final Logger logger = LoggerFactory.getLogger(ReplaceExecutor.class);

    private String indexTableName;

    private String doSql;

    private TransactionResource resource;

    public BatchHandleExecutor(TransactionResource resource, String indexTableName, String action) {
        this.indexTableName = indexTableName;
        this.resource = resource;
        doSql =
                String.format(SQLConstant.BATCH_SQL,
                        action, indexTableName,
                        FieldDefine.ID,
                        FieldDefine.ENTITY,
                        FieldDefine.ENTITY_F,
                        FieldDefine.PREF,
                        FieldDefine.CREF,
                        FieldDefine.TX,
                        FieldDefine.COMMIT_ID,
                        FieldDefine.JSON_FIELDS,
                        FieldDefine.FULL_FIELDS,
                        FieldDefine.MAINTAIN_ID,
                        FieldDefine.TIME,
                        FieldDefine.OQS_MAJOR);
    }

    public static BatchHandleExecutor build(TransactionResource resource, String indexTableName, String action) {
        return new BatchHandleExecutor(resource, indexTableName, action);
    }

    @Override
    public Integer execute(List<StorageEntity> storageEntities) throws SQLException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format(doSql, indexTableName));

        for (int i = 0; i < storageEntities.size(); i++) {
            if (i > 0) {
                stringBuilder.append(",");
            }
            stringBuilder.append(String.format(" (%s)", getBatch(storageEntities.get(i))));
        }

        PreparedStatement st = ((Connection) resource.value()).prepareStatement(stringBuilder.toString());

        try {
            int results = st.executeUpdate();
            return results;
        } finally {
            st.close();
        }
    }

    private String getBatch(StorageEntity storageEntity) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(storageEntity.getId())
                .append(",").append(storageEntity.getEntity())
                .append(",").append("'").append(storageEntity.getEntity()).append("'")
                .append(",").append(storageEntity.getPref())
                .append(",").append(storageEntity.getCref())
                .append(",").append(storageEntity.getTx())
                .append(",").append(storageEntity.getCommitId())
                .append(",").append("'").append(SphinxQLHelper.serializableJson(storageEntity.getJsonFields())).append("'")
                .append(",").append("'").append(toFullString(storageEntity.getFullFields())).append("'")
                .append(",").append(storageEntity.getMaintainId())
                .append(",").append(storageEntity.getTime())
                .append(",").append(OqsVersion.MAJOR);
        return stringBuilder.toString();
    }
}
