package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

import java.sql.SQLException;

/**
 * 定义了元信息的类型.
 *
 * @since 1.8
 */
public enum EntityClassType {
    /**
     * 未知.一般表示发生了某种错误.
     */
    UNKNOWN(0),
    /**
     * 原始类型.
     */
    ORIGINAL(1),
    /**
     * OQS托管类型.
     */
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
    public static EntityClassType getInstance(int type) throws SQLException {
        for (EntityClassType t : EntityClassType.values()) {
            if (t.getType() == type) {
                return t;
            }
        }
        throw new SQLException("un-support entityClass-type.");
    }

    /**
     * 校验.
     */
    public static boolean validate(int type) {
        for (EntityClassType t : EntityClassType.values()) {
            if (t.type == type) {
                return true;
            }
        }
        return false;
    }
}
