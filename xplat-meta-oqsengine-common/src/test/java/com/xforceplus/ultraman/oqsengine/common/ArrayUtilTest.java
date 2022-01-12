package com.xforceplus.ultraman.oqsengine.common;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 数组帮助工具测试.
 *
 * @author dongbin
 * @version 0.1 2021/09/22 15:17
 * @since 1.8
 */
public class ArrayUtilTest {

    @Test
    public void testRemoveDuplicateWithSort() {
        String[] target = new String[] {
            "1", "1", "3", "5", "8", "9", "9", "10"
        };

        String[] expected = new String[] {
            "1", "3", "5", "8", "9", "10"
        };

        String[] results = ArrayUtil.removeDuplicateWithSort(target);

        Assertions.assertArrayEquals(expected, results);
    }

    @Test
    public void testRemoveDuplicateIntWithSort() {
        int[] target = new int[] {
            1, 1, 3, 5, 8, 9, 9, 10
        };

        int[] expected = new int[] {
            1, 3, 5, 8, 9, 10
        };

        int[] results = ArrayUtil.removeDuplicateWithSort(target);

        Assertions.assertArrayEquals(expected, results);
    }

    @Test
    public void testRemoveDuplicateLongWithSort() {
        long[] target = new long[] {
            1, 1, 3, 5, 8, 9, 9, 10
        };

        long[] expected = new long[] {
            1, 3, 5, 8, 9, 10
        };

        long[] results = ArrayUtil.removeDuplicateWithSort(target);

        Arrays.equals(expected, results);

        target = new long[] {
            1, 1, 1, 1, 1, 1
        };
        expected = new long[] {
            1
        };
        results = ArrayUtil.removeDuplicateWithSort(target);
        Assertions.assertArrayEquals(expected, results);
    }

    @Test
    public void testLongMergeRemoveDuplicateWithSort() {
        long[] a = new long[] {
            1, 1, 3, 5, 8, 9, 9, 10
        };
        long[] b = new long[] {
            1, 4, 7, 8, 11, 12
        };
        long[] expected = new long[] {
            1, 3, 4, 5, 7, 8, 9, 10, 11, 12
        };

        long[] results = ArrayUtil.mergeRemoveDuplicateWithSort(a, b);

        Assertions.assertArrayEquals(expected, results);

        // 现在a 比较短.
        a = new long[] {
            10, 10, 10, 10
        };
        b = new long[] {
            8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8
        };
        expected = new long[] {
            8, 10
        };

        results = ArrayUtil.mergeRemoveDuplicateWithSort(a, b);

        Assertions.assertArrayEquals(expected, results);

        // 两者相等
        a = new long[] {
            10, 10, 10, 10,
        };
        b = new long[] {
            8, 8, 8, 8,
        };
        expected = new long[] {
            8, 10
        };

        results = ArrayUtil.mergeRemoveDuplicateWithSort(a, b);

        Assertions.assertArrayEquals(expected, results);
    }

    @Test
    public void testIntMergeRemoveDuplicateWithSort() {
        int[] a = new int[] {
            1, 1, 3, 5, 8, 9, 9, 10
        };
        int[] b = new int[] {
            1, 4, 7, 8, 11, 12
        };
        int[] expected = new int[] {
            1, 3, 4, 5, 7, 8, 9, 10, 11, 12
        };

        int[] results = ArrayUtil.mergeRemoveDuplicateWithSort(a, b);

        Assertions.assertArrayEquals(expected, results);

        // 现在a 比较短.
        a = new int[] {
            10, 10, 10, 10
        };
        b = new int[] {
            8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8
        };
        expected = new int[] {
            8, 10
        };

        results = ArrayUtil.mergeRemoveDuplicateWithSort(a, b);

        Assertions.assertArrayEquals(expected, results);

        // 两者相等
        a = new int[] {
            10, 10, 10, 10,
        };
        b = new int[] {
            8, 8, 8, 8,
        };
        expected = new int[] {
            8, 10
        };

        results = ArrayUtil.mergeRemoveDuplicateWithSort(a, b);

        Assertions.assertArrayEquals(expected, results);
    }

    @Test
    public void testObjectMergeRemoveDuplicateWithSort() {
        String[] a = new String[] {
            "1", "1", "3", "5", "8", "9", "9", "10"
        };
        String[] b = new String[] {
            "1", "4", "7", "8", "11", "12"
        };
        String[] expected = new String[] {
            "1", "3", "4", "5", "7", "8", "9", "10", "11", "12"
        };

        String[] results = ArrayUtil.mergeRemoveDuplicateWithSort(a, b, (o1, o2) -> {
            int v1 = Integer.parseInt(o1);
            int v2 = Integer.parseInt(o2);
            if (v1 < v2) {
                return -1;
            } else if (v1 > v2) {
                return 1;
            } else {
                return 0;
            }
        });

        Assertions.assertArrayEquals(expected, results);

        // 现在a 比较短.
        a = new String[] {
            "10", "10", "10", "10"
        };
        b = new String[] {
            "8", "8", "8", "8", "8", "8", "8", "8", "8", "8", "8", "8"
        };
        expected = new String[] {
            "8", "10"
        };

        results = ArrayUtil.mergeRemoveDuplicateWithSort(a, b, (o1, o2) -> {
            int v1 = Integer.parseInt(o1);
            int v2 = Integer.parseInt(o2);
            if (v1 < v2) {
                return -1;
            } else if (v1 > v2) {
                return 1;
            } else {
                return 0;
            }
        });

        Assertions.assertArrayEquals(expected, results);

        // 两者相等
        a = new String[] {
            "10", "10", "10", "10",
        };
        b = new String[] {
            "8", "8", "8", "8",
        };
        expected = new String[] {
            "8", "10"
        };

        results = ArrayUtil.mergeRemoveDuplicateWithSort(a, b, (o1, o2) -> {
            int v1 = Integer.parseInt(o1);
            int v2 = Integer.parseInt(o2);
            if (v1 < v2) {
                return -1;
            } else if (v1 > v2) {
                return 1;
            } else {
                return 0;
            }
        });

        Assertions.assertArrayEquals(expected, results);
    }

    @Test
    public void testBinarySearch() throws Exception {
        Bean[] beans = IntStream.range(0, 100).mapToObj(i -> new Bean(i, Integer.toString(i))).toArray(Bean[]::new);

        int index = ArrayUtil.binarySearch(beans, 32, (b) -> b.attr0, Integer::compare);
        Assertions.assertTrue(index >= 0);

        Bean bean = beans[index];
        Assertions.assertEquals(32, bean.attr0);
        Assertions.assertEquals("32", bean.attr1);

        index = ArrayUtil.binarySearch(beans, "33", (b) -> b.attr1, Comparator.comparingInt(Integer::parseInt));
        Assertions.assertTrue(index >= 0);
        bean = beans[index];

        Assertions.assertEquals(33, bean.attr0);
        Assertions.assertEquals("33", bean.attr1);

        index = ArrayUtil.binarySearch(beans, "330", (b) -> b.attr1, Comparator.comparingInt(Integer::parseInt));
        Assertions.assertTrue(index < 0);
    }

    private static class Bean {
        private int attr0;
        private String attr1;

        public Bean(int attr0, String attr1) {
            this.attr0 = attr0;
            this.attr1 = attr1;
        }
    }
}