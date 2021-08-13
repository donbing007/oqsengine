package com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl;

import static com.xforceplus.ultraman.oqsengine.idgenerator.common.constant.Constants.DATE_PATTEN_PARSER;

import com.xforceplus.ultraman.oqsengine.idgenerator.parser.Pattern;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParser;
import java.time.LocalDateTime;

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
