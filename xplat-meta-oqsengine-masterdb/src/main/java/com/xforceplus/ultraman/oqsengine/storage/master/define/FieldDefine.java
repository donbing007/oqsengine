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

    /**
     * 数据标识.
     */
    public static final String ID = "id";

    /**
     * 实例在0层的类型.
     */
    public static final String ENTITYCLASS_LEVEL_0 = "entityclassl0";

    /**
     * 实例在1层的类型.
     */
    public static final String ENTITYCLASS_LEVEL_1 = "entityclassl1";

    /**
     * 实例在2层的类型.
     */
    public static final String ENTITYCLASS_LEVEL_2 = "entityclassl2";
    /**
     * 实例在3层的类型.
     */
    public static final String ENTITYCLASS_LEVEL_3 = "entityclassl3";
    /**
     * 实例在4层的类型.
     */
    public static final String ENTITYCLASS_LEVEL_4 = "entityclassl4";

    /**
     * 实例产生时的元数据版本.
     */
    public static final String ENTITYCLASS_VERSION = "entityclassver";

    /**
     * 无数据类型各层字段定义.
     */
    public static final String[] ENTITYCLASS_LEVEL_LIST = {
        ENTITYCLASS_LEVEL_0,
        ENTITYCLASS_LEVEL_1,
        ENTITYCLASS_LEVEL_2,
        ENTITYCLASS_LEVEL_3,
        ENTITYCLASS_LEVEL_4,
    };

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
     * 创建时间.
     */
    public static final String CREATE_TIME = "createtime";

    /**
     * 修改时间
     */
    public static final String UPDATE_TIME = "updatetime";

    /**
     * 数据是否被删除.
     */
    public static final String DELETED = "deleted";

    /**
     * 数据属性集合.
     */
    public static final String ATTRIBUTE = "attribute";
    /**
     * 产生数据的大版本号.
     */
    public static final String OQS_MAJOR = "oqsmajor";

    /**
     * 业务主键字段
     */
    public static final String UNIQUE_KEY = "unique_key";
}
