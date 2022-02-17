package com.xforceplus.ultraman.oqsengine.common;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;

/**
 * 数组帮助工具.
 *
 * @author dongbin
 * @version 0.1 2021/09/22 15:01
 * @since 1.8
 */
public class ArrayUtil {

    private ArrayUtil() {}

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

    /**
     * 一个二分搜索的实现.
     * 其有JDK默认实现不同的是,其可以不比较元素本身,而是比较 keyFunction给出的返回值.
     * 这将允许例如下的调用.
     * Bean[] beans = ...
     * Bean b = ArrayUtil.binarySearch(beans, 3, (bean) -> bean.getId(), (id1, id2) -> Long.compare(id, o.id));
     * 这里查询beans中每一个元素中的id属性为3的bean实例.
     * 注意: 只能处理排序的数组,并且只能是升序排序.
     *
     * @param target      目标数组.
     * @param key         查找的目标.
     * @param keyFunction 查询元素获得方法.
     * @param comparator  比较器.
     * @param <T>         目标元素类型.
     * @param <R>         比较元素类型.
     * @return 目标元素的下标.
     */
    public static <T, R> int binarySearch(T[] target, R key, Function<T, R> keyFunction, Comparator<R> comparator) {
        int low = 0;
        int high = target.length - 1;
        int mid;
        int compareResult;
        R midKey;

        while (low <= high) {
            // div 2
            mid = (low + high) >>> 1;
            midKey = keyFunction.apply(target[mid]);

            compareResult = comparator.compare(key, midKey);

            if (compareResult == 0) {
                // 找到
                return mid;

            } else if (compareResult == -1) {

                high = mid - 1;

            } else {

                low = mid + 1;

            }
        }

        return -1;
    }

}
