package com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl;

import static com.xforceplus.ultraman.oqsengine.idgenerator.common.constant.Constants.DATE_PATTEN_PARSER;

import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.TimeDelay;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.Pattern;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParser;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明: 日期模版解析类.
 * 作者(@author): liwei
 * 创建时间: 5/11/21 3:51 PM
 */
public class DatePatternParser implements PatternParser {
    @Override
    public String getName() {
        return DATE_PATTEN_PARSER;
    }

    public LocalDateTime getLocalDate() {
        return LocalDateTime.now();
    }

    /**
     * 获取最大过期时间.
     *
     * @param pattern the pattern of counter.
     * @return TimeDelay.
     */
    public static TimeDelay getMaxExpireDate(String pattern) {
        if (pattern.contains(Pattern.HOUR)) {
            return new TimeDelay(1, TimeUnit.HOURS);
        }
        if (pattern.contains(Pattern.DAY)
            && !pattern.contains(Pattern.HOUR)) {
            return new TimeDelay(1, TimeUnit.DAYS);
        }
        if (pattern.contains(Pattern.MONTH)
            && !pattern.contains(Pattern.DAY)
            && !pattern.contains(Pattern.HOUR)) {
            return new TimeDelay(31, TimeUnit.DAYS);
        }
        if (pattern.contains(Pattern.YEAR)
            && !pattern.contains(Pattern.MONTH)
            && !pattern.contains(Pattern.DAY) && !pattern.contains(Pattern.HOUR)) {
            return new TimeDelay(366, TimeUnit.DAYS);
        }
        return null;
    }

    @Override
    public String parse(String patten, Long id) {
        LocalDateTime date = getLocalDate();
        String year = String.valueOf(date.getYear());
        String month = String.format("%02d", date.getMonthValue());
        String day = String.format("%02d", date.getDayOfMonth());
        String hour = String.format("%02d", date.getHour());
        return patten.replace(Pattern.DAY, day).replace(Pattern.MONTH, month).replace(Pattern.YEAR, year)
            .replace(Pattern.HOUR, hour);
    }

    @Override
    public boolean needHandle(String patten) {
        return patten.contains(Pattern.YEAR)
            || patten.contains(Pattern.MONTH)
            || patten.contains(Pattern.DAY)
            || patten.contains(Pattern.HOUR);
    }
}
