package com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.storage.value.LongStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.JdbcOriginalFieldHelper;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.ReadJdbcOriginalSource;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.WriteJdbcOriginalSource;
import java.sql.Types;

/**
 * Types.DOUBLE 类型处理.
 *
 * @author dongbin
 * @version 0.1 2022/3/9 12:09
 * @since 1.8
 */
public class JdbcDoubleOriginalFieldAgent extends AbstractJdbcOriginalFieldAgent {
    @Override
    protected StorageValue doRead(IEntityField field, ReadJdbcOriginalSource rs) throws Exception {
        double value = rs.getResultSet().getDouble(field.name());

        String plainValue = Double.toString(value);
        return JdbcOriginalFieldHelper.buildDecimalStorageValue(field.idString(), plainValue);
    }

    @Override
    protected StorageValue doReadNothing(IEntityField field) throws Exception {
        return new LongStorageValue(field.idString(), true);
    }

    @Override
    public int supportJdbcType() {
        return Types.DOUBLE;
    }

    @Override
    public String plainText(IEntityField field, StorageValue data) throws Exception {
        return JdbcOriginalFieldHelper.buildDecimalStorageValuePlainValue(data);
    }

    @Override
    protected void doWriteDefault(IEntityField field, String s, WriteJdbcOriginalSource ws) throws Exception {
        double defaultValue = Double.parseDouble(s);
        ws.getPreparedStatement().setDouble(ws.getColumnNumber(), defaultValue);
    }

    @Override
    protected void doWrite(IEntityField field, StorageValue data, WriteJdbcOriginalSource ws) throws Exception {
        ws.getPreparedStatement().setDouble(ws.getColumnNumber(),
            Double.parseDouble(JdbcOriginalFieldHelper.buildDecimalStorageValuePlainValue(data)));
    }
}
