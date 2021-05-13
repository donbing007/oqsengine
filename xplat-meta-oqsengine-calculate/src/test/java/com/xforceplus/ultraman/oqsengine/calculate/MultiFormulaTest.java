package com.xforceplus.ultraman.oqsengine.calculate;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
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

    String expression = "a = 1010 + d; b = a + 100; c = a + b; let m = seq.map('%s', c, '%s', b, '%s', a); return m;";

    @Test
    public void test() {
        String c = "c";
        String b = "b";
        String a = "a";
        String fullExpression = String.format(expression, c, b, a);

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
}
