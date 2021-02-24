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
 * 目标字段列表如下.
 *     id             bigint                not null comment '数据主键',
 *     entityclassl0  bigint  default 0     not null comment '数据家族中在0层的entityclass标识',
 *     entityclassl1  bigint  default 0     not null comment '数据家族中在1层的entityclass标识',
 *     entityclassl2  bigint  default 0     not null comment '数据家族中在2层的entityclass标识',
 *     entityclassl3  bigint  default 0     not null comment '数据家族中在3层的entityclass标识',
 *     entityclassl4  bigint  default 0     not null comment '数据家族中在4层的entityclass标识',
 *     entityclassver int     default 0     not null comment '产生数据的entityclass版本号.',
 *     tx             bigint  default 0     not null comment '提交事务号',
 *     commitid       bigint  default 0     not null comment '提交号',
 *     op             tinyint default 0     not null comment '最后操作类型,0(插入),1(更新),2(删除)',
 *     version        int     default 0     not null comment '当前数据版本.',
 *     createtime     bigint  default 0     not null comment '数据创建时间.',
 *     updatetime     bigint  default 0     not null comment '数据操作最后时间.',
 *     deleted        boolean default false not null comment '是否被删除.',
 *     attribute      json                  not null comment '当前 entity 的属性集合.',
 *     oqsmajor       int     default 0     not null comment '产生数据的oqs主版本号',
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
        String sql = buildSQL();
        try (PreparedStatement st = getResource().value().prepareStatement(sql)) {
            int pos = 1;
            st.setLong(pos++, storageEntity.getId());
            pos = fullEntityClass(pos, st, storageEntity);
            st.setInt(++pos, storageEntity.getEntityClassVersion());
            st.setLong(++pos, storageEntity.getTx());
            st.setLong(++pos, storageEntity.getCommitid());
            st.setInt(++pos, storageEntity.getOp());
            st.setInt(++pos, storageEntity.getVersion());
            st.setLong(++pos, storageEntity.getCreateTime());
            st.setLong(++pos, storageEntity.getUpdateTime());
            st.setBoolean(++pos, storageEntity.isDeleted());
            st.setString(++pos, storageEntity.getAttribute());
            st.setInt(++pos, OqsVersion.MAJOR);

            checkTimeout(st);
            return st.executeUpdate();

        }
    }

    private int fullEntityClass(int startPos, PreparedStatement st, StorageEntity storageEntity) throws SQLException {
        int pos = startPos;
        for (int i = 0; i < storageEntity.getEntityClasses().length; i++) {
            st.setLong(pos++, storageEntity.getEntityClasses()[i]);
        }
        return pos;
    }

    private String buildSQL() {
        StringBuilder buff = new StringBuilder();
        // insert into ${table}
        buff.append("INSERT INTO ").append(getTableName())
            .append(' ')
            .append("(").append(String.join(",",
            FieldDefine.ID,
            FieldDefine.ENTITYCLASS_LEVEL_0,
            FieldDefine.ENTITYCLASS_LEVEL_1,
            FieldDefine.ENTITYCLASS_LEVEL_2,
            FieldDefine.ENTITYCLASS_LEVEL_3,
            FieldDefine.ENTITYCLASS_LEVEL_4,
            FieldDefine.ENTITYCLASS_VERSION,
            FieldDefine.TX,
            FieldDefine.COMMITID,
            FieldDefine.OP,
            FieldDefine.VERSION,
            FieldDefine.CREATE_TIME,
            FieldDefine.UPDATE_TIME,
            FieldDefine.DELETED,
            FieldDefine.ATTRIBUTE,
            FieldDefine.OQS_MAJOR)
        )
            .append(") VALUES (")
            .append(String.join(",", Collections.nCopies(16, "?")))
            .append(")");
        return buff.toString();
    }
}
