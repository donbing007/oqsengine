package com.xforceplus.ultraman.oqsengine.storage.kv.sql;

import com.xforceplus.ultraman.oqsengine.common.iterator.AbstractDataIterator;
import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.common.serializable.SerializeStrategy;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.executor.ResourceTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.hint.ExecutorHint;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.executor.DeleteTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.executor.ExistTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.executor.GetTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.executor.SelectKeysTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.executor.SaveTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Resource;

/**
 * 基于SQL的KV储存实现.
 * 依赖于底层jdbc目标储存的对于字符串的自然排序.
 * 所以如果需要保证顺序,需要key的长度也尽量保证一致否则可能出现顺序错误.如下.
 * <br>
 * l-9223372036854775807-9223372036854775806-0
 * l-9223372036854775807-9223372036854775806-1
 * l-9223372036854775807-9223372036854775806-10
 * l-9223372036854775807-9223372036854775806-100
 * l-9223372036854775807-9223372036854775806-2
 * <br>
 * 预期最后的 l-9223372036854775807-9223372036854775806-2 将在第三个,但是由于自然排序被排在了最后.
 * <br>
 * l-9223372036854775807-9223372036854775806-000
 * l-9223372036854775807-9223372036854775806-001
 * l-9223372036854775807-9223372036854775806-002
 * l-9223372036854775807-9223372036854775806-010
 * l-9223372036854775807-9223372036854775806-100
 * <br>
 * 通过填充将末尾的数字填充为一样的位数来保证顺序.
 *
 * @author dongbin
 * @version 0.1 2021/07/16 10:25
 * @since 1.8
 */
public class SqlKeyValueStorage implements KeyValueStorage<Object> {

    private static final char DISABLE_SYMBOL = '%';

    @Resource(name = "kvStorageJDBCTransactionExecutor")
    private TransactionExecutor transactionExecutor;

    @Resource
    private SerializeStrategy serializeStrategy;

    private String tableName;

    private long timeout;

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public void save(String key, Object value) throws SQLException {
        checkKey(key);

        checkValue(value);

        byte[] data = serializeStrategy.serialize(value);
        long size = (long) transactionExecutor.execute(new ResourceTask() {
            @Override
            public Object run(Transaction transaction, TransactionResource resource, ExecutorHint hint)
                throws SQLException {
                return new SaveTaskExecutor(tableName, resource, timeout).execute(
                    Arrays.asList(new AbstractMap.SimpleEntry<>(key, data)));
            }

            @Override
            public boolean isAttachmentMaster() {
                return true;
            }
        });

        final long onlyOne = 1;
        if (size < onlyOne) {
            throw new SQLException(String.format("Failed to save key-value successfully.[KEY =%s]", key));
        }
    }

    @Override
    public long save(Collection<Map.Entry<String, Object>> kvs) throws SQLException {
        Collection<Map.Entry<String, byte[]>> keyValues = new ArrayList<>(kvs.size());
        for (Map.Entry<String, Object> kv : kvs) {
            checkKey(kv.getKey());
            checkValue(kv.getValue());
            keyValues.add(new AbstractMap.SimpleEntry(kv.getKey(), serializeStrategy.serialize(kv.getValue())));
        }

        return (long) transactionExecutor.execute(new ResourceTask() {
            @Override
            public Object run(Transaction transaction, TransactionResource resource, ExecutorHint hint)
                throws SQLException {
                return new SaveTaskExecutor(tableName, resource, timeout).execute(keyValues);
            }

            @Override
            public boolean isAttachmentMaster() {
                return true;
            }
        });
    }

    @Override
    public boolean exist(String key) throws SQLException {
        checkKey(key);

        return (boolean) transactionExecutor.execute(new ResourceTask() {
            @Override
            public Object run(Transaction transaction, TransactionResource resource, ExecutorHint hint)
                throws SQLException {
                return new ExistTaskExecutor(tableName, resource, timeout).execute(key);
            }

            @Override
            public boolean isAttachmentMaster() {
                return true;
            }
        });
    }

    @Override
    public Optional<Object> get(String key) throws SQLException {
        checkKey(key);

        return (Optional<Object>) transactionExecutor.execute(new ResourceTask() {
            @Override
            public Object run(Transaction transaction, TransactionResource resource, ExecutorHint hint)
                throws SQLException {
                byte[] data = new GetTaskExecutor(tableName, resource, timeout).execute(key);

                if (data == null) {
                    return Optional.empty();
                } else {
                    return Optional.of(serializeStrategy.unserialize(data));
                }
            }

            @Override
            public boolean isAttachmentMaster() {
                return true;
            }
        });
    }

    @Override
    public void delete(String key) throws SQLException {
        checkKey(key);

        transactionExecutor.execute(new ResourceTask() {
            @Override
            public Object run(Transaction transaction, TransactionResource resource, ExecutorHint hint)
                throws SQLException {
                return new DeleteTaskExecutor(tableName, resource, timeout).execute(key);
            }

            @Override
            public boolean isAttachmentMaster() {
                return true;
            }
        });

    }

    @Override
    public DataIterator<String> iterator(String keyPrefix, boolean first) throws SQLException {
        checkKey(keyPrefix);

        KeysIterator iterator = new KeysIterator();
        iterator.setTableName(tableName);
        iterator.setTimeout(timeout);
        iterator.setTransactionExecutor(transactionExecutor);
        iterator.setFirst(first);
        if (!keyPrefix.endsWith("%")) {
            iterator.setKeyPrefix(keyPrefix + "%");
        } else {
            iterator.setKeyPrefix(keyPrefix);
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
        private String keyPrefix;
        private String lastKey;
        private boolean first;

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

        public void setKeyPrefix(String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }

        public void setFirst(boolean first) {
            this.first = first;
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
                keys = (Collection<String>) transactionExecutor.execute(new ResourceTask() {
                    @Override
                    public Object run(Transaction transaction, TransactionResource resource, ExecutorHint hint)
                        throws SQLException {
                        SelectKeysTaskExecutor task = new SelectKeysTaskExecutor(tableName, resource, timeout);
                        task.setLastKey(lastKey);
                        task.setBlockSize(limit);
                        task.setFirst(first);
                        return task.execute(keyPrefix);
                    }

                    @Override
                    public boolean isAttachmentMaster() {
                        return true;
                    }
                });
            } catch (SQLException ex) {
                throw new IllegalArgumentException(ex.getMessage(), ex);
            }

            buff.addAll(keys);
        }
    }

    private void checkKey(String key) throws SQLException {
        if (key == null || key.isEmpty()) {
            throw new SQLException("The key is invalid.");
        }

        for (char c : key.toCharArray()) {
            if (c == DISABLE_SYMBOL) {
                throw new SQLException("The '%' symbol cannot be used in KEY.");
            }
        }
    }

    private void checkValue(Object value) throws SQLException {
        if (value == null) {
            throw new SQLException("Invalid Value.");
        }
    }
}
