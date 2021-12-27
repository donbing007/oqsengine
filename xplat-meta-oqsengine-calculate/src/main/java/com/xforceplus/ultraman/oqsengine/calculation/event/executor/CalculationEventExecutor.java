package com.xforceplus.ultraman.oqsengine.calculation.event.executor;

import com.xforceplus.ultraman.oqsengine.calculation.dto.CalculationEvent;
import com.xforceplus.ultraman.oqsengine.calculation.event.helper.CalculationEventResource;
import com.xforceplus.ultraman.oqsengine.calculation.factory.CachedEntityClass;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by justin.xu on 12/2021.
 *
 * @since 1.8
 */
public interface CalculationEventExecutor {

    /**
     * 对一种计算类型的所有影响字段进行更新、这个操作传入的是整个AppId下的所有该类型的字段.
     *
     * @param calculationEvent 计算事件.
     * @param cachedEntityClass 该次计算的entityClass缓存.
     * @param resource 计算所依赖的资源,通常是系统启动时所注入的resource.
     * @return true、false.
     * @throws SQLException 异常.
     */
    boolean execute(CalculationEvent calculationEvent, CachedEntityClass cachedEntityClass, CalculationEventResource resource) throws SQLException;

    /**
     * 对于delete操作的统一处理.
     *
     * @param deletes 删除列表.
     * @param resource 计算所依赖的资源,通常是系统启动时所注入的resource.
     */
    void deleteTypeExecute(Map<Long, List<Long>> deletes, CalculationEventResource resource);
}
