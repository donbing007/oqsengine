package com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.ReadJdbcOriginalSource;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.WriteJdbcOriginalSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Types;

/**
 * Types.BLOB 类型支持.
 *
 * @author dongbin
 * @version 0.1 2022/3/9 14:26
 * @since 1.8
 */
public class JdbcBlobOriginalFieldAgent extends AbstractJdbcOriginalFieldAgent {

    @Override
    protected StorageValue doRead(IEntityField field, ReadJdbcOriginalSource rs) throws Exception {
        Blob blob = rs.getResultSet().getBlob(field.name());
        byte[] value;
        try (InputStream in = blob.getBinaryStream()) {
            ByteArrayOutputStream buff = new ByteArrayOutputStream();
            byte[] readBuff = new byte[1024];
            int len = 0;
            while ((len = in.read(readBuff)) != -1) {
                buff.write(readBuff, 0, len);
            }
            value = buff.toByteArray();
        }

        String strValue = new String(value, "utf8");
        return new StringStorageValue(field.idString(), strValue, true);
    }

    @Override
    protected StorageValue doReadNothing(IEntityField field) throws Exception {
        return new StringStorageValue(field.idString(), true);
    }

    @Override
    public int supportJdbcType() {
        return Types.BLOB;
    }

    @Override
    public void write(IEntityField field, StorageValue data, WriteJdbcOriginalSource ws) throws Exception {
        String value = ((StringStorageValue) data).value();
        try (ByteArrayInputStream in = new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8))) {
            ws.getPreparedStatement().setBlob(ws.getColumnNumber(), in);
        }
    }
}
