package com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.storage.value.LongStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.ReadJdbcOriginalSource;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.WriteJdbcOriginalSource;
import java.sql.Types;

/**
 * JDBC中 Types.BIGINT 的读取实现.
 *
 * @author dongbin
 * @version 0.1 2022/3/9 11:04
 * @since 1.8
 */
public class JdbcBigintOriginalFieldAgent extends AbstractJdbcOriginalFieldAgent {

    @Override
    protected StorageValue doRead(IEntityField field, ReadJdbcOriginalSource rs) throws Exception {
        long value = rs.getResultSet().getLong(field.name());

        return new LongStorageValue(field.idString(), value, true);
    }

    @Override
    protected StorageValue doReadNothing(IEntityField field) throws Exception {
        return new LongStorageValue(field.idString(), true);
    }

    @Override
    public int supportJdbcType() {
        return Types.BIGINT;
    }


    @Override
    protected void doWriteDefault(IEntityField field, String s, WriteJdbcOriginalSource ws) throws Exception {
        ws.getPreparedStatement().setLong(ws.getColumnNumber(), Long.parseLong(s));
    }

    @Override
    protected void doWrite(IEntityField field, StorageValue data, WriteJdbcOriginalSource ws) throws Exception {
        long value = ((LongStorageValue) data).value();
        ws.getPreparedStatement().setLong(ws.getColumnNumber(), value);
    }
}
