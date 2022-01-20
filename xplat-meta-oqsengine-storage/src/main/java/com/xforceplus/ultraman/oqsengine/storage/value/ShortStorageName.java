package com.xforceplus.ultraman.oqsengine.storage.value;

import java.io.Serializable;
import java.util.Objects;

/**
 * 储存短名称.
 *
 * @author dongbin
 * @version 0.1 2021/3/5 13:58
 * @since 1.8
 */
public final class ShortStorageName implements Serializable {
    private String head;
    private String prefix;
    private String suffix;
    private String tail;

    /**
     * construct.
     */
    public ShortStorageName(String head, String prefix, String suffix, String tail) {
        this.head = head;
        this.prefix = prefix;
        this.suffix = suffix;
        this.tail = tail;
    }

    public String getPrefix() {
        return prefix;
    }

    /**
     * 获取head.
     */
    public String getHead() {
        return head;
    }

    /**
     * 默认会返回带tails的后缀.
     */
    public String getSuffix() {
        return suffix + tail;
    }

    /**
     * 返回不带tails的suffix.
     */
    public String getOriginSuffix() {
        return suffix;
    }

    /**
     * 返回tails.
     */
    public String getTail() {
        return tail;
    }

    /**
     * 获取无位置信息的tail.
     */
    public String getNoLocationTail() {
        int noNumberIndex = 0;
        for (int i = tail.length() - 1; i >= 0; i--) {
            if (!Character.isDigit(this.tail.charAt(i))) {
                noNumberIndex = i;
                break;
            }
        }

        return tail.substring(0, noNumberIndex + 1);
    }

    @Override
    public String toString() {
        return String.join("", head, prefix, suffix, tail);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ShortStorageName)) {
            return false;
        }
        ShortStorageName that = (ShortStorageName) o;
        return Objects.equals(getPrefix(), that.getPrefix()) && Objects.equals(getSuffix(), that.getSuffix());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPrefix(), getSuffix());
    }
}
