package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation;

import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.task.Task;
import com.xforceplus.ultraman.oqsengine.task.TaskCoordinator;
import com.xforceplus.ultraman.oqsengine.task.TaskRunner;
import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聚合任务初始化.
 *
 * @author weikai.
 * @version 1.0 2021/8/27 11:27
 * @since 1.8
 */
public class AggregationTaskRunner implements TaskRunner {

    @Resource
    private KeyValueStorage kv;


    @Override
    public void run(TaskCoordinator coordinator, Task task) {
       // TODO 执行聚合初始化，step 1: 查询被聚合  Step 2: 调用聚合函数 Step 3: 乐观锁更新

    }
}
