package com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.original;

import com.xforceplus.ultraman.oqsengine.common.profile.OqsProfile;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.executor.AbstractMasterTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

/**
 * 静态任务处理抽像.
 * 这里所有实现遵守一个约定,如果控制表和业务表同时查询时控制表的别名为小写的"c",业务表为小写的"b".
 *
 * @author dongbin
 * @version 0.1 2022/2/24 15:49
 * @since 1.8
 */
public abstract class AbstractOriginalMasterTaskExecutor<R, T> extends AbstractMasterTaskExecutor<R, T> {

    public AbstractOriginalMasterTaskExecutor(String tableName,
                                              TransactionResource<Connection> resource) {
        super(tableName, resource);
    }

    public AbstractOriginalMasterTaskExecutor(String tableName,
                                              TransactionResource<Connection> resource, long timeoutMs) {
        super(tableName, resource, timeoutMs);
    }

    /**
     * 构造静态表名, oqs_{应用code}_{对象code}_{定制}.
     */
    protected String buildOriginalTableName() {

        StringBuilder buff = new StringBuilder();
        buff.append("oqs_")
            .append(getEntityClass().appCode())
            .append('_')
            .append(getEntityClass().code());
        if (!getEntityClass().profile().equals(OqsProfile.UN_DEFINE_PROFILE)) {
            buff.append('_')
                .append(getEntityClass().profile());
        }

        return buff.toString();
    }

    /**
     * 获取指定字段名称的JDBC类型.
     *
     * @param metaData 元信息.
     * @param name 字段名称.
     * @return java.sql.Types 中定义的值. Types.NULL 表示没有找到类型.
     * @throws SQLException 异常.
     */
    protected int findJdbcType(ResultSetMetaData metaData, String name) throws SQLException {
        int colLen = metaData.getColumnCount();
        for (int i = 1; i <= colLen; i++) {
            if (metaData.getColumnName(i).equals(name)) {
                return metaData.getColumnType(i);
            }
        }

        return Types.NULL;
    }

}
