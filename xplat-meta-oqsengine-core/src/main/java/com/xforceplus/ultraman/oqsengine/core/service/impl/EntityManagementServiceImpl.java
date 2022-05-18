package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.calculation.Calculation;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationScenarios;
import com.xforceplus.ultraman.oqsengine.calculation.context.DefaultCalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.factory.CalculationLogicFactory;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.common.mode.OqsMode;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.OqsResult;
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
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntitys;
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
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.task.TaskCoordinator;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
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

    @Resource(name = "taskThreadPool")
    public ExecutorService taskThreadPool;

    @Resource
    private ResourceLocker resourceLocker;

    @Resource
    private CalculationLogicFactory calculationLogicFactory;

    /*
    独占锁的等待时间.
     */
    private long lockTimeoutMs = 30000;

    /**
     * 设置悲观锁尝试加锁的超时等待毫秒值.
     *
     * @param lockTimeoutMs 等待毫秒值.
     */
    public void setLockTimeoutMs(long lockTimeoutMs) {
        if (lockTimeoutMs > 0) {
            this.lockTimeoutMs = lockTimeoutMs;
        }
    }

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

        logger.info("Pessimistic lock attempt timeout milliseconds is {}.", this.lockTimeoutMs);

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

            }, 12, 12, TimeUnit.SECONDS);
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


    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        percentiles = {0.5, 0.9, 0.99},
        extraTags = {"initiator", "all", "action", "builds"})
    @Override
    public OqsResult<IEntity[]> build(IEntity[] entities) throws SQLException {
        checkReady();

        if (entities.length == 0) {
            return OqsResult.success();
        }

        IEntityClass[] entityClasses = new IEntityClass[entities.length];
        Optional<IEntityClass> entityClassOp;
        EntityClassRef ref;
        for (int i = 0; i < entities.length; i++) {
            ref = entities[i].entityClassRef();
            entityClassOp = metaManager.load(ref);

            if (!entityClassOp.isPresent()) {

                if (logger.isDebugEnabled()) {
                    logger.debug("[builds] Unable to find meta information {}.", ref);
                }

                return OqsResult.notExistMeta(ref);

            } else {

                entityClasses[i] = entityClassOp.get();
            }
        }

        OqsResult result = preview(entities, entityClasses, true);
        if (!result.isSuccess()) {
            return result;
        }

        CalculationContext calculationContext = buildCalculationContext(CalculationScenarios.BUILD);
        try {
            result = (OqsResult) transactionExecutor.execute((tx, resource) -> {

                calculationContext.focusTx(tx);

                Map.Entry<VerifierResult, IEntityField> verify = null;
                IEntity currentEntity;
                IEntityClass currentEntityClass;
                /*
                循环处理当前新创建的实体其本身的计算字段或者其字段造成的其他实例改变的影响.
                注意: CalculationContext.maintain 会对所影响的实例增加独占锁,最后定需要调用
                CalculationContext.destroy 进行清理.
                 */

                List<IEntity> dirtyEntities = new ArrayList(entities.length);
                for (int i = 0; i < entities.length; i++) {
                    currentEntity = entities[i];
                    currentEntityClass = entityClasses[i];
                    // 计算字段处理.
                    calculationContext.focusSourceEntity(currentEntity);
                    calculationContext.focusEntity(currentEntity, currentEntityClass);
                    calculation.calculate(calculationContext);
                    setValueChange(calculationContext, currentEntity, null);

                    verify = verifyFields(currentEntityClass, currentEntity);
                    if (VerifierResult.OK != verify.getKey()) {
                        return transformVerifierResultToOperationResult(verify, currentEntity);
                    }

                    if (currentEntity.isDirty()) {
                        dirtyEntities.add(currentEntity);
                    }

                }

                if (dirtyEntities.isEmpty()) {
                    return OqsResult.success();
                }

                // 开始持久化
                EntityPackage entityPackage = new EntityPackage();
                int len = dirtyEntities.size();
                for (int i = 0; i < len; i++) {
                    entityPackage.put(dirtyEntities.get(i), entityClasses[i], false);
                }

                // 主操作
                masterStorage.build(entityPackage);

                Map.Entry<IEntity, IEntityClass> entityEntry;

                for (int i = 0; i < len; i++) {
                    entityEntry = entityPackage.get(i).get();
                    if (entityEntry.getKey().isDirty()) {

                        if (logger.isDebugEnabled()) {
                            logger.debug("[builds] Entity {} failed to be created.", entityEntry.getKey().toString());
                        }

                        tx.getHint().setRollback(true);
                        return OqsResult.unCreated();
                    } else {

                        if (!tx.getAccumulator().accumulateBuild(entityEntry.getKey())) {

                            if (logger.isDebugEnabled()) {
                                logger.debug("[builds] Transaction accumulator processing failed.");
                            }

                            tx.getHint().setRollback(true);
                            return OqsResult.unAccumulate();
                        }

                        // 所有维护都必须在主操作之后.
                        calculationContext.focusSourceEntity(entityEntry.getKey());
                        calculationContext.focusEntity(entityEntry.getKey(), entityEntry.getValue());
                        calculation.maintain(calculationContext);
                    }
                }

                if (!calculationContext.persist()) {

                    if (logger.isDebugEnabled()) {
                        logger.debug("[builds] Failed to persist the calculated field maintenance result.");
                    }

                    tx.getHint().setRollback(true);
                    return OqsResult.unAccumulate();
                }

                //批量设置为不进行CDC等待.
                tx.getHint().setCanWaitCommitSync(false);

                IEntity[] actualEntities = dirtyEntities.stream().toArray(IEntity[]::new);
                eventBus.notify(new ActualEvent(EventType.ENTITY_BUILD,
                    new BuildPayload(tx.id(), actualEntities)));

                return OqsResult.success(actualEntities);
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

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        percentiles = {0.5, 0.9, 0.99},
        extraTags = {"initiator", "all", "action", "build"})
    @Override
    public OqsResult<IEntity> build(IEntity entity) throws SQLException {
        checkReady();

        Optional<IEntityClass> entityClassOp = metaManager.load(entity.entityClassRef());
        IEntityClass entityClass;
        if (!entityClassOp.isPresent()) {
            EntityClassRef ref = entity.entityClassRef();
            return OqsResult.notExistMeta(ref);
        } else {
            entityClass = entityClassOp.get();
        }

        OqsResult oqsResult = preview(entity, entityClass, true);
        if (!oqsResult.isSuccess()) {
            return oqsResult;
        }

        if (entity.isDeleted()) {
            return OqsResult.conflict("[build] It has been deleted.");
        }

        CalculationContext calculationContext = buildCalculationContext(CalculationScenarios.BUILD);
        try {
            oqsResult = (OqsResult) transactionExecutor.execute((tx, resource) -> {

                calculationContext.focusTx(tx);
                calculationContext.focusSourceEntity(entity);
                calculationContext.focusEntity(entity, entityClass);
                IEntity currentEntity = calculation.calculate(calculationContext);
                setValueChange(calculationContext, currentEntity, null);

                Map.Entry<VerifierResult, IEntityField> verify = verifyFields(entityClass, currentEntity);
                if (VerifierResult.OK != verify.getKey()) {
                    return transformVerifierResultToOperationResult(verify, entity);
                }

                // 非脏,不需要继续.
                if (!entity.isDirty()) {

                    if (logger.isDebugEnabled()) {
                        logger.debug("[build] The instance is not \"dirty\" and creation is abandoned.");
                    }

                    return OqsResult.success();
                }

                // 主操作
                masterStorage.build(currentEntity, entityClass);
                if (currentEntity.isDirty()) {
                    // 仍是"脏"的,表示没有持久化成功.
                    if (logger.isDebugEnabled()) {
                        logger.debug("[build] Entity {} failed to be created.", currentEntity.toString());
                    }

                    tx.getHint().setRollback(true);
                    return OqsResult.unCreated();
                }

                if (!tx.getAccumulator().accumulateBuild(currentEntity)) {

                    if (logger.isDebugEnabled()) {
                        logger.debug("[build] Transaction accumulator processing failed.");
                    }

                    tx.getHint().setRollback(true);
                    return OqsResult.unAccumulate();
                }

                // 需要保证所有维护都在主操作结束后.
                calculation.maintain(calculationContext);

                if (!calculationContext.persist()) {

                    if (logger.isDebugEnabled()) {
                        logger.debug("[build] Failed to persist the calculated field maintenance result.");
                    }

                    tx.getHint().setRollback(true);
                    return OqsResult.conflict("Conflict maintenance.");
                }

                eventBus.notify(new ActualEvent(EventType.ENTITY_BUILD, new BuildPayload(tx.id(), currentEntity)));

                // 单个操作需要等待同步.
                tx.getHint().setCanWaitCommitSync(true);

                return OqsResult.success(currentEntity);
            });

            if (calculationContext.hasHint()) {
                oqsResult.addHints(calculationContext.getHints());
            }

            return oqsResult;
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

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        percentiles = {0.5, 0.9, 0.99},
        extraTags = {"initiator", "all", "action", "replaces"})
    @Override
    public OqsResult<Map<IEntity, IValue[]>> replace(IEntity[] entities) throws SQLException {
        checkReady();

        if (entities.length == 0) {
            if (logger.isWarnEnabled()) {
                logger.warn("[replaces] The target object list updated in batches is empty.");
            }

            return OqsResult.success();
        }

        IEntity[] dirtyEntities = Arrays.stream(entities)
            .filter(e -> e.isDirty() || !e.isDeleted())
            .toArray(IEntity[]::new);

        IEntityClass[] entityClasses = new IEntityClass[dirtyEntities.length];
        Optional<IEntityClass> entityClassOp;
        EntityClassRef ref;
        for (int i = 0; i < dirtyEntities.length; i++) {
            ref = dirtyEntities[i].entityClassRef();
            entityClassOp = metaManager.load(ref);

            if (!entityClassOp.isPresent()) {

                if (logger.isDebugEnabled()) {
                    logger.debug("[replaces] Unable to find meta information {}.", ref);
                }

                return OqsResult.notExistMeta(ref);

            } else {

                entityClasses[i] = entityClassOp.get();
            }
        }

        OqsResult oqsResult = preview(dirtyEntities, entityClasses, false);
        if (!oqsResult.isSuccess()) {
            return oqsResult;
        }

        Map<Long, IEntityClass> entityClassTable = Arrays.stream(entityClasses).collect(Collectors.toMap(
            ec -> ec.id(),
            ec -> ec,
            (ec0, ec1) -> ec0
        ));
        // help gc
        entityClasses = null;

        CalculationContext calculationContext = buildCalculationContext(CalculationScenarios.REPLACE);
        try {
            oqsResult = (OqsResult) transactionExecutor.execute((tx, resource) -> {

                calculationContext.focusTx(tx);

                long[] targetIds = Arrays.stream(dirtyEntities).mapToLong(e -> e.id()).toArray();

                // 当前需要更新的目标实例列表.
                List<IEntity> targetEntities = new ArrayList(masterStorage.selectMultiple(targetIds));

                if (targetEntities.isEmpty()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[replaces] None of the targets that need to be updated exist.");
                    }

                    return OqsResult.success();
                }

                // 含有更新字段的对象速查表,其每一个entity都只包含需要更新的IValue实例,所以并不完整.
                // key为实例id.
                Map<Long, IEntity> replaceEntityTable =
                    Arrays.stream(dirtyEntities)
                        .collect(Collectors.toMap(e -> e.id(), e -> e, (e0, e1) -> e0));

                /*
                处理计算字段,并替换需要更新的值.
                注意: CalculationContext.maintain 会对所影响的实例增加独占锁,最后定需要调用
                CalculationContext.destroy 进行清理.
                 */
                IEntity replaceEntity;
                IEntity newEntity;
                IEntity oldEntity;

                // 加锁
                String[] resoruces =
                    targetEntities.stream().map(e -> IEntitys.resource(e.id())).toArray(String[]::new);
                boolean lockResult = resourceLocker.tryLocks(this.lockTimeoutMs, resoruces);
                if (!lockResult) {
                    // 加锁失败.
                    if (logger.isDebugEnabled()) {
                        logger.debug("[replaces] Failed to lock the instance.");
                    }

                    return OqsResult.conflict();
                }

                ReplacePayload replacePayload = new ReplacePayload(tx.id());
                try {
                    for (int i = 0; i < targetEntities.size(); i++) {
                        newEntity = targetEntities.get(i);
                        // 保留旧实例.
                        oldEntity = newEntity.copy();

                        replaceEntity = replaceEntityTable.get(newEntity.id());
                        if (replaceEntity == null) {

                            return OqsResult.notFound(
                                String.format("The instance identified as %d does not exist.", newEntity.id()));

                        } else {

                            /*
                             * 这里注意,必须先将需要更新的值替换才能进行正确计算字段计算.
                             * 如果新旧两个IValue一致那么将不会更新.
                             */
                            for (IValue newValue : replaceEntity.entityValue().values()) {

                                newEntity.entityValue().addValue(newValue);

                            }

                            if (!newEntity.isDirty()) {
                                continue;
                            }

                            newEntity.markTime();

                            IEntityClass entityClass = entityClassTable.get(newEntity.entityClassRef().getId());
                            Map.Entry<VerifierResult, IEntityField> verify = verifyFields(entityClass, newEntity);
                            if (VerifierResult.OK != verify.getKey()) {
                                return transformVerifierResultToOperationResult(verify, newEntity);
                            }

                            calculationContext.focusSourceEntity(newEntity);
                            calculationContext.focusEntity(newEntity, entityClass);
                            newEntity = calculation.calculate(calculationContext);
                            setValueChange(calculationContext, newEntity, oldEntity);

                            replacePayload.addChange(oldEntity,
                                newEntity.entityValue().values().stream()
                                    .filter(v -> v.isDirty()).toArray(IValue[]::new));
                        }
                    }

                    EntityPackage entityPackage = new EntityPackage();
                    // 忽略掉所有替换后计算后仍然是干净的对象,表示没有任何改变.
                    targetEntities.stream().filter(e -> e.isDirty()).forEach(e ->
                        entityPackage.put(e, entityClassTable.get(e.entityClassRef().getId()), false)
                    );

                    if (entityPackage.isEmpty()) {

                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                "[replaces] No instances are \"dirty\" after the calculation, so no updates are required.");
                        }

                        return OqsResult.success();
                    }

                    // 主操作
                    masterStorage.replace(entityPackage);
                    for (int i = 0; i < entityPackage.size(); i++) {
                        newEntity = entityPackage.getNotSafe(i).getKey();

                        if (newEntity.isDirty()) {

                            if (logger.isDebugEnabled()) {
                                logger.debug("[replace] Failed to update instance ({}).", newEntity.id());
                            }

                            tx.getHint().setRollback(true);
                            return OqsResult.unReplaced(newEntity.id());
                        }

                        if (!tx.getAccumulator().accumulateReplace(newEntity)) {

                            if (logger.isDebugEnabled()) {
                                logger.debug("[replaces] Transaction accumulator processing failed.");
                            }

                            tx.getHint().setRollback(true);
                            return OqsResult.unAccumulate();
                        }

                        // 维护必须在主操作之后.
                        calculationContext.focusSourceEntity(newEntity);
                        calculationContext.focusEntity(newEntity, entityPackage.getNotSafe(i).getValue());
                        calculation.maintain(calculationContext);
                    }

                    if (!calculationContext.persist()) {

                        if (logger.isDebugEnabled()) {
                            logger.debug("[replaces] Failed to persist the calculated field maintenance result.");
                        }

                        tx.getHint().setRollback(true);
                        return OqsResult.conflict("Conflict maintenance.");
                    }

                    //批量设置为不进行CDC等待.
                    tx.getHint().setCanWaitCommitSync(false);

                } finally {
                    resourceLocker.unlocks(resoruces);
                }

                eventBus.notify(new ActualEvent(EventType.ENTITY_REPLACE, replacePayload));

                return OqsResult.success(replacePayload.getChanges());
            });

            if (calculationContext.hasHint()) {
                oqsResult.addHints(calculationContext.getHints());
            }

        } catch (Exception ex) {

            logger.error(ex.getMessage(), ex);

            failCountTotal.increment(entities.length);

            throw ex;

        } finally {
            // 保证必须清理
            if (calculationContext != null) {
                calculationContext.destroy();
            }

            replaceCountTotal.increment(entities.length);
        }

        return oqsResult;
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        percentiles = {0.5, 0.9, 0.99},
        extraTags = {"initiator", "all", "action", "replace"})
    @Override
    public OqsResult<Map.Entry<IEntity, IValue[]>> replace(IEntity entity) throws SQLException {
        checkReady();

        Optional<IEntityClass> entityClassOp = metaManager.load(entity.entityClassRef());
        IEntityClass entityClass;
        if (!entityClassOp.isPresent()) {
            EntityClassRef ref = entity.entityClassRef();

            if (logger.isDebugEnabled()) {
                logger.debug("[replace] Unable to find meta information {}.", ref);
            }

            return OqsResult.notExistMeta(ref);
        } else {
            entityClass = entityClassOp.get();
        }

        OqsResult oqsResult = preview(entity, entityClass, false);
        if (!oqsResult.isSuccess()) {
            return oqsResult;
        }

        if (entity.isDeleted()) {

            if (logger.isDebugEnabled()) {
                logger.debug("[replace] Instance ({}) has been deleted.", entity.id());
            }

            return OqsResult.notFound(entity.id());
        }

        if (!entity.isDirty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("[replace] Instances ({}) are not dirty.", entity.id());
            }

            return OqsResult.success();
        }

        CalculationContext calculationContext = buildCalculationContext(CalculationScenarios.REPLACE);
        try {
            oqsResult = (OqsResult) transactionExecutor.execute((tx, resource) -> {
                String lockResource = IEntitys.resource(entity.id());
                boolean lockResult = resourceLocker.tryLock(lockTimeoutMs, lockResource);
                if (!lockResult) {

                    if (logger.isDebugEnabled()) {
                        logger.debug("[replace] Failed to lock the instance.");
                    }

                    return OqsResult.conflict();
                }

                IEntity newEntity;
                IEntity oldEntity;
                ReplacePayload replacePayload;
                try {
                    /*
                     * 获取当前的原始版本.
                     */
                    Optional<IEntity> targetEntityOp = masterStorage.selectOne(entity.id(), entityClass);
                    if (!targetEntityOp.isPresent()) {

                        if (logger.isDebugEnabled()) {
                            logger.debug("[replace] The instance ({}) that needs to be updated does not exist.",
                                entity.id());
                        }

                        return OqsResult.notFound();
                    }

                    // 新实例.
                    newEntity = targetEntityOp.get();

                    // 保留旧实例.
                    oldEntity = newEntity.copy();

                    // 操作时间
                    newEntity.markTime();

                    // 新的字段值加入当前实例.
                    for (IValue newValue : entity.entityValue().values()) {
                        newEntity.entityValue().addValue(newValue);
                    }

                    // 没有任何更新.
                    if (!newEntity.isDirty()) {

                        if (logger.isDebugEnabled()) {
                            logger.debug("The instance ({}) is unchanged and does not need to be updated.",
                                entity.id());
                        }

                        return OqsResult.success();
                    }

                    calculationContext.focusTx(tx);
                    calculationContext.focusSourceEntity(newEntity);
                    calculationContext.focusEntity(newEntity, entityClass);
                    newEntity = calculation.calculate(calculationContext);
                    setValueChange(calculationContext, newEntity, oldEntity);

                    Map.Entry<VerifierResult, IEntityField> verifyResult = verifyFields(entityClass, newEntity);
                    if (VerifierResult.OK != verifyResult.getKey()) {
                        tx.getHint().setRollback(true);
                        return transformVerifierResultToOperationResult(verifyResult, newEntity);
                    }

                    replacePayload = new ReplacePayload(tx.id());
                    replacePayload.addChange(oldEntity,
                        newEntity.entityValue().values().stream().filter(v -> v.isDirty()).toArray(IValue[]::new));

                    // 主操作
                    masterStorage.replace(newEntity, entityClass);
                    if (newEntity.isDirty()) {

                        if (logger.isDebugEnabled()) {
                            logger.debug("[replace] Failed to update instance ({}).", newEntity.id());
                        }

                        tx.getHint().setRollback(true);
                        return OqsResult.unReplaced(newEntity.id());
                    }

                    //  这里将版本+1，使得外部获取的版本为当前成功版本
                    newEntity.resetVersion(newEntity.version() + ONE_INCREMENT_POS);

                    if (!tx.getAccumulator().accumulateReplace(oldEntity)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("[replace] Transaction accumulator processing failed.");
                        }
                        tx.getHint().setRollback(true);
                        return OqsResult.unAccumulate();
                    }

                    // 所有维护动作都必须保证在主操作之后.
                    calculation.maintain(calculationContext);

                    if (!calculationContext.persist()) {

                        if (logger.isDebugEnabled()) {
                            logger.debug("[replace] Failed to persist the calculated field maintenance result.");
                        }

                        tx.getHint().setRollback(true);
                        return OqsResult.conflict();
                    }

                } finally {
                    resourceLocker.unlock(lockResource);
                }

                tx.getHint().setCanWaitCommitSync(true);

                eventBus.notify(new ActualEvent(EventType.ENTITY_REPLACE, replacePayload));

                return OqsResult.success(replacePayload.getChanage(oldEntity).get());
            });

            if (calculationContext.hasHint()) {
                oqsResult.addHints(calculationContext.getHints());
            }

            return oqsResult;
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

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        percentiles = {0.5, 0.9, 0.99},
        extraTags = {"initiator", "all", "action", "deletes"})
    @Override
    public OqsResult<IEntity[]> delete(IEntity[] entities) throws SQLException {
        checkReady();

        IEntityClass[] entityClasses = new IEntityClass[entities.length];
        Optional<IEntityClass> entityClassOp;
        EntityClassRef ref;
        for (int i = 0; i < entities.length; i++) {
            ref = entities[i].entityClassRef();
            entityClassOp = metaManager.load(ref);

            if (!entityClassOp.isPresent()) {

                if (logger.isDebugEnabled()) {
                    logger.debug("[deletes] Unable to find meta information {}.", ref);
                }

                return OqsResult.notExistMeta(ref);

            } else {

                entityClasses[i] = entityClassOp.get();
            }
        }

        for (IEntity entity : entities) {
            markTime(entity);
        }

        Map<Long, IEntityClass> entityClassTable = Arrays.stream(entityClasses).collect(Collectors.toMap(
            ec -> ec.id(),
            ec -> ec,
            (ec0, ec1) -> ec0
        ));
        // help gc
        entityClasses = null;

        CalculationContext calculationContext = buildCalculationContext(CalculationScenarios.DELETE);

        OqsResult oqsResult;
        try {
            oqsResult = (OqsResult) transactionExecutor.execute((tx, resource) -> {

                calculationContext.focusTx(tx);

                // 过滤已经被标示删除实例.
                long[] targetIds = Arrays.stream(entities).filter(e -> !e.isDeleted()).mapToLong(e -> e.id()).toArray();

                List<IEntity> targetEntities = new ArrayList(masterStorage.selectMultiple(targetIds));

                if (targetEntities.isEmpty()) {

                    if (logger.isDebugEnabled()) {
                        logger.debug("[deletes] None of the targets that need to be delete exist.");
                    }

                    return OqsResult.success();
                }

                String[] lockResource =
                    targetEntities.stream().map(e -> IEntitys.resource(e.id())).toArray(String[]::new);

                boolean lockResult = resourceLocker.tryLocks(lockTimeoutMs, lockResource);
                if (!lockResult) {

                    if (logger.isDebugEnabled()) {
                        logger.debug("[deletes] Failed to lock the instance.");
                    }

                    return OqsResult.conflict();
                }

                try {
                    IEntity targetEntity;
                    IEntityClass entityClass;

                    EntityPackage entityPackage = new EntityPackage();
                    for (int i = 0; i < targetEntities.size(); i++) {
                        targetEntity = targetEntities.get(i);
                        setValueChange(calculationContext, null, targetEntity);
                        entityClass = entityClassTable.get(targetEntity.entityClassRef().getId());
                        entityPackage.put(targetEntity, entityClass, false);
                    }
                    // 主操作
                    masterStorage.delete(entityPackage);

                    for (int i = 0; i < entityPackage.size(); i++) {
                        targetEntity = entityPackage.getNotSafe(i).getKey();

                        if (!targetEntity.isDeleted()) {

                            if (logger.isDebugEnabled()) {
                                logger.debug("[deletes] Failed to deletes instance ({}).", targetEntity.id());
                            }

                            tx.getHint().setRollback(true);
                            return OqsResult.unDeleted(targetEntity.id());
                        }

                        if (!tx.getAccumulator().accumulateBuild(targetEntity)) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("[deletes] Transaction accumulator processing failed.");
                            }
                            tx.getHint().setRollback(true);
                            return OqsResult.unAccumulate();
                        }

                        calculationContext.focusSourceEntity(targetEntity);
                        calculationContext.focusEntity(targetEntity, entityPackage.getNotSafe(i).getValue());
                        calculation.maintain(calculationContext);

                    }

                    if (!calculationContext.persist()) {

                        if (logger.isDebugEnabled()) {
                            logger.debug("[deletes] Failed to persist the calculated field maintenance result.");
                        }

                        tx.getHint().setRollback(true);
                        return OqsResult.conflict("Conflict maintenance.");
                    }

                } finally {
                    resourceLocker.unlocks(lockResource);
                }

                Arrays.stream(entities).forEach(e -> e.delete());

                tx.getHint().setCanWaitCommitSync(false);

                DeletePayload deletePayload =
                    new DeletePayload(tx.id(), targetEntities.stream().toArray(IEntity[]::new));
                eventBus.notify(new ActualEvent(EventType.ENTITY_DELETE, deletePayload));

                return OqsResult.success(deletePayload.getEntities());

            });

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            failCountTotal.increment(entities.length);

            throw ex;

        } finally {

            if (calculationContext != null) {
                calculationContext.destroy();
            }

            deleteCountTotal.increment(entities.length);
        }

        for (IEntity entity : entities) {
            entity.delete();
        }

        return oqsResult;
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        percentiles = {0.5, 0.9, 0.99},
        extraTags = {"initiator", "all", "action", "delete"})
    @Override
    public OqsResult<IEntity> delete(IEntity entity) throws SQLException {
        checkReady();

        Optional<IEntityClass> entityClassOp = metaManager.load(entity.entityClassRef());
        IEntityClass entityClass;
        if (!entityClassOp.isPresent()) {
            if (logger.isDebugEnabled()) {
                logger.debug("[deletes] Unable to find meta information {}.", entity.entityClassRef());
            }
            return OqsResult.notExistMeta(entity.entityClassRef());
        } else {
            entityClass = entityClassOp.get();
        }

        if (entity.isDeleted()) {
            if (logger.isDebugEnabled()) {
                logger.debug("[delete] Instance ({}) has been deleted.", entity.id());
            }
            return OqsResult.success();
        }

        markTime(entity);

        CalculationContext calculationContext = buildCalculationContext(CalculationScenarios.DELETE);
        try {
            return (OqsResult) transactionExecutor.execute((tx, resource) -> {

                Optional<IEntity> targetEntityOp = masterStorage.selectOne(entity.id(), entityClass);
                if (!targetEntityOp.isPresent()) {
                    return OqsResult.notFound();
                }

                IEntity targetEntity = targetEntityOp.get();

                String lockResource = IEntitys.resource(targetEntity.id());
                boolean lockResult = resourceLocker.tryLock(lockTimeoutMs, lockResource);
                if (!lockResult) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[delete] Failed to lock the instance.");
                    }
                    return OqsResult.conflict();
                }

                try {
                    /*
                    删除时计算字段不需要计算,只需要进行维护.
                    */
                    calculationContext.focusTx(tx);
                    calculationContext.focusSourceEntity(targetEntity);
                    calculationContext.focusEntity(targetEntity, entityClass);
                    setValueChange(calculationContext, null, targetEntity);

                    // 主操作
                    masterStorage.delete(targetEntity, entityClass);
                    if (!targetEntity.isDeleted()) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("[delete] Failed to deletes instance ({}).", targetEntity.id());
                        }
                        tx.getHint().setRollback(true);
                        return OqsResult.unDeleted(targetEntity.id());
                    }

                    if (!tx.getAccumulator().accumulateDelete(targetEntity)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("[delete] Transaction accumulator processing failed.");
                        }
                        tx.getHint().setRollback(true);
                        return OqsResult.unAccumulate();
                    }

                    // 维护必须在主操作之后.
                    calculation.maintain(calculationContext);

                    if (!calculationContext.persist()) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("[delete] Failed to persist the calculated field maintenance result.");
                        }

                        tx.getHint().setRollback(true);
                        return OqsResult.conflict("Conflict maintenance.");
                    }

                } finally {
                    resourceLocker.unlock(lockResource);
                }

                entity.delete();

                tx.getHint().setCanWaitCommitSync(true);

                eventBus.notify(new ActualEvent(
                    EventType.ENTITY_DELETE,
                    new DeletePayload(tx.id(), targetEntity)));

                return OqsResult.success(entity);
            });
        } catch (Exception ex) {

            logger.error(ex.getMessage(), ex);

            failCountTotal.increment();

            throw ex;

        } finally {

            // 保证必须清理
            if (calculationContext != null) {
                calculationContext.destroy();
            }

            deleteCountTotal.increment();
        }
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        percentiles = {0.5, 0.9, 0.99},
        extraTags = {"initiator", "all", "action", "deleteforces"}
    )
    @Deprecated
    @Override
    public OqsResult<IEntity[]> deleteForce(IEntity[] entities) throws SQLException {

        return delete(entities);
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        percentiles = {0.5, 0.9, 0.99},
        extraTags = {"initiator", "all", "action", "deleteforce"}
    )
    @Deprecated
    @Override
    public OqsResult<IEntity> deleteForce(IEntity entity) throws SQLException {

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
    private OqsResult transformVerifierResultToOperationResult(
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

        switch (verify.getKey()) {
            case REQUIRED:
                return OqsResult.fieldMust(field);
            case TOO_LONG:
                return OqsResult.fieldTooLong(field);
            case HIGH_PRECISION:
                return OqsResult.fieldHighPrecision(field);
            case NON_EXISTENT:
                return OqsResult.fieldNonExist(field);
            default:
                return OqsResult.unknown();
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
            .withLockTimeroutMs(this.lockTimeoutMs)
            .withCalculationLogicFactory(this.calculationLogicFactory)
            .build();
    }

    /**
     * 设置当前操作造成的值改变.
     *
     * @param context   当前计算上下文.
     * @param newEntity 变更后的实例,没有需要设置为null.
     * @param oldEntity 变更前的实例,没有需要设置为null.
     */
    private void setValueChange(
        CalculationContext context, IEntity newEntity, IEntity oldEntity) {
        if (newEntity == null && oldEntity == null) {
            return;
        }

        if (newEntity != null && oldEntity == null) {
            // build
            newEntity.entityValue().scan(v -> {
                IEntityField field = v.getField();
                context.addValueChange(ValueChange.build(newEntity.id(), new EmptyTypedValue(field), v));
            });
        } else if (newEntity == null && oldEntity != null) {
            // delete
            oldEntity.entityValue().scan(v -> {
                IEntityField field = v.getField();
                context.addValueChange(ValueChange.build(oldEntity.id(), v, new EmptyTypedValue(field)));
            });
        } else {
            // replace
            IValue oldValue;
            IEntityField field;
            for (IValue newValue : newEntity.entityValue().values()) {
                field = newValue.getField();
                oldValue = oldEntity.entityValue().getValue(field.id()).orElse(new EmptyTypedValue(field));

                if (newValue.equals(oldValue)) {
                    continue;
                } else {
                    context.addValueChange(ValueChange.build(oldEntity.id(), oldValue, newValue));
                }
            }
        }
    }

    // 批量预检.
    private OqsResult preview(IEntity[] entities, IEntityClass[] entityClasses, boolean build)
        throws SQLException {
        if (entities.length != entityClasses.length) {
            return OqsResult.notExistMeta();
        }

        OqsResult result = OqsResult.success();
        for (int i = 0; i < entities.length; i++) {
            result = preview(entities[i], entityClasses[i], build);

            if (!result.isSuccess()) {
                return result;
            }
        }

        return result;
    }

    // 预检
    private OqsResult preview(IEntity entity, IEntityClass entityClass, boolean build) {
        if (entity == null) {

            if (logger.isDebugEnabled()) {
                logger.debug("Entity verification failure: Null value.");
            }

            return OqsResult.notFound();
        }

        // 不合式的实例.
        if (entity.entityClassRef() == null
            || entity.entityClassRef().getId() <= 0
            || entity.entityClassRef().getCode() == null) {

            if (logger.isDebugEnabled()) {
                logger.debug("Entity is invalid and object information is missing.");
            }

            return OqsResult.notExistMeta();
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

                if (logger.isDebugEnabled()) {
                    logger.debug("Unable to find field ({}) in object meta-information ({}).",
                        entityClass.code(), field.fieldName().toString());
                }

                return OqsResult.fieldNonExist(field);
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

        return OqsResult.success();
    }

    private void markTime(IEntity entity) {
        if (entity.time() <= 0) {
            entity.markTime(System.currentTimeMillis());
        }
    }
}
