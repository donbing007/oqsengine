package com.xforceplus.ultraman.oqsengine.meta.common.exception;

import static com.xforceplus.ultraman.oqsengine.meta.common.exception.Code.BUSINESS_HANDLER_ERROR;
import static com.xforceplus.ultraman.oqsengine.meta.common.exception.Code.CONNECTION_ERROR;


/**
 * client exception.
 *
 * @author xujia
 * @since 1.8
 */
public class MetaSyncClientException extends RuntimeException {
    private int code;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public MetaSyncClientException(String message, int code) {
        super(message);
        this.code = code;
    }

    public MetaSyncClientException(String message, boolean isConnectionError) {
        super(message);
        this.code = isConnectionError ? CONNECTION_ERROR.ordinal() : BUSINESS_HANDLER_ERROR.ordinal();
    }
}
