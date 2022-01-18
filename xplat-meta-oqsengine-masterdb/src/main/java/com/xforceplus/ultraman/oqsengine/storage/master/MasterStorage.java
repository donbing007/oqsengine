package com.xforceplus.ultraman.oqsengine.storage.master;

import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import com.xforceplus.ultraman.oqsengine.storage.ConditionsSelectStorage;
import com.xforceplus.ultraman.oqsengine.storage.PreciseSelectStorage;
import com.xforceplus.ultraman.oqsengine.storage.Storage;

/**
 * 主要储存定义.
 *
 * @author dongbin
 * @version 0.1 2020/2/13 11:59
 * @since 1.8
 */
public interface MasterStorage extends Storage, PreciseSelectStorage, ConditionsSelectStorage, Lifecycle {

    /**
     * 进行索引重建.
     *
     * @param entityClassId 业务类别id.
     * @param maintainId 维护id.
     * @param startTime 开始时间.
     * @param endTime 结束时间.
     *
     * @return 影响的记录条数.
     */
    default int rebuild(long entityClassId, long maintainId, long startTime, long endTime) throws Exception {
        return 0;
    }
}
