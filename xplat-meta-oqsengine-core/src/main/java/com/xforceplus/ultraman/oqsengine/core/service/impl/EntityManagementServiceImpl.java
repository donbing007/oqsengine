package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.calculate.CalculateStorage;
import com.xforceplus.ultraman.oqsengine.calculate.dto.ExecutionWrapper;
import com.xforceplus.ultraman.oqsengine.calculate.dto.ExpressionWrapper;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.common.mode.OqsMode;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.common.serializable.SerializeUtils;
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
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.Calculator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.FormulaTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.verifier.ValueVerifier;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.verifier.VerifierFactory;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.verifier.VerifierResult;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IValueUtils;
import com.xforceplus.ultraman.oqsengine.status.CDCStatusService;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.ErrorStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private CalculateStorage calculateStorage;


    @Resource
    private BizIDGenerator bizIDGenerator;


    private VerifierFactory verifierFactory;

    /*
    只读的原因.
     */
    enum ReadOnleyModeRease {
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

        Map<String, String> failedMap = null;
        try {
            failedMap = prepareBuild(entityClass, entity);
        } catch (Exception e) {
            String message = e.toString();
            logger.warn("prepare build error, message [{}]", message);
            return new OperationResult(
                0, entity.id(), UN_KNOW_VERSION, EventType.ENTITY_BUILD.getValue(),
                ResultStatus.ELEVATEFAILED, message);
        }

        Map.Entry<VerifierResult, IEntityField> verifyResult = verifyFields(entityClass, entity);
        if (VerifierResult.OK != verifyResult.getKey()) {
            // 表示有些校验不通过.
            return new OperationResult(0, entity.id(), UN_KNOW_VERSION, EventType.ENTITY_BUILD.getValue(),
                transformVerifierResultToReusltStatus(verifyResult.getKey()),
                instantiateMessage(verifyResult.getKey(), verifyResult.getValue()));
        }

        OperationResult operationResult = null;
        try {
            operationResult = (OperationResult) transactionExecutor.execute((tx, resource, hint) -> {

                if (entity.id() <= 0) {
                    long newId = idGenerator.next();
                    entity.resetId(newId);
                }
                entity.resetVersion(0);
                entity.restMaintainId(0);

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

            if (null != failedMap && !failedMap.isEmpty()) {
                operationResult.resetStatus(failedMap);
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

                /*
                    执行自动编号、公式字段计算
                 */
                Map<String, String> failedMap = null;
                try {
                    failedMap = prepareReplace(entityClass, targetEntity, entity);
                } catch (Exception e) {
                    String message = e.toString();
                    logger.warn("prepare replace error, message [{}]", message);
                    return new OperationResult(
                        tx.id(), entity.id(), UN_KNOW_VERSION, EventType.ENTITY_REPLACE.getValue(),
                        ResultStatus.ELEVATEFAILED, message);
                }

                // 操作时间
                targetEntity.markTime(entity.time());

                Map.Entry<VerifierResult, IEntityField> verifyResult = verifyFields(entityClass, entity);
                if (VerifierResult.OK != verifyResult.getKey()) {
                    hint.setRollback(true);
                    // 表示有些校验不通过.
                    return new OperationResult(0, entity.id(), UN_KNOW_VERSION, EventType.ENTITY_BUILD.getValue(),
                        transformVerifierResultToReusltStatus(verifyResult.getKey()),
                        instantiateMessage(verifyResult.getKey(), verifyResult.getValue()));
                }

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

                //  半成功
                if (null != failedMap && !failedMap.isEmpty()) {
                    return new OperationResult(tx.id(), entity.id(), targetEntity.version(),
                        EventType.ENTITY_REPLACE.getValue(), ResultStatus.HALF_SUCCESS, failedMap,
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
        IEntityClass entityClass = EntityClassHelper.checkEntityClass(metaManager, entity.entityClassRef());
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
            ValueVerifier verifier = verifierFactory.getVerifier(field.type());
            Optional<IValue> valueOp = entity.entityValue().getValue(field.id());
            result = verifier.verify(field, valueOp.orElse(null));
            if (VerifierResult.OK != result) {
                return new AbstractMap.SimpleEntry(result, field);
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
            default:
                return ResultStatus.UNKNOWN;
        }
    }

    private String instantiateMessage(VerifierResult verifierResult, IEntityField field) {
        if (logger.isDebugEnabled()) {
            logger.debug("Field {}({}) validation result {}, validation is based on {}.",
                field.name(), field.id(), verifierResult.name(), field.config().toString());
        }

        switch (verifierResult) {
            case REQUIRED:
                return String.format("The field %s is required.", field.name());
            case TOO_LONG:
                return String.format("The value of field %s is too long. The maximum acceptable length is %d.",
                    field.name(), field.config().getLen());
            case HIGH_PRECISION:
                return String.format("The accuracy of field %s is too high. The maximum accepted accuracy is %d.",
                    field.name(), field.config().getPrecision());
            default:
                return "Unknown validation failed.";
        }
    }


    //  build前的准备
    private Map<String, String> prepareBuild(IEntityClass entityClass, IEntity entity) {

        //  生成的新的entityValue
        IEntityValue entityValue = EntityValue.build();

        //  context
        Map<String, Object> context = toContext(entityClass, entity);

        //  需要进行计算的公式字段
        List<ExecutionWrapper<?>> executionWrappers = new ArrayList<>();

        entity.entityValue().values().forEach(
            v -> {
                IEntityField entityField = v.getField();
                //  自动填充
                if (entityField.calculateType().equals(Calculator.Type.AUTO_FILL)) {
                    Object result = bizIDGenerator.nextId(String.valueOf(entityField.id()));
                    if (null != result) {
                        context.put(entityField.name(), result);
                        entityValue.addValue(IValueUtils.toIValue(entityField, result));
                    }
                } else if (entityField.calculateType().equals(Calculator.Type.FORMULA)) {
                    addContextWrappers(v, context, executionWrappers);
                } else {
                    //  将没有置入context中的常量加入到计算中
                    context.putIfAbsent(entityField.name(), v.getValue());
                    entityValue.addValue(v);
                }
            }
        );

        if (logger.isDebugEnabled()) {
            logger.debug("before formula-elevator, entity-id :[{}], context : [{}].",
                entity.id(),
                context.entrySet().stream().map(Objects::toString).collect(Collectors.toList()));
        }

        //  计算公式字段
        Map<String, String> failedMaps
            = formulaElevator(entityValue, context, executionWrappers);

        if (logger.isDebugEnabled()) {
            logger.debug("after formula-elevator, entity-id :[{}], entityValue : [{}].",
                entity.id(), entityValue.values().toArray());
        }

        //  将entityValue加入到目标中
        entity.resetEntityValue(entityValue);

        return failedMaps;
    }

    //  replace前的准备
    private Map<String, String> prepareReplace(IEntityClass entityClass, IEntity targetEntity, IEntity updateEntity) {

        //  生成的新的entityValue
        IEntityValue entityValue = EntityValue.build();

        //  context
        Map<String, Object> context = toContext(entityClass, updateEntity);

        //  需要进行计算的公式字段
        List<ExecutionWrapper<?>> executionWrappers = new ArrayList<>();

        //  合并new
        updateEntity.entityValue().values().forEach(
            v -> {
                IEntityField entityField = v.getField();
                //  公式字段，v传入的类型应该为FormulaTypedValue-> v.getValue()为Map<String, Object>类型
                if (entityField.calculateType().equals(Calculator.Type.FORMULA)) {
                    addContextWrappers(v, context, executionWrappers);
                } else if (!entityField.calculateType().equals(Calculator.Type.AUTO_FILL)) {
                    //  加入新的entityValue中
                    entityValue.addValue(v);
                }
            }
        );

        //  将targetEntity中剩余entity加入进行计算
        targetEntity.entityValue().values().forEach(
            v -> {
                switch (v.getField().calculateType()) {
                    case AUTO_FILL: {
                        //  自动填充字段强制覆盖
                        entityValue.addValue(v);
                        context.put(v.getField().name(), v.getValue());
                        break;
                    }
                    case FORMULA: {
                        break;
                    }
                    default: {
                        //  当context不存在该值时写入
                        context.putIfAbsent(v.getField().name(), v.getValue());
                        if (!entityValue.getValue(v.getField().id()).isPresent()) {
                            entityValue.addValue(v);
                        }
                        break;
                    }
                }
            }
        );

        //  计算公式字段
        Map<String, String> failedMaps =
            formulaElevator(entityValue, context, executionWrappers);

        //  将entityValue加入到目标中
        targetEntity.resetEntityValue(entityValue);

        return failedMaps;
    }

    private void addContextWrappers(IValue<?> v, Map<String, Object> context,
                                    List<ExecutionWrapper<?>> executionWrappers) {
        if (!(v instanceof FormulaTypedValue)) {
            throw new IllegalArgumentException(
                "entityValue must be formulaTypedValue when calculateType equals [FORMULA].");
        }
        executionWrappers.add(initExecutionWrapper(v.getField()));

        //  公式字段，v传入的类型应该为FormulaTypedValue-> v.getValue()为Map<String, Object>类型
        Map<String, Object> local = (Map<String, Object>) v.getValue();
        if (null != local) {
            local.entrySet().stream().filter(
                entry -> {
                    return null != entry.getValue();
                }
            ).forEach(
                entry -> {
                    context.putIfAbsent(entry.getKey(), entry.getValue());
                }
            );
        }
    }

    private Map<String, String> formulaElevator(IEntityValue entityValue,
                                                Map<String, Object> context,
                                                List<ExecutionWrapper<?>> executionWrappers) {
        AbstractMap.SimpleEntry<List<IValue>, Map<String, String>> result =
            calculateStorage.execute(executionWrappers, context);
        if (null != result && null != result.getKey()) {
            result.getKey().forEach(entityValue::addValue);
        }

        return result.getValue();
    }

    private ExecutionWrapper<?> initExecutionWrapper(IEntityField entityField) {
        return ExecutionWrapper.Builder.anExecution()
            .withCode(entityField.name())
            .withRetClass(entityField.type().getJavaType())
            .witLevel(entityField.calculator().getLevel())
            .withIEntityField(entityField)
            .withExpressionWrapper(
                ExpressionWrapper.Builder.anExpression()
                    .withExpression(entityField.calculator().getExpression())
                    .withCached(true)
                    .build()
            ).build();
    }

    private Map<String, Object> toContext(IEntityClass entityClass, IEntity entity) {
        Map<String, Object> context = new HashMap<>();
        entityClass.fields().forEach(
            entityField -> {
                if (entityField.calculateType().equals(Calculator.Type.NORMAL)) {
                    entity.entityValue().getValue(entityField.id())
                        .ifPresent(
                            value -> {
                                //  context只加入有值的数据
                                if (!(value instanceof EmptyTypedValue) && null != value.getValue()) {
                                    context.putIfAbsent(entityField.name(), value.getValue());
                                }
                            });
                }
            }
        );

        return context;
    }

    private void handleHalfSuccessOrRecover(long maintainId, long entityClassId, OperationResult operationResult) {
        if (null != operationResult && (maintainId > 0 && operationResult.getResultStatus().equals(ResultStatus.SUCCESS)
            || operationResult.getResultStatus().equals(ResultStatus.HALF_SUCCESS))) {

            String errors = "serialize errors failed.";
            try {
                errors = SerializeUtils.OBJECT_MAPPER.writeValueAsString(operationResult.getFailedMap());
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
}
