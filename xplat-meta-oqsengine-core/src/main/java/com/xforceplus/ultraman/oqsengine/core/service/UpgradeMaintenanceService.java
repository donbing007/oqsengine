package com.xforceplus.ultraman.oqsengine.core.service;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;

import java.sql.SQLException;

/**
 * 升级维护帮助服务.
 *
 * @author dongbin
 * @version 0.1 2020/12/10 14:04
 * @since 1.8
 */
public interface UpgradeMaintenanceService {

    /**
     * 修复小于当前主版本号的oqs产生的数据.
     * 升级过程是一个异常过程,会马上返回并有一个后台任务在运行.
     *
     * @param classes 目标对象信息列表.
     * @throws SQLException
     */
    public void repairData(IEntityClass... classes) throws SQLException;

    /**
     * 取消正在执行的修复任务.
     */
    public void cancel();

    /**
     * 是否已经完成.
     */
    public boolean isDone();
}
