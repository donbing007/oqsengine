package com.xforceplus.ultraman.oqsengine.task;

import com.xforceplus.ultraman.oqsengine.task.queue.TaskQueue;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author dongbin
 * @version 0.1 2021/08/12 17:22
 * @since 1.8
 */
public class DefaultTaskCoordinatorTest {

    private DefaultTaskCoordinator coordinator;
    private MockTaskQueue taskQueue;
    private List<String> finishedTaskIds;

    /**
     * 初始化.
     */
    @BeforeEach
    public void before() throws Exception {
        coordinator = new DefaultTaskCoordinator();
        coordinator.setWorkerNumber(3);

        taskQueue = new MockTaskQueue();
        Field queueField = DefaultTaskCoordinator.class.getDeclaredField("taskQueue");
        queueField.setAccessible(true);
        queueField.set(coordinator, taskQueue);

        finishedTaskIds = new CopyOnWriteArrayList();

        coordinator.init();

    }

    /**
     * 清理.
     */
    @AfterEach
    public void after() throws Exception {
        coordinator.destroy();

        coordinator = null;
        taskQueue = null;

        finishedTaskIds = null;
    }

    @Test
    public void testRegisterRunner() throws Exception {
        MockTaskRunner runner = new MockTaskRunner(finishedTaskIds);
        coordinator.registerRunner(runner);

        Assert.assertEquals(1, coordinator.getRunners().size());
        Assert.assertTrue(coordinator.getRunners().containsKey(MockTaskRunner.class.getSimpleName()));

        coordinator.registerRunner(new MockTaskRunner(finishedTaskIds));
        Assert.assertTrue(coordinator.getRunners().containsValue(runner));
    }

    @Test
    public void testRunTask() throws Exception {
        MockTaskRunner runner = new MockTaskRunner(finishedTaskIds);
        coordinator.registerRunner(runner);

        int size = 1000;
        CountDownLatch latch = new CountDownLatch(size);
        List<String> exceptedTaskIds = new ArrayList<>(size);
        Task newTask;
        for (int i = 0; i < size; i++) {
            newTask = new MockTask(latch);

            exceptedTaskIds.add(newTask.id());

            Assertions.assertTrue(coordinator.addTask(newTask));
        }

        latch.await();

        Assertions.assertEquals(size, finishedTaskIds.size());
        Assertions.assertEquals(exceptedTaskIds.size(), finishedTaskIds.size());
        Collections.sort(exceptedTaskIds);
        Collections.sort(finishedTaskIds);
        for (int i = 0; i < exceptedTaskIds.size(); i++) {
            Assertions.assertEquals(exceptedTaskIds.get(i), this.finishedTaskIds.get(i));
        }
    }

    static class MockTask extends AbstractTask {

        private CountDownLatch latch;

        public MockTask(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public long location() {
            return 1;
        }

        @Override
        public void setLocation(long l) {

        }

        @Override
        public Class runnerType() {
            return MockTaskRunner.class;
        }

        @Override
        public Map<String, String> parameter() {
            return Collections.emptyMap();
        }

        public void finish() {
            latch.countDown();
        }
    }

    static class MockTaskRunner implements TaskRunner {

        private List<String> finishedTaskIds;

        public MockTaskRunner(List<String> finishedTaskIds) {
            this.finishedTaskIds = finishedTaskIds;
        }

        @Override
        public void run(TaskCoordinator coordinator, Task task) {
            finishedTaskIds.add(task.id());

            ((MockTask) task).finish();
        }
    }

    static class MockTaskQueue implements TaskQueue {

        private BlockingQueue<Task> tasks = new LinkedBlockingQueue();
        private ConcurrentMap<String, Task> waitAckTasks = new ConcurrentHashMap<>();

        public List<Task> getTasks() {
            return new ArrayList<>(tasks);
        }

        public Map<String, Task> getWaitAckTasks() {
            return new HashMap<>(waitAckTasks);
        }

        @Override
        public void append(Task task) {
            tasks.add(task);
        }

        @Override
        public Task get() {
            Task task = null;
            try {
                task = tasks.take();
            } catch (InterruptedException e) {
                return null;
            } finally {
                if (task != null) {
                    waitAckTasks.put(task.id(), task);
                }
            }

            return task;
        }

        @Override
        public Task get(long awaitTimeMs) {
            Task task = null;
            try {
                task = tasks.poll(awaitTimeMs, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                return null;
            } finally {
                if (task != null) {
                    waitAckTasks.put(task.id(), task);
                }
            }
            return task;
        }

        @Override
        public void ack(Task task) {
            waitAckTasks.remove(task.id());
        }
    }
}