package com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.dynamic;


import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.pojo.define.OperationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.AbstractMasterTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.utils.OriginalEntityUtils;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OqsEngineEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class DynamicQueryOriginalExecutor extends AbstractMasterTaskExecutor<Long, Optional<OqsEngineEntity>> {

    private boolean noDetail;

    /**
     * 查询包含详细信息.
     *
     * @param tableName 表名.
     * @param resource  事务资源.
     * @return 执行器实例.
     */
    public static Executor<Long, Optional<OqsEngineEntity>> buildHaveDetail(
        String tableName, TransactionResource resource, long timeoutMs) {
        return new DynamicQueryOriginalExecutor(tableName, resource, false, timeoutMs);
    }

    /**
     * 查询不包含详细信息.只有版本和事务信息.
     *
     * @param tableName 表名.
     * @param resource  事务资源.
     * @return 执行器实例.
     */
    public static Executor<Long, Optional<OqsEngineEntity>> buildNoDetail(
        String tableName, TransactionResource resource, long timeoutMs) {
        return new DynamicQueryOriginalExecutor(tableName, resource, true, timeoutMs);
    }


    public DynamicQueryOriginalExecutor(String tableName, TransactionResource<Connection> resource, boolean noDetail, long timeoutMs) {
        super(tableName, resource, timeoutMs);
        this.noDetail = noDetail;
    }

    @Override
    public Optional<OqsEngineEntity> execute(Long id) throws Exception {
        String sql = buildSQL(id);
        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {

            st.setLong(1, id);

            checkTimeout(st);

            OqsEngineEntity.Builder builder = null;

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    boolean isDelete = rs.getBoolean(FieldDefine.DELETED);

                    builder = OqsEngineEntity.Builder.anOriginalEntity()
                        .withId(id)
                        .withVersion(rs.getInt(FieldDefine.VERSION))
                        .withEntityClassRef(toEntityClassRef(rs))
                        .withTx(rs.getLong(FieldDefine.TX))
                        .withCommitid(rs.getLong(FieldDefine.COMMITID))
                        .withOp(isDelete ? OperationType.DELETE.getValue() : OperationType.UPDATE.getValue())
                        .withCreateTime(rs.getLong(FieldDefine.CREATE_TIME))
                        .withUpdateTime(rs.getLong(FieldDefine.UPDATE_TIME))
                        .withDeleted(isDelete)
                        .withOqsMajor(rs.getInt(FieldDefine.OQS_MAJOR));

                    if (!noDetail) {
                        String attr = rs.getString(FieldDefine.ATTRIBUTE);
                        if (!attr.isEmpty()) {
                            builder.withAttributes(OriginalEntityUtils.attributesToMap(attr));
                        }
                    }

                    return Optional.of(builder.build());
                }
            }
        }

        return Optional.empty();
    }

    private EntityClassRef toEntityClassRef(ResultSet rs) throws SQLException {
        long classId = 0;
        for (int i = FieldDefine.ENTITYCLASS_LEVEL_LIST.length - 1; i >= 0; i--) {
            classId = rs.getLong(FieldDefine.ENTITYCLASS_LEVEL_LIST[i]);
            if (classId > 0) {
                break;
            }
        }

        if (classId > 0) {
            return new EntityClassRef(classId, "", rs.getString(FieldDefine.PROFILE));
        }

        return null;
    }

    private String buildSQL(long id) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(String.join(",",
            FieldDefine.ENTITYCLASS_LEVEL_0,
            FieldDefine.ENTITYCLASS_LEVEL_1,
            FieldDefine.ENTITYCLASS_LEVEL_2,
            FieldDefine.ENTITYCLASS_LEVEL_3,
            FieldDefine.ENTITYCLASS_LEVEL_4,
            FieldDefine.TX,
            FieldDefine.COMMITID,
            FieldDefine.OP,
            FieldDefine.VERSION,
            FieldDefine.CREATE_TIME,
            FieldDefine.UPDATE_TIME,
            FieldDefine.DELETED,
            FieldDefine.ATTRIBUTE,
            FieldDefine.OQS_MAJOR,
            FieldDefine.PROFILE
            )
        );

        if (!noDetail) {
            sql.append(",")
                .append(String.join(",",
                    FieldDefine.ATTRIBUTE
                    )
                );
        }

        sql.append(" FROM ")
            .append(getTableName())
            .append(" WHERE ")
            .append(FieldDefine.ID).append("=").append("?");

        return sql.toString();
    }
}
