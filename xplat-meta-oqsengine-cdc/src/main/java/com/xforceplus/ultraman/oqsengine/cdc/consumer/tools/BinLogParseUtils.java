package com.xforceplus.ultraman.oqsengine.cdc.consumer.tools;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.ZERO;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns;
import java.sql.SQLException;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * mysql bin log 解析工具.
 *
 * @author xujia 2020/11/11
 * @since : 1.8
 */
public class BinLogParseUtils {
    /**
     * 获取长整形数值型字段值.
     */
    public static long getLongFromColumn(List<CanalEntry.Column> columns, OqsBigEntityColumns oqsBigEntityColumns,
                                         Long defaultValue) {
        try {
            return getLongFromColumn(columns, oqsBigEntityColumns);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static long getLongFromColumn(List<CanalEntry.Column> columns, OqsBigEntityColumns oqsBigEntityColumns)
        throws SQLException {
        return Long.parseLong(getColumnWithoutNull(columns, oqsBigEntityColumns).getValue());
    }

    /**
     * 获取整形数值字段值.
     */
    public static int getIntegerFromColumn(List<CanalEntry.Column> columns, OqsBigEntityColumns oqsBigEntityColumns,
                                           Integer defaultValue) {
        try {
            return getIntegerFromColumn(columns, oqsBigEntityColumns);
        } catch (Exception e) {
            return defaultValue;
        }
    }


    public static int getIntegerFromColumn(List<CanalEntry.Column> columns, OqsBigEntityColumns oqsBigEntityColumns)
        throws SQLException {
        return Integer.parseInt(getColumnWithoutNull(columns, oqsBigEntityColumns).getValue());
    }

    public static String getStringFromColumn(List<CanalEntry.Column> columns, OqsBigEntityColumns oqsBigEntityColumns)
        throws SQLException {
        return getColumnWithoutNull(columns, oqsBigEntityColumns).getValue();
    }

    /**
     * 获取bool类型字段值.
     */
    public static boolean getBooleanFromColumn(List<CanalEntry.Column> columns, OqsBigEntityColumns oqsBigEntityColumns)
        throws SQLException {
        String booleanValue = getColumnWithoutNull(columns, oqsBigEntityColumns).getValue();

        return convertStringToBoolean(booleanValue);
    }

    /**
     * 转换字符串为布尔.
     */
    public static boolean convertStringToBoolean(String str) {
        try {
            return str.equalsIgnoreCase("true")
                || (StringUtils.isNumeric(str) && Integer.parseInt(str) > ZERO);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 获取字段表示.
     */
    public static CanalEntry.Column getColumnWithoutNull(List<CanalEntry.Column> columns,
                                                         OqsBigEntityColumns oqsBigEntityColumns) throws SQLException {
        CanalEntry.Column column = existsColumn(columns, oqsBigEntityColumns);
        if (null == column || column.getValue().isEmpty()) {
            throw new SQLException(String.format("%s must not be null.", oqsBigEntityColumns.name()));
        }
        return column;
    }

    /**
     * 判断是否存在字段.
     */
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
