package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.UpgradeMaintenanceService;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.iterator.QueryIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * @author dongbin
 * @version 0.1 2020/12/10 14:24
 * @since 1.8
 */
public class UpgradeMaintenanceServiceImpl implements UpgradeMaintenanceService {

    static final Logger logger = LoggerFactory.getLogger(UpgradeMaintenanceServiceImpl.class);

    @Resource(name = "callRebuildThreadPool")
    private ExecutorService worker;

    @Resource
    private MasterStorage masterStorage;

    @Resource
    private EntityManagementService entityManagementService;

    private Map<Long, Future> taskFutures;

    @Override
    public synchronized void repairData(IEntityClass... classes) throws SQLException {
        if (classes.length == 0) {
            return;
        }

        if (taskFutures == null) {
            taskFutures = new HashMap();
        } else {
            taskFutures.clear();
        }

        for (IEntityClass c : classes) {
            // 只处理子类.
            if (c.extendEntityClass() != null) {
                taskFutures.put(c.id(), worker.submit(
                    new RepairTask(
                        masterStorage.newIterator(c, 0, Long.MAX_VALUE, worker, 0, 100),
                        entityManagementService, (cid) -> taskFutures.remove(cid)
                    )
                ));
            }
        }
    }

    @Override
    public synchronized void cancel() {
        if (taskFutures != null) {
            taskFutures.values().stream().map(f -> f.cancel(true));
        }
        taskFutures = null;
    }

    @Override
    public synchronized boolean isDone() {
        return taskFutures == null || taskFutures.isEmpty();
    }

    static class RepairTask implements Runnable {

        private QueryIterator dataQueryIterator;
        private EntityManagementService entityManagementService;
        private long dealSize;
        private Consumer<Long> callback;
        private IEntityClass entityClass;

        public RepairTask(
            QueryIterator dataQueryIterator, EntityManagementService entityManagementService, Consumer<Long> callback) {
            this.dataQueryIterator = dataQueryIterator;
            this.entityManagementService = entityManagementService;
            this.callback = callback;
        }

        @Override
        public void run() {
            List<IEntity> entities = null;
            while (dataQueryIterator.hasNext()) {
                try {
                    entities = dataQueryIterator.next();
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                    return;
                }

                for (IEntity entity : entities) {
                    if (entityClass == null) {
                        entityClass = entity.entityClass();
                    }

                    try {
                        entityManagementService.replace(entity);
                        dealSize++;

                        logger.info("Repair schedule: entityClass {}, {}/{}.", entity.entityClass().code(),
                            dealSize, dataQueryIterator.size());

                    } catch (SQLException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }

            callback.accept(entityClass.id());
        }
    }
}
