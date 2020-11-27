package com.xforceplus.ultraman.oqsengine.cdc.consumer.tools;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.List;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.*;
/**
 * desc :
 * name : BinLogParseUtils
 *
 * @author : xujia
 * date : 2020/11/11
 * @since : 1.8
 */
public class BinLogParseUtils {


    public static long getLongFromColumn(List<CanalEntry.Column> columns, OqsBigEntityColumns oqsBigEntityColumns) throws SQLException {
        return Long.parseLong(getColumnWithoutNull(columns, oqsBigEntityColumns).getValue());
    }

    public static int getIntegerFromColumn(List<CanalEntry.Column> columns, OqsBigEntityColumns oqsBigEntityColumns) throws SQLException {
        return Integer.parseInt(getColumnWithoutNull(columns, oqsBigEntityColumns).getValue());
    }

    public static String getStringFromColumn(List<CanalEntry.Column> columns, OqsBigEntityColumns oqsBigEntityColumns) throws SQLException {
        return getColumnWithoutNull(columns, oqsBigEntityColumns).getValue();
    }

    public static boolean getBooleanFromColumn(List<CanalEntry.Column> columns, OqsBigEntityColumns oqsBigEntityColumns) throws SQLException {
        String booleanValue = getColumnWithoutNull(columns, oqsBigEntityColumns).getValue();

        return convertStringToBoolean(booleanValue);
    }

    public static boolean convertStringToBoolean(String str) {
        try {
            return str.equalsIgnoreCase("true") ||
                    (StringUtils.isNumeric(str) && Integer.parseInt(str) > ZERO);
        } catch (Exception e) {
            throw e;
        }
    }

    public static CanalEntry.Column getColumnWithoutNull(List<CanalEntry.Column> columns, OqsBigEntityColumns oqsBigEntityColumns) throws SQLException {
        CanalEntry.Column column = existsColumn(columns, oqsBigEntityColumns);
        if (null == column || column.getValue().isEmpty()) {
            throw new SQLException(String.format("%s must not be null.", oqsBigEntityColumns.name()));
        }
        return column;
    }

    public static CanalEntry.Column existsColumn(List<CanalEntry.Column> columns, OqsBigEntityColumns compare) {
        CanalEntry.Column column = null;
        try {
            //  通过下标找一次，如果名字相同，则返回当前column
            column = columns.get(compare.ordinal());
            if (column.getName().toLowerCase().equals(compare.name().toLowerCase())) {
                return column;
            }
        } catch (Exception e) {
            //  out of band, logger error?
        }

        //  binlog记录在columns中顺序不对，需要遍历再找一次(通过名字)
        for (CanalEntry.Column value : columns) {
            if (compare.name().toLowerCase().equals(value.getName().toLowerCase())) {
                return value;
            }
        }

        return null;
    }

}
