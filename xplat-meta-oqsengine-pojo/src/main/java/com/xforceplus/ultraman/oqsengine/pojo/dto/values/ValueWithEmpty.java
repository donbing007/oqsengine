package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

/**
 * 表示一个IValue的值为ValueWithEmtpy.
 */
public class ValueWithEmpty {

    public static final ValueWithEmpty EMPTY_VALUE = new ValueWithEmpty();

    private static final String NULL_VALUE = "$NULL$";

    public static boolean isEmpty(String strValue) {
        return strValue.equals(NULL_VALUE);
    }

    @Override
    public String toString() {
        return "$NULL$";
    }
}
