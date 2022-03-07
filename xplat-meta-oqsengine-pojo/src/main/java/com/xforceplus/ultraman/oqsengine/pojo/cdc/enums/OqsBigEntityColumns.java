package com.xforceplus.ultraman.oqsengine.pojo.cdc.enums;

import java.util.Optional;

/**
 * 这里的顺序和主库中字段顺序严格一一对应，可保证每次checkColumn都是O(1)时间复杂度
 * 当顺序不一致时，程序不会出错，做了doublecheck，但代价是消耗了时间复杂度，
 * 即每个column匹配都会遍历OqsBigEntityColumns.values().
 * name : OqsBigEntityColumns
 *
 * @author : xujia 2020/11/4
 * @since : 1.8
 */
public enum OqsBigEntityColumns {
    ID("id"),
    ENTITYCLASSL0("entityclassl0"),
    ENTITYCLASSL1("entityclassl1"),
    ENTITYCLASSL2("entityclassl2"),
    ENTITYCLASSL3("entityclassl3"),
    ENTITYCLASSL4("entityclassl4"),
    ENTITYCLASSVER("entityclassver"),
    TX("tx"),
    COMMITID("commitid"),
    OP("op"),
    VERSION("version"),
    CREATETIME("createtime"),
    UPDATETIME("updatetime"),
    DELETED("deleted"),
    ATTRIBUTE("attribute"),
    OQSMAJOR("oqsmajor"),
    PROFILE("profile");

    private String code;

    OqsBigEntityColumns(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * 获取顺序实例.
     *
     * @param ordinal 顺序.
     * @return 实例.
     */
    public static Optional<OqsBigEntityColumns> getByOrdinal(int ordinal) {
        for (OqsBigEntityColumns entityColumns : OqsBigEntityColumns.values()) {
            if (ordinal == entityColumns.ordinal()) {
                return Optional.of(entityColumns);
            }
        }
        return Optional.empty();
    }
}
