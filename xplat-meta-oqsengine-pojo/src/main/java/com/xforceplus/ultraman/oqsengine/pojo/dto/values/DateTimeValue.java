package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

/**
 * 表示日期/时间值,储存时时间使用 Asia/Shanghai.
 *
 * @author wangzheng dongbin
 * @version 0.1 2020/2/18 20:54
 * @since 1.8
 */
public class DateTimeValue extends AbstractValue<LocalDateTime> {

    private Logger logger = LoggerFactory.getLogger(DateTimeValue.class);

    /**
     * 格式化时使用的时区.
     */
    public static final ZoneId zoneId = ZoneId.of("Asia/Shanghai");

    public DateTimeValue(IEntityField field, LocalDateTime value) {
        super(field, value);
    }

    @Override
    LocalDateTime fromString(String value) {
        if (value == null)
            return null;
        try {
            long timestamp = Long.parseLong(value);
            LocalDateTime time =
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zoneId);

            return time;
        } catch (Exception ex) {
            logger.error("{}", ex);
            return null;
        }
    }

    @Override
    public long valueToLong() {
        Instant instant = getValue().atZone(zoneId).toInstant();
        return instant.toEpochMilli();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getField(), getValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DateTimeValue)) {
            return false;
        }

        DateTimeValue that = (DateTimeValue) o;

        return Objects.equals(getField(), that.getField()) &&
                Objects.equals(this.getValue(), that.getValue());
    }

    @Override
    public IValue<LocalDateTime> shallowClone() {
        return new DateTimeValue(this.getField(), getValue());
    }

    @Override
    public boolean compareByString() {
        return false;
    }

    @Override
    public String toString() {
        return "DateTimeValue{" +
                "field=" + getField() +
                ", value=" + getValue() +
                '}';
    }
}
