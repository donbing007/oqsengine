package com.xforceplus.ultraman.oqsengine.common;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;

/**
 * 数组帮助工具.
 *
 * @author dongbin
 * @version 0.1 2021/09/22 15:01
 * @since 1.8
 */
public class ArrayUtil {

    /**
     * 对有序长整形进行去重.
     *
     * @param target 目标整形列表.
     * @return 去重结果.
     */
    public static long[] removeDuplicateWithSort(long[] target) {
        if (target == null) {
            return target;
        }
        // 一个或者无元素不需要去重.
        final int threshold = 1;
        if (target.length <= threshold) {
            return target;
        }

        long[] useTarget = Arrays.copyOf(target, target.length);
        int slow = 0;
        int fast = 1;

        long slowElem;
        long fastElem;
        while (fast < useTarget.length) {
            slowElem = useTarget[slow];
            fastElem = useTarget[fast];

            if (slowElem != fastElem) {
                useTarget[++slow] = useTarget[fast];
            }

            fast++;
        }

        return Arrays.copyOfRange(useTarget, 0, slow + 1);
    }

    /**
     * 对有序整形进行去重.
     *
     * @param target 目标整形列表.
     * @return 去重结果.
     */
    public static int[] removeDuplicateWithSort(int[] target) {
        if (target == null) {
            return target;
        }
        // 一个或者无元素不需要去重.
        final int threshold = 1;
        if (target.length <= threshold) {
            return target;
        }

        int[] useTarget = Arrays.copyOf(target, target.length);
        int slow = 0;
        int fast = 1;

        int slowElem;
        int fastElem;
        while (fast < useTarget.length) {
            slowElem = useTarget[slow];
            fastElem = useTarget[fast];

            if (slowElem != fastElem) {
                useTarget[++slow] = useTarget[fast];
            }

            fast++;
        }

        return Arrays.copyOfRange(useTarget, 0, slow + 1);
    }

    /**
     * 对有序元素进行排重. 注意:数组必须保证有序,否则将得到意外的结果.
     *
     * @param target 目标元素列表.
     * @param <T>    元素类型.
     * @return 去重结果.
     */
    public static <T> T[] removeDuplicateWithSort(T[] target) {
        if (target == null) {
            return target;
        }
        // 一个或者无元素不需要去重.
        final int threshold = 1;
        if (target.length <= threshold) {
            return target;
        }

        T[] useTarget = Arrays.copyOf(target, target.length);
        int slow = 0;
        int fast = 1;

        T slowElem;
        T fastElem;
        while (fast < useTarget.length) {
            slowElem = useTarget[slow];
            fastElem = useTarget[fast];

            if (!slowElem.equals(fastElem)) {
                useTarget[++slow] = useTarget[fast];
            }

            fast++;
        }

        return Arrays.copyOfRange(useTarget, 0, slow + 1);
    }

    /**
     * 合并两个有序long型数组, 并且去除重复.
     *
     * @param a 目标数组.
     * @param b 来源数组.
     * @return 合并后的结果.
     */
    public static long[] mergeRemoveDuplicateWithSort(long[] a, long[] b) {
        if ((a == null || a.length == 0) && (b == null || b.length == 0)) {
            return new long[0];
        }

        if (a == null || a.length == 0) {
            return b;
        }

        if (b == null || b.length == 0) {
            return a;
        }

        long[] useA = removeDuplicateWithSort(a);
        long[] useB = removeDuplicateWithSort(b);
        long[] buff = new long[a.length + b.length];
        int p0 = 0;
        int p1 = 0;
        int point = 0;
        while (p0 < useA.length && p1 < useB.length) {
            if (useA[p0] < useB[p1]) {
                // lt
                buff[point] = useA[p0];
                p0++;
            } else if (useA[p0] > b[p1]) {
                // ge
                buff[point] = b[p1];
                p1++;
            } else {
                // eq
                buff[point] = useA[p0];
                p0++;
                p1++;
            }
            if (point > 0) {
                // 重复
                if (buff[point] != buff[point - 1]) {
                    point++;
                }
            } else {
                // 单个元素不会重复.
                point++;
            }
        }

        if (p0 < useA.length) {
            System.arraycopy(useA, p0, buff, point, useA.length - p0);
            point += useA.length - p0;
        } else {
            System.arraycopy(useB, p1, buff, point, useB.length - p1);
            point += useB.length - p1;
        }

        return Arrays.copyOfRange(buff, 0, point);
    }

    /**
     * 合并两个有序int型数组, 并且去除重复.
     *
     * @param a 目标数组.
     * @param b 来源数组.
     * @return 合并后的结果.
     */
    public static int[] mergeRemoveDuplicateWithSort(int[] a, int[] b) {
        if ((a == null || a.length == 0) && (b == null || b.length == 0)) {
            return new int[0];
        }

        if (a == null || a.length == 0) {
            return b;
        }

        if (b == null || b.length == 0) {
            return a;
        }

        int[] useA = removeDuplicateWithSort(a);
        int[] useB = removeDuplicateWithSort(b);
        int[] buff = new int[a.length + b.length];
        int p0 = 0;
        int p1 = 0;
        int point = 0;
        while (p0 < useA.length && p1 < useB.length) {
            if (useA[p0] < useB[p1]) {
                // lt
                buff[point] = useA[p0];
                p0++;
            } else if (useA[p0] > b[p1]) {
                // ge
                buff[point] = b[p1];
                p1++;
            } else {
                // eq
                buff[point] = useA[p0];
                p0++;
                p1++;
            }
            if (point > 0) {
                // 重复
                if (buff[point] != buff[point - 1]) {
                    point++;
                }
            } else {
                // 单个元素不会重复.
                point++;
            }
        }

        if (p0 < useA.length) {
            System.arraycopy(useA, p0, buff, point, useA.length - p0);
            point += useA.length - p0;
        } else {
            System.arraycopy(useB, p1, buff, point, useB.length - p1);
            point += useB.length - p1;
        }

        return Arrays.copyOfRange(buff, 0, point);
    }

    /**
     * 合并两个数组,并且去除重复.
     *
     * @param a          目标数组.
     * @param b          来源数组.
     * @param comparator 比较器.
     * @param <T>        元素类型.
     * @return 合并结果.
     */
    public static <T> T[] mergeRemoveDuplicateWithSort(T[] a, T[] b, Comparator<T> comparator) {
        Class<? extends T[]> newType = (Class<? extends T[]>) a.getClass();

        if ((a == null || a.length == 0) && (b == null || b.length == 0)) {
            return ((Object) newType == (Object) Object[].class)
                ? (T[]) new Object[0]
                : (T[]) Array.newInstance(newType.getComponentType(), 0);
        }

        if (a == null || a.length == 0) {
            return b;
        }

        if (b == null || b.length == 0) {
            return a;
        }

        T[] useA = removeDuplicateWithSort(a);
        T[] useB = removeDuplicateWithSort(b);

        T[] buff = ((Object) newType == (Object) Object[].class)
            ? (T[]) new Object[a.length + b.length]
            : (T[]) Array.newInstance(newType.getComponentType(), a.length + b.length);

        int p0 = 0;
        int p1 = 0;
        int point = 0;
        int compareResult;
        while (p0 < useA.length && p1 < useB.length) {
            compareResult = comparator.compare(useA[p0], useB[p1]);
            if (compareResult < 0) {
                // lt
                buff[point] = useA[p0];
                p0++;
            } else if (compareResult > 0) {
                // ge
                buff[point] = b[p1];
                p1++;
            } else {
                // eq
                buff[point] = useA[p0];
                p0++;
                p1++;
            }
            if (point > 0) {
                // 重复
                if (comparator.compare(buff[point], buff[point - 1]) != 0) {
                    point++;
                }
            } else {
                // 单个元素不会重复.
                point++;
            }
        }

        if (p0 < useA.length) {
            System.arraycopy(useA, p0, buff, point, useA.length - p0);
            point += useA.length - p0;
        } else {
            System.arraycopy(useB, p1, buff, point, useB.length - p1);
            point += useB.length - p1;
        }

        return Arrays.copyOfRange(buff, 0, point);
    }
}
