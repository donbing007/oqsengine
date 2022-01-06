package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.calculation.Calculation;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationScenarios;
import com.xforceplus.ultraman.oqsengine.calculation.context.DefaultCalculationContext;
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
import com.xforceplus.ultraman.oqsengine.lock.ResourceLocker;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCAckMetrics;
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
import com.xforceplus.ultraman.oqsengine.storage.pojo.EntityPackage;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.task.TaskCoordinator;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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

    @Resource(name = "taskThreadPool")
    public ExecutorService taskThreadPool;

    @Resource
    private ResourceLocker resourceLocker;

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
     * 未设置的对象实体主键,将等于此值.
     */
    private static final long UNSET_PRIMARY_ID = 0;
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
    public OperationResult build(IEntity[] entities) throws SQLException {
        checkReady();

        IEntityClass[] entityClasses = EntityClassHelper.checkEntityClasses(metaManager, entities);

        OperationResult result = preview(entities, entityClasses, true);
        if (!result.isSuccess()) {
            return result;
        }

        CalculationContext calculationContext = buildCalculationContext(CalculationScenarios.BUILD);
        try {
            result = (OperationResult) transactionExecutor.execute((tx, resource, hint) -> {

                calculationContext.focusTx(tx);

                Map.Entry<VerifierResult, IEntityField> verify = null;
                IEntity currentEntity;
                /*
                循环处理当前新创建的实体其本身的计算字段或者其字段造成的其他实例改变的影响.
                注意: CalculationContext.maintain 会对所影响的实例增加独占锁,最后定需要调用
                CalculationContext.destroy 进行清理.
                 */
                for (int i = 0; i < entities.length; i++) {

                    // 计算字段处理.
                    calculationContext.focusSourceEntity(entities[i]);
                    calculationContext.focusEntity(entities[i], entityClasses[i]);
                    currentEntity = calculation.calculate(calculationContext);
                    setValueChange(calculationContext, Optional.of(currentEntity), Optional.empty(), entityClasses[i]);

                    verify = verifyFields(entityClasses[i], currentEntity);
                    if (VerifierResult.OK != verify.getKey()) {
                        return transformVerifierResultToOperationResult(verify, currentEntity);
                    }

                    calculation.maintain(calculationContext);

                }

                // 开始持久化
                EntityPackage entityPackage = new EntityPackage();
                int len = entities.length;
                for (int i = 0; i < len; i++) {
                    entityPackage.put(entities[i], entityClasses[i]);
                }

                masterStorage.build(entityPackage);

                Map.Entry<IEntity, IEntityClass> entityEntry;

                for (int i = 0; i < len; i++) {
                    entityEntry = entityPackage.get(i).get();
                    if (entityEntry.getKey().isDirty()) {
                        hint.setRollback(true);
                        return OperationResult.unCreated(
                            String.format("The entity for %s could not be created successfully.",
                                entityEntry.getValue().name()));
                    } else {

                        if (tx.getAccumulator().accumulateBuild(entityEntry.getKey())) {
                            hint.setRollback(true);
                            return OperationResult.unAccumulate();
                        }
                    }
                }

                if (!calculationContext.persist()) {
                    hint.setRollback(true);
                    return OperationResult.unAccumulate();
                }


                return OperationResult.success();
            });

            return result;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            failCountTotal.increment(entities.length);

            throw ex;
        } finally {

            // 保证必须清理
            if (calculationContext != null) {
                calculationContext.destroy();
            }

            inserCountTotal.increment(entities.length);
        }

    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "all", "action", "build"})
    @Override
    public OperationResult build(IEntity entity) throws SQLException {
        checkReady();

        IEntityClass entityClass = EntityClassHelper.checkEntityClass(metaManager, entity.entityClassRef());

        OperationResult operationResult = preview(entity, entityClass, true);
        if (!operationResult.isSuccess()) {
            return operationResult;
        }

        CalculationContext calculationContext = buildCalculationContext(CalculationScenarios.BUILD);
        try {
            operationResult = (OperationResult) transactionExecutor.execute((tx, resource, hint) -> {

                calculationContext.focusTx(tx);
                calculationContext.focusSourceEntity(entity);
                calculationContext.focusEntity(entity, entityClass);
                IEntity currentEntity = calculation.calculate(calculationContext);
                setValueChange(calculationContext, Optional.of(currentEntity), Optional.empty(), entityClass);

                Map.Entry<VerifierResult, IEntityField> verify = verifyFields(entityClass, currentEntity);
                if (VerifierResult.OK != verify.getKey()) {
                    return transformVerifierResultToOperationResult(verify, entity);
                }

                calculation.maintain(calculationContext);

                if (!masterStorage.build(currentEntity, entityClass)) {
                    return OperationResult.unCreated();
                }

                if (!calculationContext.persist()) {
                    hint.setRollback(true);
                    return OperationResult.unAccumulate();
                }

                if (!tx.getAccumulator().accumulateBuild(currentEntity)) {
                    hint.setRollback(true);
                    return OperationResult.unAccumulate();
                }

                noticeEvent(tx, EventType.ENTITY_BUILD, currentEntity);

                return OperationResult.success();
            });

            if (calculationContext.hasHint()) {
                operationResult.addHints(calculationContext.getHints());
            }

            return operationResult;
        } catch (Exception ex) {

            logger.error(ex.getMessage(), ex);

            failCountTotal.increment();
            throw ex;

        } finally {

            // 保证必须清理
            if (calculationContext != null) {
                calculationContext.destroy();
            }

            inserCountTotal.increment();
        }
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "all", "action", "replaces"})
    @Override
    public OperationResult replace(IEntity[] entities) throws SQLException {
        checkReady();

        IEntityClass[] entityClasses = EntityClassHelper.checkEntityClasses(metaManager, entities);

        OperationResult result = preview(entities, entityClasses, true);
        if (!result.isSuccess()) {
            return result;
        }

        CalculationContext calculationContext = buildCalculationContext(CalculationScenarios.REPLACE);
        try {
            result = (OperationResult) transactionExecutor.execute((tx, resource, hint) -> {

                calculationContext.focusTx(tx);

                for (IEntity entity : entities) {

                }

            });

        } finally {
            replaceCountTotal.increment();
        }

    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "all", "action", "replace"})
    @Override
    public OperationResult replace(IEntity entity) throws SQLException {
        checkReady();

        IEntityClass entityClass = EntityClassHelper.checkEntityClass(metaManager, entity.entityClassRef());

        OperationResult operationResult = preview(entity, entityClass, false);
        if (!operationResult.isSuccess()) {
            return operationResult;
        }

        CalculationContext calculationContext = buildCalculationContext(CalculationScenarios.REPLACE);
        try {
            operationResult = (OperationResult) transactionExecutor.execute((tx, resource, hint) -> {

                /*
                 * 获取当前的原始版本.
                 */
                Optional<IEntity> targetEntityOp = masterStorage.selectOne(entity.id(), entityClass);
                if (!targetEntityOp.isPresent()) {
                    return OperationResult.notFound();
                }

                // 新实例.
                IEntity newEntity = targetEntityOp.get();

                // 保留旧实例.
                IEntity oldEntity = null;
                try {
                    oldEntity = (IEntity) newEntity.clone();
                } catch (CloneNotSupportedException e) {
                    return OperationResult.unknown();
                }

                // 操作时间
                newEntity.markTime(entity.time());

                // 新的字段值加入当前实例.
                for (IValue newValue : entity.entityValue().values()) {
                    newEntity.entityValue().addValue(newValue);
                }

                calculationContext.focusTx(tx);
                calculationContext.focusSourceEntity(newEntity);
                calculationContext.focusEntity(newEntity, entityClass);
                newEntity = calculation.calculate(calculationContext);
                setValueChange(calculationContext, Optional.of(newEntity), Optional.of(oldEntity), entityClass);

                Map.Entry<VerifierResult, IEntityField> verifyResult = verifyFields(entityClass, newEntity);
                if (VerifierResult.OK != verifyResult.getKey()) {
                    hint.setRollback(true);
                    return transformVerifierResultToOperationResult(verifyResult, newEntity);
                }

                calculation.maintain(calculationContext);

                if (!masterStorage.replace(newEntity, entityClass)) {
                    hint.setRollback(true);
                    return OperationResult.conflict();
                }

                if (!calculationContext.persist()) {
                    hint.setRollback(true);
                    return OperationResult.conflict();
                }

                //  这里将版本+1，使得外部获取的版本为当前成功版本
                newEntity.resetVersion(newEntity.version() + ONE_INCREMENT_POS);

                if (!tx.getAccumulator().accumulateReplace(newEntity, oldEntity)) {
                    hint.setRollback(true);
                    return OperationResult.unAccumulate();
                }

                noticeEvent(tx, EventType.ENTITY_REPLACE, entity);

                return OperationResult.success();
            });

            if (calculationContext.hasHint()) {
                operationResult.addHints(calculationContext.getHints());
            }

            return operationResult;
        } catch (Exception ex) {

            logger.error(ex.getMessage(), ex);

            failCountTotal.increment();
            throw ex;
        } finally {

            // 保证必须清理
            if (calculationContext != null) {
                calculationContext.destroy();
            }

            replaceCountTotal.increment();
        }
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "all", "action", "deletes"})
    @Override
    public OperationResult delete(IEntity[] entities) throws SQLException {
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

        CalculationContext calculationContext = buildCalculationContext(CalculationScenarios.DELETE);
        try {
            return (OperationResult) transactionExecutor.execute((tx, resource, hint) -> {

                Optional<IEntity> targetEntityOp = masterStorage.selectOne(entity.id(), entityClass);
                if (!targetEntityOp.isPresent()) {
                    return OperationResult.notFound();
                }

                IEntity targetEntity = targetEntityOp.get();
                if (targetEntity.version() < entity.version()) {
                    targetEntity.resetVersion(entity.version());
                }

                if (!masterStorage.delete(targetEntity, entityClass)) {
                    hint.setRollback(true);
                    return OperationResult.conflict();
                }

                if (!tx.getAccumulator().accumulateDelete(targetEntity)) {
                    hint.setRollback(true);
                    return OperationResult.unAccumulate();
                }

                /*
                删除时计算字段不需要计算,只需要进行维护.
                 */
                calculationContext.focusTx(tx);
                calculationContext.focusSourceEntity(targetEntity);
                calculationContext.focusEntity(targetEntity, entityClass);
                setValueChange(calculationContext, Optional.empty(), Optional.of(targetEntity), entityClass);
                calculation.maintain(calculationContext);

                noticeEvent(tx, EventType.ENTITY_DELETE, targetEntity);

                return OperationResult.success();
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
    public OperationResult deleteForce(IEntity[] entities) throws SQLException {
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

    // 转换校验不通过后的响应.
    private OperationResult transformVerifierResultToOperationResult(
        Map.Entry<VerifierResult, IEntityField> verify, IEntity entity) {

        IEntityField field = verify.getValue();
        IValue value = entity.entityValue().getValue(verify.getValue().id()).orElse(null);
        if (logger.isDebugEnabled()) {
            logger.debug("Field {}({}) validation result {}, validation is based on {}.[%s]",
                field.name(),
                field.id(),
                verify.getKey().name(),
                field.config().toString(),
                value != null ? value.getValue().toString() : "NULL");
        }

        String msg;
        switch (verify.getKey()) {
            case REQUIRED: {
                msg = String.format("The field %s is required.", field.name());
                break;
            }
            case TOO_LONG: {
                msg = String.format("The value of field %s is too long. The maximum acceptable length is %d.[%s]",
                    field.name(), field.config().getLen(), value != null ? value.getValue().toString() : "NULL");
                break;
            }
            case HIGH_PRECISION: {
                msg = String.format("The accuracy of field %s is too high. The maximum accepted accuracy is %d.[%s]",
                    field.name(), field.config().getPrecision(), value != null ? value.getValue().toString() : "NULL");
                break;
            }
            case NON_EXISTENT: {
                msg = String.format("The %s field does not exist.", field.name());
                break;
            }
            default:
                msg = "Unknown validation failed.";
        }

        switch (verify.getKey()) {
            case REQUIRED:
                return OperationResult.fieldMust(msg);
            case TOO_LONG:
                return OperationResult.fieldToLong(msg);
            case HIGH_PRECISION:
                return OperationResult.fieldHighPrecision(msg);
            case NON_EXISTENT:
                return OperationResult.fieldNonExist(msg);
            default:
                return OperationResult.unknown();
        }
    }

    private CalculationContext buildCalculationContext(
        CalculationScenarios scenarios) {

        return DefaultCalculationContext.Builder.anCalculationContext()
            .withScenarios(scenarios)
            .withMetaManager(this.metaManager)
            .withMasterStorage(this.masterStorage)
            .withTaskCoordinator(this.taskCoordinator)
            .withKeyValueStorage(this.kv)
            .withEventBus(this.eventBus)
            .withTaskExecutorService(this.taskThreadPool)
            .withBizIDGenerator(this.bizIDGenerator)
            .withConditionsSelectStorage(this.combinedSelectStorage)
            .withResourceLocker(this.resourceLocker)
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

    // 批量预检.
    private OperationResult preview(IEntity[] entities, IEntityClass[] entityClasses, boolean build) throws SQLException {
        if (entities.length != entityClasses.length) {
            return OperationResult.notExistMeta();
        }

        OperationResult result = OperationResult.success();
        for (int i = 0; i < entities.length; i++) {
            result = preview(entities[i], entityClasses[i], build);

            if (!result.isSuccess()) {
                return result;
            }
        }

        return result;
    }

    // 预检
    private OperationResult preview(IEntity entity, IEntityClass entityClass, boolean build) {
        if (entity == null) {
            return OperationResult.notFound();
        }

        // 不合式的实例.
        if (entity.entityClassRef() == null
            || entity.entityClassRef().getId() <= 0
            || entity.entityClassRef().getCode() == null) {

            return OperationResult.notExistMeta();
        }

        // 过滤不应该被改写的字段.
        if (entity.entityValue() != null) {
            entity.entityValue().filter(v ->
                v.getField().calculationType() == CalculationType.STATIC
                    || v.getField().calculationType() == CalculationType.LOOKUP
            );
        }

        IEntityField field;
        for (IValue value : entity.entityValue().values()) {
            field = value.getField();
            if (!entityClass.field(field.id()).isPresent()) {
                return OperationResult.fieldNonExist(String.format("Field '%s' does not exist.", field.name()));
            }
        }

        markTime(entity);

        // 设置对象主键标识.
        if (entity.id() <= UNSET_PRIMARY_ID) {
            long newId = longNoContinuousPartialOrderIdGenerator.next();
            entity.resetId(newId);
        }

        // 如果是创建,那么重置版本号和维护id.
        if (build) {
            entity.resetVersion(BUILD_VERSION);
            entity.restMaintainId(0);
        }

        return OperationResult.success();
    }

    private void markTime(IEntity entity) {
        if (entity.time() <= 0) {
            entity.markTime(System.currentTimeMillis());
        }
    }

}
