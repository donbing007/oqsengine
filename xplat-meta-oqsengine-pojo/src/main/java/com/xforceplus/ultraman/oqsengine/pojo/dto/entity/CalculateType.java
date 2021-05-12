package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

/**
 * Created by justin.xu on 05/2021.
 */
public enum CalculateType {
    UNKNOWN(0),
    NORMAL(1),
    FORMULA(2),
    AUTO_FILL(3);

    private int type;

    CalculateType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    /**
     * instance.
     */
    public static CalculateType instance(int type) {
        for (CalculateType calculateType : CalculateType.values()) {
            if (calculateType.type == type) {
                return calculateType;
            }
        }

        return UNKNOWN;
    }
}
