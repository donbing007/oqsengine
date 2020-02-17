package com.xforceplus.ultraman.oqsengine.storage.master;

import com.xforceplus.ultraman.oqsengine.core.metadata.IEntity;
import com.xforceplus.ultraman.oqsengine.core.metadata.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.Storage;

import java.sql.SQLException;
import java.util.List;
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
    List<IEntity> selectMultiple(Map<IEntityClass, int[]> ids) throws SQLException;

}
