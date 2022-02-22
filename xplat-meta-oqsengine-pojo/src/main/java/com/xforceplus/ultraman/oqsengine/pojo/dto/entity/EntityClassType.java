package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public enum EntityClassType {
    UNKNOWN(0),
    STATIC(1),
    DYNAMIC(2);

    private int type;
    public int getType() {
        return type;
    }

    EntityClassType(int type) {
        this.type = type;
    }

    /**
     * 根据类型获得实例.
     *
     * @param type 类型.
     * @return 实例.
     */
    public static EntityClassType getInstance(int type) {
        for (EntityClassType t : EntityClassType.values()) {
            if (t.getType() == type) {
                return t;
            }
        }
        return UNKNOWN;
    }

    public static boolean validate(int type) {
        for (EntityClassType t : EntityClassType.values()) {
            if (t.type != EntityClassType.UNKNOWN.type && t.type == type) {
                return true;
            }
        }
        return false;
    }
}
