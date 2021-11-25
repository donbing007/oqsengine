package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 表示日期/时间值,储存时时间使用 Asia/Shanghai.
 *
 * @author wangzheng dongbin
 * @version 0.1 2020/2/18 20:54
 * @since 1.8
 */
public class DateTimeValue extends AbstractValue<LocalDateTime> {

    private final Logger logger = LoggerFactory.getLogger(DateTimeValue.class);

    /**
     * 格式化时使用的时区.
     */
    public static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    public static final LocalDateTime MIN_DATE_TIME = LocalDateTime.of(LocalDate.of(0, 1, 1),
        LocalTime.MIN);

    public DateTimeValue(IEntityField field, LocalDateTime value) {
        super(field, value);
    }

    public DateTimeValue(IEntityField field, LocalDateTime value, String attachment) {
        super(field, value, attachment);
    }

    @Override
    LocalDateTime fromString(String value) {
        if (value == null) {
            return null;
        }
        try {
            long timestamp = Long.parseLong(value);

            return toLocalDateTime(timestamp);
        } catch (Exception ex) {
            logger.error("{}", ex);
            return null;
        }
    }

    @Override
    public long valueToLong() {
        Instant instant = getValue().atZone(ZONE_ID).toInstant();
        return instant.toEpochMilli();
    }

    @Override
    protected IValue<LocalDateTime> doCopy(IEntityField newField, String attachment) {
        return new DateTimeValue(newField, getValue(), attachment);
    }

    @Override
    public String valueToString() {
        return Long.toString(valueToLong());
    }

    @Override
    public boolean compareByString() {
        return false;
    }

    /**
     * 默认的timestamp转LocalDateTime.
     */
    public static LocalDateTime toLocalDateTime(long timestamp) {
        return
            LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZONE_ID);
    }
}
