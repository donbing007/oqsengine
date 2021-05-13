package com.xforceplus.ultraman.oqsengine.calculate.utils;

/**
 * @author j.xu
 * @version 0.1 2021/05/2021/5/12
 * @since 1.8
 */
public class TypeCheck {

    /**
     * 校验当前传入的值类型是否和预期相符，如果预期为空，则直接返回true.
     */
    public static boolean check(Class<?> expectedType, Object v) {
        if (null == expectedType) {
            return true;
        }

        return v.getClass().equals(expectedType);
    }
}
