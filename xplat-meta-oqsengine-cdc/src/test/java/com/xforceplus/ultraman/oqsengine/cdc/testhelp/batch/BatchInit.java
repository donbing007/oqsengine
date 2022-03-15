package com.xforceplus.ultraman.oqsengine.cdc.testhelp.batch;

import com.google.common.collect.Lists;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.master.mock.MasterDBInitialization;
import com.xforceplus.ultraman.oqsengine.storage.master.mysql.SQLMasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.EntityPackage;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class BatchInit {

    private static ExecutorService asyncThreadPool;

    private static int batchThreads = 10;

    public static void init() {
        if (null == asyncThreadPool) {
            asyncThreadPool = new ThreadPoolExecutor(batchThreads, batchThreads,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(1024 * 1000),
                ExecutorHelper.buildNameThreadFactory("task-threads", false));
        }
    }

    public static void destroy() {
        if (null != asyncThreadPool) {
            ExecutorHelper.shutdownAndAwaitTermination(asyncThreadPool, 3600);
        }

    }

    // 初始化数据
    public static boolean initData(List<IEntity> entities, IEntityClass entityClass, int batchSize) throws Exception {
        SQLMasterStorage storage = MasterDBInitialization.getInstance().getMasterStorage();

        List<List<IEntity>> partitions = Lists.partition(entities, 10000);

        List<List<IEntity>> par = new ArrayList<>();
        for (int i = 0; i < partitions.size(); i++) {
            if (par.size() == batchSize) {
                batch(par, storage, entityClass);
                par.clear();
            } else {
                par.add(partitions.get(i));
            }
        }

        if (par.size() > 0) {
            batch(par, storage, entityClass);
        }

        return true;
    }

    private static void batch(List<List<IEntity>> partitions, SQLMasterStorage storage, IEntityClass entityClass)
        throws SQLException {
        CountDownLatch countDownLatch = new CountDownLatch(partitions.size());
        List<Future<Boolean>> futures = new ArrayList<>(partitions.size());

        for (int i = 0; i < partitions.size(); i++) {
            final List<IEntity> entityList = partitions.get(i);
            futures.add(
                asyncThreadPool.submit(() -> {
                    return build(countDownLatch, storage, entityClass, entityList);
                })
            );
        }

        try {
            if (!countDownLatch.await(300, TimeUnit.SECONDS)) {
                throw new SQLException("batch failed, timeout.");
            }
        } catch (InterruptedException e) {
            throw new SQLException(e.getMessage(), e);
        }

        for (Future<Boolean> f : futures) {
            try {
                if (!f.get()) {
                    throw new SQLException("failed.");
                }
            } catch (Exception e) {
                throw new SQLException(e.getMessage(), e);
            }
        }
    }

    private static boolean build(CountDownLatch countDownLatch, SQLMasterStorage storage,
                                 IEntityClass entityClass, List<IEntity> entityList) {
        try {
            final EntityPackage entityPackage = new EntityPackage();
            entityList.forEach(
                e -> {
                    entityPackage.put(e, entityClass);
                }
            );
            storage.build(entityPackage);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            countDownLatch.countDown();
        }
    }
}
