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

    public ShortStorageName(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    /**
     * 获得无位置信息的后辍.
     * 只有物理储存是多个字段的时候才有效,否则和getSuffix行为一致.
     *
     * @return 无定位信息的字段后辍.
     */
    public String getNoLocationSuffix() {
        int noNumberIndex = 0;
        for (int i = suffix.length() - 1; i >= 0; i--) {
            if (!Character.isDigit(this.suffix.charAt(i))) {
                noNumberIndex = i;
                break;
            }
        }

        return suffix.substring(0, noNumberIndex + 1);
    }

    @Override
    public String toString() {
        return String.join("", prefix, suffix);
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
