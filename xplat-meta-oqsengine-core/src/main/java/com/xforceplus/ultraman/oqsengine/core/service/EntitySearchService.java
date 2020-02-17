package com.xforceplus.ultraman.oqsengine.core.service;

import com.xforceplus.ultraman.oqsengine.core.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.core.metadata.IEntity;
import com.xforceplus.ultraman.oqsengine.core.metadata.IEntityClass;
import sun.jvm.hotspot.debugger.Page;

import java.util.List;

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
     * @param id 目标 id.
     * @param entityClass 目标 entity 类型.
     * @return 目标 entity 实体.
     */
    IEntity selectOne(long id, IEntityClass entityClass);

    /**
     * 条件分页搜索 entity 列表.
     * @param conditions 搜索条件.
     * @param entityClass 目标 entity 类型.
     * @param page 分页信息.
     * @return 目标 entity 列表.
     */
    List<IEntity> selectByConditions(Conditions conditions, IEntityClass entityClass, Page page);
}
