package com.xforceplus.ultraman.oqsengine.cdc.consumer.enums;

/**
 * 这里的顺序和主库中字段顺序严格一一对应，可保证每次checkColumn都是O(1)时间复杂度
 * 当顺序不一致时，程序不会出错，做了doublecheck，但代价是消耗了时间复杂度，
 * 即每个column匹配都会遍历OqsBigEntityColumns.values().
 * <p>
 * name : OqsBigEntityColumns
 *
 * @author : xujia
 * date : 2020/11/4
 * @since : 1.8
 */
public enum OqsBigEntityColumns {
    ID,
    ENTITY,
    TX,
    COMMITID,
    OP,
    VERSION,
    TIME,
    PREF,
    CREF,
    DELETED,
    ATTRIBUTE,
    META;
}
