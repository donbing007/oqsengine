package com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.storage.value.LongStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.ReadJdbcOriginalSource;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.WriteJdbcOriginalSource;
import java.sql.Date;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

/**
 * Types.DATE 属性读取器.
 *
 * @author dongbin
 * @version 0.1 2022/3/9 12:11
 * @since 1.8
 */
public class JdbcDateOriginalFieldAgent extends AbstractJdbcOriginalFieldAgent {

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-DD");

    @Override
    protected StorageValue doRead(IEntityField field, ReadJdbcOriginalSource rs) throws Exception {
        Date value = rs.getResultSet().getDate(field.name());

        long ms = value.getTime();

        return new LongStorageValue(field.idString(), ms, true);
    }

    @Override
    protected StorageValue doReadNothing(IEntityField field) throws Exception {
        return new LongStorageValue(field.idString(),  true);
    }

    @Override
    public int supportJdbcType() {
        return Types.DATE;
    }

    @Override
    protected void doWriteDefault(IEntityField field, String s, WriteJdbcOriginalSource ws) throws Exception {
        Date defaultDate = Date.valueOf(s);
        ws.getPreparedStatement().setDate(ws.getColumnNumber(), defaultDate);
    }

    @Override
    protected void doWrite(IEntityField field, StorageValue data, WriteJdbcOriginalSource ws) throws Exception {
        long value = ((LongStorageValue) data).value();
        ws.getPreparedStatement().setDate(ws.getColumnNumber(), new Date(value));
    }

    @Override
    public String plainText(IEntityField field, StorageValue data) throws Exception {
        long value = ((LongStorageValue) data).value();

        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-DD");

        return String.format("\'%s\'", format.format(new Date(value)));
    }
}
