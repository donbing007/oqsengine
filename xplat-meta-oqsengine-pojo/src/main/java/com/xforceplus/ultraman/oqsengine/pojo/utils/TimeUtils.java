package com.xforceplus.ultraman.oqsengine.pojo.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 日期、时间戳转LocalDateTime.
 */
public class TimeUtils {
    public static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    /**
     * 将timestamp转为LocalDateTime.
     */
    public static LocalDateTime convert(Long timestamp) {
        try {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZONE_ID);
        } catch (Exception ex) {
            throw new IllegalArgumentException("convert timestamp to localDateTime failed.");
        }
    }

    /**
     * 将date转为LocalDateTime.
     */
    public static LocalDateTime convert(Date date) {
        try {
            return date.toInstant().atZone(ZONE_ID).toLocalDateTime();
        } catch (Exception ex) {
            throw new IllegalArgumentException("convert date to localDateTime failed.");
        }
    }
}
