package com.xforceplus.ultraman.oqsengine.storage;

/**
 * 实际储存类型定义.
 *
 * @author dongbin
 * @version 0.1 2020/2/18 21:51
 * @since 1.8
 */
public enum StorageType {
    UNKNOWN('U'),
    STRING('S'),
    LONG('L');

    private char type;

    StorageType(char type) {
        this.type = type;
    }

    public char getType() {
        return type;
    }

    public void setType(char type) {
        this.type = type;
    }

    /**
     * 获得实例.
     *
     * @param type 字面量.
     * @return 实例.
     */
    public static StorageType valueOf(char type) {
        switch (type) {
            case 'S':
                return StorageType.STRING;
            case 'L':
                return StorageType.LONG;
            default:
                return null;
        }
    }
}
