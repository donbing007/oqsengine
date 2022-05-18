package com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.storage.value.LongStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.ReadJdbcOriginalSource;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.WriteJdbcOriginalSource;
import java.nio.charset.StandardCharsets;
import java.sql.Types;

/**
 * Types.BIT 支持.
 *
 * @author dongbin
 * @version 0.1 2022/3/9 14:34
 * @since 1.8
 */
public class JdbcBitOriginalFieldAgent extends AbstractJdbcOriginalFieldAgent {
    @Override
    protected StorageValue doRead(IEntityField field, ReadJdbcOriginalSource rs) throws Exception {
        String originalName = field.fieldName().originalName().get();
        if (field.type() == FieldType.BOOLEAN) {

            boolean value = rs.getResultSet().getBoolean(originalName);
            return new LongStorageValue(field.idString(), value ? 1 : 0, true);

        } else {

            byte[] value = rs.getResultSet().getBytes(originalName);
            return new StringStorageValue(field.idString(), new String(value, StandardCharsets.UTF_8), true);
        }
    }

    @Override
    protected StorageValue doReadNothing(IEntityField field) throws Exception {
        if (field.type() == FieldType.BOOLEAN) {

            return new LongStorageValue(field.idString(), true);

        } else {

            return new StringStorageValue(field.idString(), true);
        }
    }

    @Override
    public int supportJdbcType() {
        return Types.BIT;
    }

    @Override
    protected void doWriteDefault(IEntityField field, String s, WriteJdbcOriginalSource ws) throws Exception {
        if (field.type() == FieldType.BOOLEAN) {

            ws.getPreparedStatement().setBoolean(ws.getColumnNumber(), Boolean.parseBoolean(s));

        } else {

            ws.getPreparedStatement().setBytes(ws.getColumnNumber(), s.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Override
    protected void doWrite(IEntityField field, StorageValue data, WriteJdbcOriginalSource ws) throws Exception {
        if (field.type() == FieldType.BOOLEAN) {

            Long value = ((LongStorageValue) data).value();

            ws.getPreparedStatement().setBoolean(ws.getColumnNumber(), value > 0 ? true : false);

        } else {

            String value = ((StringStorageValue) data).value();
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            ws.getPreparedStatement().setBytes(ws.getColumnNumber(), bytes);
        }
    }

    @Override
    public String plainText(IEntityField field, StorageValue data) throws Exception {
        if (field.type() == FieldType.BOOLEAN) {
            Long value = ((LongStorageValue) data).value();
            return Boolean.toString(value > 0 ? true : false);
        } else {
            String value = ((StringStorageValue) data).value();
            return String.format("'%s'", value);
        }
    }
}
