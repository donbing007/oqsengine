package com.xforceplus.ultraman.oqsengine.core.service;

/**
 * 事务管理服务.
 * @author dongbin
 * @version 0.1 2020/2/17 20:43
 * @since 1.8
 */
public interface TransactionManagementService {

    /**
     * 开始一个事务.
     * @return 事务 id.
     */
    long begin();

    /**
     * 提交一个事务.
     * @param id 事务 id.
     */
    void commit(long id);

    /**
     * 回滚一个事务.
     * @param id 事务 id.
     */
    void rollback(long id);
}
