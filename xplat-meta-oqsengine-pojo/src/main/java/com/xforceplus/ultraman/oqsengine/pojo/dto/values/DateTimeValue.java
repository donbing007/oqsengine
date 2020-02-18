package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 表示日期/时间值,储存时时间使用 Asia/Shanghai.
 * @author wangzheng dongbin
 * @version 0.1 2020/2/18 20:54
 * @since 1.8
 */
public class DateTimeValue extends AbstractValue<LocalDateTime> {

    /**
     * 格式化时使用的时区.
     */
    public static final ZoneId zoneId = ZoneId.of("Asia/Shanghai");

    public DateTimeValue(String name, LocalDateTime value) {
        super(name, value);
    }

    @Override
    public long valueToLong() {
        Instant instant = getValue().atZone(zoneId).toInstant();
        return instant.toEpochMilli();
    }
}
