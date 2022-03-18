package com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.storage.value.LongStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.ReadJdbcOriginalSource;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.WriteJdbcOriginalSource;
import java.sql.Time;
import java.sql.Types;
import java.text.SimpleDateFormat;

/**
 * Types.TIME 的代理.
 *
 * @author dongbin
 * @version 0.1 2022/3/9 13:58
 * @since 1.8
 */
public class JdbcTimeOriginalFieldAgent extends AbstractJdbcOriginalFieldAgent {
    @Override
    protected StorageValue doRead(IEntityField field, ReadJdbcOriginalSource rs) throws Exception {
        Time time = rs.getResultSet().getTime(field.name());

        long value = time.getTime();

        return new LongStorageValue(field.idString(), value, true);
    }

    @Override
    protected StorageValue doReadNothing(IEntityField field) throws Exception {
        return new LongStorageValue(field.idString(), true);
    }

    @Override
    public int supportJdbcType() {
        return Types.TIME;
    }

    @Override
    protected void doWriteDefault(IEntityField field, String s, WriteJdbcOriginalSource ws) throws Exception {
        Time time = Time.valueOf(s);
        ws.getPreparedStatement().setTime(ws.getColumnNumber(), time);
    }

    @Override
    protected void doWrite(IEntityField field, StorageValue data, WriteJdbcOriginalSource ws) throws Exception {
        long value = (long) data.value();
        Time time = new Time(value);
        ws.getPreparedStatement().setTime(ws.getColumnNumber(), time);
    }

    @Override
    public String plainText(IEntityField field, StorageValue data) throws Exception {
        long value = ((LongStorageValue) data).value();

        SimpleDateFormat format = new SimpleDateFormat("HH:MM:SS");
        return String.format("\'%s\'", format.format(new Time(value)));
    }
}
