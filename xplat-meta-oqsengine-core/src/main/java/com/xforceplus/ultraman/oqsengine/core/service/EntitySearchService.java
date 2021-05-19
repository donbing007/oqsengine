package com.xforceplus.ultraman.oqsengine.core.service;

import com.xforceplus.ultraman.oqsengine.core.service.pojo.ServiceSearchConfig;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.ServiceSelectConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
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
     * @param id             目标 id.
     * @param entityClassRef 目标 entity 类型指针.
     * @return 目标 entity 实体.
     */
    Optional<IEntity> selectOne(long id, EntityClassRef entityClassRef) throws SQLException;

    /**
     * 根据多个 id 搜索 entity 实例.
     *
     * @param ids            目标 id 列表.
     * @param entityClassRef 目标 entity 类型.
     * @return 实体列表.
     */
    Collection<IEntity> selectMultiple(long[] ids, EntityClassRef entityClassRef) throws SQLException;

    /**
     * 条件分页搜索 entity 列表.使用默认排序.
     *
     * @param conditions     搜索条件.
     * @param entityClassRef 目标 entity 类型.
     * @param page           分页信息.
     * @return 目标 entity 列表.
     * @deprecated 请使用带有searchconfig的方法.
     */
    Collection<IEntity> selectByConditions(Conditions conditions, EntityClassRef entityClassRef, Page page)
        throws SQLException;

    /**
     * 条件分页搜索 entity 列表.
     *
     * @param conditions     搜索条件.
     * @param entityClassRef 目标 entity 类型.
     * @param sort           排序信息.
     * @param page           分页信息.
     * @return 目标 entity 列表.
     * @deprecated 请使用带有searchconfig的方法.
     */
    @Deprecated
    Collection<IEntity> selectByConditions(Conditions conditions, EntityClassRef entityClassRef, Sort sort, Page page)
        throws SQLException;

    /**
     * 条件查询,可以指定一个搜索配置项进行搜索调整.
     *
     * @param conditions     查询主条件组.
     * @param entityClassRef 查询目标entity元信息.
     * @param config         查询配置.
     * @return 查询结果列表.
     */
    Collection<IEntity> selectByConditions(Conditions conditions, EntityClassRef entityClassRef, ServiceSelectConfig config)
        throws SQLException;

    /**
     * 搜索实例.
     *
     * @param config 搜索配置.
     * @return 搜索结果.
     */
    Collection<IEntity> search(ServiceSearchConfig config) throws SQLException;
}


