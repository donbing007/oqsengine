package com.xforceplus.ultraman.oqsengine.storage.index;

import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.Storage;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * 索引储存实现.
 *
 * @author dongbin
 * @version 0.1 2020/2/13 19:44
 * @since 1.8
 */
public interface IndexStorage extends Storage {

    /**
     * 条件搜索 entity 的指针信息.
     * 如果没有某个条件没有指定 entityClass,那么将假定为和返回 entityClass 一致.
     *
     * @param conditions 搜索条件.
     * @param entityClass 搜索目标的 entityClass.
     * @param sort 搜索结果排序.
     * @param page 搜索结果分页信息.
     * @return 搜索结果列表.
     * @throws SQLException
     */
    Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, Sort sort, Page page
            , List<Long> filterIds, Long commitId)
        throws SQLException;

    /**
     * 替换索引中某些属性的值.
     *
     * @param attribute 需要更新的属性值.
     */
    void replaceAttribute(IEntityValue attribute) throws SQLException;

}
