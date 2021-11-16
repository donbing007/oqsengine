package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define;

/**
 * 字段定义.
 *
 * @author dongbin
 * @version 0.1 2020/3/1 20:59
 * @since 1.8
 */
public class FieldDefine {

    private FieldDefine() {}

    /**
     * 数据标识.
     */
    public static final String ID = "id";
    /**
     * 事务标识.
     */
    public static final String TX = "tx";
    /**
     * 提交号.
     */
    public static final String COMMITID = "commitid";
    /**
     * 产生时间.
     */
    public static final String CREATE_TIME = "createtime";
    /**
     * 最后更新时间.
     */
    public static final String UPDATE_TIME = "updatetime";
    /**
     * 产生的OQS大版本号.
     */
    public static final String OQSMAJOR = "oqsmajor";
    /**
     * 排序过滤属性.
     */
    public static final String ATTRIBUTE = "attr";
    /**
     * 全文条件查找属性.
     */
    public static final String ATTRIBUTEF = "attrf";
    /**
     * 类型全文搜索属性.
     */
    public static final String ENTITYCLASSF = "entityclassf";
    /**
     * 维护id.
     */
    public static final String MAINTAIN_ID = "maintainid";

    /**
     * 排序字段别名前辍.
     */
    public static final String SORT_FIELD_ALIAS_PREFIX = "sort";
}
