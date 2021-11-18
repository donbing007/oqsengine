package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.calculation.Calculation;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationScenarios;
import com.xforceplus.ultraman.oqsengine.calculation.context.DefaultCalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.dto.CalculationHint;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.common.mode.OqsMode;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
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
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.verifier.ValueVerifier;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.verifier.VerifierFactory;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.verifier.VerifierResult;
import com.xforceplus.ultraman.oqsengine.status.CDCStatusService;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.ConditionsSelectStorage;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.task.TaskCoordinator;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
    private TransactionManager transactionManager;

    @Resource
    private MasterStorage masterStorage;

    @Resource(name = "combinedSelectStorage")
    private ConditionsSelectStorage combinedSelectStorage;

    @Resource
    private KeyValueStorage kv;

    @Resource
    private CDCStatusService cdcStatusService;

    @Resource
    private CommitIdStatusService commitIdStatusService;

    @Resource
    private MetaManager metaManager;

    @Resource
    private EventBus eventBus;

    @Resource
    private BizIDGenerator bizIDGenerator;

    @Resource
    private TaskCoordinator taskCoordinator;

    @Resource
    private Calculation calculation;

    /**
     * 字段校验器工厂.
     */
    private VerifierFactory verifierFactory;

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


    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "all", "action", "builds"})
    @Override
    public OperationResult[] build(IEntity[] entities) throws SQLException {
        OperationResult[] results = new OperationResult[entities.length];
        Optional<Transaction> tx = transactionManager != null ? transactionManager.getCurrent() : Optional.empty();
        for (int i = 0; i < results.length; i++) {

            if (tx.isPresent()) {
                transactionManager.bind(tx.get().id());
            }

            results[i] = build(entities[i]);
        }
        return results;
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "all", "action", "build"})
    @Override
    public OperationResult build(IEntity entity) throws SQLException {
        checkReady();

        filter(entity);

        IEntityClass entityClass = EntityClassHelper.checkEntityClass(metaManager, entity.entityClassRef());

        preview(entity, entityClass);

        markTime(entity);

        if (entity.id() <= 0) {
            long newId = longNoContinuousPartialOrderIdGenerator.next();
            entity.resetId(newId);
        }
        entity.resetVersion(0);
        entity.restMaintainId(0);

        OperationResult operationResult = null;
        Collection<CalculationHint> hits = new ArrayList<>();
        try {
            operationResult = (OperationResult) transactionExecutor.execute((tx, resource, hint) -> {

                CalculationContext calculationContext = buildCalculationContext(CalculationScenarios.BUILD, tx);
                calculationContext.focusEntity(entity, entityClass);
                IEntity currentEntity = calculation.calculate(calculationContext);
                setValueChange(calculationContext, Optional.of(currentEntity), Optional.empty(), entityClass);

                Map.Entry<VerifierResult, IEntityField> verifyResult = verifyFields(entityClass, currentEntity);
                if (VerifierResult.OK != verifyResult.getKey()) {
                    // 表示有些校验不通过.
                    return new OperationResult(0, currentEntity.id(), UN_KNOW_VERSION,
                        EventType.ENTITY_BUILD.getValue(),
                        transformVerifierResultToReusltStatus(verifyResult.getKey()),
                        instantiateMessage(
                            verifyResult.getKey(),
                            verifyResult.getValue(),
                            currentEntity.entityValue().getValue(verifyResult.getValue().id()).orElse(null)
                        )
                    );
                }

                if (masterStorage.build(currentEntity, entityClass) <= 0) {
                    return new OperationResult(tx.id(), currentEntity.id(), UN_KNOW_VERSION,
                        EventType.ENTITY_BUILD.getValue(),
                        ResultStatus.UNCREATED);
                }

                if (!tx.getAccumulator().accumulateBuild(currentEntity)) {
                    hint.setRollback(true);
                    return new OperationResult(tx.id(), currentEntity.id(), UN_KNOW_VERSION,
                        EventType.ENTITY_BUILD.getValue(),
                        ResultStatus.UNACCUMULATE);
                }

                calculation.maintain(calculationContext);

                noticeEvent(tx, EventType.ENTITY_BUILD, currentEntity);

                hits.addAll(calculationContext.getHints());

                return new OperationResult(tx.id(), currentEntity.id(), BUILD_VERSION,
                    EventType.ENTITY_BUILD.getValue(),
                    ResultStatus.SUCCESS);
            });

            operationResult.resetStatus(hits);

            return operationResult;
        } catch (Exception ex) {

            logger.error(ex.getMessage(), ex);

            failCountTotal.increment();
            throw ex;

        } finally {
            inserCountTotal.increment();
        }
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "all", "action", "replaces"})
    @Override
    public OperationResult[] replace(IEntity[] entities) throws SQLException {
        OperationResult[] results = new OperationResult[entities.length];
        Optional<Transaction> tx = transactionManager != null ? transactionManager.getCurrent() : Optional.empty();
        for (int i = 0; i < results.length; i++) {

            if (tx.isPresent()) {
                transactionManager.bind(tx.get().id());
            }

            results[i] = replace(entities[i]);
        }
        return results;
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "all", "action", "replace"})
    @Override
    public OperationResult replace(IEntity entity) throws SQLException {
        checkReady();

        filter(entity);

        IEntityClass entityClass = EntityClassHelper.checkEntityClass(metaManager, entity.entityClassRef());

        preview(entity, entityClass);

        markTime(entity);


        OperationResult operationResult = null;
        Collection<CalculationHint> hits = new ArrayList<>();
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

                // 新实例.
                IEntity newEntity = targetEntityOp.get();

                // 保留旧实例.
                IEntity oldEntity = null;
                try {
                    oldEntity = (IEntity) newEntity.clone();
                } catch (CloneNotSupportedException e) {
                    return new OperationResult(
                        tx.id(), entity.id(), UN_KNOW_VERSION, EventType.ENTITY_REPLACE.getValue(),
                        ResultStatus.NOT_FOUND);
                }

                // 操作时间
                newEntity.markTime(entity.time());

                // 新的字段值加入当前实例.
                // 注意:将会删选AUTO_FILL字段
                withoutReplaceNoChange(entity, newEntity);

                CalculationContext calculationContext = buildCalculationContext(CalculationScenarios.REPLACE, tx);
                calculationContext.focusEntity(newEntity, entityClass);
                newEntity = calculation.calculate(calculationContext);
                setValueChange(calculationContext, Optional.of(newEntity), Optional.of(oldEntity), entityClass);

                Map.Entry<VerifierResult, IEntityField> verifyResult = verifyFields(entityClass, newEntity);
                if (VerifierResult.OK != verifyResult.getKey()) {
                    hint.setRollback(true);
                    // 表示有些校验不通过.
                    return new OperationResult(0, newEntity.id(), UN_KNOW_VERSION, EventType.ENTITY_BUILD.getValue(),
                        transformVerifierResultToReusltStatus(verifyResult.getKey()),
                        instantiateMessage(
                            verifyResult.getKey(),
                            verifyResult.getValue(),
                            newEntity.entityValue().getValue(verifyResult.getValue().id()).orElse(null)
                        )
                    );
                }

                if (isConflict(masterStorage.replace(newEntity, entityClass))) {
                    hint.setRollback(true);
                    return new OperationResult(
                        tx.id(), entity.id(), UN_KNOW_VERSION, EventType.ENTITY_REPLACE.getValue(),
                        ResultStatus.CONFLICT);
                }

                //  这里将版本+1，使得外部获取的版本为当前成功版本
                newEntity.resetVersion(newEntity.version() + ONE_INCREMENT_POS);

                if (!tx.getAccumulator().accumulateReplace(newEntity, oldEntity)) {
                    hint.setRollback(true);
                    return new OperationResult(
                        tx.id(), entity.id(), UN_KNOW_VERSION, EventType.ENTITY_REPLACE.getValue(),
                        ResultStatus.UNACCUMULATE);
                }

                noticeEvent(tx, EventType.ENTITY_REPLACE, entity);

                calculation.maintain(calculationContext);

                hits.addAll(calculationContext.getHints());

                return new OperationResult(tx.id(), entity.id(), newEntity.version(),
                    EventType.ENTITY_REPLACE.getValue(), ResultStatus.SUCCESS);
            });

            operationResult.resetStatus(hits);

            return operationResult;
        } catch (Exception ex) {

            logger.error(ex.getMessage(), ex);

            failCountTotal.increment();
            throw ex;
        } finally {
            replaceCountTotal.increment();
        }
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "all", "action", "deletes"})
    @Override
    public OperationResult[] delete(IEntity[] entities) throws SQLException {
        OperationResult[] results = new OperationResult[entities.length];
        Optional<Transaction> tx = transactionManager != null ? transactionManager.getCurrent() : Optional.empty();
        for (int i = 0; i < results.length; i++) {

            if (tx.isPresent()) {
                transactionManager.bind(tx.get().id());
            }

            results[i] = delete(entities[i]);
        }
        return results;
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

                /*
                删除时计算字段不需要计算,只需要进行维护.
                 */
                CalculationContext calculationContext = buildCalculationContext(CalculationScenarios.DELETE, tx);
                calculationContext.focusEntity(targetEntity, entityClass);
                setValueChange(calculationContext, Optional.empty(), Optional.of(targetEntity), entityClass);
                calculation.maintain(calculationContext);

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
        extraTags = {"initiator", "all", "action", "deleteforces"}
    )
    @Override
    public OperationResult[] deleteForce(IEntity[] entities) throws SQLException {
        OperationResult[] results = new OperationResult[entities.length];
        Optional<Transaction> tx = transactionManager != null ? transactionManager.getCurrent() : Optional.empty();
        for (int i = 0; i < results.length; i++) {

            if (tx.isPresent()) {
                transactionManager.bind(tx.get().id());
            }

            results[i] = deleteForce(entities[i]);
        }
        return results;
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
    private void preview(IEntity entity, IEntityClass entityClass) throws SQLException {
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

        IEntityField field;
        for (IValue value : entity.entityValue().values()) {
            field = value.getField();
            if (!entityClass.field(field.id()).isPresent()) {
                throw new SQLException(String.format("Field '%s' does not exist.", field.name()));
            }
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

    private CalculationContext buildCalculationContext(
        CalculationScenarios scenarios, Transaction tx) {

        return DefaultCalculationContext.Builder.anCalculationContext()
            .withScenarios(scenarios)
            .withMetaManager(this.metaManager)
            .withMasterStorage(this.masterStorage)
            .withTaskCoordinator(this.taskCoordinator)
            .withKeyValueStorage(this.kv)
            .withBizIDGenerator(this.bizIDGenerator)
            .withTransaction(tx)
            .withCombindedSelectStorage(this.combinedSelectStorage)
            .build();
    }

    // 设置改变的值.
    private void setValueChange(
        CalculationContext context, Optional<IEntity> newEntityOp, Optional<IEntity> oldEntityOp,
        IEntityClass entityClass) {
        if (!newEntityOp.isPresent() && !oldEntityOp.isPresent()) {
            return;
        }

        if (newEntityOp.isPresent() && !oldEntityOp.isPresent()) {
            // build
            IEntity newEntity = newEntityOp.get();
            newEntity.entityValue().scan(v -> {
                IEntityField field = v.getField();
                context.addValueChange(ValueChange.build(newEntity.id(), new EmptyTypedValue(field), v));
            });
        } else if (!newEntityOp.isPresent() && oldEntityOp.isPresent()) {
            // delete
            IEntity oldEntity = oldEntityOp.get();
            oldEntity.entityValue().scan(v -> {
                IEntityField field = v.getField();
                context.addValueChange(ValueChange.build(oldEntity.id(), v, new EmptyTypedValue(field)));
            });
        } else {
            // replace
            IEntity oldEntity = oldEntityOp.get();
            IEntity newEntity = newEntityOp.get();
            for (IEntityField f : entityClass.fields()) {
                IValue oldValue = oldEntity.entityValue().getValue(f.id()).orElse(new EmptyTypedValue(f));
                IValue newValue = newEntity.entityValue().getValue(f.id()).orElse(new EmptyTypedValue(f));

                if (oldValue.equals(newValue)) {
                    continue;
                } else {
                    context.addValueChange(ValueChange.build(oldEntity.id(), oldValue, newValue));
                }
            }
        }
    }

    private void withoutReplaceNoChange(IEntity entity, IEntity newEntity) {
        List<IValue> filterValues = entity.entityValue().values().stream().filter(e -> {
            return !e.getField().calculationType().isReplaceNeedNotChange();
        }).collect(Collectors.toList());

        for (IValue newValue : filterValues) {
            newEntity.entityValue().addValue(newValue);
        }
    }

    // 只允许静态字段进入写事务.
    private void filter(IEntity entity) {
        entity.entityValue().filter(v ->
            v.getField().calculationType() == CalculationType.STATIC
        );
    }

}
