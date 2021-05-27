package com.xforceplus.ultraman.oqsengine.meta.common.exception;

import static com.xforceplus.ultraman.oqsengine.meta.common.exception.Code.BUSINESS_HANDLER_ERROR;
import static com.xforceplus.ultraman.oqsengine.meta.common.exception.Code.CONNECTION_ERROR;

/**
 * server exception.
 *
 * @author xujia
 * @since 1.8
 */
public class MetaSyncServerException extends RuntimeException {
    private int code;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public MetaSyncServerException(String message, int code) {
        super(message);
        this.code = code;
    }

    public MetaSyncServerException(String message) {
        super(message);
        this.code = -1;
    }

    public MetaSyncServerException(String message, boolean isConnectionError) {
        super(message);
        this.code = isConnectionError ? CONNECTION_ERROR.ordinal() : BUSINESS_HANDLER_ERROR.ordinal();
    }
}
