package com.xforceplus.ultraman.oqsengine.cdc.consumer.tools;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.ZERO;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class ColumnsUtils {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final Map<FieldType, BiFunction<List<CanalEntry.Column>, String, Object>> STATIC_HANDLER =
        new HashMap<>();

    static {
        STATIC_HANDLER.put(FieldType.STRING, ColumnsUtils::parseStringType);
        STATIC_HANDLER.put(FieldType.LONG, ColumnsUtils::parseLongType);
        STATIC_HANDLER.put(FieldType.BOOLEAN, ColumnsUtils::parseBooleanType);
        STATIC_HANDLER.put(FieldType.DECIMAL, ColumnsUtils::parseBigDecimalType);
        STATIC_HANDLER.put(FieldType.DATETIME, ColumnsUtils::parseDateTimeType);
        STATIC_HANDLER.put(FieldType.ENUM, ColumnsUtils::parseEnumType);
    }

    /**
     * 静态执行器, 选择执行器, 解析当前columns,获取最终值.
     *
     * @param columns   数据集,一个数据集代表一整行记录.
     * @param code      column名字.
     * @param fieldType column类型.
     * @return 最终值.
     */
    public static Object execute(List<CanalEntry.Column> columns, String code, FieldType fieldType) {
        BiFunction<List<CanalEntry.Column>, String, Object> func = STATIC_HANDLER.get(fieldType);
        if (null == func) {
            return null;
        }

        return func.apply(columns, code);
    }

    /**
     * 获取一个String类型的值.
     *
     * @param columns 数据集,一个数据集代表一整行记录.
     * @param code    column名字.
     * @return 最终值.
     */
    private static String parseStringType(List<CanalEntry.Column> columns, String code) {
        for (CanalEntry.Column column : columns) {
            if (column.hasName() && column.getName().equals(code)) {
                return column.getValue();
            }
        }
        return "";
    }

    /**
     * 获取一个Long类型的值.
     *
     * @param columns 数据集,一个数据集代表一整行记录.
     * @param code    column名字.
     * @return 最终值.
     */
    private static Long parseLongType(List<CanalEntry.Column> columns, String code) {
        String str = parseStringType(columns, code);
        if (!str.isEmpty()) {
            return Long.parseLong(str);
        }
        return null;
    }

    /**
     * 获取一个BigDecimal类型的值.
     *
     * @param columns 数据集,一个数据集代表一整行记录.
     * @param code    column名字.
     * @return 最终值.
     */
    private static String parseBigDecimalType(List<CanalEntry.Column> columns, String code) {
        return parseStringType(columns, code);
    }

    /**
     * 获取一个Boolean类型的值.
     *
     * @param columns 数据集,一个数据集代表一整行记录.
     * @param code    column名字.
     * @return 最终值.
     */
    private static Long parseBooleanType(List<CanalEntry.Column> columns, String code) {
        String str = parseStringType(columns, code);
        if (!str.isEmpty()) {
            if (str.equalsIgnoreCase("true") || (StringUtils.isNumeric(str) && Integer.parseInt(str) > ZERO)) {
                return 1L;
            }
            return 0L;
        }
        return null;
    }

    /**
     * 获取一个DateTime类型的值.
     *
     * @param columns 数据集,一个数据集代表一整行记录.
     * @param code    column名字.
     * @return 最终值.
     */
    private static Long parseDateTimeType(List<CanalEntry.Column> columns, String code) {
        String str = parseStringType(columns, code);
        if (!str.isEmpty()) {
            return toEpochMilli(str);
        }
        return null;
    }

    /**
     * 获取一个Enum类型的值.
     *
     * @param columns 数据集,一个数据集代表一整行记录.
     * @param code    column名字.
     * @return 最终值.
     */
    private static String parseEnumType(List<CanalEntry.Column> columns, String code) {
        return parseStringType(columns, code);
    }

    /**
     * 将时间格式转为Long型格式.
     *
     * @param strTime 字符串的时间戳.
     * @return long型时间戳.
     */
    public static Long toEpochMilli(String strTime) {
        if (null == strTime) {
            return null;
        }
        return LocalDateTime.parse(strTime, DATE_TIME_FORMATTER).atZone(DateTimeValue.ZONE_ID).toInstant().toEpochMilli();
    }
}
