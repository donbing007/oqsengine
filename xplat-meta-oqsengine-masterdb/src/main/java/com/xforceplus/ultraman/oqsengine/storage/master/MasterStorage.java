package com.xforceplus.ultraman.oqsengine.storage.master;

import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.storage.Storage;
import com.xforceplus.ultraman.oqsengine.storage.master.iterator.QueryIterator;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * 主要储存定义.
 *
 * @author dongbin
 * @version 0.1 2020/2/13 11:59
 * @since 1.8
 */
public interface MasterStorage extends Storage {

    /**
     * 产生一个批量搜索迭代器
     *
     * @param entityClass 目标实例类型.
     * @param startTimeMs 开始时间
     * @param endTimeMs   结束时间
     * @param threadPool  外部传入执行线程池，如为null表示采用默认的线程池
     * @param queryTimeout 超时时间
     * @param pageSize 页大小
     * @return 目标实例.
     */
    QueryIterator newIterator(IEntityClass entityClass,
                              long startTimeMs,
                              long endTimeMs,
                              ExecutorService threadPool,
                              int queryTimeout,
                              int pageSize) throws SQLException;


    /**
     * 根据唯一标识查找相应的实例.
     *
     * @param id          目标实例标识.
     * @param entityClass 目标实例类型.
     * @return 目标实例.
     */
    Optional<IEntity> selectOne(long id, IEntityClass entityClass) throws SQLException;

    /**
     * 根据唯一标识查找相应的实例.
     *
     * @param id 目标实例标识.
     * @return 目标实例.
     */
    Optional<IEntityValue> selectEntityValue(long id) throws SQLException;

    /**
     * 同时查找多个不同类型的不同实例.
     *
     * @param ids 不同类型的不同实例的映射.
     * @return 多个实例列表.
     */
    Collection<IEntity> selectMultiple(Map<Long, IEntityClass> ids) throws SQLException;

    /**
     * 条件搜索数据.
     * 注意,是否对最终结果排序由实现决定.
     * sort只是指定在返回结果中需要返回排序的依据值.
     *
     * @param commitid   所有查询必须大于等于此提交号.
     * @param conditions 搜索条件.
     * @return 搜索结果列表.
     */
    Collection<EntityRef> select(long commitid, Conditions conditions, IEntityClass entityClass, Sort sort)
        throws SQLException;

    /**
     * 同步两个 id 表示的信息.实际需要同步的信息由实现定义.
     *
     * @param id    源数据标识.
     * @param child 目标数据标识.
     * @return 同步的数量.
     */
    int synchronize(long id, long child) throws SQLException;

    /**
     * entity信息同步,如果当前entity含有子类那么此方法需要保证如下.
     * 1. 除了包含自己的属性还需要包含父类属性.
     * 2. 事务,提交号必须和父类保持一致.
     *
     * @param entity
     * @return
     * @throws SQLException
     */
    int synchronizeToChild(IEntity entity) throws SQLException;

    /**
     *  获取当前主库中最大的CommitId + 1
     */
    Optional<Long> maxCommitId() throws SQLException;
}
