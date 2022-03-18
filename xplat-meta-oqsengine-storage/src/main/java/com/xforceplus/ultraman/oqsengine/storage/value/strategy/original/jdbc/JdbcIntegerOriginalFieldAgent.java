package com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.storage.value.LongStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.ReadJdbcOriginalSource;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.WriteJdbcOriginalSource;
import java.sql.Types;

/**
 * JDBC中 Types.INTEGER 的读取实现.
 *
 * @author dongbin
 * @version 0.1 2022/3/9 10:57
 * @since 1.8
 * @see Types
 */
public class JdbcIntegerOriginalFieldAgent extends AbstractJdbcOriginalFieldAgent {

    @Override
    protected StorageValue doRead(IEntityField field, ReadJdbcOriginalSource rs) throws Exception {
        int value = rs.getResultSet().getInt(field.name());

        return new LongStorageValue(field.idString(), value, true);
    }

    @Override
    protected StorageValue doReadNothing(IEntityField field) throws Exception {
        return new LongStorageValue(field.idString(), true);
    }

    @Override
    public int supportJdbcType() {
        return Types.INTEGER;
    }

    @Override
    public String plainText(IEntityField field, StorageValue data) throws Exception {
        int value = (int) data.value();
        return Integer.toString(value);
    }

    @Override
    protected void doWriteDefault(IEntityField field, String s, WriteJdbcOriginalSource ws) throws Exception {

        ws.getPreparedStatement().setInt(ws.getColumnNumber(), Integer.parseInt(s));
    }

    @Override
    protected void doWrite(IEntityField field, StorageValue data, WriteJdbcOriginalSource ws) throws Exception {
        long value = (long) data.value();

        ws.getPreparedStatement().setInt(ws.getColumnNumber(), (int) value);
    }
}
