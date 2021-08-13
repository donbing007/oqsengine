package com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl;

import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParserManager;
import java.time.format.DateTimeFormatter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 5/12/21 4:19 PM
 */
public class DatePatternParserTest {

    @Test
    public void testNotNeedParse() {
        String expression = "yyyy:mm:dd{}";
        DatePatternParser parser = new DatePatternParser();
        boolean need = parser.needHandle(expression);
        Assertions.assertFalse(need);
    }

    @Test
    public void testNeedParse() {
        String expression = "{yyyy}:mm:dd{}";
        DatePatternParser parser = new DatePatternParser();
        boolean need = parser.needHandle(expression);
        Assertions.assertTrue(need);
        String formatStr = parser.parse(expression, 1001l);
        Assertions.assertEquals(LocalDateTime.now().toLocalDate().getYear() + ":mm:dd{}", formatStr);
    }

    @Test
    public void testDateParse() {
        String expression = "{yyyy}-{MM}-{dd}";
        DatePatternParser parser = new DatePatternParser();
        boolean need = parser.needHandle(expression);
        Assertions.assertTrue(need);
        String formatStr = parser.parse(expression, 1001l);
        LocalDate date = LocalDateTime.now().toLocalDate();
        Assertions.assertEquals(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), formatStr);
    }

    @Test
    public void testDateParseTime() {
        String expression = "{yyyy}-{MM}-{dd}-{HH}";
        DatePatternParser parser = new DatePatternParser();
        boolean need = parser.needHandle(expression);
        Assertions.assertEquals(need,true);
        String formatStr = parser.parse(expression,1001l);
        LocalDateTime dateTime = LocalDateTime.now();
        String pre = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
        Assertions.assertEquals(pre,formatStr);
    }

    @Test
    public void testNumberParse() {
        String expression = "AP-SAP-{00000}";
        NumberPatternParser parser = new NumberPatternParser();
        boolean need = parser.needHandle(expression);
        Assertions.assertTrue(need);
        String ret = parser.parse(expression, 1234l);
        Assertions.assertEquals(ret, "AP-SAP-01234");
        String ret2 = parser.parse(expression, 109989898l);
        Assertions.assertEquals(ret2, "AP-SAP-109989898");
        String exp = "{yyyy}-{MM}-{dd}-{00000}";
        boolean result = parser.needHandle(exp);
        Assertions.assertTrue(result);
    }


    @Test
    public void testNumberAndDateParse() {
        PatternParserManager manager = new PatternParserManager();
        String expression = "{yyyy}-{MM}-{dd}-{00000}";
        NumberPatternParser parser = new NumberPatternParser();
        DatePatternParser datePattenParser = new DatePatternParser();
        manager.registVariableParser(parser);
        manager.registVariableParser(datePattenParser);
        String ret = manager.parse(expression, 123l);
        LocalDateTime date = LocalDateTime.now();
        String expect = date.getYear() + "-" + String.format("%02d", date.getMonthValue())
            + "-" + String.format("%02d", date.getDayOfMonth()) + "-" + "00123";
        Assertions.assertEquals(expect, ret);
    }

}
