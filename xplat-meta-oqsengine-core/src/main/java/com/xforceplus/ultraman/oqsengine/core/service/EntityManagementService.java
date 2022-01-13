package com.xforceplus.ultraman.oqsengine.core.service;

import com.xforceplus.ultraman.oqsengine.core.service.pojo.OqsResult;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import java.sql.SQLException;

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
    OqsResult build(IEntity entity) throws SQLException;

    /**
     * 创建多个实体.
     *
     * @param entities 目标实体列表.
     * @return 创建结果.
     */
    default OqsResult build(IEntity[] entities) throws SQLException {
        OqsResult result = OqsResult.success();
        for (IEntity entity : entities) {
            result = build(entity);

            if (ResultStatus.SUCCESS != result.getResultStatus()
                && ResultStatus.HALF_SUCCESS != result.getResultStatus()) {
                return result;
            }
        }

        return result;
    }

    /**
     * 替换一个已经存在的 entity 的信息.
     * 注意: 只包含需要替换的属性即可.
     *
     * @param entity 目标 entity.
     */
    OqsResult replace(IEntity entity) throws SQLException;

    /**
     * 批量更新.
     *
     * @param entities 目标实体列表.
     * @return 创建结果.
     */
    default OqsResult replace(IEntity[] entities) throws SQLException {
        OqsResult result = OqsResult.success();
        for (IEntity entity : entities) {
            result = replace(entity);

            if (ResultStatus.SUCCESS != result.getResultStatus()
                && ResultStatus.HALF_SUCCESS != result.getResultStatus()) {
                return result;
            }
        }

        return result;
    }

    /**
     * 删除一个已经存在的 entity.
     *
     * @param entity 目标 entity.
     */
    OqsResult delete(IEntity entity) throws SQLException;

    /**
     * 删除多个已经存在的entity.
     *
     * @param entities 目标 entity 列表.
     * @return 结果.
     */
    default OqsResult delete(IEntity[] entities) throws SQLException {
        OqsResult result = OqsResult.success();
        for (IEntity entity : entities) {
            result = delete(entity);

            if (ResultStatus.SUCCESS != result.getResultStatus()
                && ResultStatus.HALF_SUCCESS != result.getResultStatus()) {
                return result;
            }
        }

        return result;
    }

    /**
     * 删除一个已经存在的 entity,和delete不同的是这个优先级最高.
     * 即不论数据是何状态都将进行删除.
     * 这是一个危险的操作,如果数据正在被更新那将造成其他操作失败.
     *
     * @param entity 目标entity.
     * @return 操作结果.
     * @throws SQLException 操作异常.
     */
    OqsResult deleteForce(IEntity entity) throws SQLException;

    /**
     * 删除多个已经存在的 entity,和delete不同的是这个优先级最高.
     *
     * @param entities 目标列表.
     * @return 操作结果.
     */
    default OqsResult deleteForce(IEntity[] entities) throws SQLException {
        OqsResult result = OqsResult.success();
        for (IEntity entity : entities) {
            result = deleteForce(entity);

            if (ResultStatus.SUCCESS != result.getResultStatus()
                && ResultStatus.HALF_SUCCESS != result.getResultStatus()) {
                return result;
            }
        }

        return result;
    }
}
