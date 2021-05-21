package com.xforceplus.ultraman.oqsengine.storage;

import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import java.sql.SQLException;
import java.util.Collection;

/**
 * 条件查询定义.
 *
 * @author dongbin
 * @version 0.1 2021/04/01 15:46
 * @since 1.8
 */
public interface ConditionsSelectStorage {

    /**
     * 条件搜索数据.
     * 注意,是否对最终结果排序由实现决定.
     * sort只是指定在返回结果中需要返回排序的依据值.
     *
     * @param conditions  搜索条件.
     * @param entityClass 目标类型.
     * @param config      查询配置.
     * @return 搜索结果列表.
     */
    Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, SelectConfig config)
        throws SQLException;


}
