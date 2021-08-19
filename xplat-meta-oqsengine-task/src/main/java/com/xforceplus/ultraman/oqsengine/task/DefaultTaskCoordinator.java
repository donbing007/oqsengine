package com.xforceplus.ultraman.oqsengine.task;

import com.xforceplus.ultraman.oqsengine.task.queue.TaskQueue;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认的任务协调者实现.
 * 对于每一种任务都只允许一个runner.
 * 相同class的Runner只允许注册同一个,之后的将被忽略.
 *
 * @author dongbin
 * @version 0.1 2021/08/12 15:02
 * @since 1.8
 */
public class DefaultTaskCoordinator implements TaskCoordinator {

    final Logger logger = LoggerFactory.getLogger(DefaultTaskCoordinator.class);

    /**
     * 任务队列.
     */
    @Resource
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

    public Map<String, TaskRunner> getRunners() {
        return new HashMap<>(runners);
    }

    @PostConstruct
    public void init() {
        if (running) {
            return;
        }
        if (worker == null) {
            throw new IllegalArgumentException("No execution thread pool is set.");
        }

        runners = new ConcurrentHashMap<>();

        running = true;

        for (int i = 0; i < workerNumber; i++) {
            this.worker.submit(new Actuator());
        }
    }

    @PreDestroy
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
                            runner.run(DefaultTaskCoordinator.this, task);
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
