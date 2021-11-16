package com.xforceplus.ultraman.oqsengine.task;

import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import java.util.Optional;

/**
 * 任务协调者.
 * 用以管理任务的执行调度.
 *
 * @author dongbin
 * @version 0.1 2021/08/12 14:56
 * @since 1.8
 */
public interface TaskCoordinator extends Lifecycle {

    /**
     * 注册一个任务执行者.
     * 相同类型的Runner将被忽略.
     *
     * @param runner 执行者实例.
     * @return true 成功,false 失败,注册冲突.
     */
    public boolean registerRunner(TaskRunner runner);

    /**
     * 获得指定任务类型的相应Runner.
     *
     * @param clazz 目标任务类型.
     * @return runner 实例.
     */
    public Optional<TaskRunner> getRunner(Class clazz);

    /**
     * 增加任务.
     *
     * @param task 任务.
     * @return true成功, false失败.
     */
    public boolean addTask(Task task);

}
