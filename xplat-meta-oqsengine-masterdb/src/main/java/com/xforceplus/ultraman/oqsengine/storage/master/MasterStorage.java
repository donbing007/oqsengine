package com.xforceplus.ultraman.oqsengine.storage.master;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.Storage;

import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
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
     * 根据唯一标识查找相应的实例.
     *
     * @param id 目标实例标识.
     * @param entityClass 目标实例类型.
     * @return 目标实例.
     */
    Optional<IEntity> select(long id, IEntityClass entityClass) throws SQLException;

    /**
     * 同时查找多个不同类型的不同实例.
     *
     * @param ids 不同类型的不同实例的映射.
     * @return 多个实例列表.
     */
    Collection<IEntity> selectMultiple(Map<Long, IEntityClass> ids) throws SQLException;

    /**
     * 同步两个 id 表示的信息.实际需要同步的信息由实现定义.
     * @param id 源数据标识.
     * @param child 目标数据标识.
     */
    void synchronize(long id, long child) throws SQLException;
}
