package com.xforceplus.ultraman.oqsengine.pojo.dto;


import java.io.Serializable;
import java.util.Objects;

/**
 * 接口对象.
 *
 * @author wangzheng
 * @version 0.1 2020/2/13 15:30
 * @since 1.8
 */
public class Api implements Serializable {

    /**
     * 请求方式
     */
    private String method;
    /**
     * URL地址
     */
    private String url;

    public Api(){

    }

    public Api(String method, String url) {
        this.method = method;
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Api)) return false;
        Api api = (Api) o;
        return getMethod().equals(api.getMethod()) &&
                getUrl().equals(api.getUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMethod(), getUrl());
    }

    @Override
    public String toString() {
        return "Api{" +
                "method='" + method + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}