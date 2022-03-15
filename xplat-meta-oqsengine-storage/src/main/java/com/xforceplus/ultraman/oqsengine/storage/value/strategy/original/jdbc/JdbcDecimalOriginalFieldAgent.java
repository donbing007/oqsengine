package com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.storage.value.LongStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.JdbcOriginalFieldHelper;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.ReadJdbcOriginalSource;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.WriteJdbcOriginalSource;
import java.math.BigDecimal;
import java.sql.Types;

/**
 * Types.DECIMAL 代理.
 *
 * @author dongbin
 * @version 0.1 2022/3/9 11:41
 * @since 1.8
 */
public class JdbcDecimalOriginalFieldAgent extends AbstractJdbcOriginalFieldAgent {

    @Override
    protected StorageValue doRead(IEntityField field, ReadJdbcOriginalSource rs) throws Exception {
        BigDecimal value = rs.getResultSet().getBigDecimal(field.name());

        String plainValue = value.toPlainString();
        return JdbcOriginalFieldHelper.buildDecimalStorageValue(field.idString(), plainValue);
    }

    @Override
    protected StorageValue doReadNothing(IEntityField field) throws Exception {
        return new LongStorageValue(field.idString(), true);
    }

    @Override
    public int supportJdbcType() {
        return Types.DECIMAL;
    }

    @Override
    public void write(IEntityField field, StorageValue data, WriteJdbcOriginalSource ws) throws Exception {

        ws.getPreparedStatement().setBigDecimal(ws.getColumnNumber(),
            new BigDecimal(JdbcOriginalFieldHelper.buildDecimalStorageValuePlainValue(data)));
    }
}
