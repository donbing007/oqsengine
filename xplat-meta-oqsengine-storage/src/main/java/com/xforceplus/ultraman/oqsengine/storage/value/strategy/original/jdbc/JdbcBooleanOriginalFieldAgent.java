package com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.storage.value.LongStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.ReadJdbcOriginalSource;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.WriteJdbcOriginalSource;
import java.sql.Types;

/**
 * Types.BOOLEAN 支持.
 *
 * @author dongbin
 * @version 0.1 2022/3/9 14:32
 * @since 1.8
 */
public class JdbcBooleanOriginalFieldAgent extends AbstractJdbcOriginalFieldAgent {
    @Override
    protected StorageValue doRead(IEntityField field, ReadJdbcOriginalSource rs) throws Exception {
        boolean value = rs.getResultSet().getBoolean(field.fieldName().originalName().get());

        return new LongStorageValue(field.idString(), value ? 1 : 0, true);
    }

    @Override
    protected StorageValue doReadNothing(IEntityField field) throws Exception {
        return new LongStorageValue(field.idString(), true);
    }

    @Override
    public int supportJdbcType() {
        return Types.BOOLEAN;
    }

    @Override
    protected void doWriteDefault(IEntityField field, String s, WriteJdbcOriginalSource ws) throws Exception {
        boolean defaultValue = Boolean.parseBoolean(s);
        ws.getPreparedStatement().setBoolean(ws.getColumnNumber(), defaultValue);
    }

    @Override
    protected void doWrite(IEntityField field, StorageValue data, WriteJdbcOriginalSource ws) throws Exception {
        long value = ((LongStorageValue) data).value();
        ws.getPreparedStatement().setBoolean(ws.getColumnNumber(), value == 0 ? false : true);
    }

    @Override
    public String plainText(IEntityField field, StorageValue data) throws Exception {
        long value = ((LongStorageValue) data).value();
        return Boolean.toString(value == 0 ? false : true);
    }
}
