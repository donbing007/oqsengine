package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.helper;

import com.xforceplus.ultraman.oqsengine.common.NumberUtils;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import java.util.Optional;

/**
 * 聚合字段的附件操作帮助工具.
 *
 * @author dongbin
 * @version 0.1 2022/6/20 18:03
 * @since 1.8
 */
public class AggregationAttachmentHelper {

    private static final char DIVIDER = '|';
    private static final int COUNT_INDEX = 0;
    private static final int SUM_INDEX = 1;

    private static final int NO = -1;

    /**
     * 读取值中附件记录的count值.
     * 如果无法读取将以小于0返回.
     *
     * @param value 目标值.
     * @return 值.
     */
    public static long count(IValue value) {
        return readLong(value, COUNT_INDEX);
    }

    /**
     * 读取值中附件记录的count值,如果读取不到将以设定的默认值返回.
     *
     * @param value        目标值.
     * @param defaultValue 读取不到的默认值.
     * @return 统计数量.
     */
    public static long count(IValue value, long defaultValue) {
        long count = count(value);
        return count == NO ? defaultValue : count;
    }

    /**
     * 读取值中附件记录的sum值.
     * 如果无法读取将以小于0返回.
     *
     * @param value 目标值.
     * @return 值.
     */
    public static long sum(IValue value) {
        return readLong(value, SUM_INDEX);
    }

    /**
     * 读取值中附件记录的sum值.
     * 如果无法读取将以指定的默认值返回.
     *
     * @param value        目标值.
     * @param defaultValue 默认值.
     * @return 值.
     */
    public static long sum(IValue value, long defaultValue) {
        long sum = sum(value);
        return sum == NO ? defaultValue : sum;
    }

    /**
     * 构造附件.
     *
     * @param count 统计数量.
     * @param sum   总值.
     * @return 附件.
     */
    public static String buildAttachment(long count, long sum) {
        StringBuilder buff = new StringBuilder();
        buff.append(count).append(DIVIDER).append(sum);
        return buff.toString();
    }

    private static long readLong(IValue value, int index) {
        if (value.getField().calculationType() != CalculationType.AGGREGATION) {
            return -1;
        }

        Optional<String> attachment = value.getAttachment();
        String str = attachment.map(v -> read(v, index)).orElse(Optional.of("")).get();
        if (NumberUtils.isIntegerString(str)) {
            return Long.parseLong(str);
        } else {
            return -1;
        }
    }

    private static Optional<String> read(String value, int index) {
        StringBuilder buff = new StringBuilder();
        int point = 0;
        for (char c : value.toCharArray()) {
            if (DIVIDER == c) {
                if (point == index) {
                    return Optional.ofNullable(buff.toString());
                } else {
                    point++;
                }
            } else {
                buff.append(c);
            }
        }

        return Optional.empty();
    }
}
