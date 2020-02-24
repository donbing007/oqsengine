package com.xforceplus.ultraman.oqsengine.core.service;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

/**
 * entity 搜索服务.
 *
 * @author dongbin
 * @version 0.1 2020/2/17 20:41
 * @since 1.8
 */
public interface EntitySearchService {

    /**
     * 根据 id 搜索一个 entity 实例.
     *
     * @param id          目标 id.
     * @param entityClass 目标 entity 类型.
     * @return 目标 entity 实体.
     */
    Optional<IEntity> selectOne(long id, IEntityClass entityClass) throws SQLException;

    /**
     * 条件分页搜索 entity 列表.使用默认排序.
     *
     * @param conditions  搜索条件.
     * @param entityClass 目标 entity 类型.
     * @param page        分页信息.
     * @return 目标 entity 列表.
     */
    Collection<IEntity> selectByConditions(Conditions conditions, IEntityClass entityClass, Page page)
        throws SQLException;

    /**
     * 条件分页搜索 entity 列表.
     *
     * @param conditions  搜索条件.
     * @param entityClass 目标 entity 类型.
     * @param sort        排序信息.
     * @param page        分页信息.
     * @return 目标 entity 列表.
     */
    Collection<IEntity> selectByConditions(Conditions conditions, IEntityClass entityClass, Sort sort, Page page)
        throws SQLException;
}


