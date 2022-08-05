package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

/**
 * 聚合类型.
 *
 * @className: AggregationType.
 * @package: com.xforceplus.ultraman.oqsengine.pojo.dto.entity
 * @author: wangzheng
 * @date: 2021/8/23 19:07
 */
public enum AggregationType {
    UNKNOWN(0),
    COUNT(1),
    SUM(2),
    AVG(3),
    MIN(4),
    MAX(5),
    COLLECT(6);

    private int type;
    public int getType() {
        return type;
    }

    AggregationType(int type) {
        this.type = type;
    }

    /**
     * 根据类型获得实例.
     *
     * @param type 类型.
     * @return 实例.
     */
    public static AggregationType getInstance(int type) {
        for (AggregationType t : AggregationType.values()) {
            if (t.getType() == type) {
                return t;
            }
        }
        return UNKNOWN;
    }
}
