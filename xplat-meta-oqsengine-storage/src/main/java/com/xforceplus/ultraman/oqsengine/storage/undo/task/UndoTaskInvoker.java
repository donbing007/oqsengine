package com.xforceplus.ultraman.oqsengine.storage.undo.task;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.storage.executor.Task;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;

import java.sql.SQLException;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/24/2020 7:46 PM
 * 功能描述:
 * 修改历史:
 */
public class UndoTaskInvoker {

    Task buildTask;
    Task replaceTask;
    Task deleteTask;

    public void invoke(OpTypeEnum opTypeEnum, IEntity entity) {
        try {
            switch (opTypeEnum) {
                case BUILD: buildTask.run(entity); break;
                case REPLACE: replaceTask.run(entity); break;
                case DELETE: deleteTask.run(entity); break;
                default:
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setTask(Task task) {
        if(task.getClass().isInstance(BuildTask.class)) {
            this.buildTask = task;
        } else if(task.getClass().isInstance(ReplaceTask.class)) {
            this.replaceTask = task;
        } else if(task.getClass().isInstance(DeleteTask.class)) {
            this.deleteTask = task;
        }
    }
}
