package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.calculation.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationLogicContext;
import com.xforceplus.ultraman.oqsengine.calculation.context.DefaultCalculationLogicContext;
import com.xforceplus.ultraman.oqsengine.calculation.context.Scenarios;
import com.xforceplus.ultraman.oqsengine.calculation.dto.CalculationHint;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationLogicException;
import com.xforceplus.ultraman.oqsengine.calculation.factory.CalculationLogicFactory;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunctionFactory;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.parse.AggregationParse;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.ParseTree;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl.PTNode;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.common.mode.OqsMode;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.common.serializable.utils.JacksonDefaultMapper;
import com.xforceplus.ultraman.oqsengine.common.version.VersionHelp;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.OperationResult;
import com.xforceplus.ultraman.oqsengine.core.service.utils.EntityClassHelper;
import com.xforceplus.ultraman.oqsengine.event.ActualEvent;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.BuildPayload;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.DeletePayload;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.ReplacePayload;
import com.xforceplus.ultraman.oqsengine.idgenerator.client.BizIDGenerator;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.CalculationComparator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.verifier.ValueVerifier;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.verifier.VerifierFactory;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.verifier.VerifierResult;
import com.xforceplus.ultraman.oqsengine.status.CDCStatusService;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.ErrorStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.task.TaskCoordinator;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * entity 管理服务实现.
 *
 * @author dongbin
 * @version 0.1 2020/2/18 14:12
 * @since 1.8
 */
public class EntityManagementServiceImpl implements EntityManagementService {

    // TODO: 业务主键操作从masterStorage中删除了,在设计完成后将在此完成业务主键. by dongbin 2021/09/17.

    final Logger logger = LoggerFactory.getLogger(EntityManagementServiceImpl.class);

    @Resource(name = "longNoContinuousPartialOrderIdGenerator")
    private LongIdGenerator longNoContinuousPartialOrderIdGenerator;

    @Resource(name = "longContinuousPartialOrderIdGenerator")
    private LongIdGenerator longContinuousPartialOrderIdGenerator;

    @Resource(name = "serviceTransactionExecutor")
    private TransactionExecutor transactionExecutor;

    @Resource
    private MasterStorage masterStorage;

    @Resource
    private KeyValueStorage kv;

    @Resource
    private CDCStatusService cdcStatusService;

    @Resource
    private CommitIdStatusService commitIdStatusService;

    @Resource
    private MetaManager metaManager;

    /**
     * 计算字段计算逻辑工厂.
     */
    @Resource
    private CalculationLogicFactory calculationLogicFactory;

    @Resource
    private EventBus eventBus;

    @Resource
    private BizIDGenerator bizIDGenerator;

    @Resource
    private TaskCoordinator taskCoordinator;

    /**
     * 字段校验器工厂.
     */
    private VerifierFactory verifierFactory;

    /**
     * 聚合计算函数工厂.
     */
    //@Resource
    private AggregationFunctionFactory aggregationFunctionFactory;

    /**
     * 聚合字段解析器.
     */
    //@Resource
    private AggregationParse aggregationParse;

    @Resource
    private IndexStorage indexStorage;

    /*
    只读的原因.
     */
    private enum ReadOnleyModeRease {
        // 未知,不应该产生.
        UNKNOWN(0),
        // 非只读,正常状态.
        NOT(1),
        // CDC心跳失败.
        CDC_HEARTBEAT(2),
        // 未同步的提交号过多.
        UNCOMMIT_TOO_MUCH(3),
        // CDC离线.
        CDC_UNCONNECTED(4);

        private final int symbol;

        ReadOnleyModeRease(int symbol) {
            this.symbol = symbol;
        }

        public int getSymbol() {
            return symbol;
        }

        public static ReadOnleyModeRease getInstance(int value) {
            for (ReadOnleyModeRease rease : ReadOnleyModeRease.values()) {
                if (rease.getSymbol() == value) {
                    return rease;
                }
            }

            return UNKNOWN;
        }
    }

    /**
     * 未知版本号.
     */
    private static final int UN_KNOW_VERSION = -1;
    /**
     * 新创建时的初始版本号.
     */
    private static final int BUILD_VERSION = 0;
    /**
     * 自增的偏移量.
     */
    private static final int ONE_INCREMENT_POS = 1;
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
    private final boolean ignoreCDCStatus;

    private final Counter inserCountTotal = Metrics.counter(MetricsDefine.WRITE_COUNT_TOTAL, "action", "build");
    private final Counter replaceCountTotal = Metrics.counter(MetricsDefine.WRITE_COUNT_TOTAL, "action", "replace");
    private final Counter deleteCountTotal = Metrics.counter(MetricsDefine.WRITE_COUNT_TOTAL, "action", "delete");
    private final Counter failCountTotal = Metrics.counter(MetricsDefine.FAIL_COUNT_TOTAL);
    private final AtomicInteger readOnly = Metrics.gauge(MetricsDefine.MODE, new AtomicInteger(0));
    private final AtomicInteger readOnlyRease =
        Metrics.gauge(MetricsDefine.READ_ONLEY_MODE_REASE, new AtomicInteger(0));

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
            checkCDCStatusWorker =
                new ScheduledThreadPoolExecutor(1, ExecutorHelper.buildNameThreadFactory("CDC-monitor"));
            checkCDCStatusWorker.scheduleWithFixedDelay(() -> {
                /*
                 * 几种情况会认为是CDC同步停止.
                 * 1. CDC状态非正常.
                 * 2. CDC心跳.
                 * 3. 未同步提交号达到阀值.
                 */
                if (!cdcStatusService.isAlive()) {
                    setReadOnlyMode(ReadOnleyModeRease.CDC_HEARTBEAT);
                    return;
                }

                long uncommentSize = commitIdStatusService.size();
                if (uncommentSize > allowMaxUnSyncCommitIdSize) {
                    setReadOnlyMode(ReadOnleyModeRease.UNCOMMIT_TOO_MUCH);
                    return;
                }

                /*
                 * 检查CDC指标.
                 */
                Optional<CDCAckMetrics> ackOp = cdcStatusService.getAck();
                if (ackOp.isPresent()) {
                    CDCAckMetrics ackMetrics = ackOp.get();
                    CDCStatus cdcStatus = ackMetrics.getCdcConsumerStatus();
                    if (CDCStatus.CONNECTED != cdcStatus) {
                        setReadOnlyMode(ReadOnleyModeRease.CDC_UNCONNECTED);
                        return;
                    }

                }
                /*
                 * 查询不到结果时,假定存活.
                 */
                setNormalMode();

            }, 6, 6, TimeUnit.SECONDS);
        } else {
            logger.info("Ignore CDC status checks.");
        }

        verifierFactory = new VerifierFactory();
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
    public OperationResult build(IEntity entity) throws SQLException {
        checkReady();

        preview(entity);

        markTime(entity);

        IEntityClass entityClass = EntityClassHelper.checkEntityClass(metaManager, entity.entityClassRef());

        if (entity.id() <= 0) {
            long newId = longNoContinuousPartialOrderIdGenerator.next();
            entity.resetId(newId);
        }
        entity.resetVersion(0);
        entity.restMaintainId(0);

        // 计算字段的计算动作.
        Collection<CalculationHint> hints;
        try {
            hints = processCalculationField(entity, entityClass, Scenarios.BUILD);
        } catch (CalculationLogicException ex) {
            logger.warn(ex.getMessage(), ex);
            return new OperationResult(
                0,
                entity.id(),
                UN_KNOW_VERSION,
                EventType.ENTITY_BUILD.getValue(),
                ResultStatus.ELEVATEFAILED,
                ex.getMessage()
            );
        }

        Map.Entry<VerifierResult, IEntityField> verifyResult = verifyFields(entityClass, entity);
        if (VerifierResult.OK != verifyResult.getKey()) {
            // 表示有些校验不通过.
            return new OperationResult(0, entity.id(), UN_KNOW_VERSION, EventType.ENTITY_BUILD.getValue(),
                transformVerifierResultToReusltStatus(verifyResult.getKey()),
                instantiateMessage(
                    verifyResult.getKey(),
                    verifyResult.getValue(),
                    entity.entityValue().getValue(verifyResult.getValue().id()).orElse(null)
                )
            );
        }

        OperationResult operationResult = null;
        try {
            operationResult = (OperationResult) transactionExecutor.execute((tx, resource, hint) -> {

                //处理聚合相关逻辑
                processAvgCalculation(entity, entityClass, Scenarios.BUILD);

                if (masterStorage.build(entity, entityClass) <= 0) {
                    return new OperationResult(tx.id(), entity.id(), UN_KNOW_VERSION, EventType.ENTITY_BUILD.getValue(),
                        ResultStatus.UNCREATED);
                }

                if (!tx.getAccumulator().accumulateBuild(entity)) {
                    hint.setRollback(true);
                    return new OperationResult(tx.id(), entity.id(), UN_KNOW_VERSION, EventType.ENTITY_BUILD.getValue(),
                        ResultStatus.UNACCUMULATE);
                }

                noticeEvent(tx, EventType.ENTITY_BUILD, entity);

                // 可能的字段维护
                maintainField(entity, entityClass, Scenarios.BUILD);

                return new OperationResult(tx.id(), entity.id(), BUILD_VERSION, EventType.ENTITY_BUILD.getValue(),
                    ResultStatus.SUCCESS);
            });

            if (hints != null && !hints.isEmpty()) {
                operationResult.resetStatus(hints);
            }

            return operationResult;
        } catch (Exception ex) {

            logger.error(ex.getMessage(), ex);

            failCountTotal.increment();
            throw ex;

        } finally {
            inserCountTotal.increment();
            //  处理半成功，将插入一条失败的Message
            handleHalfSuccessOrRecover(0, entityClass.id(), operationResult);
        }
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "all", "action", "replace"})
    @Override
    public OperationResult replace(IEntity entity) throws SQLException {
        checkReady();

        preview(entity);

        markTime(entity);

        IEntityClass entityClass = EntityClassHelper.checkEntityClass(metaManager, entity.entityClassRef());

        OperationResult operationResult = null;
        try {
            operationResult = (OperationResult) transactionExecutor.execute((tx, resource, hint) -> {

                /*
                 * 获取当前的原始版本.
                 */
                Optional<IEntity> targetEntityOp = masterStorage.selectOne(entity.id(), entityClass);
                if (!targetEntityOp.isPresent()) {
                    return new OperationResult(
                        tx.id(), entity.id(), UN_KNOW_VERSION, EventType.ENTITY_REPLACE.getValue(),
                        ResultStatus.NOT_FOUND);
                }

                IEntity targetEntity = targetEntityOp.get();

                /*
                 * 保留修改前的.
                 */
                IEntity oldEntity = null;
                try {
                    oldEntity = (IEntity) targetEntity.clone();
                } catch (CloneNotSupportedException e) {
                    return new OperationResult(
                        tx.id(), entity.id(), UN_KNOW_VERSION, EventType.ENTITY_REPLACE.getValue(),
                        ResultStatus.NOT_FOUND);
                }

                entity.entityValue().values().stream()
                    // 自增编号无法重复计算
                    .filter(f -> !CalculationType.AUTO_FILL.equals(f.getField().calculationType()))
                    .forEach(v -> {
                        targetEntity.entityValue().addValue(v);
                    });

                Collection<CalculationHint> hints;
                try {
                    hints = processCalculationField(targetEntity, entityClass, Scenarios.REPLACE);
                } catch (CalculationLogicException ex) {
                    logger.warn(ex.getMessage(), ex);
                    return new OperationResult(
                        0,
                        entity.id(),
                        UN_KNOW_VERSION,
                        EventType.ENTITY_BUILD.getValue(),
                        ResultStatus.ELEVATEFAILED,
                        ex.getMessage()
                    );
                }

                // 操作时间
                targetEntity.markTime(entity.time());

                Map.Entry<VerifierResult, IEntityField> verifyResult = verifyFields(entityClass, targetEntity);
                if (VerifierResult.OK != verifyResult.getKey()) {
                    hint.setRollback(true);
                    // 表示有些校验不通过.
                    return new OperationResult(0, targetEntity.id(), UN_KNOW_VERSION, EventType.ENTITY_BUILD.getValue(),
                        transformVerifierResultToReusltStatus(verifyResult.getKey()),
                        instantiateMessage(
                            verifyResult.getKey(),
                            verifyResult.getValue(),
                            targetEntity.entityValue().getValue(verifyResult.getValue().id()).orElse(null)
                        )
                    );
                }

                if (isConflict(masterStorage.replace(targetEntity, entityClass))) {
                    hint.setRollback(true);
                    return new OperationResult(
                        tx.id(), entity.id(), UN_KNOW_VERSION, EventType.ENTITY_REPLACE.getValue(),
                        ResultStatus.CONFLICT);
                }

                //  这里将版本+1，使得外部获取的版本为当前成功版本
                targetEntity.resetVersion(targetEntity.version() + ONE_INCREMENT_POS);

                //处理聚合相关逻辑
                processAvgCalculation(entity, entityClass, Scenarios.REPLACE);

                if (!tx.getAccumulator().accumulateReplace(targetEntity, oldEntity)) {
                    hint.setRollback(true);
                    return new OperationResult(
                        tx.id(), entity.id(), UN_KNOW_VERSION, EventType.ENTITY_REPLACE.getValue(),
                        ResultStatus.UNACCUMULATE);
                }

                noticeEvent(tx, EventType.ENTITY_REPLACE, entity);

                /*
                维护字段只关心真实被改变的字段.
                 */
                long[] maintainFields = buildReplaceMaintainFields(oldEntity, targetEntity);

                maintainField(entity, entityClass, maintainFields, Scenarios.REPLACE);

                //  半成功
                if (null != hints && !hints.isEmpty()) {
                    return new OperationResult(tx.id(), entity.id(), targetEntity.version(),
                        EventType.ENTITY_REPLACE.getValue(), ResultStatus.HALF_SUCCESS, hints,
                        ResultStatus.HALF_SUCCESS.name());
                }
                return new OperationResult(tx.id(), entity.id(), targetEntity.version(),
                    EventType.ENTITY_REPLACE.getValue(), ResultStatus.SUCCESS);
            });

            return operationResult;
        } catch (Exception ex) {

            logger.error(ex.getMessage(), ex);

            failCountTotal.increment();
            throw ex;
        } finally {
            replaceCountTotal.increment();
            //  处理半成功，将插入一条失败的Message
            handleHalfSuccessOrRecover(entity.maintainId(), entityClass.id(), operationResult);
        }
    }

    private long[] buildReplaceMaintainFields(IEntity oldEntity, IEntity targetEntity) {
        return targetEntity.entityValue().values().stream().filter(v ->
            // 只有静态字段和公式字段会被lookup.
            v.getField().calculationType() == CalculationType.STATIC
                || v.getField().calculationType() == CalculationType.FORMULA)

            .filter(v -> {
                Optional<IValue> oldOp = oldEntity.entityValue().getValue(v.getField().id());
                if (oldOp.isPresent()) {

                    // 和原值不相等才需要处理.
                    return !v.equals(oldOp.get());

                } else {

                    return false;

                }
            }).mapToLong(v -> v.getField().id()).toArray();
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "all", "action", "delete"})
    @Override
    public OperationResult delete(IEntity entity) throws SQLException {
        checkReady();

        markTime(entity);

        IEntityClass entityClass = EntityClassHelper.checkEntityClass(metaManager, entity.entityClassRef());

        try {
            return (OperationResult) transactionExecutor.execute((tx, resource, hint) -> {

                Optional<IEntity> targetEntityOp = masterStorage.selectOne(entity.id(), entityClass);
                if (!targetEntityOp.isPresent()) {
                    return new OperationResult(
                        tx.id(), entity.id(), UN_KNOW_VERSION, EventType.ENTITY_DELETE.getValue(),
                        ResultStatus.NOT_FOUND);
                }

                IEntity targetEntity = targetEntityOp.get();
                targetEntity.resetVersion(entity.version());

                //处理聚合相关逻辑
                processAvgCalculation(entity, entityClass, Scenarios.DELETE);

                if (isConflict(masterStorage.delete(targetEntity, entityClass))) {
                    hint.setRollback(true);
                    return new OperationResult(
                        tx.id(), entity.id(), UN_KNOW_VERSION, EventType.ENTITY_DELETE.getValue(),
                        ResultStatus.CONFLICT);
                }

                if (!tx.getAccumulator().accumulateDelete(targetEntity)) {
                    hint.setRollback(true);
                    return new OperationResult(
                        tx.id(), entity.id(), UN_KNOW_VERSION, EventType.ENTITY_DELETE.getValue(),
                        ResultStatus.UNACCUMULATE);
                }

                noticeEvent(tx, EventType.ENTITY_DELETE, targetEntity);

                return new OperationResult(
                    tx.id(), entity.id(), targetEntity.version(), EventType.ENTITY_DELETE.getValue(),
                    ResultStatus.SUCCESS);
            });
        } catch (Exception ex) {

            logger.error(ex.getMessage(), ex);

            failCountTotal.increment();
            throw ex;
        } finally {
            deleteCountTotal.increment();
        }
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        extraTags = {"initiator", "all", "action", "deleteforce"}
    )
    @Override
    public OperationResult deleteForce(IEntity entity) throws SQLException {
        /*
         * 设置万能版本,表示和所有的版本都匹配.
         */
        entity.resetVersion(VersionHelp.OMNIPOTENCE_VERSION);
        EntityClassHelper.checkEntityClass(metaManager, entity.entityClassRef());

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
                throw new SQLException(
                    String.format("Currently in read-only mode for the reason of [%s].", blockMessage));
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
        readOnlyRease.set(ReadOnleyModeRease.NOT.getSymbol());
        ready = true;
    }

    private void setReadOnlyMode(ReadOnleyModeRease rease) {
        switch (rease) {
            case CDC_HEARTBEAT: {
                blockMessage = "CDC heartbeat failure.";
                break;
            }
            case CDC_UNCONNECTED: {
                blockMessage = "The CDC is offline.";
                break;
            }
            case UNCOMMIT_TOO_MUCH: {
                blockMessage = String.format(
                    "Too many unsynchronized commit numbers. The maximum allowable value is %d.",
                    allowMaxUnSyncCommitIdSize);
                break;
            }
            default: {
                return;
            }
        }

        readOnly.set(OqsMode.READ_ONLY.getValue());
        readOnlyRease.set(rease.getSymbol());
        ready = false;

        if (logger.isWarnEnabled()) {
            logger.warn("Set to read-only mode because [{}].", blockMessage);
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
                eventBus
                    .notify(new ActualEvent(EventType.ENTITY_REPLACE, new ReplacePayload(txId, number, entities[0])));
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

    // 预检
    private void preview(IEntity entity) throws SQLException {
        if (entity == null) {
            throw new SQLException("Invalid object entity.");
        }

        if (entity.entityClassRef() == null
            || entity.entityClassRef().getId() <= 0
            || entity.entityClassRef().getCode() == null) {
            throw new SQLException(String.format("Incomplete entity(%d) type information.", entity.id()));
        }

        if (entity.entityValue() == null || entity.entityValue().size() == 0) {
            throw new SQLException(String.format("Entity(%d-%s) does not have any attributes.",
                entity.id(), entity.entityClassRef().getCode()));
        }
    }

    // 校验字段.
    private Map.Entry<VerifierResult, IEntityField> verifyFields(IEntityClass entityClass, IEntity entity) {
        VerifierResult result;
        for (IEntityField field : entityClass.fields()) {
            // 跳过主标识类型的检查.
            if (field.config().isIdentifie()) {
                continue;
            }

            ValueVerifier verifier = VerifierFactory.getVerifier(field.type());
            Optional<IValue> valueOp = entity.entityValue().getValue(field.id());
            IValue value = valueOp.orElse(null);
            try {
                result = verifier.verify(field, value);
                if (VerifierResult.OK != result) {
                    return new AbstractMap.SimpleEntry(result, field);
                }
            } catch (Exception e) {
                logger.warn("verify error, fieldId : {}, code : {}, value : {}, message : {}",
                    field.id(), field.name(), null == value ? null : value.getValue(), e.getMessage());
                throw e;
            }

        }

        return new AbstractMap.SimpleEntry(VerifierResult.OK, null);
    }

    private ResultStatus transformVerifierResultToReusltStatus(VerifierResult verifierResult) {
        switch (verifierResult) {
            case REQUIRED:
                return ResultStatus.FIELD_MUST;
            case TOO_LONG:
                return ResultStatus.FIELD_TOO_LONG;
            case HIGH_PRECISION:
                return ResultStatus.FIELD_HIGH_PRECISION;
            case NON_EXISTENT:
                return ResultStatus.FIELD_NON_EXISTENT;
            default:
                return ResultStatus.UNKNOWN;
        }
    }

    private String instantiateMessage(VerifierResult verifierResult, IEntityField field, IValue value) {
        if (logger.isDebugEnabled()) {
            logger.debug("Field {}({}) validation result {}, validation is based on {}.[%s]",
                field.name(),
                field.id(),
                verifierResult.name(),
                field.config().toString(),
                value != null ? value.getValue().toString() : "NULL");
        }

        switch (verifierResult) {
            case REQUIRED:
                return String.format("The field %s is required.", field.name());
            case TOO_LONG:
                return String.format("The value of field %s is too long. The maximum acceptable length is %d.[%s]",
                    field.name(), field.config().getLen(), value != null ? value.getValue().toString() : "NULL");
            case HIGH_PRECISION:
                return String.format("The accuracy of field %s is too high. The maximum accepted accuracy is %d.[%s]",
                    field.name(), field.config().getPrecision(), value != null ? value.getValue().toString() : "NULL");
            case NON_EXISTENT:
                return String.format("The %s field does not exist.", field.name());
            default:
                return "Unknown validation failed.";
        }
    }

    private Collection<CalculationHint> processCalculationField(
        IEntity sourceEntity, IEntityClass entityClass, Scenarios scenarios)
        throws CalculationLogicException {

        /*
        过滤掉所有的UNKNOWN和静态字段类型,同时按照计算的优先级从数字从小至大排序.化
        AUTO_FILL 字段为自动生成就不再变化的.所以在更新时排除.
        AGGREGATION 字段自身数据操作时无需聚合，这里进行排除，后续方法会额外进行聚合逻辑判断.
         */
        List<IEntityField> calculationFields = entityClass.fields().stream()
            .filter(
                f -> {
                    if (Scenarios.BUILD == scenarios) {
                        return CalculationType.UNKNOWN != f.calculationType()
                            && CalculationType.STATIC != f.calculationType()
                            && CalculationType.AGGREGATION != f.calculationType();
                    } else {
                        return CalculationType.UNKNOWN != f.calculationType()
                            && CalculationType.STATIC != f.calculationType()
                            && CalculationType.AUTO_FILL != f.calculationType()
                            && CalculationType.AGGREGATION != f.calculationType();
                    }
                }
            ).sorted(CalculationComparator.getInstance()).collect(Collectors.toList());

        if (calculationFields.isEmpty()) {
            return Collections.emptyList();
        }

        CalculationLogicContext context = buildCalculationLogicContext(sourceEntity, entityClass, scenarios);

        for (IEntityField field : calculationFields) {
            context.focusField(field);

            CalculationLogic logic = calculationLogicFactory.getCalculation(field.calculationType());

            Optional<IValue> newValueOp = logic.calculate(context);
            if (newValueOp.isPresent()) {
                sourceEntity.entityValue().addValue(newValueOp.get());
            } else {
                sourceEntity.entityValue().remove(field);
            }
        }

        return context.getHints();
    }

    /**
     * 聚合函数处理逻辑,本数据操作时无需聚合，在本数据操作完后进行聚合检查并操作受到影响的数据.
     *
     * @param sourceEntity 本数据信息.
     * @param sourceClass  对象信息.
     * @param scenarios    场景信息.
     * @return 返回聚合结果.
     * @throws CalculationLogicException
     */
    private Collection<CalculationHint> processAvgCalculation(
        IEntity sourceEntity, IEntityClass sourceClass, Scenarios scenarios)
        throws CalculationLogicException {

        CalculationLogicContext context = buildCalculationLogicContext(sourceEntity, sourceClass, scenarios);

        //预先校验下是否有受影响的树信息.
        Optional<List<ParseTree>> aggResult = checkAggregation(sourceEntity);
        // 处理受影响的聚合信息,需要判断该数据是否符合聚合条件.
        if (aggResult.isPresent()) {
            List<IEntity> replaceEntitys =
                findAggregationAndReplace(sourceEntity, sourceClass, aggResult.get(), null, scenarios);
            if (replaceEntitys != null && replaceEntitys.size() > 0) {
                // 批量更新数据
                try {
                    replaceAggregationEntitys(replaceEntitys);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return context.getHints();
    }

    private void maintainField(IEntity sourceEntity, IEntityClass sourceClass, Scenarios scenarios)
        throws CalculationLogicException {
        long[] fields = sourceClass.fields().stream().mapToLong(IEntityField::id).toArray();
        maintainField(sourceEntity, sourceClass, fields, scenarios);
    }

    /**
     * 字段维护,会迭代当前修改的字段进行维护.
     * 主要用于计算字段的数据一致性处理.
     */
    private void maintainField(IEntity sourceEntity, IEntityClass sourceClass, long[] fieldIds, Scenarios scenarios)
        throws CalculationLogicException {

        Collection<CalculationLogic> logics = calculationLogicFactory.getCalculations();
        CalculationLogicContext context = buildCalculationLogicContext(sourceEntity, sourceClass, scenarios);
        for (long fieldId : fieldIds) {
            context.focusField(sourceClass.field(fieldId).get());
            for (CalculationLogic logic : logics) {
                // 字段一定存在的.
                logic.maintain(context);
            }
        }
    }

    private CalculationLogicContext buildCalculationLogicContext(IEntity entity, IEntityClass entityClass,
                                                                 Scenarios scenarios) {
        return DefaultCalculationLogicContext.Builder.anCalculationLogicContext()
            .withScenarios(scenarios)
            .withMetaManager(this.metaManager)
            .withMasterStorage(this.masterStorage)
            .withTaskCoordinator(this.taskCoordinator)
            .withKeyValueStorage(this.kv)
            .withLongContinuousPartialOrderIdGenerator(this.longContinuousPartialOrderIdGenerator)
            .withEntityClass(entityClass)
            .withEntity(entity)
            .withBizIdGenerator(this.bizIDGenerator)
            .build();
    }

    private CalculationLogicContext buildCalculationLogicContextWithCount(IEntity sourceEntity, IEntity oldEntity,
                                                                          IEntity targetEntity,
                                                                          IEntityClass entityClass,
                                                                          Scenarios scenarios, int count) {
        return DefaultCalculationLogicContext.Builder.anCalculationLogicContext()
            .withScenarios(scenarios)
            .withMetaManager(this.metaManager)
            .withMasterStorage(this.masterStorage)
            .withTaskCoordinator(this.taskCoordinator)
            .withKeyValueStorage(this.kv)
            .withLongContinuousPartialOrderIdGenerator(this.longContinuousPartialOrderIdGenerator)
            .withEntityClass(entityClass)
            .withEntity(sourceEntity)
            .withBizIdGenerator(this.bizIDGenerator)
            .withAttribute("count", count)
            .withAttribute("oldEntity", oldEntity)
            .withAttribute("targetEntity", targetEntity)
            .build();
    }

    private void handleHalfSuccessOrRecover(long maintainId, long entityClassId, OperationResult operationResult) {
        if (null != operationResult && (maintainId > 0 && operationResult.getResultStatus().equals(ResultStatus.SUCCESS)
            || operationResult.getResultStatus().equals(ResultStatus.HALF_SUCCESS))) {

            String errors = "serialize errors failed.";
            try {
                errors = JacksonDefaultMapper.OBJECT_MAPPER.writeValueAsString(operationResult.getHints());
            } catch (Exception e) {
                //  ignore
            }
            //  将entityId设置为maintainId
            ErrorStorageEntity errorStorageEntity = ErrorStorageEntity.Builder.anErrorStorageEntity()
                .withMaintainId(maintainId > 0 ? maintainId : operationResult.getEntityId())
                .withEntity(entityClassId)
                .withId(operationResult.getEntityId())
                .withErrors(errors)
                .withFixedStatus(
                    operationResult.getResultStatus().equals(ResultStatus.SUCCESS)
                        ? FixedStatus.FIXED.getStatus() : FixedStatus.NOT_FIXED.getStatus()
                ).build();

            masterStorage.writeError(errorStorageEntity);
        }
    }

    /**
     * 判断该记录是否影响到其他对象记录.
     *
     * @param entity 记录信息.
     * @return 是否影响.
     */
    private Optional<List<ParseTree>> checkAggregation(IEntity entity) {
        List<ParseTree> parseTrees = aggregationParse.find(entity.entityClassRef().getId(),
            entity.entityClassRef().getProfile());
        if (parseTrees != null && parseTrees.size() > 0) {
            return Optional.of(parseTrees);
        }
        return Optional.empty();
    }

    private int countAggregationEntity(Aggregation aggregation, IEntity sourceEntity, IEntityClass entityClass) {
        // 得到count值
        Optional<IEntityClass> aggEntityClass =
            metaManager.load(aggregation.getClassId(), sourceEntity.entityClassRef().getProfile());
        int count = 1;
        if (aggEntityClass.isPresent()) {
            Conditions conditions = aggregation.getConditions();
            // 根据关系id得到关系字段
            Optional<IEntityField> entityField = entityClass.field(aggregation.getRelationId());
            if (entityField.isPresent()) {
                conditions.addAnd(new Condition(aggEntityClass.get().ref(), entityField.get(),
                    ConditionOperator.EQUALS, aggregation.getRelationId(),
                    sourceEntity.entityValue().getValue(sourceEntity.id()).get()));
            }
            Collection<EntityRef> entityRefs = null;
            try {
                entityRefs =
                    indexStorage.select(conditions, entityClass, SelectConfig.Builder.anSelectConfig().build());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (entityRefs != null && entityRefs.size() > 0) {
                count = entityRefs.size();
            }
        }
        return count;
    }

    /**
     * 迭代更新受影响的entity.
     *
     * @param sourceEntity 计算后的entity.
     * @return 返回受影响的entity.
     */
    private List<IEntity> findAggregationAndReplace(IEntity sourceEntity, IEntityClass sourceClass,
                                                    List<ParseTree> parseTrees,
                                                    List<IEntity> replaceEntitys, Scenarios scenarios) {

        // 获取当前entity的原始版本.
        Optional<IEntity> oldEntityOp = Optional.empty();
        Optional<IEntity> sourceEntityOp = Optional.empty();
        try {
            if (Scenarios.DELETE == scenarios) {
                oldEntityOp = masterStorage.selectOne(sourceEntity.id(), sourceClass);
                sourceEntityOp = Optional.empty();
            } else if (Scenarios.REPLACE == scenarios) {
                oldEntityOp = masterStorage.selectOne(sourceEntity.id(), sourceClass);
                sourceEntityOp = Optional.of(sourceEntity);
            } else {
                sourceEntityOp = Optional.of(sourceEntity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // 遍历出相同entitClass的字段
        List<PTNode> rootNodes = parseTrees.stream().map(ParseTree::root).collect(Collectors.toList());
        Map<IEntityClass, List<PTNode>> nodeMap =
            rootNodes.stream().collect(Collectors.groupingBy(PTNode::getEntityClass));

        Optional<IEntity> finalOldEntityOp = oldEntityOp;
        Optional<IEntity> finalSourceEntityOp = sourceEntityOp;
        if (replaceEntitys == null) {
            replaceEntitys = new ArrayList<>();
        }
        for (Map.Entry<IEntityClass, List<PTNode>> nd : nodeMap.entrySet()) {
            AtomicReference<Optional<IValue>> relationId = null;
            nd.getValue().forEach(ptNode -> {
                Optional<IValue> relation = sourceEntity.entityValue().getValue(ptNode.getRelationship().getId());
                if (relation.isPresent()) {
                    relationId.set(relation);
                }
            });
            Optional<IEntity> findEntity = Optional.empty();
            if (relationId != null) {
                try {
                    findEntity = masterStorage.selectOne(relationId.get().get().valueToLong(), nd.getKey());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                if (findEntity.isPresent()) {
                    IEntity targetEntity = findEntity.get();
                    // 对所有聚合字段进行运算
                    for (PTNode ptNode : nd.getValue()) {
                        boolean checkCondition =
                            checkEntityByCondition(sourceEntity, sourceClass, ptNode.getConditions());
                        if (!checkCondition) {
                            break;
                        }
                        int count = 1;
                        // 如果是求平均值的聚合，需要先把被聚合对象的记录count出来
                        Aggregation aggregation = (Aggregation) ptNode.getAggEntityField().config().getCalculation();
                        if (aggregation.getAggregationType().equals(AggregationType.AVG)) {
                            // 得到count值
                            count = countAggregationEntity(aggregation, sourceEntity, sourceClass);
                        }
                        // 没找到context set attr的方法，重新构建context
                        CalculationLogicContext avgContext =
                            buildCalculationLogicContextWithCount(finalSourceEntityOp.get(), finalOldEntityOp.get(),
                                targetEntity, sourceClass, scenarios, count);
                        avgContext.focusField(ptNode.getEntityField());

                        CalculationLogic logic =
                            calculationLogicFactory.getCalculation(ptNode.getEntityField().calculationType());
                        Optional<IValue> newValueOp = null;
                        try {
                            newValueOp = logic.calculate(avgContext);
                        } catch (CalculationLogicException e) {
                            e.printStackTrace();
                        }
                        if (newValueOp.isPresent()) {
                            findEntity.get().entityValue().addValue(newValueOp.get());
                        } else {
                            findEntity.get().entityValue().remove(ptNode.getEntityField());
                        }
                        Collection<CalculationHint> hints;
                        try {
                            hints = processCalculationField(findEntity.get(), nd.getKey(), Scenarios.REPLACE);
                        } catch (CalculationLogicException ex) {
                            logger.warn(ex.getMessage(), ex);
                        }
                        // 操作时间
                        markTime(findEntity.get());
                        targetEntity.markTime(findEntity.get().time());
                    }
                    replaceEntitys.add(findEntity.get());

                    //预先校验下是否有受影响的树信息.
                    Optional<List<ParseTree>> aggResult = checkAggregation(findEntity.get());
                    // 处理受影响的聚合信息,需要判断该数据是否符合聚合条件.
                    if (aggResult.isPresent()) {
                        replaceEntitys = findAggregationAndReplace(findEntity.get(), nd.getKey(), aggResult.get(),
                            replaceEntitys, scenarios.REPLACE);
                    }
                }
            }
        }
        return replaceEntitys;
    }

    private List<OperationResult> replaceAggregationEntitys(List<IEntity> entities) throws SQLException {
        List<OperationResult> operationResults = new ArrayList<>();
        for (IEntity entity : entities) {
            IEntityClass entityClass = EntityClassHelper.checkEntityClass(metaManager, entity.entityClassRef());
            OperationResult operationResult = null;
            try {
                operationResult = (OperationResult) transactionExecutor.execute((tx, resource, hint) -> {
                    /*
                     * 获取当前的原始版本.
                     */
                    Optional<IEntity> targetEntityOp = masterStorage.selectOne(entity.id(), entityClass);
                    if (!targetEntityOp.isPresent()) {
                        return new OperationResult(
                            tx.id(), entity.id(), UN_KNOW_VERSION, EventType.ENTITY_REPLACE.getValue(),
                            ResultStatus.NOT_FOUND);
                    }
                    IEntity targetEntity = targetEntityOp.get();
                    if (!tx.getAccumulator().accumulateReplace(entity, targetEntity)) {
                        hint.setRollback(true);
                        return new OperationResult(
                            tx.id(), entity.id(), UN_KNOW_VERSION, EventType.ENTITY_REPLACE.getValue(),
                            ResultStatus.UNACCUMULATE);
                    }
                    noticeEvent(tx, EventType.ENTITY_REPLACE, entity);
                    return new OperationResult(tx.id(), entity.id(), targetEntity.version(),
                        EventType.ENTITY_REPLACE.getValue(), ResultStatus.SUCCESS);
                });
                operationResults.add(operationResult);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                failCountTotal.increment();
                throw ex;
            } finally {
                replaceCountTotal.increment();
                //  处理半成功，将插入一条失败的Message
                handleHalfSuccessOrRecover(entity.maintainId(), entityClass.id(), operationResult);
            }
        }
        return operationResults;
    }

    /**
     * 根据条件和id来判断这条数据是否符合聚合范围.
     *
     * @param entity      被聚合数据.
     * @param entityClass 被聚合对象.
     * @param conditions  条件信息.
     * @return 是否符合.
     */
    private boolean checkEntityByCondition(IEntity entity, IEntityClass entityClass, Conditions conditions) {
        if (conditions == null || conditions.size() == 0) {
            return true;
        }
        conditions.addAnd(new Condition(entityClass.field("id").get(),
            ConditionOperator.EQUALS, entity.entityValue().getValue(entity.id()).get()));
        Collection<EntityRef> entityRefs = null;
        try {
            entityRefs = indexStorage.select(conditions, entityClass, SelectConfig.Builder.anSelectConfig().build());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (entityRefs != null && entityRefs.size() > 0) {
            return true;
        }
        return false;
    }

    /**
     * 暂时废弃这个方式.
     *
     * @param entity 数据.
     * @return 数据.
     */
    private IEntity callAggregationReplace(IEntity entity) throws SQLException {
        //1、update  2、查找所有关联树 3、处理next节点
        Optional<IEntityClass> entityClass =
            metaManager.load(entity.entityClassRef().getId(), entity.entityClassRef().getProfile());

        // replace entity
        OperationResult replace = replace(entity);
        Optional<IEntity> replaceEntity = null;
        if (entityClass.isPresent()) {

            if (ResultStatus.SUCCESS.equals(replace.getResultStatus())) {
                replaceEntity = masterStorage.selectOne(replace.getEntityId(), entityClass.get());
            }
            if (!replaceEntity.isPresent()) {

                //抛出异常
            }

            //find trees
            List<ParseTree> parseTrees = aggregationParse.find(entity.entityClassRef().getId(),
                entity.entityClassRef().getProfile());

            Optional<IEntity> finalReplaceEntity = replaceEntity;
            parseTrees.forEach(parseTree -> {
                //find next
                List<PTNode> nodes = parseTree.root().getNextNodes();
                nodes.forEach(n -> {
                    n.getRelationship().getEntityField();
                    Optional<IEntity> targetEntityOp = null;
                    Optional<IValue> relationValue =
                        finalReplaceEntity.get().entityValue().getValue(n.getRelationship().getId());
                    if (relationValue.isPresent()) {
                        try {
                            targetEntityOp =
                                masterStorage.selectOne(relationValue.get().valueToLong(), n.getEntityClass());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    IEntity targetEntity = targetEntityOp.get();
                    Optional<IValue> value = aggregationFunctionFactory.getAggregationFunction(n.getAggregationType())
                        .excute(targetEntity.entityValue().getValue(n.getEntityField().id()),
                            targetEntity.entityValue().getValue(n.getEntityField().id()),
                            finalReplaceEntity.get().entityValue().getValue(n.getEntityField().id()));
                    if (value.isPresent()) {
                        targetEntity.entityValue().addValue(value.get());
                    }
                    // 继续传递下去
                    try {
                        callAggregationReplace(targetEntity);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            });
        }
        return replaceEntity.get();
    }

}
