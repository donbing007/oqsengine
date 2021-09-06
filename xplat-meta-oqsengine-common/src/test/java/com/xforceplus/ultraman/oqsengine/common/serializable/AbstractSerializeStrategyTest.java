package com.xforceplus.ultraman.oqsengine.common.serializable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;

/**
 * 序列化测试抽像.
 *
 * @author dongbin
 * @version 0.1 2021/07/16 13:40
 * @since 1.8
 */
@Disabled("Test extraction, so no test is required.")
public abstract class AbstractSerializeStrategyTest {

    protected void doTest(SerializeStrategy serializeStrategy) {
        for (Case c : buildCase()) {
            byte[] bytes = serializeStrategy.serialize(c.getValue());

            Object unSerizlizeObj = serializeStrategy.unserialize(bytes, c.value.getClass());

            Assertions.assertEquals(c.getValue().getClass(), unSerizlizeObj.getClass());
            if (c.isArray()) {
                assertArray(c.getValue(), unSerizlizeObj);
            } else {
                Assertions.assertEquals(c.getValue(), unSerizlizeObj);
            }
        }
    }

    private void assertArray(Object expected, Object target) {
        switch (expected.getClass().getSimpleName()) {
            case "long[]": {
                Assertions.assertArrayEquals((long[]) expected, (long[]) target);
                break;
            }
            case "int[]": {
                Assertions.assertArrayEquals((int[]) expected, (int[]) target);
                break;
            }
            case "double[]": {
                Assertions.assertArrayEquals((double[]) expected, (double[]) target);
                break;
            }
            default: {
                Assertions.assertArrayEquals((Object[]) expected, (Object[]) target);
            }
        }
    }

    static class Case {
        private Serializable value;

        public Case(Serializable value) {
            this.value = value;
        }

        public Case(Supplier<Serializable> supplier) {
            this.value = supplier.get();
        }

        public Serializable getValue() {
            return value;
        }

        public boolean isArray() {
            if (value != null) {
                return value.getClass().isArray();
            } else {
                return false;
            }
        }
    }

    private Collection<Case> buildCase() {
        return Arrays.asList(
            new Case("123"),
            new Case(123),
            new Case(() -> {
                Map<String, String> map = new HashMap<>();
                map.put("1", "1");
                map.put("2", "2");
                return (Serializable) map;
            }),
            new Case(Long.MAX_VALUE),
            new Case(() -> new String[] {"abc", "bcd"}),
            new Case(() -> new long[] {1, 8, 8, 10}),
            new Case(() -> new double[] {1.0, 2.4, 8.88})
        );
    }
}
