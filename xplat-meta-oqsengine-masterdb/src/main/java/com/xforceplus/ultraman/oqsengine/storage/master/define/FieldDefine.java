package com.xforceplus.ultraman.oqsengine.storage.master.define;

/**
 * 字段定义.
 *
 * @author dongbin
 * @version 0.1 2020/3/1 20:22
 * @since 1.8
 */
public class FieldDefine {

    private FieldDefine() {
    }

    public static final String ATTRIBUTE_PREFIX = "F";

    /**
     * 数据标识.
     */
    public static final String ID = "id";

    /**
     * 数据类型标识.
     */
    public static final String ENTITY = "entity";

    /**
     * 事务号.
     */
    public static final String TX = "tx";

    /**
     * 提交号.
     */
    public static final String COMMITID = "commitid";
    /**
     * 操作类型,OperationType 枚举值.
     */
    public static final String OP = "op";

    /**
     * 数据版本号.
     */
    public static final String VERSION = "version";

    /**
     * 数据修改时间.
     */
    public static final String TIME = "time";

    /**
     * 数据继承的父类型数据标识.
     */
    public static final String PREF = "pref";

    /**
     * 数据被继承的子类型数据标识.
     */
    public static final String CREF = "cref";

    /**
     * 数据是否被删除.
     */
    public static final String DELETED = "deleted";

    /**
     * 数据属性集合.
     */
    public static final String ATTRIBUTE = "attribute";
    /**
     * 元信息.用以数据同步.
     */
    public static final String META = "meta";
    /**
     * 产生数据的大版本号.
     */
    public static final String OQS_MAJOR = "oqsmajor";
}
