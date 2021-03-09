package com.xforceplus.ultraman.oqsengine.pojo.cdc.enums;

import java.util.Optional;

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
    ENTITYCLASS0,
    ENTITYCLASS1,
    ENTITYCLASS2,
    ENTITYCLASS3,
    ENTITYCLASS4,
    ENTITYCLASSVER,
    TX,
    COMMITID,
    OP,
    VERSION,
    CREATETIME,
    UPDATETIME,
    DELETED,
    ATTRIBUTE,
    OQSMAJOR;


    public static Optional<OqsBigEntityColumns> getByOrdinal(int ordinal) {
        for (OqsBigEntityColumns entityColumns : OqsBigEntityColumns.values()) {
            if (ordinal == entityColumns.ordinal()) {
                return Optional.of(entityColumns);
            }
        }
        return Optional.empty();
    }
}
