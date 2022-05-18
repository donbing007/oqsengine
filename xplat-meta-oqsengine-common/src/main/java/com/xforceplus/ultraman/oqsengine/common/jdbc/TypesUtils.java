package com.xforceplus.ultraman.oqsengine.common.jdbc;

import java.lang.reflect.Modifier;
import java.sql.Types;
import java.util.Arrays;
import java.util.Optional;

/**
 * JDBC 类型的帮助工具.
 *
 * @author dongbin
 * @version 0.1 2022/2/28 15:03
 * @since 1.8
 */
public class TypesUtils {

    /*
    values 和 names 应该长度一致.
    values 的下标相对应的在 names 中为其字面名称.
     */
    private static TypeHolder[] holders;

    static {
        /*
        将 java.sql.Types 中的静态变量进行封装,以 值->字面名称 的二元形式保持.
        以值的降序顺序组成数组,以二分方式查询.
         */
        holders = Arrays.stream(Types.class.getFields())
            .filter(f -> Integer.TYPE.equals(f.getType()))
            .filter(f -> Modifier.isStatic(f.getModifiers()))
            .filter(f -> Modifier.isPublic(f.getModifiers()))
            .map(f -> {
                try {
                    return new TypeHolder(f.getInt(null), f.getName());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            })
            .sorted()
            .toArray(TypeHolder[]::new);

    }

    /**
     * 找到 java.sql.Types 中定义的类型的字面名称.
     *
     * @param value 类型值.
     * @see java.sql.Types
     * @return 字面名称.
     */
    public static Optional<String> name(int value) {
        TypeHolder holder = new TypeHolder(value, null);
        int index = Arrays.binarySearch(holders, holder);
        if (index > -1) {
            return Optional.ofNullable(holders[index].getName());
        } else {
            return Optional.empty();
        }
    }

    /*
    一个JDCB支持的类型表示.
     */
    private static class TypeHolder implements Comparable<TypeHolder> {
        private int value;
        private String name;

        public TypeHolder(int value, String name) {
            this.value = value;
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        @Override
        public int compareTo(TypeHolder o) {
            if (value < o.value) {
                return -1;
            } else if (value > o.value) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
