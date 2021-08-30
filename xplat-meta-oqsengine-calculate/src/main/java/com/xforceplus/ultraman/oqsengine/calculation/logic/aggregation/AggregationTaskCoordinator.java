package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation;

import com.xforceplus.ultraman.oqsengine.common.ByteUtil;
import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import com.xforceplus.ultraman.oqsengine.common.serializable.SerializeStrategy;
import com.xforceplus.ultraman.oqsengine.lock.ResourceLocker;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.task.Task;
import com.xforceplus.ultraman.oqsengine.task.TaskCoordinator;
import com.xforceplus.ultraman.oqsengine.task.TaskRunner;
import com.xforceplus.ultraman.oqsengine.task.queue.TaskKeyValueQueue;
import com.xforceplus.ultraman.oqsengine.task.queue.TaskQueue;
import jodd.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * 聚合任务协调者.
 *
 * @author weikai
 * @version 1.0 2021/8/27 11:42
 * @since 1.8
 */
public class AggregationTaskCoordinator implements TaskCoordinator, Lifecycle {

    final Logger logger = LoggerFactory.getLogger(AggregationTaskCoordinator.class);

    /**
     * 所有聚合初始化标识.
     */
    public static final String AVG_UNINIT = "avg-uninit";

    /**
     * 元数据连接顺序标识.
     */
    public static final String APP_INIT_ORDER = "app-Init-Order";

    @Resource
    private KeyValueStorage kv;

    @Resource
    private SerializeStrategy serializeStrategy;

    @Resource
    private ResourceLocker locker;

    /**
     * 每个appId-version维护一个任务队列.
     */
    public static ConcurrentHashMap<String, TaskQueue> taskQueueMap;

    /**
     * 当前正在处理的appId-version队列.
     */
    public static ConcurrentHashMap<String, TaskQueue> usingApp;


    /**
     * 任务工作线程池.
     */
    private ExecutorService worker;

    /**
     * 工作线程数量.
     */
    private int workerNumber = 3;

    /**
     * runner 池.
     */
    private ConcurrentMap<String, TaskRunner> runners;

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

    public ConcurrentHashMap<String, TaskQueue> getTaskQueueMap() {
        return taskQueueMap;
    }

    public void setTaskQueueMap(ConcurrentHashMap<String, TaskQueue> taskQueueMap) {
        this.taskQueueMap = taskQueueMap;
    }

    public Map<String, TaskRunner> getRunners() {
        return new HashMap<>(runners);
    }


    public AggregationTaskCoordinator() {
        runners = new ConcurrentHashMap<>();
        taskQueueMap = new ConcurrentHashMap<>();
        usingApp = new ConcurrentHashMap<>();
    }

    @PostConstruct
    public void init() {
        if (running) {
            return;
        }
        if (worker == null) {
            throw new IllegalArgumentException("No execution thread pool is set.");
        }

        running = true;

        for (int i = 0; i < workerNumber; i++) {
            this.worker.submit(new Actuator());
        }
    }


    @Override
    public boolean addTask(Task task) {
        return false;
    }

    /**
     * 添加指定app任务到app队列中.
     *
     * @param prefix appId-version.
     * @param task   任务
     * @return 添加是否成功.
     */
    public boolean addTask(String prefix, Task task) {
        TaskKeyValueQueue taskKeyValueQueue = new TaskKeyValueQueue(prefix);
        try {
            taskKeyValueQueue.init();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        taskQueueMap.putIfAbsent(prefix, taskKeyValueQueue);
        TaskQueue queue = taskQueueMap.get(prefix);
        try {
            queue.append(task);
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
        }
        return true;

    }


    @PreDestroy
    public void destroy() {
        if (running) {
            running = false;

            runners.clear();
            runners = null;

            taskQueueMap.clear();
            taskQueueMap = null;
        }
    }

    @Override
    public boolean registerRunner(TaskRunner runner) {
        Class clazz = runner.getClass();

        TaskRunner oldRunner = runners.putIfAbsent(clazz.getSimpleName(), runner);
        return oldRunner == null ? true : false;
    }

    @Override
    public Optional<TaskRunner> getRunner(Class clazz) {
        checkRunning();

        return Optional.ofNullable(runners.get(clazz.getSimpleName()));
    }

    private void checkRunning() {
        if (!running) {
            throw new IllegalStateException("The coordinator has stopped running.");
        }
    }

    /**
     * 每个OQS节点只会有一个节点添加对应app的聚合初始化任务.
     *
     * @param prefix appId-version.
     * @return 是否成功.
     */
    private boolean addInitAppInfo(String prefix, List<List<IEntityClass>> list) {
        try {
            // 判断是否已有节点添加任务
            if (kv.exist(buildQueueName(prefix))) {
                logger.info(String.format("%s already add addInitAppInfo", prefix));
                return false;
            } else if (kv.incr(buildQueueName(prefix)) == 1) {
                for (int i = 0; i < list.size(); i++) {
                    addTask(prefix, new AggregationTask(prefix, list.get(i)));
                }
            } else {
                logger.error("buildQueueName by prefix not equals 1 for unknown reason.");
                return false;
            }
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        addOrderInfo(prefix);
        return true;
    }

    /**
     * 将当前appId-version加入未聚合初始化列表.
     *
     * @param prefix appId-version
     */
    private void addOrderInfo(String prefix) {
        try {
            locker.lock(APP_INIT_ORDER);
            if (!kv.exist(APP_INIT_ORDER)) {
                kv.save(APP_INIT_ORDER, ByteUtil.stringToByte(prefix + ",", StandardCharsets.UTF_8));
            } else {
                Optional<byte[]> bytes = kv.get(APP_INIT_ORDER);
                if (bytes.isPresent()) {
                    String orderStr = ByteUtil.byteToString(bytes.get(), StandardCharsets.UTF_8);
                    kv.save(APP_INIT_ORDER, ByteUtil.stringToByte(orderStr + prefix + ",", StandardCharsets.UTF_8));
                }
            }
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
        } finally {
            locker.unlock(APP_INIT_ORDER);
        }

    }

    /**
     * 构建未初始化完成标识.
     *
     * @param prefix appId-version.
     * @return 唯一标识以avg-uninit开头
     */
    private String buildQueueName(String prefix) {
        StringBuilder sb = new StringBuilder();
        return sb.append(AVG_UNINIT)
                .append("-")
                .append(prefix).toString();
    }

    private class Actuator implements Runnable {
        /**
         * 无任务的检查间隔毫秒时间.
         */
        private final long checkTimeoutMs = 5000L;

        @Override
        public void run() {
            while (running) {
                Task task = null;

                if (usingApp.size() < 0) {
                    /*
                     * appId-version
                     */
                    String processingPrefix = getProcessingAppInfo();
                    if (StringUtil.isEmpty(processingPrefix)) {
                        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(checkTimeoutMs));
                    } else {
                        // TODO 添加usingApp
                        if (taskQueueMap.contains(processingPrefix)) {
                            usingApp.put(processingPrefix, taskQueueMap.get(processingPrefix));
                        } else {
                            TaskKeyValueQueue taskKeyValueQueue = new TaskKeyValueQueue(processingPrefix);
                            try {
                                taskKeyValueQueue.init();
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                            taskQueueMap.put(processingPrefix, taskKeyValueQueue);
                            logger.warn(String.format("according to the appInfo %s can not find taskQueue ", processingPrefix));
                        }

                    }
                } else {
                    TaskQueue queue = usingApp.entrySet().iterator().next().getValue();
                    try {
                        task = queue.get(1000L);
                    } catch (RuntimeException e) {
                        logger.error(e.getMessage(), e);
                    }

                    if (task == null) {
                        String prefix = usingApp.entrySet().iterator().next().getKey();
                        if (kv.exist(prefix)) {
                            if (getProcessingAppInfo().equals(prefix)) {
                                kv.delete(prefix);
                                logger.warn("appInfo %s init is done ,but not delete from the appOrderInfo", prefix);
                            }
                            taskQueueMap.get(prefix).destroy();
                            taskQueueMap.remove(prefix);
                            usingApp.clear();
                        } else {
                            if (kv.incr(String.format("%s-%s", prefix, TaskKeyValueQueue.UNUSED)) == 0) {
                                kv.incr(prefix);
                                removeAppInfoFromOrderList(prefix);
                            }
                        }
                    } else {

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
                                runner.run(AggregationTaskCoordinator.this, task);
                            } catch (Exception ex) {

                                logger.error(ex.getMessage(), ex);

                                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(checkTimeoutMs));

                            } finally {
                                try {

                                    usingApp.entrySet().iterator().next().getValue().ack(task);

                                } catch (Exception ex) {
                                    logger.error(ex.getMessage(), ex);
                                }
                            }
                        } else {
                            logger.warn("Task {} will be abandoned if the runner {} is not found.",
                                    task.id(), task.runnerType());
                        }
                    }


                }

            }

        }

        private String getProcessingAppInfo() {
            if (kv.exist(APP_INIT_ORDER)) {
                Optional<byte[]> bytes = kv.get(APP_INIT_ORDER);
                if (bytes.isPresent()) {

                    /*
                     * orderStr: appId-version,appId-version,....
                     * 123456-1,123456-2,1234567-1,1234567-2...
                     */
                    String orderStr = ByteUtil.byteToString(bytes.get(), StandardCharsets.UTF_8);
                    String[] split = orderStr.split(",");
                    if (split.length > 0) {
                        return split[0];
                    }
                }
                return null;
            }
            return null;
        }

        private void removeAppInfoFromOrderList(String prefix) {
            try {
                locker.lock(APP_INIT_ORDER);
                if (prefix.equals(getProcessingAppInfo())) {
                    StringBuilder deleteOrderHead = new StringBuilder();
                    Optional<byte[]> bytes = kv.get(APP_INIT_ORDER);
                    if (bytes.isPresent()) {

                        /*
                         * orderStr: appId-version,appId-version,....
                         * 123456-1,123456-2,1234567-1,1234567-2...
                         */
                        String orderStr = ByteUtil.byteToString(bytes.get(), StandardCharsets.UTF_8);
                        String[] split = orderStr.split(",");
                        if (split.length > 1) {
                            for (int i = 1; i < split.length; i++) {
                                deleteOrderHead.append(split[i]);
                            }
                        }
                    }
                    kv.save(APP_INIT_ORDER, ByteUtil.stringToByte(deleteOrderHead.toString(), StandardCharsets.UTF_8));
                    logger.info(String.format("%s app has been completed, and  has removed from the orderList ", prefix));
                } else {
                    logger.warn(prefix + " appInfo not equals the head of the appOrderInfo, maybe other node had delete this appInfo");
                }
            } catch (RuntimeException e) {
                logger.error(e.getMessage(), e);
            } finally {
                locker.unlock(APP_INIT_ORDER);
            }

        }
    }
}


