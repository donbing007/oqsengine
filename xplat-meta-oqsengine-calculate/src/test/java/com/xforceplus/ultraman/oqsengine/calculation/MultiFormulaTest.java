package com.xforceplus.ultraman.oqsengine.calculation;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.xforceplus.ultraman.oqsengine.calculation.dto.ExpressionWrapper;
import com.xforceplus.ultraman.oqsengine.calculation.utils.aviator.AviatorHelper;
import com.xforceplus.ultraman.oqsengine.pojo.utils.TimeUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 多个公式字段测试.
 *
 * @author j.xu
 * @version 0.1 2021/05/2021/5/12
 * @since 1.8
 */
public class MultiFormulaTest {

    @Test
    public void test() {
        String expression1 =
            "a = 1010 + d; b = a + 100; c = a + b; let m = seq.map('%s', c, '%s', b, '%s', a); return m;";

        String c = "c";
        String b = "b";
        String a = "a";
        String fullExpression = String.format(expression1, c, b, a);

        Expression expression = AviatorEvaluator.getInstance()
            .compile("test", fullExpression, false);

        Assertions.assertNotNull(expression);

        Map<String, Object> params = new HashMap<>();
        params.put("d", 1010);

        Object value = expression.execute(params);

        Assertions.assertNotNull(value);
        //  Assert.assertNotNull(value.get("a"));
        //  Assert.assertNotNull(value.get("b"));
        //  Assert.assertNotNull(value.get("c"));
    }

    @Test
    public void testEmptyCalculate() throws Exception {
        String expression1 = "${amount} * ${taxRate} / 100 + ${amount}";

        ExpressionWrapper expressionWrapper =
            ExpressionWrapper.Builder.anExpression().withExpression(expression1).build();

        Expression expression = AviatorHelper.compile(expressionWrapper);

        Map<String, Object> params = new HashMap<>();
        BigDecimal expectedAmount = new BigDecimal("10010.000000");
        //  BigDecimal taxRate = BigDecimal.valueOf(new Double("0.130000"));
        //  params.put("taxRate", taxRate);
        params.put("amount", expectedAmount);
        Exception ex = null;
        //  测试当未传入taxRate时, 抛出exception
        try {
            expression.execute(params);
        } catch (Exception e) {
            ex = e;
        }
        Assertions.assertNotNull(ex);


        String expression2 = "string.join(seq.list(${abc}, ${xyz}, '-'));";

        expressionWrapper = ExpressionWrapper.Builder.anExpression().withExpression(expression2).build();
        expression = AviatorHelper.compile(expressionWrapper);
        params = new HashMap<>();
        params.put("xyz", "xyz");
        Object res = expression.execute(params);
        //  当abc不存在时，没有报错而是返回null
        Assertions.assertNull(res);
    }

    @Test
    public void testSimpleString() throws Exception {
        String a = "${name}";
        ExpressionWrapper expressionWrapper = ExpressionWrapper.Builder.anExpression().withExpression(a).build();


        Expression expression = AviatorHelper.compile(expressionWrapper);

        Map<String, Object> params = new HashMap<>();
        String expectedValue = "aaaaa";
        params.put("name", expectedValue);
        String res = (String) expression.execute(params);

        Assertions.assertEquals(expectedValue, res);
    }

    @Test
    public void testPlus() throws Exception {
        String expression1 = "${amount} * ${taxRate} / 100 + ${amount}";
        ExpressionWrapper expressionWrapper =
            ExpressionWrapper.Builder.anExpression().withExpression(expression1).build();

        Expression expression = AviatorHelper.compile(expressionWrapper);

        Map<String, Object> params = new HashMap<>();
        BigDecimal expectedAmount = new BigDecimal("10010.000000");
        //  Integer taxRate = 13;
        //  BigDecimal taxRate = BigDecimal.valueOf(13);
        BigDecimal taxRate = BigDecimal.valueOf(new Double("0.130000"));
        params.put("taxRate", taxRate);
        params.put("amount", expectedAmount);

        Object res = expression.execute(params);
        Assertions.assertEquals(BigDecimal.class, res.getClass());
        //  BigDecimal finalRes = ((BigDecimal)res).setScale(6);
        //  System.out.println(finalRes + "");
    }

    @Test
    public void testLocalDataTime() throws Exception {
        String expression1 = "${current_time}";
        ExpressionWrapper expressionWrapper =
            ExpressionWrapper.Builder.anExpression().withExpression(expression1).build();
        Expression expression = AviatorHelper.compile(expressionWrapper);
        long now = System.currentTimeMillis();
        Map<String, Object> params = new HashMap<>();
        params.put("current_time", now);

        Object res = expression.execute(params);
        Assertions.assertEquals(LocalDateTime.class, TimeUtils.convert((Long) res).getClass());

        expression1 = "sysdate()";
        expressionWrapper = ExpressionWrapper.Builder.anExpression().withExpression(expression1).build();
        expression = AviatorHelper.compile(expressionWrapper);
        res = expression.execute();
        Assertions.assertEquals(LocalDateTime.class, TimeUtils.convert((Date) res).getClass());
    }
}
