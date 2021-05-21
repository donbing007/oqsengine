package com.xforceplus.ultraman.oqsengine.boot.grpc.utils;

/**
 * message decorator.
 */
public class MessageDecorator {

    public static final String ERROR = "500:";

    public static final String NOT_FOUND = "404:";

    public static final String OK = "200:";

    public static final String OTHER = "666:";

    public static String err(String message) {
        return ERROR.concat(message);
    }

    public static String notFound(String message) {
        return NOT_FOUND.concat(message);
    }

    public static String ok(String message) {
        return OK.concat(message);
    }

    public static String other(String message) {
        return OTHER.concat(message);
    }
}
