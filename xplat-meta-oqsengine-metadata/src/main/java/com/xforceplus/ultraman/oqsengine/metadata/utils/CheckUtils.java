package com.xforceplus.ultraman.oqsengine.metadata.utils;

import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;

import static com.xforceplus.ultraman.oqsengine.meta.common.exception.Code.BUSINESS_HANDLER_ERROR;

/**
 * desc :
 * name : CheckUtils
 *
 * @author : xujia
 * date : 2021/2/9
 * @since : 1.8
 */
public class CheckUtils {

    public static int retWithCheckByLessThan(int checker, int compare, String message) {
        if (checker > compare) {
            throw new MetaSyncClientException(message, BUSINESS_HANDLER_ERROR.ordinal());
        }
        return checker;
    }

    public static int retWithCheckByLargeThan(int checker, int compare, String message) {
        if (checker < compare) {
            throw new MetaSyncClientException(message, BUSINESS_HANDLER_ERROR.ordinal());
        }
        return checker;
    }

    public static long retWithCheckByLessThan(long checker, long compare, String message) {
        if (checker >= compare) {
            throw new MetaSyncClientException(message, BUSINESS_HANDLER_ERROR.ordinal());
        }
        return checker;
    }

    public static long retWithCheckByLargeThan(long checker, long compare, String message) {
        if (checker <= compare) {
            throw new MetaSyncClientException(message, BUSINESS_HANDLER_ERROR.ordinal());
        }
        return checker;
    }

    public static String retWithCheckByNotNull(String checker, String message) {
        if (null == checker || checker.isEmpty()) {
            throw new MetaSyncClientException(message, BUSINESS_HANDLER_ERROR.ordinal());
        }

        return checker;
    }

    public static Object retWithCheckByNotNull(Object checker, String message) {
        if (null == checker) {
            throw new MetaSyncClientException(message, BUSINESS_HANDLER_ERROR.ordinal());
        }

        return checker;
    }


}
