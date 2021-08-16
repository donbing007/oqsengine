package com.xforceplus.ultraman.oqsengine.task.queue;

import com.xforceplus.ultraman.oqsengine.common.serializable.CanNotBeSerializedException;
import com.xforceplus.ultraman.oqsengine.common.serializable.CanNotBeUnSerializedException;
import com.xforceplus.ultraman.oqsengine.common.serializable.SerializeStrategy;
import com.xforceplus.ultraman.oqsengine.lock.LocalResourceLocker;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.kv.KeyIterator;
import com.xforceplus.ultraman.oqsengine.task.AbstractTask;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author weikai
 * @version 1.0 2021/8/13 18:06
 * @since 1.8
 */
class TaskKeyValueQueueTest {
    TaskKeyValueQueue instance;
    MockKv mockKv;
    static final String NAME = "test";
    String keyPrefix;

    @BeforeEach
    void before() throws NoSuchFieldException, IllegalAccessException {
        instance = new TaskKeyValueQueue(NAME, new MockKv());
        Field locker = TaskKeyValueQueue.class.getDeclaredField("locker");
        locker.setAccessible(true);
        locker.set(instance, new LocalResourceLocker());

        Field serializeStrategy = TaskKeyValueQueue.class.getDeclaredField("serializeStrategy");
        serializeStrategy.setAccessible(true);
        serializeStrategy.set(instance, new mockSerializeStrategy());

        Field elementKeyPrefix = TaskKeyValueQueue.class.getDeclaredField("elementKeyPrefix");
        elementKeyPrefix.setAccessible(true);
        keyPrefix = elementKeyPrefix.get(instance).toString() + "-0";

        instance.append(new MockTask());
        Field kv = TaskKeyValueQueue.class.getDeclaredField("kv");
        kv.setAccessible(true);
        mockKv = (MockKv) kv.get(instance);
    }

    @Test
    void append() {
        boolean flag = false;
        for (int i = 0; i < mockKv.getTasks().size(); i++) {
            if (mockKv.getTasks().get(i).equals(keyPrefix)) {
                flag = true;
                break;
            }
        }
        Assertions.assertEquals(flag, true);
    }

    @Test
    void get() {
        instance.get();
        Assertions.assertEquals(mockKv.getTasks().size(), 0);
    }

    @Test
    void testGet() {
        instance.get(1000L);
        Assertions.assertEquals(mockKv.getTasks().size(), 0);
    }

    @Test
    void ack() {
        instance.ack(new MockTask());
        Assertions.assertEquals(mockKv.getTasks().size(), 0);
    }

    static class MockTask extends AbstractTask {
        @Override
        public long location() {
            return 1;
        }

        @Override
        public void setLocation(long l) {

        }

        @Override
        public Class runnerType() {
            return null;
        }

        @Override
        public Map<String, String> parameter() {
            return Collections.emptyMap();
        }
    }

    static class mockSerializeStrategy implements SerializeStrategy {

        @Override
        public byte[] serialize(Object source) throws CanNotBeSerializedException {
            return new byte[0];
        }

        @Override
        public Object unserialize(byte[] datas) throws CanNotBeUnSerializedException {
            return null;
        }
    }

    static class MockKv implements KeyValueStorage {
        private BlockingQueue<String> tasks = new LinkedBlockingQueue();

        public List<String> getTasks() {
            return new ArrayList<>(tasks);
        }

        @Override
        public void save(String key, byte[] value) {
            tasks.add(key);
        }

        @Override
        public long save(Collection<Map.Entry<String, byte[]>> kvs) {
            return 0;
        }

        @Override
        public boolean add(String key, byte[] value) {
            return false;
        }

        @Override
        public boolean exist(String key) {
            return false;
        }

        @Override
        public Optional<byte[]> get(String key) {
            tasks.clear();
            return Optional.empty();
        }

        @Override
        public Collection<Map.Entry<String, byte[]>> get(String[] keys) {
            return null;
        }

        @Override
        public void delete(String key) {
            tasks.clear();
        }

        @Override
        public void delete(String[] keys) {

        }

        @Override
        public KeyIterator iterator(String keyPrefix) {
            return KeyValueStorage.super.iterator(keyPrefix);
        }

        @Override
        public KeyIterator iterator(String keyPrefix, boolean asc) {
            return null;
        }


    }


}