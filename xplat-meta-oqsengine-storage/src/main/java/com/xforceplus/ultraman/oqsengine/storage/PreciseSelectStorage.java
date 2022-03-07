package com.xforceplus.ultraman.oqsengine.storage;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

/**
 * 精确实例查询.
 *
 * @author dongbin
 * @version 0.1 2021/04/01 15:44
 * @since 1.8
 */
public interface PreciseSelectStorage {

    /**
     * 查询指定标识的实例.
     *
     * @param id 目标数据id.
     * @return 实例.
     */
    Optional<IEntity> selectOne(long id) throws SQLException;

    /**
     * 查询指定标识指定类型的实例.
     *
     * @param id          目标实例标识.
     * @param entityClass 目标实例类型标识.
     * @return 目标实例.
     */
    Optional<IEntity> selectOne(long id, IEntityClass entityClass) throws SQLException;


    /**
     * 查询原始记录信息.
     * @param id 目标实例标识.
     * @param noDetail 是否需要详细说明.
     * @return
     * @throws SQLException
     */
    default Optional<OriginalEntity> selectOrigin(long id, boolean noDetail) throws SQLException {
        return Optional.empty();
    }

    /**
     * 查找多个实例.查询结果保证和查询顺序一致.
     *
     * @param ids 多个实例标识.
     * @return 实例列表.
     */
    Collection<IEntity> selectMultiple(long[] ids) throws SQLException;

    /**
     * 同时查找多个不同类型的不同实例.返回结果不保证和ids顺序一致.
     *
     * @param ids         不同类型的不同实例的映射.
     * @param entityClass 对象元信息.
     * @return 多个实例列表.
     */
    Collection<IEntity> selectMultiple(long[] ids, IEntityClass entityClass) throws SQLException;

    /**
     * 判断指定的实例是否存在.
     *
     * @param id 目标标识.
     * @return 正整数表示存在且当前的版本号, 负数表示不存在.
     * @throws SQLException 异常.
     */
    int exist(long id) throws SQLException;

}
