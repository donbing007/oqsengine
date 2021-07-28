package com.xforceplus.ultraman.oqsengine.storage.kv;

import com.xforceplus.ultraman.oqsengine.common.iterator.AbstractDataIterator;
import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.common.serializable.SerializeStrategy;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.executor.DeleteTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.executor.ExistTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.executor.GetTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.executor.ListKeysTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.executor.SaveTaskExecutor;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.annotation.Resource;

/**
 * 基于SQL的KV储存实现.
 *
 * @author dongbin
 * @version 0.1 2021/07/16 10:25
 * @since 1.8
 */
public class SqlKeyValueStorage implements KeyValueStorage<Object> {

    @Resource(name = "storageJDBCTransactionExecutor")
    private TransactionExecutor transactionExecutor;

    @Resource
    private SerializeStrategy serializeStrategy;

    private String tableName;

    private long timeout;

    @Override
    public void save(String key, Object value) throws SQLException {
        if (key == null || key.isEmpty()) {
            throw new SQLException("key cannot use NULL.");
        }

        if (value == null) {
            throw new SQLException("value cannot use NULL.");
        }

        byte[] data = serializeStrategy.serialize(value);
        int size = (int) transactionExecutor.execute((tx, resource, hint) -> {
            return new SaveTaskExecutor(tableName, resource, timeout).execute(new AbstractMap.SimpleEntry<>(key, data));
        });

        final int onlyOne = 1;
        if (size <= onlyOne) {
            throw new SQLException(String.format("Failed to save key-value successfully.[KEY =%s]", key));
        }
    }

    @Override
    public boolean exist(String key) throws SQLException {
        if (key == null || key.isEmpty()) {
            return false;
        }

        return (boolean) transactionExecutor.execute((tx, resource, hint) -> {
            return new ExistTaskExecutor(tableName, resource, timeout).execute(key);
        });
    }

    @Override
    public Optional<Object> get(String key) throws SQLException {
        if (key == null || key.isEmpty()) {
            return Optional.empty();
        }

        return (Optional<Object>) transactionExecutor.execute((tx, resource, hint) -> {
            byte[] data = new GetTaskExecutor(tableName, resource, timeout).execute(key);

            if (data == null) {
                return Optional.empty();
            } else {
                return Optional.of(data);
            }
        });
    }

    @Override
    public void delete(String key) throws SQLException {
        if (key == null || key.isEmpty()) {
            return;
        }

        transactionExecutor.execute((tx, resource, hint) -> {
            return new DeleteTaskExecutor(tableName, resource, timeout).execute(key);
        });

    }

    @Override
    public DataIterator<String> iterator(String startKey) {
        if (startKey == null || startKey.isEmpty()) {
            return KeysIterator.buildEmptyIterator();
        }

        KeysIterator iterator = new KeysIterator();
        iterator.setTableName(tableName);
        iterator.setTimeout(timeout);
        iterator.setTransactionExecutor(transactionExecutor);
        if (!startKey.endsWith("%")) {
            iterator.setStartKey(startKey + "%");
        } else {
            iterator.setStartKey(startKey);
        }

        return iterator;
    }

    /**
     * key 迭代器.
     */
    static class KeysIterator extends AbstractDataIterator<String> {

        private TransactionExecutor transactionExecutor;
        private String tableName;
        private long timeout;
        private String startKey;
        private String lastKey;

        public KeysIterator() {
            this(100);
        }

        /**
         * 初始化.
         *
         * @param buffSize 缓存大小.
         */
        public KeysIterator(int buffSize) {
            super(buffSize);
        }

        public static DataIterator<String> buildEmptyIterator() {
            return new AbstractDataIterator<String>(0) {
                @Override
                protected void load(List<String> buff, int limit) {

                }
            };
        }

        public void setTransactionExecutor(
            TransactionExecutor transactionExecutor) {
            this.transactionExecutor = transactionExecutor;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public void setTimeout(long timeout) {
            this.timeout = timeout;
        }

        public void setStartKey(String startKey) {
            this.startKey = startKey;
        }

        public void setLastKey(String lastKey) {
            this.lastKey = lastKey;
        }

        @Override
        public String next() {
            String key = super.next();
            lastKey = key;
            return key;
        }

        @Override
        protected void load(List<String> buff, int limit) {
            Collection<String> keys;
            try {
                keys = (Collection<String>) transactionExecutor.execute((tx, resource, hint) -> {
                    ListKeysTaskExecutor task = new ListKeysTaskExecutor(tableName, resource, timeout);
                    task.setLastKey(lastKey);
                    task.setBlockSize(limit);
                    return task.execute(this.startKey);
                });
            } catch (SQLException ex) {
                throw new IllegalArgumentException(ex.getMessage(), ex);
            }

            buff.addAll(keys);
        }
    }
}
