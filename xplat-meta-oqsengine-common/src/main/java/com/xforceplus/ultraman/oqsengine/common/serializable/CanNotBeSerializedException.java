package com.xforceplus.ultraman.oqsengine.common.serializable;

/**
 * 无法序列化的异常.
 *
 * @author dongbin
 * @version 1.00 2011-8-9
 * @since 1.5
 */
public class CanNotBeSerializedException extends RuntimeException {

    private static final long serialVersionUID = -6694573405108229478L;

    public CanNotBeSerializedException(Throwable cause) {
        super(cause);
    }

    public CanNotBeSerializedException(String message, Throwable cause) {
        super(message, cause);
    }

    public CanNotBeSerializedException(String message) {
        super(message);
    }

    public CanNotBeSerializedException() {
    }
}
