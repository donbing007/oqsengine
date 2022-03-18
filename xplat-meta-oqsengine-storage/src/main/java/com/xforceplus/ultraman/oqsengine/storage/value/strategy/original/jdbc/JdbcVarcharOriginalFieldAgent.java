package com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.ReadJdbcOriginalSource;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.WriteJdbcOriginalSource;
import java.sql.Types;

/**
 * Types.VARCHAR 支持.
 *
 * @author dongbin
 * @version 0.1 2022/3/9 14:20
 * @since 1.8
 */
public class JdbcVarcharOriginalFieldAgent extends AbstractJdbcOriginalFieldAgent {
    @Override
    protected StorageValue doRead(IEntityField field, ReadJdbcOriginalSource rs) throws Exception {
        String value = rs.getResultSet().getString(field.name());
        return new StringStorageValue(field.idString(), value, true);
    }

    @Override
    protected StorageValue doReadNothing(IEntityField field) throws Exception {
        return new StringStorageValue(field.idString(), true);
    }

    @Override
    public int supportJdbcType() {
        return Types.VARCHAR;
    }

    @Override
    protected void doWriteDefault(IEntityField field, String s, WriteJdbcOriginalSource ws) throws Exception {
        ws.getPreparedStatement().setString(ws.getColumnNumber(), s);
    }

    @Override
    protected void doWrite(IEntityField field, StorageValue data, WriteJdbcOriginalSource ws) throws Exception {
        String value = (String) data.value();

        ws.getPreparedStatement().setString(ws.getColumnNumber(), value);
    }

    @Override
    public String plainText(IEntityField field, StorageValue data) throws Exception {
        String value = (String) data.value();

        return String.format("\'%s\'", value);
    }
}
