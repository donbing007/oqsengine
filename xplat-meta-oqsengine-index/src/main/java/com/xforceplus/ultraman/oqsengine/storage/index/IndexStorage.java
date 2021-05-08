package com.xforceplus.ultraman.oqsengine.storage.index;

import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.ConditionsSelectStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import java.sql.SQLException;
import java.util.Collection;

/**
 * 索引储存实现.
 *
 * @author dongbin
 * @version 0.1 2020/2/13 19:44
 * @since 1.8
 */
public interface IndexStorage extends ConditionsSelectStorage, Lifecycle {

    /**
     * 维护接口,时间范围清理.
     *
     * @param entityClass 目标类型.
     * @param maintainId  搜索目标的 taskId.
     * @param start       开始时间.
     * @param end         结束时间.
     * @return 删除的条件.
     */
    long clean(IEntityClass entityClass, long maintainId, long start, long end) throws SQLException;

    /**
     * 保存原始实体.来源可能是其他的storage实现中的数据.
     *
     * @param originalEntities 原始实体列表.
     */
    void saveOrDeleteOriginalEntities(Collection<OriginalEntity> originalEntities) throws SQLException;

}
