package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.common.mode.OqsMode;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.common.version.VersionHelp;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.utils.EntityClassHelper;
import com.xforceplus.ultraman.oqsengine.event.ActualEvent;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.BuildPayload;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.DeletePayload;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.ReplacePayload;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.status.CDCStatusService;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Resource
    private MetaManager metaManager;

    @Resource
    private EventBus eventBus;

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
    public ResultStatus build(IEntity entity) throws SQLException {
        checkReady();

        markTime(entity);

        IEntityClass entityClass = EntityClassHelper.checkEntityClass(metaManager, entity.entityClassRef());

        try {

            return (ResultStatus) transactionExecutor.execute((tx, resource, hint) -> {

                if (entity.id() <= 0) {
                    long newId = idGenerator.next();
                    entity.resetId(newId);
                }
                entity.resetVersion(0);
                entity.restMaintainId(0);

                if (masterStorage.build(entity, entityClass) <= 0) {
                    return ResultStatus.UNCREATED;
                }

                tx.getAccumulator().accumulateBuild(entity.id());

                noticeEvent(tx, EventType.ENTITY_BUILD, entity);

                return ResultStatus.SUCCESS;

            });
        } catch (Exception ex) {

            logger.error(ex.getMessage(), ex);

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

        IEntityClass entityClass = EntityClassHelper.checkEntityClass(metaManager, entity.entityClassRef());

        try {
            return (ResultStatus) transactionExecutor.execute((tx, resource, hint) -> {

                Optional<IEntity> targetEntityOp = masterStorage.selectOne(entity.id(), entityClass);
                if (!targetEntityOp.isPresent()) {
                    return ResultStatus.NOT_FOUND;
                }

                IEntity targetEntity = targetEntityOp.get();
                // 操作时间
                targetEntity.markTime(entity.time());
                targetEntity.entityValue().addValues(entity.entityValue().values());

                if (isConflict(masterStorage.replace(targetEntity, entityClass))) {
                    hint.setRollback(true);
                    return ResultStatus.CONFLICT;
                }

                tx.getAccumulator().accumulateReplace(entity.id());

                noticeEvent(tx, EventType.ENTITY_REPLACE, entity);

                return ResultStatus.SUCCESS;
            });
        } catch (Exception ex) {

            logger.error(ex.getMessage(), ex);

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

        IEntityClass entityClass = EntityClassHelper.checkEntityClass(metaManager, entity.entityClassRef());

        try {
            return (ResultStatus) transactionExecutor.execute((tx, resource, hint) -> {

                if (!masterStorage.exist(entity.id())) {
                    return ResultStatus.NOT_FOUND;
                }

                if (isConflict(masterStorage.delete(entity, entityClass))) {
                    hint.setRollback(true);
                    return ResultStatus.CONFLICT;
                }

                tx.getAccumulator().accumulateDelete(entity.id());

                noticeEvent(tx, EventType.ENTITY_DELETE, entity);

                return ResultStatus.SUCCESS;
            });
        } catch (Exception ex) {

            logger.error(ex.getMessage(), ex);

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
        if (entity.time() <= 0) {
            entity.markTime(System.currentTimeMillis());
        }
    }

    private void setNormalMode() {
        blockMessage = null;
        readOnly.set(OqsMode.NORMAL.getValue());
        ready = true;
    }

    private void setReadOnlyMode(String msg) {
        blockMessage = msg;
        readOnly.set(OqsMode.READ_ONLY.getValue());
        ready = false;

        if (logger.isWarnEnabled()) {
            logger.warn("Set to read-only mode because [{}].", msg);
        }
    }

    /**
     * 发布事件.
     * entities 在 ENTITY_REPLACE 事件中会传递两个IEntity实例,第一个表示旧的,第二个表示新的.
     */
    private void noticeEvent(Transaction tx, EventType type, IEntity... entities) {
        long txId = 0;
        if (tx != null) {
            txId = tx.id();
        } else {
            logger.warn("No transaction found, object change event cannot be published.");
            return;
        }

        long number = tx.getAccumulator().operationNumber();

        switch (type) {
            case ENTITY_BUILD: {
                eventBus.notify(new ActualEvent(EventType.ENTITY_BUILD, new BuildPayload(txId, number, entities[0])));
                break;
            }
            case ENTITY_REPLACE: {
                eventBus.notify(new ActualEvent(EventType.ENTITY_REPLACE, new ReplacePayload(txId, number, entities[0])));
                break;
            }
            case ENTITY_DELETE: {
                eventBus.notify(new ActualEvent(EventType.ENTITY_DELETE, new DeletePayload(txId, number, entities[0])));
                break;
            }
            default: {
                logger.warn("Cannot handle event type, cannot publish event.[{}]", type.name());
            }
        }
    }

}
