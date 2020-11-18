package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.common.version.VersionHelp;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityFamily;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.status.CDCStatusService;
import com.xforceplus.ultraman.oqsengine.storage.executor.ResourceTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.hint.ExecutorHint;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * entity 管理服务实现.
 *
 * @author dongbin
 * @version 0.1 2020/2/18 14:12
 * @since 1.8
 */
public class EntityManagementServiceImpl implements EntityManagementService {

    final Logger logger = LoggerFactory.getLogger(EntityManagementServiceImpl.class);

    @Resource(name = "snowflakeIdGenerator")
    private LongIdGenerator idGenerator;

    @Resource(name = "serviceTransactionExecutor")
    private TransactionExecutor transactionExecutor;

    @Resource
    private MasterStorage masterStorage;

    @Resource
    private CDCStatusService cdcStatusService;

    /**
     * 可以接受的最大同步时间毫秒.
     */
    private long allowMaxSyncTimeMs = 10000;

    private Counter inserCountTotal = Metrics.counter(MetricsDefine.WRITE_COUNT_TOTAL, "action", "build");
    private Counter replaceCountTotal = Metrics.counter(MetricsDefine.WRITE_COUNT_TOTAL, "action", "replace");
    private Counter deleteCountTotal = Metrics.counter(MetricsDefine.WRITE_COUNT_TOTAL, "action", "delete");
    private Counter failCountTotal = Metrics.counter(MetricsDefine.FAIL_COUNT_TOTAL);

    private ScheduledExecutorService checkCDCStatusWorker;
    private volatile boolean ready = true;

    @PostConstruct
    public void init() {
        checkCDCStatusWorker = Executors.newScheduledThreadPool(1, ExecutorHelper.buildNameThreadFactory("CDC-monitor"));
        checkCDCStatusWorker.scheduleWithFixedDelay(() -> {
            Optional<CDCMetrics> mOp = cdcStatusService.get();
            if (mOp.isPresent()) {
                CDCMetrics metrics = mOp.get();
                CDCAckMetrics ackMetrics = metrics.getCdcAckMetrics();
                CDCStatus cdcStatus = ackMetrics.getCdcConsumerStatus();
                if (CDCStatus.CONNECTED != cdcStatus) {
                    logger.warn(
                        "Detected that the CDC synchronization service has stopped and is currently in a state of {}.",
                        cdcStatus.name());
                    ready = false;
                    return;
                }

                long useTimeMs = ackMetrics.getTotalUseTime();
                if (useTimeMs > allowMaxSyncTimeMs) {
                    ready = false;
                    return;
                }

                ready = true;
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void destroy() {
        ExecutorHelper.shutdownAndAwaitTermination(checkCDCStatusWorker, 3600);
    }


    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"action", "build"})
    @Override
    public IEntity build(IEntity entity) throws SQLException {
        checkReady();
        // 克隆一份,后续的修改不影响入参.
        IEntity entityClone;
        try {
            entityClone = (IEntity) entity.clone();
        } catch (CloneNotSupportedException e) {
            failCountTotal.increment();
            throw new SQLException(e.getMessage(), e);
        }

        try {

            return (IEntity) transactionExecutor.execute(new ResourceTask() {
                @Override
                public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {

                    if (EntityManagementServiceImpl.this.isSub(entityClone)) {
                        // 处理父类
                        long fatherId = idGenerator.next();
                        long childId = idGenerator.next();

                        IEntity fathcerEntity = buildFatherEntity(entityClone, childId);
                        fathcerEntity.resetId(fatherId);

                        IEntity childEntity = buildChildEntity(entityClone, fatherId);
                        childEntity.resetId(childId);

                        // master
                        masterStorage.build(fathcerEntity);
                        masterStorage.build(childEntity); // child

                        entity.resetId(childId);
                        entityClone.resetId(childId);
                        entity.resetFamily(childEntity.family());

                    } else {

                        entity.resetId(idGenerator.next());
                        entityClone.resetId(entity.id());

                        masterStorage.build(entityClone);

                    }

                    return entity;
                }

                @Override
                public DataSource getDataSource() {
                    return null;
                }
            });
        } catch (Exception ex) {

            failCountTotal.increment();
            throw ex;

        } finally {

            inserCountTotal.increment();

        }
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"action", "replace"})
    @Override
    public ResultStatus replace(IEntity entity) throws SQLException {
        checkReady();

        if (!masterStorage.selectOne(entity.id(), entity.entityClass()).isPresent()) {
            failCountTotal.increment();
            throw new SQLException(String.format("An Entity that does not exist cannot be updated (%d).", entity.id()));
        }

        // 克隆一份,后续的修改不影响入参.
        IEntity target;
        try {
            target = (IEntity) entity.clone();
        } catch (CloneNotSupportedException e) {
            replaceCountTotal.increment();
            throw new SQLException(e.getMessage(), e);
        }

        try {
            return (ResultStatus) transactionExecutor.execute(new ResourceTask() {
                @Override
                public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {

                    if (EntityManagementServiceImpl.this.isSub(entity)) {

                        /**
                         * 拆分为父与子.
                         */
                        IEntity fatherEntity = buildFatherEntity(target, target.id());
                        fatherEntity.resetId(entity.family().parent());

                        IEntity childEntity = buildChildEntity(target, target.family().parent());

                        if (isConflict(masterStorage.replace(fatherEntity))) {
                            hint.setRollback(true);
                            return ResultStatus.CONFLICT;
                        }

                        if (isConflict(masterStorage.replace(childEntity))) {
                            hint.setRollback(true);
                            return ResultStatus.CONFLICT;
                        }

                    } else {

                        if (isConflict(masterStorage.replace(target))) {
                            hint.setRollback(true);
                            return ResultStatus.CONFLICT;
                        }

                        // 有子类
                        if (target.family().child() > 0) {
                            // 父子同步
                            if (isConflict(masterStorage.synchronize(target.id(), target.family().child()))) {
                                hint.setRollback(true);
                                return ResultStatus.CONFLICT;
                            }
                        }
                    }

                    return ResultStatus.SUCCESS;
                }

                @Override
                public DataSource getDataSource() {
                    return null;
                }
            });
        } catch (Exception ex) {
            failCountTotal.increment();
            throw ex;
        } finally {
            replaceCountTotal.increment();
        }
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"action", "delete"})
    @Override
    public ResultStatus delete(IEntity entity) throws SQLException {
        checkReady();

        try {
            return (ResultStatus) transactionExecutor.execute(new ResourceTask() {

                @Override
                public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {

                    if (EntityManagementServiceImpl.this.isSub(entity)) {

                        IEntity fatherEntity = buildFatherEntity(entity, entity.id());
                        fatherEntity.resetId(entity.family().parent());

                        IEntity childEntity = buildChildEntity(entity, entity.family().parent());

                        if (isConflict(masterStorage.delete(fatherEntity))) {
                            hint.setRollback(true);
                            return ResultStatus.CONFLICT;
                        }
                        if (isConflict(masterStorage.delete(childEntity))) {
                            hint.setRollback(true);
                            return ResultStatus.CONFLICT;
                        }


                    } else {

                        if (isConflict(masterStorage.delete(entity))) {
                            hint.setRollback(true);
                            return ResultStatus.CONFLICT;
                        }

                    }

                    if (logger.isInfoEnabled()) {
                        logger.info("Entity({}), Class({}), Version({}) was successfully deleted.",
                            entity.id(), entity.entityClass().id(), entity.version());
                    }

                    return ResultStatus.SUCCESS;
                }

                @Override
                public DataSource getDataSource() {
                    return null;
                }
            });
        } catch (Exception ex) {
            failCountTotal.increment();
            throw ex;
        } finally {
            deleteCountTotal.increment();
        }
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"action", "deleteforce"})
    @Override
    public ResultStatus deleteForce(IEntity entity) throws SQLException {
        /**
         * 设置万能版本,表示和所有的版本都匹配.
         */
        entity.resetVersion(VersionHelp.OMNIPOTENCE_VERSION);

        return delete(entity);
    }

    private boolean isSub(IEntity entity) {
        return entity.entityClass().extendEntityClass() != null;
    }

    private IEntity buildChildEntity(IEntity entity, long pref) {
        return build(entity, entity.entityClass(), new EntityFamily(pref, 0));
    }

    private IEntity buildFatherEntity(IEntity entity, long cref) {
        return build(entity, entity.entityClass().extendEntityClass(), new EntityFamily(0, cref));
    }

    private IEntity build(IEntity entity, IEntityClass entityClass, IEntityFamily family) {
        // 当前属于子类的属性速查表.
        Map<IEntityField, Object> fieldTable =
            entityClass.fields().stream().collect(Collectors.toMap(v -> v, v -> ""));

        IEntityValue newValues = new EntityValue(entityClass.id());
        entity.entityValue().values().stream()
            .filter(v -> fieldTable.containsKey(v.getField()))
            .forEach(v -> {
                newValues.addValue(v);
            });

        return new Entity(entity.id(), entityClass, newValues, family, entity.version());
    }

    // true 发起了警告,false 没有发起警告.
    private boolean warnNoSearchable(IEntity entity) {
        IEntityClass entityClass = entity.entityClass();
        long indexNumber = entityClass.fields().stream().filter(f -> f.config().isSearchable()).count();
        if (indexNumber == 0 && isSub(entity)) {
            indexNumber +=
                entityClass.extendEntityClass().fields().stream().filter(f -> f.config().isSearchable()).count();
        }
        if (indexNumber == 0) {
            logger.warn("An attempt was made to create an Entity({})-EntityClass({}) without any index.",
                entity.id(), entity.entityClass().id());
            return true;
        }

        return false;
    }

    // 判断是否操作冲突.
    private boolean isConflict(int size) {
        return size <= 0;
    }

    // 检查当前是状态是否可写入.
    private void checkReady() throws SQLException {
        if (!ready) {
            throw new SQLException("Data is blocked synchronously and cannot be written now.");
        }
    }

}
