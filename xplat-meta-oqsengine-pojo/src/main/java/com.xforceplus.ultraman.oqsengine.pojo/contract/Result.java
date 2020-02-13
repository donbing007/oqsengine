package com.xforceplus.ultraman.oqsengine.pojo.contract;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

/**
 * service result 返回值.
 *
 * @param <C> 状态表示.
 * @param <V> 值类型.
 * @author wangzheng
 * @version 0.1 2019/11/13 15:15
 * @since 1.8
 */
public abstract class Result<C, V> implements Serializable {

    private C status;
    private String message;

    public Result(C status) {
        this.status = status;
    }

    public Result(C status, String message) {
        this(status, Collections.emptyList(), message);
    }

    public Result(C status, Collection<V> values, String message) {
        this.status = status;
        this.message = message;

        if (this.message == null) {
            this.message = "";
        }

    }

    public C getStatus() {
        return status;
    }

    public void setStatus(C status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Result)) {
            return false;
        }
        Result<?, ?> result = (Result<?, ?>) o;
        return Objects.equals(getStatus(), result.getStatus()) &&
            Objects.equals(getMessage(), result.getMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStatus(), getMessage());
    }

    @Override
    public String toString() {
        return "Result{" +
            "status=" + status +
            ", message='" + message + '\'' +
            '}';
    }
}
