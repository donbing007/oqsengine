package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.task.AggregationTaskCoordinator;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.task.AggregationTaskRunner;
import com.xforceplus.ultraman.oqsengine.calculation.logic.lookup.task.LookupMaintainingTaskRunner;
import com.xforceplus.ultraman.oqsengine.task.DefaultTaskCoordinator;
import com.xforceplus.ultraman.oqsengine.task.TaskCoordinator;
import com.xforceplus.ultraman.oqsengine.task.TaskRunner;
import com.xforceplus.ultraman.oqsengine.task.queue.TaskKeyValueQueue;
import com.xforceplus.ultraman.oqsengine.task.queue.TaskQueue;
import java.util.List;
import java.util.concurrent.ExecutorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 任务相关配置.
 *
 * @author dongbin
 * @version 0.1 2021/08/13 17:28
 * @since 1.8
 */
@Configuration
public class TaskConfiguration {

    @Bean
    public TaskQueue taskQueue() {
        return new TaskKeyValueQueue("com.xforceplus.ultraman.queue");
    }

    /**
     * 任务协调者.
     */
    @Bean("taskCoordinator")
    public TaskCoordinator taskCoordinator(
        @Value("${task.worker.number:3}") int number,
        ExecutorService taskThreadPool,
        List<TaskRunner> taskRunners) {
        DefaultTaskCoordinator coordinator = new DefaultTaskCoordinator();
        coordinator.setWorker(taskThreadPool);
        coordinator.setWorkerNumber(number);

        for (TaskRunner runner : taskRunners) {
            coordinator.registerRunner(runner);
        }

        return coordinator;
    }

    @Bean
    public TaskRunner lookupMaintainingTaskRunner() {
        return new LookupMaintainingTaskRunner();
    }


    /**
     * 聚合任务协调者,后期会和defaultCoordinator合并.
     *
     * @param number 线程数量.
     * @param taskThreadPool 线程池.
     * @return 聚合任务协调者
     */
    @Bean("aggregationTaskCoordinator")
    public TaskCoordinator aggregationTaskCoordinator(
            @Value("${task.worker.number:3}") int number,
            ExecutorService taskThreadPool) {
        AggregationTaskCoordinator aggregationTaskCoordinator = new AggregationTaskCoordinator();
        aggregationTaskCoordinator.setWorker(taskThreadPool);
        aggregationTaskCoordinator.setWorkerNumber(number);

        aggregationTaskCoordinator.registerRunner(new AggregationTaskRunner());

        return aggregationTaskCoordinator;
    }
}
