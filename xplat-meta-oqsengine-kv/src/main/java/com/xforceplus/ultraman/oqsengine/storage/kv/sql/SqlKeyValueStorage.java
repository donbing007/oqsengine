package com.xforceplus.ultraman.oqsengine.storage.kv.sql;

import com.xforceplus.ultraman.oqsengine.common.iterator.AbstractDataIterator;
import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.lock.ResourceLocker;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.executor.ResourceTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.hint.ExecutorHint;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.executor.DeleteTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.executor.ExistTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.executor.GetTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.executor.GetsTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.executor.IncrTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.executor.SaveTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.executor.SelectKeysTaskExecutor;
import com.xforceplus.ultraman.oqsengine.storage.pojo.kv.KeyIterator;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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
public class SqlKeyValueStorage implements KeyValueStorage {

    private static final byte[] EMPTY_VALUES = new byte[0];
    private static final char DISABLE_SYMBOL = '%';
    private static final String NUMBER_KEY_PREIFIX = "@n";

    @Resource(name = "kvStorageJDBCTransactionExecutor")
    private TransactionExecutor transactionExecutor;

    @Resource
    private ResourceLocker locker;

    private String tableName;

    private long timeout;

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public void save(String key, byte[] value) {
        checkKey(key);

        long size = 0;
        try {
            size = (long) transactionExecutor.execute(new ResourceTask() {
                @Override
                public Object run(Transaction transaction, TransactionResource resource, ExecutorHint hint)
                    throws Exception {
                    return new SaveTaskExecutor(tableName, resource, timeout, false, false).execute(
                        Arrays.asList(new AbstractMap.SimpleEntry<>(key, value == null ? EMPTY_VALUES : value)));
                }

                @Override
                public boolean isAttachmentMaster() {
                    return true;
                }
            });
        } catch (SQLException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }

        final long onlyOne = 1;
        if (size < onlyOne) {
            throw new RuntimeException(String.format("Failed to save key-value successfully.[KEY =%s]", key));
        }
    }

    @Override
    public long save(Collection<Map.Entry<String, byte[]>> kvs) {
        Collection<Map.Entry<String, Object>> keyValues = new ArrayList<>(kvs.size());
        for (Map.Entry<String, byte[]> kv : kvs) {
            checkKey(kv.getKey());
            keyValues
                .add(new AbstractMap.SimpleEntry(kv.getKey(), kv.getValue() == null ? EMPTY_VALUES : kv.getValue()));
        }

        try {
            return (long) transactionExecutor.execute(new ResourceTask() {
                @Override
                public Object run(Transaction transaction, TransactionResource resource, ExecutorHint hint)
                    throws Exception {
                    return new SaveTaskExecutor(tableName, resource, timeout, false, false)
                        .execute(keyValues);
                }

                @Override
                public boolean isAttachmentMaster() {
                    return true;
                }
            });
        } catch (SQLException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    @Override
    public boolean add(String key, byte[] value) {
        checkKey(key);

        long size = 0;
        try {
            size = (long) transactionExecutor.execute(new ResourceTask() {
                @Override
                public Object run(Transaction transaction, TransactionResource resource, ExecutorHint hint)
                    throws Exception {
                    return new SaveTaskExecutor(tableName, resource, timeout, true, isNumber(key)).execute(
                        Arrays.asList(new AbstractMap.SimpleEntry<>(key, value == null ? EMPTY_VALUES : value)));
                }

                @Override
                public boolean isAttachmentMaster() {
                    return true;
                }
            });
        } catch (SQLException ex) {
            if (SQLIntegrityConstraintViolationException.class.isInstance(ex.getCause())) {
                return false;
            } else {
                throw new RuntimeException(ex.getMessage(), ex);
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }

        final long onlyOne = 1;
        return size == onlyOne;
    }

    @Override
    public boolean exist(String key) {
        checkKey(key);

        try {
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
        } catch (SQLException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    @Override
    public Optional<byte[]> get(String key) {
        checkKey(key);

        try {
            return (Optional<byte[]>) transactionExecutor.execute(new ResourceTask() {
                @Override
                public Object run(Transaction transaction, TransactionResource resource, ExecutorHint hint)
                    throws SQLException {
                    byte[] data = new GetTaskExecutor(tableName, resource, timeout).execute(key);

                    if (data == null) {
                        return Optional.empty();
                    } else {
                        return Optional.of(data);
                    }
                }

                @Override
                public boolean isAttachmentMaster() {
                    return true;
                }
            });
        } catch (SQLException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    @Override
    public Collection<Map.Entry<String, byte[]>> get(String[] keys) {
        for (String key : keys) {
            checkKey(key);
        }

        Collection<Map.Entry<String, byte[]>> results = null;
        try {
            results = (Collection<Map.Entry<String, byte[]>>) transactionExecutor.execute(new ResourceTask() {
                @Override
                public Object run(Transaction transaction, TransactionResource resource, ExecutorHint hint)
                    throws SQLException {

                    return new GetsTaskExecutor(tableName, resource, timeout).execute(keys);
                }

                @Override
                public boolean isAttachmentMaster() {
                    return true;
                }
            });
        } catch (SQLException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }

        return results.stream().map(r ->
            new AbstractMap.SimpleEntry<>(r.getKey(), r.getValue())
        ).collect(Collectors.toList());
    }

    @Override
    public void delete(String key) {
        delete(new String[] {key});
    }

    @Override
    public void delete(String[] keys) {
        if (keys == null || keys.length == 0) {
            return;
        }

        for (String key : keys) {
            checkKey(key);
        }

        try {
            transactionExecutor.execute(new ResourceTask() {
                @Override
                public Object run(Transaction transaction, TransactionResource resource, ExecutorHint hint)
                    throws SQLException {
                    return new DeleteTaskExecutor(tableName, resource, timeout).execute(keys);
                }

                @Override
                public boolean isAttachmentMaster() {
                    return true;
                }
            });
        } catch (SQLException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    @Override
    public KeyIterator iterator(String keyPrefix, boolean asc) {
        checkKey(keyPrefix);

        SqlKeyIterator iterator = new SqlKeyIterator();
        iterator.setTableName(tableName);
        iterator.setTimeout(timeout);
        iterator.setTransactionExecutor(transactionExecutor);
        iterator.setAsc(asc);
        if (!keyPrefix.endsWith("%")) {
            iterator.setKeyPrefix(keyPrefix + "%");
        } else {
            iterator.setKeyPrefix(keyPrefix);
        }

        return iterator;
    }

    @Override
    public long incr(String key, long step) {
        String useKey = String.format("%s-%s", NUMBER_KEY_PREIFIX, key);
        long useStep = step < 0 ? 0 : step;

        locker.lock(useKey);
        try {
            return (long) transactionExecutor.execute(new ResourceTask() {
                @Override
                public Object run(Transaction transaction, TransactionResource resource, ExecutorHint hint)
                    throws Exception {
                    IncrTaskExecutor executor = new IncrTaskExecutor(tableName, resource, timeout, useKey);
                    return executor.execute(useStep);
                }

                @Override
                public boolean isAttachmentMaster() {
                    return true;
                }
            });
        } catch (SQLException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        } finally {
            locker.unlock(useKey);
        }
    }

    private boolean isNumber(String key) {
        return key.startsWith(NUMBER_KEY_PREIFIX);
    }

    /**
     * key 迭代器.
     */
    static class SqlKeyIterator extends AbstractDataIterator<String> implements KeyIterator {

        private TransactionExecutor transactionExecutor;
        private String tableName;
        private long timeout;
        private String keyPrefix;
        private String lastKey;
        private boolean asc;

        public SqlKeyIterator() {
            this(100);
        }

        /**
         * 初始化.
         *
         * @param buffSize 缓存大小.
         */
        public SqlKeyIterator(int buffSize) {
            super(buffSize);
        }

        @Override
        public void seek(String key) {
            this.lastKey = key;
        }

        @Override
        public String currentKey() {
            return this.lastKey;
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

        public void setAsc(boolean asc) {
            this.asc = asc;
        }

        @Override
        public String next() {
            String key = super.next();
            lastKey = key;
            return key;
        }

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
                        task.setAsc(asc);
                        return task.execute(keyPrefix);
                    }

                    @Override
                    public boolean isAttachmentMaster() {
                        return true;
                    }
                });
            } catch (SQLException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }

            buff.addAll(keys);
        }
    }

    private void checkKey(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("The key is invalid.");
        }

        for (char c : key.toCharArray()) {
            if (c == DISABLE_SYMBOL) {
                throw new IllegalArgumentException("The '%' symbol cannot be used in KEY.");
            }
        }
    }
}
