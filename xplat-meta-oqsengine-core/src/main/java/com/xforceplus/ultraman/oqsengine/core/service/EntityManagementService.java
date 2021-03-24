package com.xforceplus.ultraman.oqsengine.core.service;

import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;

import java.sql.SQLException;

/**
 * entity 管理服务.
 * @author dongbin
 * @version 0.1 2020/2/17 20:38
 * @since 1.8
 */
public interface EntityManagementService {

    /**
     * 创建一个新的 entity 实例.
     * @param entity 目标 entity 数据.
     * @return 新对象的标识.
     */
    ResultStatus build(IEntity entity) throws SQLException;

    /**
     * 替换一个已经存在的 entity 的信息.
     * 注意: 可能只包含需要替换的属性即可.
     * @param entity 目标 entity.
     */
    ResultStatus replace(IEntity entity) throws SQLException;

    /**
     * 删除一个已经存在的 entity.
     * @param entity 目标 entity.
     */
    ResultStatus delete(IEntity entity) throws SQLException;

    /**
     * 删除一个已经存在的 entity,和delete不同的是这个优先级最高.
     * 即不论数据是何状态都将进行删除.
     * 这是一个危险的操作,如果数据正在被更新那将造成其他操作失败.
     *
     * @param entity 目标entity.
     * @return 操作结果.
     * @throws SQLException 操作异常.
     */
    ResultStatus deleteForce(IEntity entity) throws SQLException;
}
