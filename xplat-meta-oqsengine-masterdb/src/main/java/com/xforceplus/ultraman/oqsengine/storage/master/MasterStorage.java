package com.xforceplus.ultraman.oqsengine.storage.master;

import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.Storage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

/**
 * 主要储存定义.
 *
 * @author dongbin
 * @version 0.1 2020/2/13 11:59
 * @since 1.8
 */
public interface MasterStorage extends Storage {

    /**
     * 迭代某个entityClass的所有实例(包含所有子类).
     * @param entityClass 目标entityClass.
     * @param startTime 开始时间
     * @param endTime 结束时间.
     * @param lastId 上次迭代的最后id.
     * @return 迭代器.
     * @throws SQLException
     */
    DataIterator<OriginalEntity> iterator(IEntityClass entityClass, long startTime, long endTime, long lastId) throws SQLException;


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
     * @param ids 不同类型的不同实例的映射.
     * @param entityClass 对象元信息.
     * @return 多个实例列表.
     */
    Collection<IEntity> selectMultiple(long[] ids, IEntityClass entityClass) throws SQLException;

    /**
     * 条件搜索数据.
     * 注意,是否对最终结果排序由实现决定.
     * sort只是指定在返回结果中需要返回排序的依据值.
     *
     * @param conditions 搜索条件.
     * @param entityClass 目标类型.
     * @param config 查询配置.
     * @return 搜索结果列表.
     */
    Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, SelectConfig config)
        throws SQLException;

    /**
     * 判断指定的实例是否存在.
     * @param id 目标标识.
     * @return true 存在,false不存在.
     * @throws SQLException
     */
    boolean exist(long id) throws SQLException;
}
