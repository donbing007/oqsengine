package com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl;

import com.google.common.collect.Maps;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PattenParser;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PattenParserManager;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 5/12/21 4:19 PM
 */
public class DatePattenParserTest {

    @Test
    public void testNotNeedParse() {
        String expression = "yyyy:mm:dd{}";
        DatePattenParser parser = new DatePattenParser();
        boolean need =  parser.needHandle(expression);
        Assert.assertEquals(need,false);
    }

    @Test
    public void testNeedParse() {
        String expression = "{yyyy}:mm:dd{}";
        DatePattenParser parser = new DatePattenParser();
        boolean need = parser.needHandle(expression);
        Assert.assertEquals(need,true);
        String formatStr = parser.parse(expression,1001l);
        Assert.assertEquals(LocalDateTime.now().toLocalDate().getYear()+":mm:dd{}",formatStr);
    }

    @Test
    public void testDateParse() {
        String expression = "{yyyy}-{MM}-{dd}";
        DatePattenParser parser = new DatePattenParser();
        boolean need = parser.needHandle(expression);
        Assert.assertEquals(need,true);
        String formatStr = parser.parse(expression,1001l);
        LocalDate date = LocalDateTime.now().toLocalDate();
        Assert.assertEquals(date.getYear()+"-"+date.getMonth()
                +"-"+date.getDayOfMonth(),formatStr);
    }

    @Test
    public void testNumberParse() {
        String expression = "AP-SAP-{00000}";
        NumberPattenParser parser = new NumberPattenParser();
        boolean need = parser.needHandle(expression);
        Assert.assertEquals(need,true);
        String ret = parser.parse(expression,1234l);
        Assert.assertEquals(ret,"AP-SAP-01234");
        String ret2 = parser.parse(expression,109989898l);
        Assert.assertEquals(ret2,"AP-SAP-109989898");
        String exp = "{yyyy}-{MM}-{dd}-{00000}";
        boolean result = parser.needHandle(exp);
        Assert.assertEquals(result,true);
    }



    @Test
    public void testNumberAndDateParse() {
        PattenParserManager manager = new PattenParserManager();
        String expression = "{yyyy}-{MM}-{dd}-{00000}";
        NumberPattenParser parser = new NumberPattenParser();
        DatePattenParser datePattenParser = new DatePattenParser();
        manager.registVariableParser(parser);
        manager.registVariableParser(datePattenParser);
        String ret = manager.parse(expression,123l);
        LocalDateTime date = LocalDateTime.now();
       String expect =  date.getYear()+"-"+String.format("%02d",date.getMonthValue())
               +"-"+String.format("%02d",date.getDayOfMonth())+"-"+"00123";
       Assert.assertEquals(expect,ret);


    }

}
