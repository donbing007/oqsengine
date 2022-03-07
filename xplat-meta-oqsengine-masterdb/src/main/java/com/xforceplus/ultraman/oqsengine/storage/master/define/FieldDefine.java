package com.xforceplus.ultraman.oqsengine.storage.master.define;

import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns;

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
    public static final String ID = OqsBigEntityColumns.ID.getCode();

    /**
     * 实例在0层的类型.
     */
    public static final String ENTITYCLASS_LEVEL_0 = OqsBigEntityColumns.ENTITYCLASSL0.getCode();

    /**
     * 实例在1层的类型.
     */
    public static final String ENTITYCLASS_LEVEL_1 = OqsBigEntityColumns.ENTITYCLASSL1.getCode();

    /**
     * 实例在2层的类型.
     */
    public static final String ENTITYCLASS_LEVEL_2 = OqsBigEntityColumns.ENTITYCLASSL2.getCode();
    /**
     * 实例在3层的类型.
     */
    public static final String ENTITYCLASS_LEVEL_3 = OqsBigEntityColumns.ENTITYCLASSL3.getCode();
    /**
     * 实例在4层的类型.
     */
    public static final String ENTITYCLASS_LEVEL_4 = OqsBigEntityColumns.ENTITYCLASSL4.getCode();

    /**
     * 实例产生时的元数据版本.
     */
    public static final String ENTITYCLASS_VERSION = OqsBigEntityColumns.ENTITYCLASSVER.getCode();

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
    public static final String TX = OqsBigEntityColumns.TX.getCode();

    /**
     * 提交号.
     */
    public static final String COMMITID = OqsBigEntityColumns.COMMITID.getCode();

    /**
     * 操作类型,OperationType 枚举值.
     */
    public static final String OP = OqsBigEntityColumns.OP.getCode();

    /**
     * 数据版本号.
     */
    public static final String VERSION = OqsBigEntityColumns.VERSION.getCode();

    /**
     * 创建时间.
     */
    public static final String CREATE_TIME = OqsBigEntityColumns.CREATETIME.getCode();

    /**
     * 修改时间.
     */
    public static final String UPDATE_TIME = OqsBigEntityColumns.UPDATETIME.getCode();

    /**
     * 数据是否被删除.
     */
    public static final String DELETED = OqsBigEntityColumns.DELETED.getCode();

    /**
     * 数据属性集合.
     */
    public static final String ATTRIBUTE = OqsBigEntityColumns.ATTRIBUTE.getCode();
    /**
     * 产生数据的大版本号.
     */
    public static final String OQS_MAJOR = OqsBigEntityColumns.OQSMAJOR.getCode();

    /**
     * 替身JOJO.
     */
    public static final String PROFILE = OqsBigEntityColumns.PROFILE.getCode();

    /**
     * 业务主键字段.
     */
    public static final String UNIQUE_KEY = "unique_key";
}
