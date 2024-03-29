package com.xforceplus.ultraman.oqsengine.storage.kv.sql.define;

/**
 * 字段定义.
 *
 * @author dongbin
 * @version 0.1 2021/07/16 10:27
 * @since 1.8
 */
public final class FieldDefine {

    private FieldDefine() {
    }

    /**
     * key的哈希.
     */
    public static final String HASH = "h";
    /**
     * 记录key的字段.
     */
    public static final String KEY = "k";

    /**
     * 记录value的字段.
     */
    public static final String VALUE = "v";
}
