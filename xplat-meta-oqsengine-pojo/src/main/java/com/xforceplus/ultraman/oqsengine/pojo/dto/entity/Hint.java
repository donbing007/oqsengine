package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * 表示一个提示信息.
 *
 * @author dongbin
 * @version 0.1 2021/12/21 14:09
 * @since 1.8
 */
public class Hint<T> implements Serializable {

    private T target;
    private String msg;

    public Hint(T target, String msg) {
        this.target = target;
        this.msg = msg;
    }

    public T getTarget() {
        return target;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Hint)) {
            return false;
        }
        Hint<?> hint = (Hint<?>) o;
        return Objects.equals(target, hint.target) && Objects.equals(msg, hint.msg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, msg);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Hint{");
        sb.append("target=").append(target);
        sb.append(", msg='").append(msg).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
