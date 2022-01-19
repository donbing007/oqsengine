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

    private String prefix;
    private String suffix;
    private String tails;

    /**
     * construct.
     */
    public ShortStorageName(String prefix, String suffix, String tails) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.tails = tails;
    }

    public String getPrefix() {
        return prefix;
    }

    /**
     * 默认会返回带tails的后缀.
     */
    public String getSuffix() {
        return suffix + tails;
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
    public String getTails() {
        return tails;
    }

    /**
     * 获得无位置信息的后辍.
     * 只有物理储存是多个字段的时候才有效,否则和getSuffix行为一致.
     *
     * @return 无定位信息的字段后辍.
     */
    public String getNoLocationSuffix() {
        int noNumberIndex = 0;
        for (int i = tails.length() - 1; i >= 0; i--) {
            if (!Character.isDigit(this.tails.charAt(i))) {
                noNumberIndex = i;
                break;
            }
        }

        return suffix + tails.substring(0, noNumberIndex + 1);
    }

    /**
     * 获取无位置信息的tails.
     */
    public String getNoLocationTails() {
        int noNumberIndex = 0;
        for (int i = tails.length() - 1; i >= 0; i--) {
            if (!Character.isDigit(this.tails.charAt(i))) {
                noNumberIndex = i;
                break;
            }
        }

        return tails.substring(0, noNumberIndex + 1);
    }

    @Override
    public String toString() {
        return String.join("", prefix, suffix, tails);
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
