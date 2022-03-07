package com.xforceplus.ultraman.oqsengine.storage.master.mysql.original;

import com.xforceplus.ultraman.oqsengine.common.jdbc.TypesUtils;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;

/**
 * mysql 原始类型字段读取器.
 *
 * @author dongbin
 * @version 0.1 2022/3/2 15:22
 * @since 1.8
 */
public class MasterOriginalFieldReader {

    /**
     *
     * @param field
     * @param rs
     * @return
     * @throws SQLException
     */
    public static long readLong(IEntityField field, ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        String fieldName = field.name();
        int colIndex = findColumnSchemaIndex(metaData, fieldName);

        if (colIndex < 0) {
            throw buildColumnNotExist(fieldName);
        }

        int colType = metaData.getColumnType(colIndex);
        switch (colType) {
            case Types.INTEGER:
            case Types.TINYINT:
            case Types.SMALLINT: {
                return rs.getInt(fieldName);
            }
            case Types.BIGINT: {
                return rs.getLong(fieldName);
            }
            default: {
                throw buildTypeErrorException(
                    new String[] {
                        "int", "integer", "bigint", "tinyint", "smallint", "mediumint"
                    },
                    colType
                );
            }
        }
    }

    public static BigDecimal readDecimal(IEntityField field, ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        String fieldName = field.name();
        int colIndex = findColumnSchemaIndex(metaData, fieldName);

        if (colIndex < 0) {
            throw buildColumnNotExist(fieldName);
        }

        int colType = metaData.getColumnType(colIndex);
        switch (colType) {
            case Types.FLOAT:
            case Types.DOUBLE: {
                return new BigDecimal(rs.getFloat(fieldName));
            }
            case Types.DECIMAL: {
                return rs.getBigDecimal(fieldName);
            }
            default: {
                throw buildTypeErrorException(
                    new String[] {
                        "float", "double", "decimal"
                    },
                    colType
                );
            }
        }
    }

    public static LocalDateTime readDateTime(IEntityField field, ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        String fieldName = field.name();
        int colIndex = findColumnSchemaIndex(metaData, fieldName);

        if (colIndex < 0) {
            throw buildColumnNotExist(fieldName);
        }

        int colType = metaData.getColumnType(colIndex);
        switch (colType) {
            case Types.DATE: {
                Date date = rs.getDate(fieldName);
                return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
            }
            case Types.TIME: {
                Time time = rs.getTime(fieldName);
                return Instant.ofEpochMilli(time.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
            }
            case Types.TIMESTAMP: {
                Timestamp timestamp = rs.getTimestamp(fieldName);
                return Instant.ofEpochMilli(timestamp.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
            }
            default: {
                throw buildTypeErrorException(
                    new String[] {
                        "date", "time", "year", "datetime", "timestamp"
                    },
                    colType
                );
            }
        }
    }

    public static String readString(IEntityField field, ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        String fieldName = field.name();
        int colIndex = findColumnSchemaIndex(metaData, fieldName);

        if (colIndex < 0) {
            throw buildColumnNotExist(fieldName);
        }

        int colType = metaData.getColumnType(colIndex);
        switch (colType) {
            case Types.CHAR:
            case Types.VARCHAR: {
                return rs.getString(fieldName);
            }
            case Types.BLOB: {
                Blob blob = rs.getBlob(fieldName);
                byte[] buff = blob.getBytes(0, (int) blob.length());
                return new String(buff);
            }
            default: {
                throw buildTypeErrorException(
                    new String[] {
                        "char", "varchar", "tinytext", "text", "mediumtext",
                        "longtext", "tinyblob", "blob", "mediumblob"
                    },
                    colType
                );
            }
        }
    }

    public static boolean readBoolean(IEntityField field, ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        String fieldName = field.name();
        int colIndex = findColumnSchemaIndex(metaData, fieldName);

        if (colIndex < 0) {
            throw buildColumnNotExist(fieldName);
        }

        int colType = metaData.getColumnType(colIndex);
        switch (colType) {
            case Types.BOOLEAN: {
                return rs.getBoolean(fieldName);
            }
            case Types.BIT: {
                if (metaData.getColumnDisplaySize(colIndex) == 1) {
                    return rs.getBoolean(fieldName);
                } else {
                    throw buildTypeErrorException(
                        new String[] {
                            "bool", "bit(1)"
                        },
                        colType
                    );
                }
            }
            default: {
                throw buildTypeErrorException(
                    new String[] {
                        "bool", "bit(1)"
                    },
                    colType
                );
            }
        }
    }

    public static String readEnum(IEntityField field, ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        String fieldName = field.name();
        int colIndex = findColumnSchemaIndex(metaData, fieldName);

        if (colIndex < 0) {
            throw buildColumnNotExist(fieldName);
        }

        int colType = metaData.getColumnType(colIndex);
        switch (colType) {
            case Types.VARCHAR: {
                return rs.getString(fieldName);
            }
            default: {
                throw buildTypeErrorException(
                    new String[] {"varchar"},
                    colType
                );
            }
        }
    }

    private static int findColumnSchemaIndex(ResultSetMetaData metaData, String fieldName) throws SQLException {
        for (int i = 0; i < metaData.getColumnCount(); i++) {
            if (metaData.getColumnName(i + 1).equals(fieldName)) {
                return i + 1;
            }
        }

        return -1;
    }

    private static SQLException buildColumnNotExist(String fieldName) {
        return new SQLException(
            String.format("Field (%s), could not find a corresponding field in the native definition.", fieldName));
    }

    private static SQLException buildTypeErrorException(String[] acceptTypeNames, int currentType) {
        return new SQLException(String.format(
            "OQS type for Long, primitive types only accept [%s], the current for %s.",
            Arrays.toString(acceptTypeNames),
            TypesUtils.name(currentType)
        ));
    }
}
