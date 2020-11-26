package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define;

/**
 * @author dongbin
 * @version 0.1 2020/3/1 20:59
 * @since 1.8
 */
public class FieldDefine {

    private FieldDefine() {}

    /**
     * 标识
     */
    public static final String ID = "id";

    /**
     * entity 标识.
     */
    public static final String ENTITY = "entity";

    /**
     * entity标识,全文字段.
     */
    public static final String ENTITY_F = "entityf";

    /**
     * 父类实体标识.
     */
    public static final String PREF = "pref";

    /**
     * 子类实体标识.
     */
    public static final String CREF = "cref";

    /**
     * transaction id
     */
    public static final String TX = "tx";

    /**
     * transaction commit id
     */
    public static final String COMMIT_ID = "commitid";

    /**
     * 属性TIME.
     */
    public static final String TIME = "time";

    /**
     * 属性 json 储存.
     */
    public static final String JSON_FIELDS = "jsonfields";

    /**
     * 属性全文储存.
     */
    public static final String FULL_FIELDS = "fullfields";

    /**
     * 属性维护ID.
     */
    public static final String MAINTAIN_ID = "maintainid";

    /**
     * 统计行数字段.
     */
    public static final String COUNT = "count";
}
