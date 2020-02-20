package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

/**
 * 家族信息.
 * @author dongbin
 * @version 0.1 2020/2/19 18:02
 * @since 1.8
 */
public interface IEntityFamily {

    /**
     * 继承于的数据结点.
     * @return
     */
    long parent();

    /**
     * 被继承于的数据结点.
     * @return
     */
    long child();
}
