package com.xforceplus.ultraman.oqsengine.calculate;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.xforceplus.ultraman.oqsengine.calculate.dto.ExpressionWrapper;
import com.xforceplus.ultraman.oqsengine.calculate.utils.ExpressionUtils;
import com.xforceplus.ultraman.oqsengine.calculate.utils.TimeUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author j.xu
 * @version 0.1 2021/05/2021/5/12
 * @since 1.8
 */
public class MultiFormulaTest {

    @Test
    public void test() {
        String expression1 = "a = 1010 + d; b = a + 100; c = a + b; let m = seq.map('%s', c, '%s', b, '%s', a); return m;";

        String c = "c";
        String b = "b";
        String a = "a";
        String fullExpression = String.format(expression1, c, b, a);

        Expression expression = AviatorEvaluator.getInstance()
            .compile("test",  fullExpression, false);

        Assert.assertNotNull(expression);

        Map<String, Object> params = new HashMap<>();
        params.put("d", 1010);

        Object value = expression.execute(params);

        Assert.assertNotNull(value);
//        Assert.assertNotNull(value.get("a"));
//        Assert.assertNotNull(value.get("b"));
//        Assert.assertNotNull(value.get("c"));
    }

    @Test
    public void testSimpleString() {
        String a = "${name}";
        ExpressionWrapper expressionWrapper = ExpressionWrapper.Builder.anExpression().withExpression(a).build();


        Expression expression = ExpressionUtils.compile(expressionWrapper);

        Map<String, Object> params = new HashMap<>();
        String expectedValue = "aaaaa";
        params.put("name", expectedValue);
        String res = (String) expression.execute(params);

        Assert.assertEquals(expectedValue, res);
    }

    @Test
    public void testPlus() {
        String expression1 = "${amount} * ${taxRate} / 100 + ${amount}";
        ExpressionWrapper expressionWrapper = ExpressionWrapper.Builder.anExpression().withExpression(expression1).build();

        Expression expression = ExpressionUtils.compile(expressionWrapper);

        Map<String, Object> params = new HashMap<>();
        long expectedAmount = 10010;
//        Integer taxRate = 13;
//        BigDecimal taxRate = BigDecimal.valueOf(13);
        BigDecimal taxRate = BigDecimal.valueOf(new Double("0.13"));
        params.put("taxRate", taxRate);
        params.put("amount", expectedAmount);

        Object res = expression.execute(params);
        Assert.assertEquals(BigDecimal.class, res.getClass());
    }

    @Test
    public void testLocalDataTime() {
        String expression1 = "${current_time}";
        ExpressionWrapper expressionWrapper = ExpressionWrapper.Builder.anExpression().withExpression(expression1).build();
        Expression expression = ExpressionUtils.compile(expressionWrapper);
        long now = System.currentTimeMillis();
        Map<String, Object> params = new HashMap<>();
        params.put("current_time", now);

        Object res = expression.execute(params);
        Assert.assertEquals(LocalDateTime.class, TimeUtils.convert((Long) res).getClass());

        expression1 = "sysdate()";
        expressionWrapper = ExpressionWrapper.Builder.anExpression().withExpression(expression1).build();
        expression = ExpressionUtils.compile(expressionWrapper);
        res = expression.execute();
        Assert.assertEquals(LocalDateTime.class, TimeUtils.convert((Date) res).getClass());
    }
}
