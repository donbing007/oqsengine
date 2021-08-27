package com.xforceplus.ultraman.oqsengine.task;

import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import com.xforceplus.ultraman.oqsengine.task.queue.TaskQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * 聚合初始化任务协调者.
 *
 * @author weikai
 * @version 1.0 2021/8/26 17:55
 * @since 1.8
 */
public class AggregationTaskCoordinator  implements TaskCoordinator, Lifecycle {
    final Logger logger = LoggerFactory.getLogger(AggregationTaskCoordinator.class);

    /**
     * 任务队列.
     */
    private TaskQueue taskQueue;

    /**
     * 任务工作线程池.
     */
    private ExecutorService worker;

    /**
     * runner 池.
     */
    private ConcurrentMap<String, TaskRunner> runners;

    /**
     * 工作线程数量.
     */
    private int workerNumber = 3;

    private volatile boolean running = false;

    public ExecutorService getWorker() {
        return worker;
    }

    public void setWorker(ExecutorService worker) {
        this.worker = worker;
    }

    public int getWorkerNumber() {
        return workerNumber;
    }

    public void setWorkerNumber(int workerNumber) {
        this.workerNumber = workerNumber;
    }

    public TaskQueue getTaskQueue() {
        return taskQueue;
    }

    public void setTaskQueue(TaskQueue taskQueue) {
        this.taskQueue = taskQueue;
    }

    public Map<String, TaskRunner> getRunners() {
        return new HashMap<>(runners);
    }

    public AggregationTaskCoordinator(TaskQueue taskQueue) {
        this.taskQueue = taskQueue;
        running = true;
        runners = new ConcurrentHashMap<>();
    }

    public void start(ExecutorService worker) {
        this.worker = worker;
        for (int i = 0; i < workerNumber; i++) {
            this.worker.submit(new Actuator());
        }
    }


    public void destroy() {
        if (running) {
            running = false;

            runners.clear();
            runners = null;
        }
    }

    @Override
    public boolean registerRunner(TaskRunner runner) {
        checkRunning();

        Class clazz = runner.getClass();

        TaskRunner oldRunner = runners.putIfAbsent(clazz.getSimpleName(), runner);
        return oldRunner == null ? true : false;
    }

    @Override
    public Optional<TaskRunner> getRunner(Class clazz) {
        return Optional.ofNullable(runners.get(clazz.getSimpleName()));
    }

    @Override
    public boolean addTask(Task task) {
        checkRunning();

        try {
            taskQueue.append(task);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return false;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("A new task [{}] has been successfully added.", task.id());
        }
        return true;
    }

    private void checkRunning() {
        if (!running) {
            throw new IllegalStateException("The coordinator has stopped running.");
        }
    }

    /**
     * 执行者.
     */
    private class Actuator implements Runnable {

        /**
         * 无任务的检查间隔毫秒时间.
         */
        private final long checkTimeoutMs = 1000L;

        @Override
        public void run() {
            while (running) {
                Task task = null;
                try {
                    task = taskQueue.get(checkTimeoutMs);
                } catch (Exception ex) {

                    logger.error(ex.getMessage(), ex);

                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(checkTimeoutMs));

                }

                if (task != null) {

                    if (logger.isDebugEnabled()) {
                        logger
                                .debug("Task [{}, {}] is obtained and ready to be executed.", task.id(), task.runnerType());
                    }

                    TaskRunner runner = runners.get(task.runnerType().getSimpleName());
                    if (runner != null) {

                        if (logger.isDebugEnabled()) {
                            logger.debug("Find the Runner that matches task [{},{}].", task.id(), runner.getClass());
                        }

                        try {
//                            runner.run(DefaultTaskCoordinator.this, task);
                        } catch (Exception ex) {

                            logger.error(ex.getMessage(), ex);

                            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(checkTimeoutMs));

                        } finally {
                            try {

                                taskQueue.ack(task);

                            } catch (Exception ex) {
                                logger.error(ex.getMessage(), ex);
                            }
                        }
                    } else {
                        logger.warn("Task {} will be abandoned if the runner {} is not found.",
                                task.id(), task.runnerType());
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("No task found, wait {} milliseconds and try again.", checkTimeoutMs);
                    }
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(checkTimeoutMs));
                }
            }
        }
    }
}
