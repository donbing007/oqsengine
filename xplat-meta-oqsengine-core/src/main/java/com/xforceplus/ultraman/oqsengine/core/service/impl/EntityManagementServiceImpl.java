package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.common.mode.OqsMode;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.common.version.VersionHelp;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityFamily;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.status.CDCStatusService;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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

    @Resource
    private CommitIdStatusService commitIdStatusService;

    /**
     * 可以接爱的最大心跳间隔.
     */
    private long allowMaxLiveTimeMs = 3000;

    /**
     * 可以接受的最大未同步提交号数量.
     */
    private long allowMaxUnSyncCommitIdSize = 30;

    /**
     * 忽略CDC状态检查.
     */
    private boolean ignoreCDCStatus;

    private Counter inserCountTotal = Metrics.counter(MetricsDefine.WRITE_COUNT_TOTAL, "action", "build");
    private Counter replaceCountTotal = Metrics.counter(MetricsDefine.WRITE_COUNT_TOTAL, "action", "replace");
    private Counter deleteCountTotal = Metrics.counter(MetricsDefine.WRITE_COUNT_TOTAL, "action", "delete");
    private Counter failCountTotal = Metrics.counter(MetricsDefine.FAIL_COUNT_TOTAL);
    private AtomicInteger readOnly = Metrics.gauge(MetricsDefine.MODE, new AtomicInteger(0));

    private ScheduledExecutorService checkCDCStatusWorker;
    private volatile boolean ready = true;
    private volatile String blockMessage;

    public EntityManagementServiceImpl() {
        this(false);
    }

    public EntityManagementServiceImpl(boolean ignoreCDCStatus) {
        this.ignoreCDCStatus = ignoreCDCStatus;
    }

    public long getAllowMaxLiveTimeMs() {
        return allowMaxLiveTimeMs;
    }

    public void setAllowMaxLiveTimeMs(long allowMaxLiveTimeMs) {
        this.allowMaxLiveTimeMs = allowMaxLiveTimeMs;
    }

    public long getAllowMaxUnSyncCommitIdSize() {
        return allowMaxUnSyncCommitIdSize;
    }

    public void setAllowMaxUnSyncCommitIdSize(long allowMaxUnSyncCommitIdSize) {
        this.allowMaxUnSyncCommitIdSize = allowMaxUnSyncCommitIdSize;
    }

    @PostConstruct
    public void init() {
        setNormalMode();
        if (!ignoreCDCStatus) {
            checkCDCStatusWorker = new ScheduledThreadPoolExecutor(1, ExecutorHelper.buildNameThreadFactory("CDC-monitor"));
            checkCDCStatusWorker.scheduleWithFixedDelay(() -> {
                /**
                 * 几种情况会认为是CDC同步停止.
                 * 1. CDC状态非正常.
                 * 2. CDC心跳.
                 * 3. 未同步提交号达到阀值.
                 */
                if (!cdcStatusService.isAlive()) {
                    setReadOnlyMode("CDC heartbeat test failed.");
                    return;
                }

                long uncommentSize = commitIdStatusService.size();
                if (uncommentSize > allowMaxUnSyncCommitIdSize) {
                    setReadOnlyMode(
                        String.format("Not synchronizing the submission number over %d.", allowMaxUnSyncCommitIdSize));
                    return;
                }

                /**
                 * 检查CDC指标.
                 */
                Optional<CDCAckMetrics> ackOp = cdcStatusService.getAck();
                if (ackOp.isPresent()) {
                    CDCAckMetrics ackMetrics = ackOp.get();
                    CDCStatus cdcStatus = ackMetrics.getCdcConsumerStatus();
                    if (CDCStatus.CONNECTED != cdcStatus) {
                        setReadOnlyMode(String.format("CDC status is %s.", cdcStatus.name()));
                        return;
                    }

                }
                /**
                 * 查询不到结果时,假定存活.
                 */
                setNormalMode();

            }, 6, 6, TimeUnit.SECONDS);
        } else {
            logger.info("Ignore CDC status checks.");
        }
    }

    @PreDestroy
    public void destroy() {
        ready = false;
        if (!ignoreCDCStatus) {
            ExecutorHelper.shutdownAndAwaitTermination(checkCDCStatusWorker, 3600);
        }
    }


    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "all", "action", "build"})
    @Override
    public IEntity build(IEntity entity) throws SQLException {
        checkReady();

        markTime(entity);

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

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "all", "action", "replace"})
    @Override
    public ResultStatus replace(IEntity entity) throws SQLException {
        checkReady();

        markTime(entity);

        if (!masterStorage.selectOne(entity.id(), entity.entityClass()).isPresent()) {
            failCountTotal.increment();
            throw new SQLException(String.format("An Entity that does not exist cannot be updated (%d).", entity.id()));
        }

        // 克隆一份,后续的修改不影响入参.
        IEntity entityClone;
        try {
            entityClone = (IEntity) entity.clone();
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
                        IEntity fatherEntity = buildFatherEntity(entityClone, entityClone.id());
                        fatherEntity.resetId(entity.family().parent());

                        IEntity childEntity = buildChildEntity(entityClone, entityClone.family().parent());

                        if (isConflict(masterStorage.replace(fatherEntity))) {
                            hint.setRollback(true);
                            return ResultStatus.CONFLICT;
                        }

                        if (isConflict(masterStorage.replace(childEntity))) {
                            hint.setRollback(true);
                            return ResultStatus.CONFLICT;
                        }

                    } else {

                        if (isConflict(masterStorage.replace(entityClone))) {
                            hint.setRollback(true);
                            return ResultStatus.CONFLICT;
                        }

                        // 有子类
                        if (entityClone.family().child() > 0) {
                            // 父子同步
                            if (isConflict(masterStorage.synchronize(entityClone.id(), entityClone.family().child()))) {
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

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "all", "action", "delete"})
    @Override
    public ResultStatus delete(IEntity entity) throws SQLException {
        checkReady();

        markTime(entity);

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

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "all", "action", "deleteforce"})
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
        return build(entity, entity.entityClass(), new EntityFamily(pref, 0), true);
    }

    private IEntity buildFatherEntity(IEntity entity, long cref) {
        return build(entity, entity.entityClass().extendEntityClass(), new EntityFamily(0, cref), false);
    }

    /**
     * 构造一个新的IEntity实例,all参数决定了是对父子属性分离构造还是子类包含父类属性.
     */
    private IEntity build(IEntity entity, IEntityClass entityClass, IEntityFamily family, boolean includeFather) {
        // 当前属于子类的属性速查表.
        Map<IEntityField, Object> fieldTable =
            entityClass.fields().stream().collect(Collectors.toMap(v -> v, v -> ""));

        if (includeFather && entityClass.extendEntityClass() != null) {
            fieldTable.putAll(
                entityClass.extendEntityClass().fields().stream().collect(Collectors.toMap(v -> v, v -> "")));
        }

        IEntityValue newValues = new EntityValue(entityClass.id());
        entity.entityValue().values().stream()
            .filter(v -> fieldTable.containsKey(v.getField()))
            .forEach(v -> {
                newValues.addValue(v);
            });

        IEntity newEntity = new Entity(entity.id(), entityClass, newValues, family, entity.version(), OqsVersion.MAJOR);
        newEntity.markTime(entity.time());
        return newEntity;
    }

    // 判断是否操作冲突.
    private boolean isConflict(int size) {
        return size <= 0;
    }

    // 检查当前是状态是否可写入.
    private void checkReady() throws SQLException {
        if (!ready) {
            if (blockMessage != null) {
                throw new SQLException(String.format("Currently in read-only mode for the reason of [%s].", blockMessage));
            } else {
                throw new SQLException("Currently in read-only mode for unknown reasons.");
            }
        }
    }

    private void markTime(IEntity entity) {
        entity.markTime(System.currentTimeMillis());
    }

    private void setNormalMode() {
        blockMessage = null;
        readOnly.set(OqsMode.NORMAL.getValue());
        ready = true;

        if (logger.isDebugEnabled()) {
            logger.debug("The current check status is normal.");
        }
    }

    private void setReadOnlyMode(String msg) {
        blockMessage = msg;
        readOnly.set(OqsMode.READ_ONLY.getValue());
        ready = false;

        if (logger.isWarnEnabled()) {
            logger.warn("Set to read-only mode because [{}].", msg);
        }
    }

}
