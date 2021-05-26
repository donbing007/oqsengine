package com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl;

import com.xforceplus.ultraman.oqsengine.idgenerator.parser.Pattern;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PattenParser;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.xforceplus.ultraman.oqsengine.idgenerator.common.constant.Constants.DATE_PATTEN_PARSER;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 5/11/21 3:51 PM
 */
public class DatePattenParser implements PattenParser {
    @Override
    public String getName() {
        return DATE_PATTEN_PARSER;
    }

    public LocalDate getLocalDate() {
       return LocalDateTime.now().toLocalDate();
    }

    @Override
    public String parse(String patten, Long id) {
        LocalDate date =  getLocalDate();
        String year = String.valueOf(date.getYear());
        String month = String.format("%02d",date.getMonthValue());
        String day = String.format("%02d",date.getDayOfMonth());
        return patten.replace(Pattern.DAY,day).replace(Pattern.MONTH,month).replace(Pattern.YEAR,year);
    }

    @Override
    public boolean needHandle(String patten) {
        return patten.contains(Pattern.YEAR)
                || patten.contains(Pattern.MONTH)
                || patten.contains(Pattern.DAY);
    }
}
