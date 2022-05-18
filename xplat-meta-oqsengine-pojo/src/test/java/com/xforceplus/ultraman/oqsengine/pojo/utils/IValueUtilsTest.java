package com.xforceplus.ultraman.oqsengine.pojo.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

/**
 * Created by justin.xu on 05/2022.
 *
 * @since 1.8
 */
public class IValueUtilsTest {
    private class TestCase {
        Object origin;
        Integer scale;
        Integer model;
        BigDecimal expected;


        public TestCase(Object origin, BigDecimal expected, Integer scale,
            Integer model) {
            this.origin = origin;
            this.expected = expected;
            this.scale = scale;
            this.model = model;
        }
    }

    List<TestCase> testCases = Arrays.asList(
        new TestCase("1.1111", new BigDecimal("1.1111"), null, null),
        new TestCase("1.545", new BigDecimal(1.54), 2, RoundingMode.DOWN.ordinal()),
        new TestCase(1, new BigDecimal(1), null, null),
        new TestCase(2L, new BigDecimal(2L), null, null),
        new TestCase(new BigDecimal(1024.386), new BigDecimal(1024.386), null, null),
        new TestCase(new BigDecimal(1024.385), new BigDecimal(1024.39), 2, RoundingMode.UP.ordinal()),
        new TestCase(1025.385, new BigDecimal(1025.385), null, null),
        new TestCase(1026.385, new BigDecimal(1026.38), 2, RoundingMode.DOWN.ordinal())
    );


    @Test
    public void toBigDecimalTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        IValueUtils iValueUtils = new IValueUtils();
        Method m = iValueUtils.getClass()
            .getDeclaredMethod("toBigDecimal", Object.class, Integer.class, Integer.class);
        m.setAccessible(true);

        //  test
        for (TestCase testCase : testCases) {
            BigDecimal r = (BigDecimal) m.invoke(iValueUtils, testCase.origin, testCase.scale, testCase.model);
            Assertions.assertEquals(testCase.expected.doubleValue(), r.doubleValue());
        }
    }
}
