package com.xforceplus.ultraman.oqsengine.storage.mock;

import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.mock.BeanInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.common.mock.ReflectionUtils;
import com.xforceplus.ultraman.oqsengine.status.impl.CommitIdStatusServiceImpl;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.DoNothingCacheEventHandler;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Optional;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */
public class StorageInitialization implements BeanInitialization {

    private static volatile StorageInitialization instance = null;

    private CommitIdStatusServiceImpl commitIdStatusService;
    private TransactionManager transactionManager;

    private StorageInitialization() {
    }

    /**
     * 获取单例.
     */
    public static StorageInitialization getInstance() throws Exception {
        if (null == instance) {
            synchronized (StorageInitialization.class) {
                if (null == instance) {
                    instance = new StorageInitialization();
                    instance.init();
                    InitializationHelper.add(instance);
                }
            }
        }
        return instance;
    }

    @Override
    public void init() throws Exception {
        commitIdStatusService = new CommitIdStatusServiceImpl();
        Collection<Field> fields = ReflectionUtils.printAllMembers(commitIdStatusService);
        ReflectionUtils.reflectionFieldValue(fields, "redisClient",
            commitIdStatusService, CommonInitialization.getInstance().getRedisClient());

        commitIdStatusService.init();

        transactionManager = DefaultTransactionManager.Builder.anDefaultTransactionManager()
            .withTxIdGenerator(new IncreasingOrderLongIdGenerator(0))
            .withCommitIdGenerator(new IncreasingOrderLongIdGenerator(0))
            .withCommitIdStatusService(commitIdStatusService)
            .withCacheEventHandler(new DoNothingCacheEventHandler())
            .build();
    }

    @Override
    public void clear() throws Exception {
        Optional<Transaction> t = transactionManager.getCurrent();
        if (t.isPresent()) {
            Transaction tx = t.get();
            if (!tx.isCompleted()) {
                tx.rollback();
            }
        }

        transactionManager.finish();
    }

    @Override
    public void destroy() throws Exception {
        commitIdStatusService.destroy();
        transactionManager = null;
        instance = null;
    }

    public CommitIdStatusServiceImpl getCommitIdStatusService() {
        return commitIdStatusService;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }
}
