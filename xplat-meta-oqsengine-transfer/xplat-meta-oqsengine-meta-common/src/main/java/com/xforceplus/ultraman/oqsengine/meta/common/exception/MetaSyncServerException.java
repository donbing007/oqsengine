package com.xforceplus.ultraman.oqsengine.meta.common.exception;

import static com.xforceplus.ultraman.oqsengine.meta.common.exception.Code.BUSINESS_HANDLER_ERROR;
import static com.xforceplus.ultraman.oqsengine.meta.common.exception.Code.CONNECTION_ERROR;

/**
 * desc :
 * name : MetaSyncServerException
 *
 * @author : xujia
 * date : 2021/2/3
 * @since : 1.8
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

    public MetaSyncServerException(String message, boolean isConnectionError) {
        super(message);
        this.code = isConnectionError ? CONNECTION_ERROR.ordinal() : BUSINESS_HANDLER_ERROR.ordinal();
    }
}
