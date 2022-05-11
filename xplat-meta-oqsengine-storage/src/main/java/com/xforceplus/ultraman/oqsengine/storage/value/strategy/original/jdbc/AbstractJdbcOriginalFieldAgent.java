package com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityFieldName;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.ReadJdbcOriginalSource;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.WriteJdbcOriginalSource;
import java.sql.SQLException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽像的JDBC代理实现.
 *
 * @author dongbin
 * @version 0.1 2022/3/9 11:05
 * @since 1.8
 */
public abstract class AbstractJdbcOriginalFieldAgent implements JdbcOriginalFieldAgent {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJdbcOriginalFieldAgent.class);

    @Override
    public StorageValue read(IEntityField field, ReadJdbcOriginalSource rs) throws Exception {
        try {

            EntityFieldName fieldName = field.fieldName();
            Optional<String> originalFieldName = fieldName.originalName();

            if (!originalFieldName.isPresent()) {
                // 非静态字段,没有静态字段名称.
                throw new Exception(
                    String.format("The expected static field name was not found for field (%s).",
                        fieldName.dynamicName()));

            }

            rs.getResultSet().findColumn(originalFieldName.get());

            return doRead(field, rs);

        } catch (SQLException ex) {

            LOGGER.warn(ex.getMessage(), ex);

            return doReadNothing(field);
        }
    }

    @Override
    public void write(IEntityField field, StorageValue data, WriteJdbcOriginalSource ws) throws Exception {
        if (data.isEmpty() | data == null) {

            Optional<String> defaultOp = field.defaultValue();
            if (defaultOp.isPresent()) {

                doWriteDefault(field, defaultOp.get(), ws);

            } else {

                ws.getPreparedStatement().setNull(ws.getColumnNumber(), supportJdbcType());
            }

        } else {

            doWrite(field, data, ws);
        }
    }

    /**
     * 写入默认值.
     *
     * @param field 目标字段.
     * @param s     默认值.由子类解释.
     * @param ws    写入源.
     */
    protected abstract void doWriteDefault(IEntityField field, String s, WriteJdbcOriginalSource ws) throws Exception;

    /**
     * 写入原生类型.
     *
     * @param field 目标字段.
     * @param data  OQS储存表示.
     * @param ws    写入源.
     */
    protected abstract void doWrite(IEntityField field, StorageValue data, WriteJdbcOriginalSource ws) throws Exception;

    /**
     * 实际读取实现.
     *
     * @param field 目标OQSEngine字段.
     * @param rs    JDBC数据读取器.
     * @return 物理储存值.
     * @throws Exception 异常.
     */
    protected abstract StorageValue doRead(IEntityField field, ReadJdbcOriginalSource rs) throws Exception;

    /**
     * 没有相应的值,构造空值表示.
     *
     * @param field 目标OQSEngine字段.
     * @return 空值表示.
     * @throws Exception 异常.
     */
    protected abstract StorageValue doReadNothing(IEntityField field) throws Exception;

}
