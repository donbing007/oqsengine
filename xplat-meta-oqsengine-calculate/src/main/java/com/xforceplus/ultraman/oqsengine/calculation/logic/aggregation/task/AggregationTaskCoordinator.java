package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.task;

import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.ParseTree;
import com.xforceplus.ultraman.oqsengine.common.ByteUtil;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import com.xforceplus.ultraman.oqsengine.common.serializable.SerializeStrategy;
import com.xforceplus.ultraman.oqsengine.lock.ResourceLocker;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.task.Task;
import com.xforceplus.ultraman.oqsengine.task.TaskCoordinator;
import com.xforceplus.ultraman.oqsengine.task.TaskRunner;
import com.xforceplus.ultraman.oqsengine.task.queue.TaskKeyValueQueue;
import com.xforceplus.ultraman.oqsengine.task.queue.TaskQueue;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import jodd.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 聚合任务协调者,后期会和defaultCoordinator合并.
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

    public static final String APPENDING = "appending";

    /**
     * 元数据连接顺序标识.
     */
    public static final String APP_INIT_ORDER = "agg-app-Init-Order";

    @Resource
    private KeyValueStorage kv;

    @Resource
    private SerializeStrategy serializeStrategy;

    @Resource
    private ResourceLocker locker;

    @Resource(name = "longContinuousPartialOrderIdGenerator")
    private LongIdGenerator idGenerator;


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


    /**
     * 初始化.
     */
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
        throw new UnsupportedOperationException("aggregationTaskCoordinator not support this method for now");
    }

    /**
     * 添加指定app任务到app队列中.
     *
     * @param prefix appId-version.
     * @param task   任务
     * @return 添加是否成功.
     */
    public boolean addTask(String prefix, Task task) throws Exception {
        TaskQueue queue;
        if (!taskQueueMap.containsKey(prefix)) {
            TaskKeyValueQueue taskKeyValueQueue = new TaskKeyValueQueue(locker, idGenerator, kv, serializeStrategy, 1L, prefix);

            taskKeyValueQueue.init();
            taskQueueMap.put(prefix, taskKeyValueQueue);
        }
        queue = taskQueueMap.get(prefix);
        queue.append(task);
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
     * @param list 要更新的聚合字段信息集合.
     * @return 是否成功.
     */
    public boolean addInitAppInfo(String prefix, List<ParseTree> list) {
        if (list.isEmpty() || list == null) {
            logger.warn(String.format("not support empty task: appVersion is : %s", prefix));
            return false;
        }
        try {
            logger.info(String.format("============================try add %s task to queue, aggTask size is : %s ", prefix, list.size()));
            // 判断是否已有节点添加任务
            if (kv.exist(buildUnInitAppName(prefix))) {
                logger.info(String.format("%s already add addInitAppInfo", prefix));
                return true;
            } else if (kv.incr(buildAppendingAppName(prefix)) == 1) {
                for (int i = 0; i < list.size(); i++) {
                    if (!addTask(prefix, new AggregationTask(prefix, list.get(i)))) {
                        return false;
                    }
                }
            } else {
                logger.warn(String.format("kv incr this prefix: %s not equals 1.  Maybe other oqs node already incr %s", prefix, prefix));
                return false;
            }
        } catch (Exception e) {
            logger.error("===============================添加聚合初始化任务失败======================");
            e.printStackTrace();
            return false;
        }
        kv.incr(buildUnInitAppName(prefix));
        addOrderInfo(prefix);
        logger.info(String.format("===========================add %s task to queue success, aggTask size is : %s ", prefix, list.size()));

        // 添加当前队列任务之后，关闭队列线程池
        TaskKeyValueQueue queue = (TaskKeyValueQueue) taskQueueMap.get(prefix);
        queue.shutDownWorker();
        return true;
    }

    // 每个版本的全量树持久化
    public void addFullTree(String prefix, ArrayList<ParseTree> trees) {
        kv.save(prefix, serializeStrategy.serialize(trees));
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
                logger.info(String.format("add %s to the appOrder ", prefix));
            } else {
                Optional<byte[]> bytes = kv.get(APP_INIT_ORDER);
                if (bytes.isPresent()) {
                    String orderStr = ByteUtil.byteToString(bytes.get(), StandardCharsets.UTF_8);
                    kv.save(APP_INIT_ORDER, ByteUtil.stringToByte(orderStr + prefix + ",", StandardCharsets.UTF_8));
                    logger.info(String.format("add %s to the appOrder %s", prefix, orderStr));
                }
            }
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
        } finally {
            locker.unlock(APP_INIT_ORDER);
        }
    }

    /**
     * 构建正在添加任务完成标识.
     *
     * @param prefix appId-version.
     * @return 唯一标识以avg-appending开头
     */
    public String buildAppendingAppName(String prefix) {
        StringBuilder sb = new StringBuilder();
        return sb.append(APPENDING)
                .append("-")
                .append(prefix).toString();
    }


    /**
     * 获取当前待初始化or正在初始化的appID-version应用.
     *
     * @return appId-version
     */
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
                Optional<byte[]> bytes = kv.get(APP_INIT_ORDER);
                String deleteOrderHead = "";
                if (bytes.isPresent()) {

                    /*
                     * orderStr: appId-version,appId-version,....
                     * 123456-1,123456-2,1234567-1,1234567-2...
                     */
                    String orderStr = ByteUtil.byteToString(bytes.get(), StandardCharsets.UTF_8);
                    deleteOrderHead = StringUtils.substringAfter(orderStr, ",");
                }
                kv.save(APP_INIT_ORDER, ByteUtil.stringToByte(deleteOrderHead, StandardCharsets.UTF_8));
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

    /**
     * 构建未初始化完成标识.
     *
     * @param prefix appId-version.
     * @return 唯一标识以avg-uninit开头
     */
    public String buildUnInitAppName(String prefix) {
        StringBuilder sb = new StringBuilder();
        return sb.append(AVG_UNINIT)
                .append("-")
                .append(prefix).toString();
    }

    private class Actuator implements Runnable {
        /**
         * 无任务的检查间隔毫秒时间.
         */
        private final long checkTimeoutMs = 15000;

        @Override
        public void run() {
            Task task = null;
            TaskQueue queue = null;
            while (running) {
                if (usingApp.size() <= 0) {
                    /*
                     * appId-version
                     */
                    String processingPrefix = getProcessingAppInfo();
                    if (StringUtil.isEmpty(processingPrefix)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("当前无初始化聚合任务");
                        }

                        if (!running) {
                            break;
                        }

                        try {
                            TimeUnit.MILLISECONDS.sleep(checkTimeoutMs);
                        } catch (InterruptedException e) {
                            if (!running) {
                                break;
                            }
                        }

                    } else {
                        // 添加usingApp,多oqs节点中只有添加聚合任务的oqs节点包含当前任务队列，其它oqs节点需在本地进程中新建属性相同的taskQueue后再获取任务执行
                        if (taskQueueMap.containsKey(processingPrefix)) {
                            usingApp.put(processingPrefix, taskQueueMap.get(processingPrefix));
                            if (logger.isDebugEnabled()) {
                                logger.debug(String.format("current usingApp is %s", processingPrefix));
                                logger.debug(String.format("current taskQueue is %s", usingApp.get(processingPrefix).toString()));
                            }
                        } else {
                            TaskKeyValueQueue taskKeyValueQueue = new TaskKeyValueQueue(locker, idGenerator, kv, serializeStrategy, 1L, processingPrefix);
                            try {
                                taskKeyValueQueue.init();
                                taskKeyValueQueue.shutDownWorker();
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                            taskQueueMap.put(processingPrefix, taskKeyValueQueue);
                            logger.warn(String.format("according to the appInfo %s can not find taskQueue ", processingPrefix));
                        }

                    }
                } else {
                    logger.info("======================start agg init");
                    queue = usingApp.entrySet().iterator().next().getValue();
                    try {
                        task = queue.get(1000L);
                    } catch (InterruptedException e) {
                        logger.error(e.getMessage(), e);
                    }

                    if (task == null) {
                        String prefix = usingApp.entrySet().iterator().next().getKey();
                        // 判断当前app-version是否初始化完成
                        if (kv.exist(prefix)) {
                            if (getProcessingAppInfo().equals(prefix)) {
                                removeAppInfoFromOrderList(prefix);
                                logger.warn("appInfo %s init is done ,but not delete from the appOrderInfo, maybe deleted by other oqs node", prefix);
                            }
                        } else {
                            // 判断当前app-version队列是否还有任务
                            if (kv.incr(String.format("%s-%s", prefix, TaskKeyValueQueue.UNUSED), 0) == 0) {
                                kv.incr(prefix);
                                removeAppInfoFromOrderList(prefix);
                                try {
                                    taskQueueMap.get(prefix).destroy();
                                } catch (Exception ex) {
                                    logger.error(ex.getMessage(), ex);
                                }
                                taskQueueMap.remove(prefix);
                                usingApp.remove(prefix);
                            }
                        }
                    } else {

                        if (logger.isDebugEnabled()) {
                            logger.debug("Task [{}, {}] is obtained and ready to be executed.", task.id(), task.runnerType());
                        }

                        TaskRunner runner = runners.get(task.runnerType().getSimpleName());
                        if (runner != null) {

                            if (logger.isDebugEnabled()) {
                                logger.debug("Find the Runner that matches task [{},{}].", task.id(), runner.getClass());
                            }

                            try {
                                int count = 0;
                                while (count < 3) {
                                    try {
                                        runner.run(AggregationTaskCoordinator.this, task);
                                        break;
                                    } catch (Exception ex) {

                                        if (!running) {
                                            break;
                                        }

                                        logger.error(ex.getMessage(), ex);

                                        try {
                                            TimeUnit.MILLISECONDS.sleep(checkTimeoutMs);
                                        } catch (InterruptedException e) {
                                            if (!running) {
                                                break;
                                            }
                                        }

                                        count++;
                                    }
                                }
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
    }
}


