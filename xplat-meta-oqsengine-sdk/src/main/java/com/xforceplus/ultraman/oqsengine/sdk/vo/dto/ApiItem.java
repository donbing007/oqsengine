package com.xforceplus.ultraman.oqsengine.sdk.vo.dto;

/**
 * vo api item
 */
public class ApiItem {

    private String url;

    private String method;


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public ApiItem(String url, String method) {
        this.url = url;
        this.method = method;
    }

    @Override
    public String toString() {
        return "ApiItem{" +
                "url='" + url + '\'' +
                ", method='" + method + '\'' +
                '}';
    }
}
