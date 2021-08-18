package com.xforceplus.ultraman.oqsengine.storage.kv.memory;

import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.kv.KeyIterator;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * 基于内存的KV实现,数据不过时行持久化.
 *
 * @author dongbin
 * @version 0.1 2021/08/17 10:52
 * @since 1.8
 */
public class MemoryKeyValueStorage implements KeyValueStorage {

    private static final byte[] EMPTY_VALUE = new byte[0];
    private ConcurrentMap<String, byte[]> data;

    public MemoryKeyValueStorage() {
        this.data = new ConcurrentSkipListMap<>();
    }

    @Override
    public void save(String key, byte[] value) {
        data.put(key, value == null ? EMPTY_VALUE : value);
    }

    @Override
    public long save(Collection<Map.Entry<String, byte[]>> kvs) {
        kvs.stream().forEach(kv -> {
            data.put(kv.getKey(), kv.getValue() == null ? EMPTY_VALUE : kv.getValue());
        });

        return kvs.size();
    }

    @Override
    public boolean add(String key, byte[] value) {
        return data.putIfAbsent(key, value == null ? EMPTY_VALUE : value) == null;
    }

    @Override
    public boolean exist(String key) {
        return data.containsKey(key);
    }

    @Override
    public Optional<byte[]> get(String key) {
        byte[] value = data.get(key);
        if (Arrays.equals(value, EMPTY_VALUE)) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(value);
        }
    }

    @Override
    public Collection<Map.Entry<String, byte[]>> get(String[] keys) {
        Collection<Map.Entry<String, byte[]>> datas = new ArrayList<>(keys.length);
        for (String key : keys) {
            byte[] value = data.get(key);
            if (Arrays.equals(value, EMPTY_VALUE)) {
                value = null;
            }
            if (value != null) {
                datas.add(new AbstractMap.SimpleEntry<>(key, value));
            }
        }
        return datas;
    }

    @Override
    public void delete(String key) {
        data.remove(key);
    }

    @Override
    public void delete(String[] keys) {

        for (String key : keys) {
            data.remove(key);
        }
    }

    @Override
    public KeyIterator iterator(String keyPrefix) {
        return KeyValueStorage.super.iterator(keyPrefix);
    }

    @Override
    public KeyIterator iterator(String keyPrefix, boolean asc) {
        return new MemoryKeyIterator(keyPrefix, asc);
    }

    @Override
    public long incr(String key) {
        return KeyValueStorage.super.incr(key);
    }

    @Override
    public long incr(String key, long step) {
        return 0;
    }

    class MemoryKeyIterator implements KeyIterator {

        private String keyPrefix;
        private List<String> keys;
        private String lastKey;
        private int point = 0;
        private boolean asc;

        public MemoryKeyIterator(String keyPrefix, boolean asc) {
            this.keyPrefix = keyPrefix;
            this.asc = asc;

            this.keys = new ArrayList<>(data.keySet());
            if (this.asc) {

                for (; point < keys.size(); ) {
                    if (keys.get(point).startsWith(keyPrefix)) {
                        break;
                    }
                    point++;
                }

            } else {

                for (this.point = this.keys.size() - 1; this.point >= 0; ) {
                    if (keys.get(point).startsWith(keyPrefix)) {
                        break;
                    }
                    point--;
                }

            }
        }

        @Override
        public void seek(String key) {
            for (int i = 0; i < this.keys.size(); i++) {
                if (this.keys.get(i).equals(key)) {
                    this.point = i;
                    break;
                }
            }

            if (this.asc) {
                this.point++;
            } else {
                this.point--;
            }
        }

        @Override
        public String currentKey() {
            return this.lastKey;
        }

        @Override
        public boolean hasNext() {
            if (keys.isEmpty()) {
                return false;
            }

            if (keys.size() - point == 0) {
                return false;
            } else {
                return this.keys.get(point).startsWith(keyPrefix);
            }
        }

        @Override
        public String next() {
            lastKey = keys.get(point);

            if (this.asc) {
                this.point++;
            } else {
                this.point--;
            }

            return lastKey;
        }
    }
}
