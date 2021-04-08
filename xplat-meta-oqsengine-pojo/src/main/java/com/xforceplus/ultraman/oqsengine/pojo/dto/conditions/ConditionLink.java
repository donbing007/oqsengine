package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions;

/**
 * 枚举操作符号
 *
 * @author wangzheng
 * @version 1.0 2020/3/26 15:10
 */
public enum ConditionLink {
    UNKNOWN(0),
    AND(1),
    OR(2);

    private int value;

    private ConditionLink(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ConditionLink getInstance(int value) {
        for (ConditionLink link : ConditionLink.values()) {
            if (link.getValue() == value) {
                return link;
            }
        }
        return ConditionLink.UNKNOWN;
    }
}
