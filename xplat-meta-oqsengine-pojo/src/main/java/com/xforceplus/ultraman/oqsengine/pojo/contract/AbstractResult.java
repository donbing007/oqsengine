package com.xforceplus.ultraman.oqsengine.pojo.contract;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * service result 返回值.
 *
 * @param <C> 状态表示.
 * @param <V> 值类型.
 * @author wangzheng
 * @version 0.1 2019/11/13 15:15
 * @since 1.8
 */
public abstract class AbstractResult<C, V> implements Serializable {

    private C status;
    private String message;

    public AbstractResult(C status) {
        this.status = status;
    }

    public AbstractResult(C status, String message) {
        this(status, Collections.emptyList(), message);
    }

    /**
     * 实例化.
     */
    public AbstractResult(C status, Collection<V> values, String message) {
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
        if (!(o instanceof AbstractResult)) {
            return false;
        }
        AbstractResult<?, ?> result = (AbstractResult<?, ?>) o;
        return Objects.equals(getStatus(), result.getStatus())
            && Objects.equals(getMessage(), result.getMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStatus(), getMessage());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("AbstractResult{");
        sb.append("status=").append(status);
        sb.append(", message='").append(message).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
