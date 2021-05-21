package com.xforceplus.ultraman.oqsengine.storage;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
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
     * 根据唯一标识查找相应的实例.
     *
     * @param id          目标实例标识.
     * @param entityClass 目标实例类型标识.
     * @return 目标实例.
     */
    Optional<IEntity> selectOne(long id, IEntityClass entityClass) throws SQLException;

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
     * @return true 存在,false不存在.
     * @throws SQLException 异常.
     */
    boolean exist(long id) throws SQLException;

}
