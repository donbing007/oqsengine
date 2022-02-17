package com.xforceplus.ultraman.oqsengine.cdc.consumer.tools;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.ZERO;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant;
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
     * 获取长整形数值型字段值，当异常时返回默认值.
     */
    public static long getLongFromColumn(List<CanalEntry.Column> columns, OqsBigEntityColumns oqsBigEntityColumns,
                                         Long defaultValue) {
        try {
            return getLongFromColumn(columns, oqsBigEntityColumns);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 获取长整形数值型字段值，抛出异常.
     */
    public static long getLongFromColumn(List<CanalEntry.Column> columns, OqsBigEntityColumns oqsBigEntityColumns)
        throws SQLException {
        return Long.parseLong(getColumnWithoutNull(columns, oqsBigEntityColumns).getValue());
    }

    /**
     * 获取整形数值字段值，当异常时返回默认值.
     */
    public static int getIntegerFromColumn(List<CanalEntry.Column> columns, OqsBigEntityColumns oqsBigEntityColumns,
                                           Integer defaultValue) {
        try {
            return getIntegerFromColumn(columns, oqsBigEntityColumns);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 获取整形数值字段值，抛出异常.
     */
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
    public static boolean getBooleanFromColumn(List<CanalEntry.Column> columns, OqsBigEntityColumns oqsBigEntityColumns) {
        String booleanValue = getColumnWithoutNull(columns, oqsBigEntityColumns).getValue();

        return stringToBoolean(booleanValue);
    }

    /**
     * 转换字符串为布尔.
     */
    public static boolean stringToBoolean(String str) {
        return str.equalsIgnoreCase("true")
            || (StringUtils.isNumeric(str) && Integer.parseInt(str) > ZERO);
    }

    /**
     * 获取字符串，允许为空.
     */
    public static String getStringWithoutNullCheck(List<CanalEntry.Column> columns,
                                                   OqsBigEntityColumns oqsBigEntityColumns) {
        return columns.get(oqsBigEntityColumns.ordinal()).getValue();
    }

    /**
     * 获取字段表示.
     */
    public static CanalEntry.Column getColumnWithoutNull(List<CanalEntry.Column> columns,
                                                         OqsBigEntityColumns oqsBigEntityColumns) {
        return columns.get(oqsBigEntityColumns.ordinal());
    }

}
