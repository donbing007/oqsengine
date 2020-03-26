package com.xforceplus.ultraman.oqsengine.storage.undo.task;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.storage.executor.Task;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/24/2020 7:25 PM
 * 功能描述:
 * 修改历史:
 */
public abstract class ReplaceTask implements Task<ReplaceTask> {
    private Task undoTask;

    public ReplaceTask(Task undoTask) {
        this.undoTask = undoTask;
    }

    public Task getUndoTask() {
        return undoTask;
    }
}
