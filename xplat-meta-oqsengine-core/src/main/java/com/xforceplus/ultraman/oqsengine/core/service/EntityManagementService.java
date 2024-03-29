package com.xforceplus.ultraman.oqsengine.core.service;

import com.xforceplus.ultraman.oqsengine.calculation.dto.ErrorCalculateInstance;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.OqsResult;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * entity 管理服务.
 *
 * @author dongbin
 * @version 0.1 2020/2/17 20:38
 * @since 1.8
 */
public interface EntityManagementService {

    /**
     * 创建一个新的 entity 实例.
     *
     * @param entity 目标 entity 数据.
     * @return 新对象的标识.
     */
    OqsResult<IEntity> build(IEntity entity) throws SQLException;

    /**
     * 创建多个实体.
     *
     * @param entities 目标实体列表.
     * @return 创建结果.
     */
    OqsResult<IEntity[]> build(IEntity[] entities) throws SQLException;

    /**
     * 替换一个已经存在的 entity 的信息.
     * 注意: 只包含需要替换的属性即可.
     *
     * @param entity 目标 entity.
     */
    OqsResult<Map.Entry<IEntity, IValue[]>> replace(IEntity entity) throws SQLException;

    /**
     * 批量更新.
     * 同replace,每一个实体只需要包含需要更新的属性即可.
     *
     * @param entities 目标实体列表.
     * @return 创建结果.
     */
    OqsResult<Map<IEntity, IValue[]>> replace(IEntity[] entities) throws SQLException;


    /**
     * 重新计算实例.
     *
     * @param entities 实例
     * @param entityClassRef 对象
     * @param fieldCodes 字段集合
     * @return 重算后的结果
     */
    OqsResult<Map<IEntity, IValue[]>> reCalculate(IEntity[] entities, EntityClassRef entityClassRef, List<String> fieldCodes);

    /**
     * 计算字段内存试算.
     *
     * @param ids 实例id列表.
     * @param entityClassRef 对象.
     * @param fieldCodes 计算字段集合.
     * @return dryRun的差异信息
     */
    List<ErrorCalculateInstance> dryRun(List<Long> ids, EntityClassRef entityClassRef, List<String> fieldCodes);


    /**
     * 计算字段内存试算.
     *
     * @param ids 实例id列表.
     * @param entityClassRef 对象.
     * @return dryRun的差异信息
     */
    List<ErrorCalculateInstance> dryRun(List<Long> ids, EntityClassRef entityClassRef);


    /**
     * 删除一个已经存在的 entity.
     *
     * @param entity 目标 entity.
     */
    OqsResult<IEntity> delete(IEntity entity) throws SQLException;

    /**
     * 删除多个已经存在的entity.
     *
     * @param entities 目标 entity 列表.
     * @return 结果.
     */
    OqsResult<IEntity[]> delete(IEntity[] entities) throws SQLException;

    /**
     * 删除一个已经存在的 entity,和delete不同的是这个优先级最高.
     * 即不论数据是何状态都将进行删除.
     * 这是一个危险的操作,如果数据正在被更新那将造成其他操作失败.
     *
     * @param entity 目标entity.
     * @return 操作结果.
     * @throws SQLException 操作异常.
     * @deprecated 已经废弃,直接使用delete.
     */
    @Deprecated
    OqsResult<IEntity> deleteForce(IEntity entity) throws SQLException;

    /**
     * 删除多个已经存在的 entity,和delete不同的是这个优先级最高.
     *
     * @param entities 目标列表.
     * @return 操作结果.
     * @deprecated 已经废弃, 请使用delete.
     */
    @Deprecated
    OqsResult<IEntity[]> deleteForce(IEntity[] entities) throws SQLException;
}
