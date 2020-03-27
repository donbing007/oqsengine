package com.xforceplus.ultraman.oqsengine.sdk.vo.dto;

/**
 * response container for type T
 *
 * @param <T>
 */
public class Response<T> {

    public static <T> Response<T> Error(String message) {
        Response rep = new Response<>();
        rep.setMessage(message);
        rep.setCode("-1");
        return rep;
    }

    private String code;

    private String message;

    private T result;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
