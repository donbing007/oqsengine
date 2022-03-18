package com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.storage.value.LongStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.ReadJdbcOriginalSource;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.WriteJdbcOriginalSource;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;

/**
 * Types.TIMESTAMP 类型支持.
 *
 * @author dongbin
 * @version 0.1 2022/3/9 14:03
 * @since 1.8
 */
public class JdbcTimestampOriginalFieldAgent extends AbstractJdbcOriginalFieldAgent {
    @Override
    protected StorageValue doRead(IEntityField field, ReadJdbcOriginalSource rs) throws Exception {
        Timestamp timestamp = rs.getResultSet().getTimestamp(field.name());

        long value = timestamp.getTime();

        return new LongStorageValue(field.idString(), value, true);
    }

    @Override
    protected StorageValue doReadNothing(IEntityField field) throws Exception {
        return new LongStorageValue(field.idString(), true);
    }

    @Override
    public int supportJdbcType() {
        return Types.TIMESTAMP;
    }

    @Override
    protected void doWriteDefault(IEntityField field, String s, WriteJdbcOriginalSource ws) throws Exception {
        ws.getPreparedStatement().setTimestamp(ws.getColumnNumber(), Timestamp.valueOf(s));
    }

    @Override
    protected void doWrite(IEntityField field, StorageValue data, WriteJdbcOriginalSource ws) throws Exception {
        long value = (long) data.value();
        Timestamp timestamp = new Timestamp(value);
        ws.getPreparedStatement().setTimestamp(ws.getColumnNumber(), timestamp);
    }

    @Override
    public String plainText(IEntityField field, StorageValue data) throws Exception {
        long value = (long) data.value();
        Timestamp timestamp = new Timestamp(value);

        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-DD HH:MM:SS");
        return String.format("\'%s\'", format.format(timestamp));
    }
}
