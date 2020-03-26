package com.xforceplus.ultraman.oqsengine.storage.undo.task;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.storage.executor.Task;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/25/2020 9:52 AM
 * 功能描述:
 * 修改历史:
 */
public abstract class UndoTask implements Task<IEntity> {
    private Method undoMethod;

    public UndoTask(Method undoMethod) {
        this.undoMethod = undoMethod;
    }

    public void undo(IEntity entity) {
        try {
            undoMethod.invoke(this, entity);
        } catch (Exception e) {
//            throw new SQLException();
        }
    }
}
