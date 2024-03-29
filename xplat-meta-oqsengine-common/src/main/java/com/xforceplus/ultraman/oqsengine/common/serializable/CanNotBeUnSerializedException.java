package com.xforceplus.ultraman.oqsengine.common.serializable;

/**
 * 无法进行反序列化.
 *
 * @author dongbin
 * @version 1.00 2011-8-9
 * @since 1.5
 */
public class CanNotBeUnSerializedException extends RuntimeException {

    private static final long serialVersionUID = -4928715628900219251L;

    public CanNotBeUnSerializedException(Throwable cause) {
        super(cause);
    }

    public CanNotBeUnSerializedException(String message, Throwable cause) {
        super(message, cause);
    }

    public CanNotBeUnSerializedException(String message) {
        super(message);
    }

    public CanNotBeUnSerializedException() {
    }
}
