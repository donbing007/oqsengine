package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.calculate.FormulaStorage;
import com.xforceplus.ultraman.oqsengine.calculate.dto.ExecutionWrapper;
import com.xforceplus.ultraman.oqsengine.calculate.dto.ExpressionWrapper;
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
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculateType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.BooleanValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EnumValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.FormulaTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.status.CDCStatusService;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @Resource
    private FormulaStorage formulaStorage;

    private static final int UN_KNOW_VERSION = -1;
    private static final int BUILD_VERSION = 0;
    private static final int INCREMENT_POS = 1;
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
                    setReadOnlyMode("CDC heartbeat test failed.");
                    return;
                }

                long uncommentSize = commitIdStatusService.size();
                if (uncommentSize > allowMaxUnSyncCommitIdSize) {
                    setReadOnlyMode(
                        String.format("Not synchronizing the submission number over %d.", allowMaxUnSyncCommitIdSize));
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
                        setReadOnlyMode(String.format("CDC status is %s.", cdcStatus.name()));
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

        verify(entity);

        markTime(entity);

        IEntityClass entityClass = EntityClassHelper.checkEntityClass(metaManager, entity.entityClassRef());

        try {
            return (OperationResult) transactionExecutor.execute((tx, resource, hint) -> {

                if (entity.id() <= 0) {
                    long newId = idGenerator.next();
                    entity.resetId(newId);
                }
                entity.resetVersion(0);
                entity.restMaintainId(0);

                /*
                    执行自动编号、公式字段计算
                 */
                try {
                    prepareBuild(entity);
                } catch (Exception e) {
                    logger.warn("prepare build error, message [{}]", e.toString());
                    return new OperationResult(
                        tx.id(), entity.id(), UN_KNOW_VERSION, EventType.ENTITY_BUILD.getValue(),
                        ResultStatus.ELEVATEFAILED);
                }

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

                return new OperationResult(tx.id(), entity.id(), BUILD_VERSION, EventType.ENTITY_BUILD.getValue(),
                    ResultStatus.SUCCESS);
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
    public OperationResult replace(IEntity entity) throws SQLException {
        checkReady();

        verify(entity);

        markTime(entity);

        IEntityClass entityClass = EntityClassHelper.checkEntityClass(metaManager, entity.entityClassRef());

        try {
            return (OperationResult) transactionExecutor.execute((tx, resource, hint) -> {

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

                /*
                    执行自动编号、公式字段计算
                 */
                try {
                    prepareReplace(targetEntity, entity);
                } catch (Exception e) {
                    logger.warn("prepare replace error, message [{}]", e.toString());
                    return new OperationResult(
                        tx.id(), entity.id(), UN_KNOW_VERSION, EventType.ENTITY_REPLACE.getValue(),
                        ResultStatus.ELEVATEFAILED);
                }

                // 操作时间
                targetEntity.markTime(entity.time());

                if (isConflict(masterStorage.replace(targetEntity, entityClass))) {
                    hint.setRollback(true);
                    return new OperationResult(
                        tx.id(), entity.id(), UN_KNOW_VERSION, EventType.ENTITY_REPLACE.getValue(),
                        ResultStatus.CONFLICT);
                }

                //  这里将版本+1，使得外部获取的版本为当前成功版本
                targetEntity.resetVersion(targetEntity.version() + INCREMENT_POS);

                if (!tx.getAccumulator().accumulateReplace(targetEntity, oldEntity)) {
                    hint.setRollback(true);
                    return new OperationResult(
                        tx.id(), entity.id(), UN_KNOW_VERSION, EventType.ENTITY_REPLACE.getValue(),
                        ResultStatus.UNACCUMULATE);
                }

                noticeEvent(tx, EventType.ENTITY_REPLACE, entity);

                return new OperationResult(tx.id(), entity.id(), targetEntity.version(),
                    EventType.ENTITY_REPLACE.getValue(), ResultStatus.SUCCESS);
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

                if (isConflict(masterStorage.delete(targetEntityOp.orElse(entity), entityClass))) {
                    hint.setRollback(true);
                    return new OperationResult(
                        tx.id(), entity.id(), UN_KNOW_VERSION, EventType.ENTITY_DELETE.getValue(),
                        ResultStatus.CONFLICT);
                }

                if (!tx.getAccumulator().accumulateDelete(targetEntityOp.get())) {
                    hint.setRollback(true);
                    return new OperationResult(
                        tx.id(), entity.id(), UN_KNOW_VERSION, EventType.ENTITY_DELETE.getValue(),
                        ResultStatus.UNACCUMULATE);
                }

                noticeEvent(tx, EventType.ENTITY_DELETE, targetEntityOp.get());

                return new OperationResult(
                    tx.id(), entity.id(), targetEntityOp.get().version(), EventType.ENTITY_DELETE.getValue(),
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

    // 校验
    private void verify(IEntity entity) throws SQLException {
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

    //  build前的准备
    private void prepareBuild(IEntity entity) {

        //  生成的新的entityValue
        IEntityValue entityValue = EntityValue.build();

        //  context
        Map<String, Object> context = new HashMap<>();

        //  需要进行计算的公式字段
        List<ExecutionWrapper<?>> executionWrappers = new ArrayList<>();

        entity.entityValue().values().forEach(
            v -> {
                IEntityField entityField = v.getField();
                //  自动填充
                if (entityField.calculateType().equals(CalculateType.AUTO_FILL)) {
                    //  todo 计算自动填充值并写入context中
                    Object result = null;

                    if (null != result) {
                        context.put(entityField.name(), result);
                        entityValue.addValue(toIValue(entityField, result));
                    }
                } else if (entityField.calculateType().equals(CalculateType.FORMULA)) {
                    addContextWrappers(v, context, executionWrappers);
                }
            }
        );

        //  计算公式字段
        formulaElevator(entity, entityValue, context, executionWrappers);

        //  将entityValue加入到目标中
        entity.entityValue().addValues(entityValue.values());
    }

    //  replace前的准备
    private void prepareReplace(IEntity targetEntity, IEntity updateEntity) {

        //  生成的新的entityValue
        IEntityValue entityValue = EntityValue.build();

        //  context
        Map<String, Object> context = new HashMap<>();

        //  需要进行计算的公式字段
        List<ExecutionWrapper<?>> executionWrappers = new ArrayList<>();

        //  合并new
        updateEntity.entityValue().values().forEach(
            v -> {
                IEntityField entityField = v.getField();

                //  公式字段，v传入的类型应该为FormulaTypedValue-> v.getValue()为Map<String, Object>类型
                if (entityField.calculateType().equals(CalculateType.FORMULA)) {
                    addContextWrappers(v, context, executionWrappers);
                } else {
                    //  加入新的entityValue中
                    entityValue.addValue(v);
                }
            }
        );

        //  将targetEntity中剩余entity加入进行计算
        targetEntity.entityValue().values().forEach(
            v -> {
                //  old字段中所有的公式字段都不会参与replace计算
                if (!v.getField().calculateType().equals(CalculateType.FORMULA)) {
                    //  当context中不存在该key时，加入,不使用putIfAbsent的原因是允许空值
                    if (!context.containsKey(v.getField().name())) {
                        context.put(v.getField().name(), v.getValue());
                    }
                }
            }
        );

        //  计算公式字段
        formulaElevator(targetEntity, entityValue, context, executionWrappers);

        //  将entityValue加入到目标中
        targetEntity.entityValue().addValues(entityValue.values());
    }

    private void addContextWrappers(IValue<?> v, Map<String, Object> context, List<ExecutionWrapper<?>> executionWrappers) {
        if (!(v instanceof FormulaTypedValue)) {
            throw new IllegalArgumentException(
                "entityValue must be formulaTypedValue when calculateType equals [FORMULA].");
        }
        executionWrappers.add(initExecutionWrapper(v.getField()));

        //  公式字段，v传入的类型应该为FormulaTypedValue-> v.getValue()为Map<String, Object>类型
        Map<String, Object> local = (Map<String, Object>) v.getValue();
        if (null != local) {
            context.putAll(local);
        }
    }

    private void formulaElevator(IEntity entity, IEntityValue entityValue,
                                                Map<String, Object> context, List<ExecutionWrapper<?>> executionWrappers) {
        Map<String, Object> result = formulaStorage.execute(executionWrappers, context);
        if (null != result) {
            entity.entityValue().values().forEach(
                v -> {
                    IEntityField e = v.getField();
                    if (e.calculateType().equals(CalculateType.FORMULA)) {
                        Object o = result.get(e.name());
                        if (null != o) {
                            entityValue.addValue(toIValue(e, o));
                        }
                    }
                }
            );
        }

    }

    private IValue<?> toIValue(IEntityField field, Object result) {
        switch (field.type()) {
            case BOOLEAN : {
                return new BooleanValue(field, (Boolean) result);
            }
            case ENUM: {
                return new EnumValue(field, (String) result);
            }
            case DATETIME: {
                return new DateTimeValue(field, (LocalDateTime) result);
            }
            case LONG: {
                return new LongValue(field, (Long) result);
            }
            case STRING: {
                return new StringValue(field, (String) result);
            }
            case STRINGS: {
                return new StringsValue(field, (String[]) result);
            }
            case DECIMAL: {
                return new DecimalValue(field, (BigDecimal) result);
            }
            default: {
                throw new IllegalArgumentException("unknown field type.");
            }
        }
    }

    private ExecutionWrapper<?> initExecutionWrapper(IEntityField entityField) {
        return ExecutionWrapper.Builder.anExecution()
            .withCode(entityField.name())
            .withRetClass(entityField.type().getJavaType())
            .witLevel(entityField.calculator().getLevel())
            .withExpressionWrapper(
                ExpressionWrapper.Builder.anExpression()
                    .withExpression(entityField.calculator().getExpression())
                    .withCached(true)
                    .build()
            ).build();
    }

}
