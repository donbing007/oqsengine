package com.xforceplus.ultraman.oqsengine.storage.master;

import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.ConditionsSelectStorage;
import com.xforceplus.ultraman.oqsengine.storage.PreciseSelectStorage;
import com.xforceplus.ultraman.oqsengine.storage.Storage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OqsEngineEntity;
import java.sql.SQLException;

/**
 * 主要储存定义.
 *
 * @author dongbin
 * @version 0.1 2020/2/13 11:59
 * @since 1.8
 */
public interface MasterStorage extends Storage, PreciseSelectStorage, ConditionsSelectStorage, Lifecycle {

    /**
     * 迭代某个entityClass的所有实例(包含所有子类).
     * 进行索引重建.
     *
     * @param entityClass 目标entityClass.
     * @param startTime   开始时间
     * @param endTime     结束时间.
     * @param lastId      上次迭代的最后id.
     * @param useSelfEntityClass  假如传入entityClass为父类时, 是否使用真实的entityClass
     * @return 迭代器.
     * @throws SQLException 发生异常.
     */
    DataIterator<OqsEngineEntity> iterator(IEntityClass entityClass, long startTime, long endTime, long lastId, boolean useSelfEntityClass)
        throws SQLException;

    /**
     * 迭代某个entityClass的所有实例(包含所有子类).
     *
     * @param entityClass 目标entityClass.
     * @param startTime   开始时间
     * @param endTime     结束时间.
     * @param lastId      上次迭代的最后id.
     * @param size        迭代数量.
     * @param useSelfEntityClass  假如传入entityClass为父类时, 是否使用真实的entityClass
     * @return 影响的记录条数.
     * @throws SQLException 发生异常.
     */
    DataIterator<OqsEngineEntity> iterator(IEntityClass entityClass, long startTime, long endTime, long lastId, int size, boolean useSelfEntityClass)
        throws SQLException;
}
