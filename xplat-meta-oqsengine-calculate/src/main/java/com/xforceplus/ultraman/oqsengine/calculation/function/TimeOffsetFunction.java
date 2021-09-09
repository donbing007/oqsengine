package com.xforceplus.ultraman.oqsengine.calculation.function;

import com.alibaba.google.common.base.Preconditions;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.xforceplus.ultraman.oqsengine.calculation.function.constant.TimeUnit;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * .
 *
 * @author leo
 * @version 0.1 7/1/21 5:14 PM
 * @since 1.8
 */
public class TimeOffsetFunction extends AbstractFunction {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeOffsetFunction.class);

    @Override
    public String getName() {
        return "timeOffset";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject date, AviatorObject no, AviatorObject amount) {
        LOGGER.info("INPUT DATA is {}", date.getValue(env).toString());
        Preconditions.checkNotNull(env);
        Preconditions.checkNotNull(amount);
        Preconditions.checkArgument(date.getValue(env) instanceof LocalDateTime, "must be LocalDateTime instance!");
        LocalDateTime dateTime = (LocalDateTime) date.getValue(env);
        int offset = Integer.valueOf(String.valueOf(amount.getValue(env)));
        int unit = Integer.valueOf(String.valueOf(no.getValue(env)));
        LocalDateTime result = null;
        switch (TimeUnit.from(unit)) {
            case YEAR:
                result = dateTime.plus(offset, ChronoUnit.YEARS);
                break;
            case QUARTER:
                result = dateTime.plus(offset * 3, ChronoUnit.MONTHS);
                break;
            case MONTH:
                result = dateTime.plus(offset, ChronoUnit.MONTHS);
                break;
            case DAY:
                result = dateTime.plus(offset, ChronoUnit.DAYS);
                break;
            case HOUR:
                result = dateTime.plus(offset, ChronoUnit.HOURS);
                break;
            case MINUTE:
                result = dateTime.plus(offset, ChronoUnit.MINUTES);
                break;
            case SECOND:
                result = dateTime.plus(offset, ChronoUnit.SECONDS);
                break;
            case MILLI:
                result = dateTime.plus(offset, ChronoUnit.MILLIS);
                break;
            case WEEK:
                result = dateTime.plus(offset, ChronoUnit.WEEKS);
                break;
            default:
                result = dateTime;
                break;
        }
        ZoneId zoneId = ZoneId.systemDefault();
        LOGGER.info("ZoneID is {}", zoneId);
        Date newDate = Date.from(result.atZone(zoneId).toInstant());
        return FunctionUtils.wrapReturn(newDate);
    }
}
