package com.xforceplus.ultraman.oqsengine.calculate.utils;

import com.xforceplus.ultraman.oqsengine.calculate.exception.CalculateExecutionException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author j.xu
 * @version 0.1 2021/05/2021/5/17
 * @since 1.8
 */
public class TimeUtils {
    public static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    public static LocalDateTime convert(Long timestamp) {
        try {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZONE_ID);
        } catch (Exception ex) {
            throw new CalculateExecutionException("convert timestamp to localDateTime failed.");
        }
    }

    public static LocalDateTime convert(Date date) {
        try {
            return date.toInstant().atZone(ZONE_ID).toLocalDateTime();
        } catch (Exception ex) {
            throw new CalculateExecutionException("convert date to localDateTime failed.");
        }
    }

    public static Long toTimeStamp(Date date) {
        return date.getTime();
    }
}
