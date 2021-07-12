package com.xforceplus.ultraman.oqsengine.calculation.function.constant;

import java.util.Arrays;

/**
 * .
 *
 * @author leo
 * @version 0.1 7/1/21 5:41 PM
 * @since 1.8
 */
public enum TimeUnit {

    YEAR(1, "年"),
    MONTH(2, "月"),
    DAY(2, "日"),
    HOUR(2, "小时"),
    MINUTE(2, "分钟"),
    SECOND(2, "秒"),
    MILLI(2, "毫秒"),
    WEEK(2, "星期"),
    QUARTER(2, "季度");

    private Integer value;
    private String desc;

    TimeUnit(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    /**
     * 根据值获取枚举实例.
     *
     * @param value enum value
     * @return TimeUnit
     */
    public static TimeUnit from(Integer value) {
        return Arrays.stream(TimeUnit.values())
            .filter(item -> item.value.equals(value))
            .findFirst().orElseThrow(() -> new RuntimeException("不存在的时间单位!"));
    }
}
