package com.xforceplus.ultraman.oqsengine.devops.rebuild.exception;

/**
 * 表示任务已经存在.
 *
 * @author dongbin
 * @version 0.1 2020/8/19 10:39
 * @since 1.8
 */
public class DevopsTaskExistException extends Exception {

    public DevopsTaskExistException() {
    }

    public DevopsTaskExistException(String message) {
        super(message);
    }

    public DevopsTaskExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public DevopsTaskExistException(Throwable cause) {
        super(cause);
    }

    public DevopsTaskExistException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
