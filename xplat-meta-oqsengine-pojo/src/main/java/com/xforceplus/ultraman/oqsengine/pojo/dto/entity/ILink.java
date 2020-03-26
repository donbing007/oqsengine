package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;

import java.util.List;

/**
 * 关联对象连接查询器.
 * @author wangzheng
 * @version 1.0 2020/3/26 15:10
 */
public interface ILink {

    /**
     * 查询关联对象的结果集.
     * @param entityClass 关联的目标对象类信息.
     * @param conditions 查询条件.
     * @param page 查询分页.
     * @return 查询结果列表.
     */
    List<IEntity> select(IEntityClass entityClass, Conditions conditions, Page page);
}
