package com.xforceplus.ultraman.oqsengine.pojo.reader.record;

/**
 * an emptyValue means to set null or clean the origin
 */
public class EmptyValue {

    public static final EmptyValue emptyValue = new EmptyValue();

    private static final String NULL_VALUE = "$NULL$";

    public static boolean isEmpty(String strValue){
        return strValue.equals(NULL_VALUE);
    }

    @Override
    public String toString() {
        return "$NULL$";
    }
}
