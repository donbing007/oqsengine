package com.xforceplus.ultraman.oqsengine.boot.grpc.service;

import static com.xforceplus.ultraman.oqsengine.boot.grpc.utils.ConditionHelper.toConditions;
import static com.xforceplus.ultraman.oqsengine.boot.grpc.utils.EntityClassHelper.toEntity;
import static com.xforceplus.ultraman.oqsengine.boot.grpc.utils.EntityClassHelper.toEntityField;
import static com.xforceplus.ultraman.oqsengine.boot.grpc.utils.EntityClassHelper.toEntityUp;
import static com.xforceplus.ultraman.oqsengine.boot.grpc.utils.EntityClassHelper.toOperationResult;
import static com.xforceplus.ultraman.oqsengine.boot.grpc.utils.MessageDecorator.err;
import static com.xforceplus.ultraman.oqsengine.boot.grpc.utils.MessageDecorator.notFound;
import static com.xforceplus.ultraman.oqsengine.boot.grpc.utils.MessageDecorator.ok;
import static com.xforceplus.ultraman.oqsengine.boot.grpc.utils.MessageDecorator.other;
import static com.xforceplus.ultraman.oqsengine.core.service.TransactionManagementService.DEFAULT_TRANSACTION_TIMEOUT;
import static com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus.HALF_SUCCESS;
import static com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus.NOT_FOUND;
import static com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus.SUCCESS;
import static com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus.UNKNOWN;

import akka.NotUsed;
import akka.grpc.javadsl.Metadata;
import akka.stream.javadsl.Source;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ProtocolStringList;
import com.xforceplus.ultraman.oqsengine.boot.grpc.utils.EntityClassHelper;
import com.xforceplus.ultraman.oqsengine.calculation.dto.ErrorCalculateInstance;
import com.xforceplus.ultraman.oqsengine.changelog.ReplayService;
import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangeVersion;
import com.xforceplus.ultraman.oqsengine.changelog.domain.EntityAggDomain;
import com.xforceplus.ultraman.oqsengine.changelog.domain.EntityDomain;
import com.xforceplus.ultraman.oqsengine.changelog.storage.query.QueryStorage;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.TransactionManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.OqsResult;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.ServiceSelectConfig;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.Hint;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.AbstractCalculation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Lookup;
import com.xforceplus.ultraman.oqsengine.pojo.dto.select.BusinessKey;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LookupValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.ValueWithEmpty;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.sdk.ChangelogCountRequest;
import com.xforceplus.ultraman.oqsengine.sdk.ChangelogCountResponse;
import com.xforceplus.ultraman.oqsengine.sdk.ChangelogCountSingle;
import com.xforceplus.ultraman.oqsengine.sdk.ChangelogRequest;
import com.xforceplus.ultraman.oqsengine.sdk.ChangelogResponse;
import com.xforceplus.ultraman.oqsengine.sdk.ChangelogResponseList;
import com.xforceplus.ultraman.oqsengine.sdk.CompatibleRequest;
import com.xforceplus.ultraman.oqsengine.sdk.ConditionsUp;
import com.xforceplus.ultraman.oqsengine.sdk.DryRunFieldsInfo;
import com.xforceplus.ultraman.oqsengine.sdk.DryRunInstanceInfo;
import com.xforceplus.ultraman.oqsengine.sdk.DryRunResult;
import com.xforceplus.ultraman.oqsengine.sdk.EntityMultiUp;
import com.xforceplus.ultraman.oqsengine.sdk.EntityServicePowerApi;
import com.xforceplus.ultraman.oqsengine.sdk.EntityUp;
import com.xforceplus.ultraman.oqsengine.sdk.ErrorFieldUnit;
import com.xforceplus.ultraman.oqsengine.sdk.FieldConditionUp;
import com.xforceplus.ultraman.oqsengine.sdk.FieldSortUp;
import com.xforceplus.ultraman.oqsengine.sdk.Filters;
import com.xforceplus.ultraman.oqsengine.sdk.LockRequest;
import com.xforceplus.ultraman.oqsengine.sdk.LockResponse;
import com.xforceplus.ultraman.oqsengine.sdk.OperationResult;
import com.xforceplus.ultraman.oqsengine.sdk.QueryFieldsUp;
import com.xforceplus.ultraman.oqsengine.sdk.ReCalculateInfo;
import com.xforceplus.ultraman.oqsengine.sdk.ReplayRequest;
import com.xforceplus.ultraman.oqsengine.sdk.SelectByCondition;
import com.xforceplus.ultraman.oqsengine.sdk.SelectBySql;
import com.xforceplus.ultraman.oqsengine.sdk.SelectByTree;
import com.xforceplus.ultraman.oqsengine.sdk.SortNode;
import com.xforceplus.ultraman.oqsengine.sdk.TransRequest;
import com.xforceplus.ultraman.oqsengine.sdk.TransactionUp;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.synchronizer.server.LockStateService;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * grpc server.
 */
@Component
public class EntityServiceOqs implements EntityServicePowerApi {

    @Autowired(required = false)
    private EntityManagementService entityManagementService;

    @Autowired(required = false)
    private EntitySearchService entitySearchService;

    @Autowired(required = false)
    private TransactionManagementService transactionManagementService;

    private static final String ENTITYCLASS_NOT_FOUND = "Requested EntityClass not found in current OqsEngine";

    private static final String RESOURCE_IS_LOCKED = "Current Resource is locked";

    private static final String LOCK_HEADER = "lock-header";
    private static final String LOCK_TOKEN = "lock-token";

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MetaManager metaManager;

    @Resource(name = "ioThreadPool")
    private ExecutorService asyncDispatcher;

    @Autowired(required = false)
    private QueryStorage queryStorage;

    @Autowired(required = false)
    private ReplayService replayService;

    @Autowired(required = false)
    private LockStateService lockStateService;

    @Autowired
    private TransactionManager transactionManager;

    private long buffer = 10_000L;

    private Logger logger = LoggerFactory.getLogger(EntityServiceOqs.class);

    private <T> CompletableFuture<T> asyncRead(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, asyncDispatcher);
    }

    private <T> CompletableFuture<T> asyncWrite(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, asyncDispatcher);
    }

    private <T> CompletableFuture<T> asyncChangelog(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, asyncDispatcher);
    }

    @Override
    public CompletionStage<OperationResult> begin(TransactionUp in, Metadata metadata) {
        try {

            Optional<Integer> timeout = metadata.getText("timeout").map(Integer::parseInt);
            Optional<String> comment = metadata.getText("comment");
            long transId;

            if (timeout.isPresent() && timeout.get() > 0) {
                if (comment.isPresent()) {
                    transId = transactionManagementService.begin(timeout.get(), comment.get());
                } else {
                    transId = transactionManagementService.begin(timeout.get());
                }
            } else {
                if (comment.isPresent()) {
                    transId = transactionManagementService.begin(DEFAULT_TRANSACTION_TIMEOUT, comment.get());
                } else {
                    transId = transactionManagementService.begin(DEFAULT_TRANSACTION_TIMEOUT);
                }
            }

            return CompletableFuture.completedFuture(OperationResult.newBuilder()
                .setCode(OperationResult.Code.OK)
                .setTransactionResult(String.valueOf(transId)).buildPartial());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return CompletableFuture.completedFuture(
                OperationResult.newBuilder()
                    .setCode(OperationResult.Code.EXCEPTION)
                    .setMessage(Optional.ofNullable(e.getMessage()).orElseGet(e::toString))
                    .buildPartial());
        }
    }

    /**
     * checkout the entityClassRef.
     */
    private IEntityClass checkedEntityClassRef(EntityClassRef entityClassRef) {
        Optional<IEntityClass> entityClassOp = metaManager.load(entityClassRef.getId(), entityClassRef.getProfile());
        if (entityClassOp.isPresent()) {
            IEntityClass entityClass = entityClassOp.get();
            return entityClass;
        } else {
            throw new RuntimeException(ENTITYCLASS_NOT_FOUND);
        }
    }

    private OperationResult exceptional(Throwable throwable) {
        logger.error(throwable.getMessage(), throwable);
        //fast fail
        return OperationResult.newBuilder()
            .setCode(OperationResult.Code.EXCEPTION)
            .setMessage(Optional.ofNullable(throwable.getMessage()).orElseGet(throwable::toString))
            .buildPartial();
    }

    /**
     * auto fill lookup fields.
     */
    private void autoFillLookUp(IEntity entity, IEntityClass entityClass) {

        /*
         * find all relation in this entityClass
         */
        Collection<Relationship> relationships = entityClass.relationship();

        /*
         * find all related values
         * caution value can be empty !
         * relationId -> value
         * TODO
         */
        Map<Long, Object> relationValueMapping = entity.entityValue().values()
            .stream().map(val -> {
                /*
                 * TODO
                 * cuz currently we have no reference related field
                 * all the field is generated by the relation
                 * once the reference related field is done
                 * we should change the logic here to use another way to find out the relation
                 */
                Optional<Relationship> relationship = relationships.stream().filter(x -> {
                    Relationship.RelationType relationType = x.getRelationType();
                    //only deal with toOne relation
                    return relationType == Relationship.RelationType.MANY_TO_ONE
                        || relationType == Relationship.RelationType.ONE_TO_ONE;
                }).filter(rel -> {
                    long id = rel.getEntityField().id();
                    return val.getField().id() == id;
                }).findFirst();

                return relationship.map(value -> Tuple.of(value, val.getValue())).orElse(null);
            }).filter(Objects::nonNull).collect(Collectors.toMap(x -> x._1.getId(), Tuple2::_2, (a, b) -> {
                logger.error("Should not merge here " + a + " " + b);
                return b;
            }));

        /*
         * remove field
         */
        List<IEntityField> removeList = new ArrayList<>();

        /*
         * add value
         */
        List<IValue> updatedValue = new ArrayList<>();

        /*
         * find all related
         */
        entityClass.fields().forEach(field -> {
            if (field.calculationType() != null) {
                CalculationType calculationType = field.calculationType();
                if (calculationType == CalculationType.LOOKUP) {

                    AbstractCalculation calculation = field.config().getCalculation();
                    Lookup lookup = (Lookup) calculation;

                    long relationId = lookup.getRelationId();

                    boolean containsKey = relationValueMapping.containsKey(relationId);

                    if (containsKey) {
                        Object o = relationValueMapping.get(relationId);
                        if (o == null) {
                            //remove
                            removeList.add(field);
                        } else {
                            try {
                                if (ValueWithEmpty.isEmpty(o.toString())) {
                                    updatedValue.add(new EmptyTypedValue(field));
                                } else {
                                    long id = Long.parseLong(o.toString());
                                    updatedValue.add(new LookupValue(field, id));
                                }

                            } catch (Exception ex) {
                                logger.error("{}", ex);
                            }
                        }
                    }
                }
            }
        });

        /*
         * add
         */
        updatedValue.forEach(x -> {
            entity.entityValue().addValue(x);
        });

        /*
         * remove
         */
        removeList.forEach(x -> {
            entity.entityValue().remove(x);
        });
    }

    /**
     * create.
     */
    @Override
    public CompletionStage<OperationResult> build(EntityUp in, Metadata metadata) {
        return asyncWrite(() -> {

            String profile = extractProfile(metadata).orElse("");

            //check entityRef
            EntityClassRef entityClassRef = EntityClassHelper.toEntityClassRef(in, profile);
            IEntityClass entityClass;
            try {
                entityClass = checkedEntityClassRef(entityClassRef);
            } catch (Exception ex) {
                return exceptional(ex);
            }

            boolean success;
            try {
                success = tryRestoreTransaction(metadata);
            } catch (Exception ex) {
                return exceptional(ex);
            }
            if (!success) {
                OperationResult.newBuilder()
                    .setAffectedRow(0)
                    .setCode(OperationResult.Code.FAILED)
                    .setMessage("Restore transaction failure, transaction may have timed out.")
                    .buildPartial();
            }

            OperationResult result;

            try {
                IEntity entity = toEntity(entityClassRef, entityClass, in);

                /*
                 * do auto fill
                 */
                autoFillLookUp(entity, entityClass);

                OqsResult<IEntity> oqsResult = entityManagementService.build(entity);
                ResultStatus createStatus = oqsResult.getResultStatus();
                Optional<IEntity> valueOp = oqsResult.getValue();


                if (createStatus == null) {
                    result = OperationResult.newBuilder()
                        .setAffectedRow(0)
                        .setCode(OperationResult.Code.EXCEPTION)
                        .setOriginStatus(UNKNOWN.name())
                        .setMessage(
                            other("Unknown response status"))
                        .buildPartial();
                } else {
                    switch (createStatus) {
                        case SUCCESS: {
                            OperationResult.Builder builder = OperationResult
                                .newBuilder()
                                .addIds(entity.id())
                                .setCode(OperationResult.Code.OK)
                                .setOriginStatus(SUCCESS.name());

                            //add ret value
                            valueOp.ifPresent(value -> builder.addQueryResult(toEntityUp(value)));

                            result = builder.buildPartial();
                            break;
                        }
                        case HALF_SUCCESS: {
                            Map<String, String> failedMap = hintsToFails(oqsResult.getHints());
                            String failedValues = "";
                            try {
                                failedValues = mapper.writeValueAsString(failedMap);
                            } catch (Exception ex) {
                                logger.error("{}", ex);
                            }

                            OperationResult.Builder builder = OperationResult
                                .newBuilder()
                                .addIds(entity.id())
                                .setCode(OperationResult.Code.OTHER)
                                .setMessage(failedValues)
                                .setOriginStatus(HALF_SUCCESS.name());

                            //add ret value
                            valueOp.ifPresent(value -> builder.addQueryResult(toEntityUp(value)));

                            result = builder.buildPartial();
                            break;
                        }
                        case ELEVATEFAILED:
                        case FIELD_MUST:
                        case FIELD_TOO_LONG:
                        case FIELD_HIGH_PRECISION:
                        case FIELD_NON_EXISTENT:
                            result = OperationResult.newBuilder()
                                .setAffectedRow(0)
                                .setCode(OperationResult.Code.FAILED)
                                .setOriginStatus(createStatus.name())
                                .setMessage(oqsResult.getMessage())
                                .buildPartial();
                            break;
                        default:
                            result = OperationResult.newBuilder()
                                .setAffectedRow(0)
                                .setCode(OperationResult.Code.FAILED)
                                .setOriginStatus(createStatus.name())
                                .setMessage(
                                    other(String.format("Unknown response status %s.", createStatus.name())))
                                .buildPartial();
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.EXCEPTION)
                    .setMessage(Optional.ofNullable(e.getMessage()).orElseGet(e::toString))
                    .buildPartial();
            } finally {
                extractTransaction(metadata).ifPresent(id -> {
                    transactionManager.unbind();
                });
            }
            return result;
        });
    }

    /**
     * build multi.
     *
     * @param in       data.
     * @param metadata meta data.
     * @return ss
     */
    @Override
    public CompletionStage<OperationResult> buildMulti(EntityMultiUp in, Metadata metadata) {
        return asyncWrite(() -> {

            String profile = extractProfile(metadata).orElse("");

            //check entityRef
            EntityClassRef entityClassRef = EntityClassHelper.toEntityClassRef(in, profile);
            IEntityClass entityClass;
            try {
                entityClass = checkedEntityClassRef(entityClassRef);
            } catch (Exception ex) {
                return exceptional(ex);
            }

            boolean success;
            try {
                success = tryRestoreTransaction(metadata);
            } catch (Exception ex) {
                return exceptional(ex);
            }
            if (!success) {
                OperationResult.newBuilder()
                    .setAffectedRow(0)
                    .setCode(OperationResult.Code.FAILED)
                    .setMessage("Restore transaction failure, transaction may have timed out.")
                    .buildPartial();
            }

            OperationResult result;

            try {
                List<IEntity> entityList =
                    toEntity(entityClassRef, entityClass, in);

                // do auto fill
                entityList.forEach(e -> autoFillLookUp(e, entityClass));

                OqsResult<IEntity[]> oqsResult = entityManagementService.build(entityList.toArray(new IEntity[0]));
                ResultStatus createStatus = oqsResult.getResultStatus();
                Optional<IEntity[]> valueOp = oqsResult.getValue();
                switch (createStatus) {
                    case SUCCESS: {

                        OperationResult.Builder resultBuilder = OperationResult.newBuilder();
                        resultBuilder.setCode(OperationResult.Code.OK)
                            .setAffectedRow(entityList.size())
                            .setOriginStatus(SUCCESS.name());
                        entityList.forEach(e -> resultBuilder.addIds(e.id()));

                        //add values
                        valueOp.ifPresent(value -> {
                            resultBuilder.addAllQueryResult(Arrays
                                .stream(value)
                                .map(EntityClassHelper::toEntityUp)
                                .collect(
                                    Collectors.toList()));
                        });

                        result = resultBuilder.buildPartial();
                        break;
                    }
                    case HALF_SUCCESS: {

                        Map<String, String> failedMap = hintsToFails(oqsResult.getHints());
                        String failedValues = "";
                        try {
                            failedValues = mapper.writeValueAsString(failedMap);
                        } catch (Exception ex) {
                            logger.error("{}", ex);
                        }

                        OperationResult.Builder resultBuilder = OperationResult.newBuilder();
                        resultBuilder.setCode(OperationResult.Code.OTHER)
                            .setMessage(failedValues)
                            .setAffectedRow(entityList.size())
                            .setOriginStatus(HALF_SUCCESS.name());
                        entityList.forEach(e -> resultBuilder.addIds(e.id()));

                        //add values
                        valueOp.ifPresent(value -> {
                            resultBuilder.addAllQueryResult(Arrays
                                .stream(value)
                                .map(EntityClassHelper::toEntityUp)
                                .collect(
                                    Collectors.toList()));
                        });

                        result = resultBuilder.buildPartial();
                        break;
                    }
                    case ELEVATEFAILED:
                    case FIELD_MUST:
                    case FIELD_TOO_LONG:
                    case FIELD_HIGH_PRECISION:
                    case FIELD_NON_EXISTENT:
                        result = OperationResult.newBuilder()
                            .setAffectedRow(0)
                            .setCode(OperationResult.Code.FAILED)
                            .setOriginStatus(createStatus.name())
                            .setMessage(oqsResult.getMessage())
                            .buildPartial();
                        break;
                    default:
                        result = OperationResult.newBuilder()
                            .setAffectedRow(0)
                            .setCode(OperationResult.Code.FAILED)
                            .setOriginStatus(createStatus.name())
                            .setMessage(
                                other(String.format("Unknown response status %s.", createStatus.name())))
                            .buildPartial();
                }

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.EXCEPTION)
                    .setMessage(Optional.ofNullable(e.getMessage()).orElseGet(e::toString))
                    .buildPartial();
            } finally {
                extractTransaction(metadata).ifPresent(id -> {
                    transactionManager.unbind();
                });
            }
            return result;
        });
    }

    /**
     * convert hints to fails.
     */
    private Map<String, String> hintsToFails(Collection<Hint> calculationHintCollections) {
        Map<String, String> hintsMap = new HashMap<>();
        if (null != calculationHintCollections) {
            calculationHintCollections.forEach(
                hint -> {
                    if (IEntityField.class.isInstance(hint.getTarget())) {
                        hintsMap.put(((IEntityField) hint.getTarget()).name(), hint.getMsg());
                    } else {
                        hintsMap.put(hint.getTarget().toString(), hint.getMsg());
                    }
                }
            );
        }
        return hintsMap;
    }

    /**
     * fill all empty field to empty.
     */
    private void replaceEntity(IEntity entity,
                               IEntityClass entityClass) {
        Collection<IEntityField> fields = entityClass.fields();
        IEntityValue entityValue = entity.entityValue();
        fields.forEach(x -> {
            Optional<IValue> value = entityValue.getValue(x.id());
            if (!value.isPresent()) {
                entityValue.addValue(new EmptyTypedValue(x));
            }
        });
    }

    @Override
    public CompletionStage<OperationResult> replace(EntityUp in, Metadata metadata) {
        return asyncWrite(() -> {

            String profile = extractProfile(metadata).orElse("");

            //check entityRef
            EntityClassRef entityClassRef = EntityClassHelper.toEntityClassRef(in, profile);
            IEntityClass entityClass;
            try {
                entityClass = checkedEntityClassRef(entityClassRef);
            } catch (Exception ex) {
                return exceptional(ex);
            }

            boolean success;
            try {
                success = tryRestoreTransaction(metadata);
            } catch (Exception ex) {
                return exceptional(ex);
            }
            if (!success) {
                OperationResult.newBuilder()
                    .setAffectedRow(0)
                    .setCode(OperationResult.Code.FAILED)
                    .setMessage("Restore transaction failure, transaction may have timed out.")
                    .buildPartial();
            }

            OperationResult result;

            try {
                Optional<String> mode = metadata.getText("mode");
                IEntity entity = toEntity(entityClassRef, entityClass, in);

                /*
                 * do auto fill
                 */
                autoFillLookUp(entity, entityClass);

                if (mode.filter("replace"::equals).isPresent()) {
                    replaceEntity(entity, entityClass);
                }

                //side effect
                OqsResult<Map.Entry<IEntity, IValue[]>> oqsResult = entityManagementService.replace(entity);
                long txId = 0;
                int version = 0;
                ResultStatus replaceStatus = oqsResult.getResultStatus();
                Optional<Map.Entry<IEntity, IValue[]>> valueOp = oqsResult.getValue();

                if (replaceStatus == null) {
                    result = OperationResult.newBuilder()
                        .setAffectedRow(0)
                        .setCode(OperationResult.Code.EXCEPTION)
                        .setOriginStatus(UNKNOWN.name())
                        .setMessage(
                            other("Unknown response status."))
                        .buildPartial();
                } else {
                    switch (replaceStatus) {
                        case SUCCESS: { //add block
                            OperationResult.Builder builder = OperationResult.newBuilder()
                                .setAffectedRow(1)
                                .setCode(OperationResult.Code.OK)
                                .setOriginStatus(SUCCESS.name())
                                .addIds(txId)
                                .addIds(version);

                            valueOp.ifPresent(x -> {
                                IEntity key = x.getKey();
                                IValue[] value = x.getValue();
                                //add old and new
                                builder.addQueryResult(toEntityUp(key));
                                Entity newEntity = Entity.Builder.anEntity()
                                    .withId(key.id())
                                    .withEntityClassRef(key.entityClassRef())
                                    .withValues(Arrays.asList(value))
                                    .build();
                                builder.addQueryResult(toEntityUp(newEntity));
                            });
                            result = builder.buildPartial();
                            break;
                        }
                        case HALF_SUCCESS: { //add block
                            Map<String, String> failedMap = hintsToFails(oqsResult.getHints());
                            String failedValues = "";
                            try {
                                failedValues = mapper.writeValueAsString(failedMap);
                            } catch (Exception ex) {
                                logger.error("{}", ex);
                            }

                            OperationResult.Builder builder = OperationResult.newBuilder()
                                .setAffectedRow(1)
                                .setCode(OperationResult.Code.OTHER)
                                .setOriginStatus(HALF_SUCCESS.name())
                                .setMessage(failedValues)
                                .addIds(txId)
                                .addIds(version);

                            valueOp.ifPresent(x -> {
                                IEntity key = x.getKey();
                                IValue[] value = x.getValue();
                                //add old and new
                                builder.addQueryResult(toEntityUp(key));
                                Entity newEntity = Entity.Builder.anEntity()
                                    .withId(key.id())
                                    .withEntityClassRef(entityClassRef)
                                    .withValues(Arrays.asList(value))
                                    .build();
                                builder.addQueryResult(toEntityUp(newEntity));
                            });

                            result = builder.buildPartial();
                            break;
                        }
                        case CONFLICT:
                            //send to sdk
                            result = OperationResult.newBuilder()
                                .setAffectedRow(0)
                                .setCode(OperationResult.Code.OTHER)
                                .setOriginStatus(ResultStatus.CONFLICT.name())
                                .setMessage(ResultStatus.CONFLICT.name())
                                .buildPartial();
                            break;
                        case NOT_FOUND:
                            //send to sdk
                            result = OperationResult.newBuilder()
                                .setAffectedRow(0)
                                .setCode(OperationResult.Code.FAILED)
                                .setMessage(notFound("No record found."))
                                .setOriginStatus(NOT_FOUND.name())
                                .buildPartial();
                            break;
                        case ELEVATEFAILED:
                        case FIELD_MUST:
                        case FIELD_TOO_LONG:
                        case FIELD_HIGH_PRECISION:
                        case FIELD_NON_EXISTENT:
                            result = OperationResult.newBuilder()
                                .setAffectedRow(0)
                                .setCode(OperationResult.Code.FAILED)
                                .setOriginStatus(replaceStatus.name())
                                .setMessage(oqsResult.getMessage())
                                .buildPartial();
                            break;
                        default:
                            //unreachable code
                            result = OperationResult.newBuilder()
                                .setAffectedRow(0)
                                .setCode(OperationResult.Code.FAILED)
                                .setOriginStatus(replaceStatus.name())
                                .setMessage(
                                    other(String.format("Unknown response status %s.", replaceStatus.name())))
                                .buildPartial();
                    }
                }
            } catch (Exception e) {
                logger.error("{}", e);
                result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.EXCEPTION)
                    .setMessage(err(Optional.ofNullable(e.getMessage()).orElseGet(e::toString)))
                    .buildPartial();
            } finally {
                extractTransaction(metadata).ifPresent(id -> {
                    transactionManager.unbind();
                });
            }

            return result;
        });
    }

    /**
     * update multi.
     *
     * @param in       ss
     * @param metadata ss
     * @return dss
     */
    @Override
    public CompletionStage<OperationResult> replaceMulti(EntityMultiUp in, Metadata metadata) {
        return asyncWrite(() -> {

            String profile = extractProfile(metadata).orElse("");

            //check entityRef
            EntityClassRef entityClassRef = EntityClassHelper.toEntityClassRef(in, profile);
            IEntityClass entityClass;
            try {
                entityClass = checkedEntityClassRef(entityClassRef);
            } catch (Exception ex) {
                return exceptional(ex);
            }

            boolean success;
            try {
                success = tryRestoreTransaction(metadata);
            } catch (Exception ex) {
                return exceptional(ex);
            }
            if (!success) {
                OperationResult.newBuilder()
                    .setAffectedRow(0)
                    .setCode(OperationResult.Code.FAILED)
                    .setMessage("Restore transaction failure, transaction may have timed out.")
                    .buildPartial();
            }

            OperationResult result = null;

            try {

                List<IEntity> entityList = toEntity(entityClassRef, entityClass, in);

                Optional<String> mode = metadata.getText("mode");
                if (!entityList.isEmpty()) {

                    IEntity[] entities =
                        entityList.stream().peek(entity -> {
                            if (mode.filter("replace"::equals).isPresent()) {
                                //need reset version here
                                replaceEntity(entity, entityClass);
                            }
                        }).toArray(IEntity[]::new);

                    //side effect
                    try {

                        Arrays.stream(entities).forEach(e -> autoFillLookUp(e, entityClass));
                        OqsResult<Map<IEntity, IValue[]>> oqsResult = entityManagementService.replace(entities);
                        ResultStatus replaceStatus = oqsResult.getResultStatus();
                        Optional<Map<IEntity, IValue[]>> valueOp = oqsResult.getValue();

                        switch (replaceStatus) {
                            case SUCCESS: {
                                OperationResult.Builder builder = OperationResult.newBuilder()
                                    .setAffectedRow(entities.length)
                                    .setCode(OperationResult.Code.OK)
                                    .setOriginStatus(SUCCESS.name());

                                valueOp.ifPresent(value -> {
                                    value.forEach((k, v) -> {
                                        //add old and new
                                        builder.addQueryResult(toEntityUp(k));
                                        Entity newEntity = Entity.Builder.anEntity()
                                            .withId(k.id())
                                            .withEntityClassRef(k.entityClassRef())
                                            .withValues(Arrays.asList(v))
                                            .build();
                                        builder.addQueryResult(toEntityUp(newEntity));
                                    });
                                });

                                result = builder.buildPartial();
                                break;
                            }
                            case HALF_SUCCESS: {
                                Map<String, String> failedMap = hintsToFails(oqsResult.getHints());
                                String failedValues = "";
                                try {
                                    failedValues = mapper.writeValueAsString(failedMap);
                                } catch (Exception ex) {
                                    logger.error("{}", ex);
                                }


                                OperationResult.Builder builder = OperationResult.newBuilder()
                                    .setAffectedRow(entities.length)
                                    .setCode(OperationResult.Code.OTHER)
                                    .setOriginStatus(HALF_SUCCESS.name())
                                    .setMessage(failedValues);

                                valueOp.ifPresent(value -> {
                                    value.forEach((k, v) -> {
                                        //add old and new
                                        builder.addQueryResult(toEntityUp(k));
                                        Entity newEntity = Entity.Builder.anEntity()
                                            .withId(k.id())
                                            .withValues(Arrays.asList(v))
                                            .withEntityClassRef(k.entityClassRef())
                                            .build();
                                        builder.addQueryResult(toEntityUp(newEntity));
                                    });
                                });

                                result = builder.buildPartial();
                                break;
                            }
                            case CONFLICT:
                                //send to sdk
                                result = OperationResult.newBuilder()
                                    .setAffectedRow(0)
                                    .setCode(OperationResult.Code.OTHER)
                                    .setOriginStatus(ResultStatus.CONFLICT.name())
                                    .setMessage(ResultStatus.CONFLICT.name())
                                    .buildPartial();
                                break;
                            case NOT_FOUND:
                                //send to sdk
                                result = OperationResult.newBuilder()
                                    .setAffectedRow(0)
                                    .setCode(OperationResult.Code.FAILED)
                                    .setMessage(notFound("No record found."))
                                    .setOriginStatus(NOT_FOUND.name())
                                    .buildPartial();
                                break;
                            case ELEVATEFAILED:
                            case FIELD_MUST:
                            case FIELD_TOO_LONG:
                            case FIELD_HIGH_PRECISION:
                            case FIELD_NON_EXISTENT:
                                result = OperationResult.newBuilder()
                                    .setAffectedRow(0)
                                    .setCode(OperationResult.Code.FAILED)
                                    .setOriginStatus(replaceStatus.name())
                                    .setMessage(oqsResult.getMessage())
                                    .buildPartial();
                                break;
                            default:
                                //unreachable code
                                result = OperationResult.newBuilder()
                                    .setAffectedRow(0)
                                    .setCode(OperationResult.Code.FAILED)
                                    .setOriginStatus(replaceStatus.name())
                                    .setMessage(
                                        other(String.format("Unknown response status %s.", replaceStatus.name())))
                                    .buildPartial();
                        }

                    } catch (SQLException e) {
                        //TODO
                        logger.error(e.getMessage(), e);
                        throw e;
                    }

                } else {
                    result = OperationResult.newBuilder()
                        .setCode(OperationResult.Code.OK)
                        .setMessage(ok("No records have been updated."))
                        .setAffectedRow(0)
                        .buildPartial();
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.EXCEPTION)
                    .setMessage(err(Optional.ofNullable(e.getMessage()).orElseGet(e::toString)))
                    .buildPartial();
            } finally {

                extractTransaction(metadata).ifPresent(id -> {
                    transactionManager.unbind();
                });
            }

            return result;
        });
    }

    @Override
    public CompletionStage<OperationResult> reCalculate(ReCalculateInfo reCalculateInfo, Metadata metadata) {
        return asyncWrite(() -> {
            OperationResult result = null;
            try {
                List<EntityUp> entityUps = reCalculateInfo.getEntityClassesList();

                EntityClassRef entityClassRef = EntityClassRef.Builder.anEntityClassRef()
                    .withEntityClassId(reCalculateInfo.getEntityClassRef().getId())
                    .withEntityClassCode(reCalculateInfo.getEntityClassRef().getCode())
                    .withEntityClassProfile(reCalculateInfo.getEntityClassRef().getProfile()).build();

                ProtocolStringList fieldCodeList = reCalculateInfo.getFieldCodeList();
                Optional<IEntityClass> entityClass = metaManager.load(entityClassRef);

                if (!entityClass.isPresent()) {
                    throw new RuntimeException(
                        String.format("entityClass not found by entityClassRef: %s", entityClassRef));
                }
                List<IEntity> entities = new ArrayList<>();

                for (EntityUp entityUp : entityUps) {
                    IEntity entity = toEntity(entityClassRef, entityClass.get(), entityUp);
                    entities.add(entity);
                }

                OqsResult<Map<IEntity, IValue[]>> mapOqsResult =
                    entityManagementService.reCalculate(
                        entities.toArray(entities.toArray(new IEntity[entities.size()])),
                        entityClassRef, fieldCodeList);
                ResultStatus replaceStatus = mapOqsResult.getResultStatus();
                Optional<Map<IEntity, IValue[]>> valueOp = mapOqsResult.getValue();

                switch (replaceStatus) {
                    case SUCCESS: {
                        OperationResult.Builder builder = OperationResult.newBuilder()
                            .setAffectedRow(entities.size())
                            .setCode(OperationResult.Code.OK)
                            .setOriginStatus(SUCCESS.name());

                        valueOp.ifPresent(value -> {
                            value.forEach((k, v) -> {
                                //add old and new
                                builder.addQueryResult(toEntityUp(k));
                                Entity newEntity = Entity.Builder.anEntity()
                                    .withId(k.id())
                                    .withEntityClassRef(k.entityClassRef())
                                    .withValues(Arrays.asList(v))
                                    .build();
                                builder.addQueryResult(toEntityUp(newEntity));
                            });
                            builder.setAffectedRow(valueOp.get().size());
                        });

                        result = builder.buildPartial();
                        break;
                    }
                    case HALF_SUCCESS: {
                        Map<String, String> failedMap = hintsToFails(mapOqsResult.getHints());
                        String failedValues = "";
                        try {
                            failedValues = mapper.writeValueAsString(failedMap);
                        } catch (Exception ex) {
                            logger.error("{}", ex);
                        }

                        OperationResult.Builder builder = OperationResult.newBuilder()
                            .setAffectedRow(entities.size())
                            .setCode(OperationResult.Code.OTHER)
                            .setOriginStatus(HALF_SUCCESS.name())
                            .setMessage(failedValues);

                        valueOp.ifPresent(value -> {
                            value.forEach((k, v) -> {
                                //add old and new
                                builder.addQueryResult(toEntityUp(k));
                                Entity newEntity = Entity.Builder.anEntity()
                                    .withId(k.id())
                                    .withValues(Arrays.asList(v))
                                    .withEntityClassRef(k.entityClassRef())
                                    .build();
                                builder.addQueryResult(toEntityUp(newEntity));
                            });
                        });

                        result = builder.buildPartial();
                        break;
                    }
                    case CONFLICT:
                        //send to sdk
                        result = OperationResult.newBuilder()
                            .setAffectedRow(0)
                            .setCode(OperationResult.Code.OTHER)
                            .setOriginStatus(ResultStatus.CONFLICT.name())
                            .setMessage(ResultStatus.CONFLICT.name())
                            .buildPartial();
                        break;
                    case NOT_FOUND:
                        //send to sdk
                        result = OperationResult.newBuilder()
                            .setAffectedRow(0)
                            .setCode(OperationResult.Code.FAILED)
                            .setMessage(notFound("No record found."))
                            .setOriginStatus(NOT_FOUND.name())
                            .buildPartial();
                        break;
                    case ELEVATEFAILED:
                    case FIELD_MUST:
                    case FIELD_TOO_LONG:
                    case FIELD_HIGH_PRECISION:
                        result = OperationResult.newBuilder()
                            .setAffectedRow(0)
                            .setCode(OperationResult.Code.FAILED)
                            .setOriginStatus(replaceStatus.name())
                            .setMessage(mapOqsResult.getMessage())
                            .buildPartial();
                        break;
                    default:
                        //unreachable code
                        result = OperationResult.newBuilder()
                            .setAffectedRow(0)
                            .setCode(OperationResult.Code.FAILED)
                            .setOriginStatus(replaceStatus.name())
                            .setMessage(
                                other(String.format("Unknown response status %s.", replaceStatus.name())))
                            .buildPartial();
                }
            } catch (Exception e) {
                logger.info(e.getMessage());
                result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.EXCEPTION)
                    .setMessage(err(Optional.ofNullable(e.getMessage()).orElseGet(e::toString)))
                    .buildPartial();
            } finally {
                extractTransaction(metadata).ifPresent(id -> {
                    transactionManager.unbind();
                });
            }
            return result;
        });
    }

    @Override
    public CompletionStage<DryRunResult> dryRunFields(DryRunFieldsInfo dryRunFieldsInfo, Metadata metadata) {
        return asyncWrite(() -> {
            DryRunResult result = null;
            try {
                EntityClassRef entityClassRef = EntityClassRef.Builder.anEntityClassRef()
                    .withEntityClassId(dryRunFieldsInfo.getEntityClassRef().getId())
                    .withEntityClassCode(dryRunFieldsInfo.getEntityClassRef().getCode())
                    .withEntityClassProfile(dryRunFieldsInfo.getEntityClassRef().getProfile()).build();

                List<ErrorCalculateInstance> errorCalculateInstances =
                    entityManagementService.dryRun(dryRunFieldsInfo.getIdList(), entityClassRef,
                        dryRunFieldsInfo.getFieldCodeList());

                result = DryRunResult.newBuilder()
                    .addAllErrorCalculateInstance(errorCalculateInstances.stream().map(e ->
                        com.xforceplus.ultraman.oqsengine.sdk.ErrorCalculateInstance.newBuilder()
                            .setId(e.getId())
                            .addAllErrorFieldUnits(e.getErrorFieldUnits().stream().map(f -> ErrorFieldUnit.newBuilder()
                                    .setFieldCode(f.getField().name())
                                    .setExpectValue(f.getExpect().valueToString())
                                    .setNowValue(f.getNow().valueToString()).build()
                                )
                                .collect(Collectors.toList()))
                            .build()).collect(Collectors.toList())
                    )
                    .setCode(DryRunResult.Code.OK)
                    .build();
            } catch (Exception e) {
                logger.error(e.getMessage());
                return DryRunResult.newBuilder().setCode(DryRunResult.Code.EXCEPTION).setMessage(e.getMessage())
                    .build();
            }

            return result;
        });
    }

    @Override
    public CompletionStage<DryRunResult> dryRunInstances(DryRunInstanceInfo dryRunInstanceInfo, Metadata metadata) {
        return asyncWrite(() -> {
            DryRunResult result = null;
            try {
                EntityClassRef entityClassRef = EntityClassRef.Builder.anEntityClassRef()
                    .withEntityClassId(dryRunInstanceInfo.getEntityClassRef().getId())
                    .withEntityClassCode(dryRunInstanceInfo.getEntityClassRef().getCode())
                    .withEntityClassProfile(dryRunInstanceInfo.getEntityClassRef().getProfile()).build();

                List<ErrorCalculateInstance> errorCalculateInstances =
                    entityManagementService.dryRun(dryRunInstanceInfo.getIdList(), entityClassRef);

                result = DryRunResult.newBuilder()
                    .addAllErrorCalculateInstance(errorCalculateInstances.stream().map(e ->
                        com.xforceplus.ultraman.oqsengine.sdk.ErrorCalculateInstance.newBuilder()
                            .setId(e.getId())
                            .addAllErrorFieldUnits(e.getErrorFieldUnits().stream().map(f -> ErrorFieldUnit.newBuilder()
                                .setFieldCode(f.getField().name())
                                .setExpectValue(f.getExpect().valueToString())
                                .setNowValue(f.getNow().valueToString()).build()).collect(Collectors.toList()))
                            .build()).collect(Collectors.toList()))
                    .setCode(DryRunResult.Code.OK)
                    .build();
            } catch (Exception e) {
                logger.error(e.getMessage());
                return DryRunResult.newBuilder().setCode(DryRunResult.Code.EXCEPTION).setMessage(e.getMessage())
                    .build();
            }

            return result;
        });
    }

    private Sort toSort(Tuple2<FieldSortUp, IEntityField> tuple2) {
        Sort sortT = null;
        FieldSortUp sortUp = tuple2._1();
        if (sortUp.getOrder() == FieldSortUp.Order.asc) {
            sortT = Sort.buildAscSort(tuple2._2());
        } else {
            sortT = Sort.buildDescSort(tuple2._2());
        }
        return sortT;
    }

    /**
     * need to return affected ids.
     */
    @Override
    public CompletionStage<OperationResult> replaceByCondition(SelectByCondition in, Metadata metadata) {
        return asyncWrite(() -> {

            String profile = extractProfile(metadata).orElse("");

            //check entityRef
            EntityClassRef entityClassRef = EntityClassHelper.toEntityClassRef(in.getEntity(), profile);
            IEntityClass entityClass;
            try {
                entityClass = checkedEntityClassRef(entityClassRef);
            } catch (Exception ex) {
                return exceptional(ex);
            }

            boolean success;
            try {
                success = tryRestoreTransaction(metadata);
            } catch (Exception ex) {
                return exceptional(ex);
            }
            if (!success) {
                OperationResult.newBuilder()
                    .setAffectedRow(0)
                    .setCode(OperationResult.Code.FAILED)
                    .setMessage("Restore transaction failure, transaction may have timed out.")
                    .buildPartial();
            }

            OperationResult result;

            try {
                //check if has sub query for more details
                List<QueryFieldsUp> queryField = in.getQueryFieldsList();

                Page page = null;
                List<FieldSortUp> sort = in.getSortList();
                ConditionsUp conditions = in.getConditions();
                int pageNo = in.getPageNo();
                int pageSize = in.getPageSize();
                page = new Page(pageNo, pageSize);

                //builderCondition
                Optional<Conditions> consOp = toConditions(entityClass, conditions, in.getIdsList(), metaManager);
                ServiceSelectConfig.Builder serviceSelectConfigBuilder = ServiceSelectConfig.Builder.anSearchConfig();
                //inject page
                serviceSelectConfigBuilder.withPage(page);

                //build sort
                if (sort != null && !sort.isEmpty()) {
                    List<Tuple2<FieldSortUp, IEntityField>> sortFieldsList = sort.stream()
                        .map(x -> Tuple.of(x, toEntityField(entityClass, x.getField())))
                        .filter(x -> x._2().isPresent())
                        .map(x -> x.map2(Optional::get))
                        .collect(Collectors.toList());

                    int size = sortFieldsList.size();
                    if (size == 1) {
                        Tuple2<FieldSortUp, IEntityField> tuple2 = sortFieldsList.get(0);
                        serviceSelectConfigBuilder.withSort(toSort(tuple2));
                    } else if (size == 2) {
                        Tuple2<FieldSortUp, IEntityField> tuple21 = sortFieldsList.get(0);
                        serviceSelectConfigBuilder.withSort(toSort(tuple21));
                        Tuple2<FieldSortUp, IEntityField> tuple22 = sortFieldsList.get(1);
                        serviceSelectConfigBuilder.withSecondarySort(toSort(tuple22));
                    } else if (size >= 3) {
                        Tuple2<FieldSortUp, IEntityField> tuple21 = sortFieldsList.get(0);
                        serviceSelectConfigBuilder.withSort(toSort(tuple21));
                        Tuple2<FieldSortUp, IEntityField> tuple22 = sortFieldsList.get(1);
                        serviceSelectConfigBuilder.withSecondarySort(toSort(tuple22));
                        Tuple2<FieldSortUp, IEntityField> tuple23 = sortFieldsList.get(2);
                        serviceSelectConfigBuilder.withThridSort(toSort(tuple23));
                    }
                }

                OqsResult<Collection<IEntity>> entities =
                    entitySearchService
                        .selectByConditions(consOp.orElseGet(Conditions::buildEmtpyConditions), entityClassRef,
                            serviceSelectConfigBuilder.build());

                //----------------------------
                AtomicInteger affected = new AtomicInteger(0);
                Optional<String> mode = metadata.getText("mode");
                if (!entities.getValue().get().isEmpty()) {

                    entities.getValue().get().forEach(entityResult -> {

                        IEntity entity = toEntity(entityClassRef, entityClass, in.getEntity());
                        if (mode.filter("replace"::equals).isPresent()) {
                            //need reset version here
                            replaceEntity(entity, entityClass);
                        }

                        //side effect
                        try {
                            entityManagementService.replace(entity);
                            affected.incrementAndGet();
                        } catch (SQLException e) {
                            //TODO
                            logger.error(e.getMessage(), e);
                        }
                    });

                    result = OperationResult.newBuilder()
                        .setAffectedRow(affected.intValue())
                        .setCode(OperationResult.Code.OK)
                        .buildPartial();
                } else {
                    result = OperationResult.newBuilder()
                        .setCode(OperationResult.Code.OK)
                        .setMessage(ok("No records have been updated."))
                        .setAffectedRow(0)
                        .buildPartial();
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.EXCEPTION)
                    .setMessage(err(Optional.ofNullable(e.getMessage()).orElseGet(e::toString)))
                    .buildPartial();
            } finally {
                extractTransaction(metadata).ifPresent(id -> {
                    transactionManager.unbind();
                });
            }

            return result;
        });
    }

    @Override
    public CompletionStage<OperationResult> remove(EntityUp in, Metadata metadata) {
        return asyncWrite(() -> {

            String profile = extractProfile(metadata).orElse("");

            //check entityRef
            EntityClassRef entityClassRef = EntityClassHelper.toEntityClassRef(in, profile);

            boolean success;
            try {
                success = tryRestoreTransaction(metadata);
            } catch (Exception ex) {
                return exceptional(ex);
            }
            if (!success) {
                OperationResult.newBuilder()
                    .setAffectedRow(0)
                    .setCode(OperationResult.Code.FAILED)
                    .setMessage("Restore transaction failure, transaction may have timed out.")
                    .buildPartial();
            }

            String force = metadata.getText("force").orElse("false");

            OperationResult result;
            try {

                IEntity targetEntity =
                    Entity.Builder.anEntity().withId(in.getObjId()).withEntityClassRef(entityClassRef).build();

                if (!Boolean.parseBoolean(force)) {
                    OqsResult<IEntity> oqsResult =
                        entityManagementService.delete(targetEntity);
                    ResultStatus deleteStatus = oqsResult.getResultStatus();
                    Optional<IEntity> valueOp = oqsResult.getValue();
                    if (deleteStatus == null) {
                        result = OperationResult.newBuilder()
                            .setAffectedRow(0)
                            .setCode(OperationResult.Code.EXCEPTION)
                            .setOriginStatus(UNKNOWN.name())
                            .setMessage(
                                other("Unknown response status."))
                            .buildPartial();
                    } else {
                        switch (deleteStatus) {
                            case SUCCESS: {

                                OperationResult.Builder builder = OperationResult.newBuilder()
                                    .setAffectedRow(1)
                                    .setCode(OperationResult.Code.OK)
                                    .setOriginStatus(deleteStatus.name());

                                valueOp.ifPresent(value -> {
                                    builder.addQueryResult(toEntityUp(value));
                                });

                                result = builder.buildPartial();
                                break;
                            }
                            case CONFLICT:
                                //send to sdk
                                result = OperationResult.newBuilder()
                                    .setAffectedRow(0)
                                    .setCode(OperationResult.Code.OTHER)
                                    .setMessage(deleteStatus.name())
                                    .setOriginStatus(deleteStatus.name())
                                    .buildPartial();
                                break;
                            case NOT_FOUND:
                                //send to sdk
                                result = OperationResult.newBuilder()
                                    .setAffectedRow(0)
                                    .setCode(OperationResult.Code.OK)
                                    .setOriginStatus(deleteStatus.name())
                                    .setMessage(deleteStatus.name())
                                    .buildPartial();
                                break;
                            default:
                                //unreachable code
                                result = OperationResult.newBuilder()
                                    .setAffectedRow(0)
                                    .setCode(OperationResult.Code.FAILED)
                                    .setOriginStatus(deleteStatus.name())
                                    .setMessage(
                                        other(String.format("Unknown response status %s.",
                                            deleteStatus != null ? deleteStatus.name() : "NULL")))
                                    .buildPartial();
                        }
                    }
                } else {
                    OqsResult<IEntity> oqsResult =
                        entityManagementService.deleteForce(targetEntity);
                    long txId = 0;
                    long version = 0;
                    ResultStatus deleteStatus = oqsResult.getResultStatus();
                    Optional<IEntity> valueOp = oqsResult.getValue();
                    if (deleteStatus == null) {
                        result = OperationResult.newBuilder()
                            .setAffectedRow(0)
                            .setCode(OperationResult.Code.EXCEPTION)
                            .setOriginStatus(UNKNOWN.name())
                            .setMessage(
                                other("Unknown response status."))
                            .buildPartial();
                    } else {
                        switch (deleteStatus) {
                            case SUCCESS:
                                OperationResult.Builder builder = OperationResult.newBuilder()
                                    .setAffectedRow(1)
                                    .setCode(OperationResult.Code.OK)
                                    .addIds(txId)
                                    .addIds(version)
                                    .setOriginStatus(SUCCESS.name());

                                valueOp.ifPresent(value -> {
                                    builder.addQueryResult(toEntityUp(value));
                                });

                                result = builder.buildPartial();
                                break;
                            case NOT_FOUND:
                                //send to sdk
                                result = OperationResult.newBuilder()
                                    .setAffectedRow(0)
                                    .setCode(OperationResult.Code.OK)
                                    .setOriginStatus(NOT_FOUND.name())
                                    .setMessage(NOT_FOUND.name())
                                    .buildPartial();
                                break;
                            case CONFLICT:
                                result = OperationResult.newBuilder()
                                    .setAffectedRow(0)
                                    .setCode(OperationResult.Code.OTHER)
                                    .setMessage(deleteStatus.name())
                                    .setOriginStatus(deleteStatus.name())
                                    .buildPartial();
                                break;
                            default:
                                //unreachable code
                                result = OperationResult.newBuilder()
                                    .setAffectedRow(0)
                                    .setCode(OperationResult.Code.FAILED)
                                    .setMessage(
                                        other(String.format("Unknown response status %s.", deleteStatus.name())))
                                    .buildPartial();
                        }
                    }
                }

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.EXCEPTION)
                    .setMessage(err(Optional.ofNullable(e.getMessage()).orElseGet(e::toString)))
                    .buildPartial();
            } finally {

                extractTransaction(metadata).ifPresent(id -> {
                    transactionManager.unbind();
                });
            }

            return result;
        });
    }

    @Override
    public CompletionStage<OperationResult> removeMulti(EntityMultiUp in, Metadata metadata) {
        return asyncWrite(() -> {

            String profile = extractProfile(metadata).orElse("");

            //check entityRef
            EntityClassRef entityClassRef = EntityClassHelper.toEntityClassRef(in, profile);

            boolean success;
            try {
                success = tryRestoreTransaction(metadata);
            } catch (Exception ex) {
                return exceptional(ex);
            }
            if (!success) {
                OperationResult.newBuilder()
                    .setAffectedRow(0)
                    .setCode(OperationResult.Code.FAILED)
                    .setMessage("Restore transaction failure, transaction may have timed out.")
                    .buildPartial();
            }

            Entity[] entities = in.getValuesList().stream().map(x -> {
                Entity targetEntity =
                    Entity.Builder.anEntity().withId(x.getObjId()).withEntityClassRef(entityClassRef).build();
                return targetEntity;
            }).toArray(Entity[]::new);

            boolean force = Boolean.parseBoolean(metadata.getText("force").orElse("false"));

            OperationResult result;

            try {
                OqsResult<IEntity[]> oqsResult;
                if (!force) {
                    oqsResult = entityManagementService.delete(entities);
                } else {
                    oqsResult = entityManagementService.deleteForce(entities);
                }

                Optional<IEntity[]> valueOp = oqsResult.getValue();

                switch (oqsResult.getResultStatus()) {
                    case SUCCESS: {

                        OperationResult.Builder builder = OperationResult.newBuilder()
                            .setAffectedRow(valueOp.isPresent() ? valueOp.get().length : 0)
                            .setCode(OperationResult.Code.OK)
                            .setOriginStatus(oqsResult.getResultStatus().name());

                        valueOp.ifPresent(value -> {
                            List<EntityUp> entityUps = Arrays.stream(value)
                                .map(EntityClassHelper::toEntityUp)
                                .collect(Collectors.toList());
                            builder.addAllQueryResult(entityUps);
                        });

                        result = builder.buildPartial();
                        break;
                    }
                    case CONFLICT: {
                        result = OperationResult.newBuilder()
                            .setAffectedRow(valueOp.isPresent() ? valueOp.get().length : 0)
                            .setCode(OperationResult.Code.FAILED)
                            .setOriginStatus(oqsResult.getResultStatus().name())
                            .buildPartial();
                        break;
                    }
                    case NOT_FOUND: {
                        result = OperationResult.newBuilder()
                            .setAffectedRow(valueOp.isPresent() ? valueOp.get().length : 0)
                            .setCode(OperationResult.Code.OK)
                            .setOriginStatus(oqsResult.getResultStatus().name())
                            .buildPartial();
                        break;
                    }
                    default: {
                        result = OperationResult.newBuilder()
                            .setAffectedRow(valueOp.isPresent() ? valueOp.get().length : 0)
                            .setCode(OperationResult.Code.FAILED)
                            .setOriginStatus(oqsResult.getResultStatus().name())
                            .buildPartial();
                    }
                }

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.EXCEPTION)
                    .setMessage(err(Optional.ofNullable(e.getMessage()).orElseGet(e::toString)))
                    .buildPartial();
            } finally {

                extractTransaction(metadata).ifPresent(id -> {
                    transactionManager.unbind();
                });
            }

            return result;
        });
    }

    @Override
    public CompletionStage<OperationResult> selectOne(EntityUp in, Metadata metadata) {
        return asyncRead(() -> {

            String profile = extractProfile(metadata).orElse("");

            //check entityRef
            EntityClassRef entityClassRef = EntityClassHelper.toEntityClassRef(in, profile);

            boolean success;
            try {
                success = tryRestoreTransaction(metadata);
            } catch (Exception ex) {
                return exceptional(ex);
            }
            if (!success) {
                OperationResult.newBuilder()
                    .setAffectedRow(0)
                    .setCode(OperationResult.Code.FAILED)
                    .setMessage("Restore transaction failure, transaction may have timed out.")
                    .buildPartial();
            }

            OperationResult result;

            try {

                OqsResult<IEntity> ds =
                    entitySearchService.selectOne(in.getObjId(), entityClassRef);

                result = ds.getValue().map(entity -> OperationResult
                    .newBuilder()
                    .setCode(OperationResult.Code.OK)
                    .addQueryResult(toEntityUp(entity))
                    .setTotalRow(1)
                    .buildPartial()).orElseGet(() -> OperationResult
                    .newBuilder()
                    .setCode(OperationResult.Code.OK)
                    .setTotalRow(0)
                    .buildPartial());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.EXCEPTION)
                    .setMessage(Optional.ofNullable(e.getMessage()).orElseGet(e::toString))
                    .buildPartial();
            } finally {

                extractTransaction(metadata).ifPresent(id -> {
                    transactionManager.unbind();
                });
            }

            return result;
        });
    }

    private List<BusinessKey> getRawBusinessKeys(IEntityClass entityClass, List<FieldConditionUp> fieldConditionUps) {
        String code = entityClass.code();
        Collection<IEntityField> fields = entityClass.fields();
        List<IEntityField> uniqueFields = fields.stream()
            .filter(x -> {
                String uniqueName = x.config().getUniqueName();
                String[] split = uniqueName.split(":");
                if (split.length > 1) {
                    return code.equals(split[0]);
                }

                return false;
            })
            .collect(Collectors.toList());

        List<BusinessKey> collect = uniqueFields.stream().map(x -> {
            Optional<FieldConditionUp> first = fieldConditionUps.stream()
                .filter(f -> f.getOperation().equals(FieldConditionUp.Op.eq) && f.getValuesList().size() == 1)
                .filter(f -> f.getCode().equals(x.name()))
                .findFirst();

            return first.map(unique -> {
                BusinessKey businessKey = new BusinessKey();
                businessKey.setFieldName(unique.getCode());
                businessKey.setValue(unique.getValuesList().get(0));
                return businessKey;
            });
        }).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());

        if (collect.size() == uniqueFields.size()) {
            return collect;
        }

        return Collections.emptyList();
    }

    private List<BusinessKey> getBusinessKeys(IEntityClass entityClass, List<FieldConditionUp> fieldConditionUps) {

        List<BusinessKey> businessKeys = Collections.emptyList();

        IEntityClass ptr = entityClass;

        while (ptr != null && businessKeys.isEmpty()) {
            businessKeys = getRawBusinessKeys(ptr, fieldConditionUps);
            ptr = ptr.father().orElse(null);
        }

        return businessKeys;
    }

    /**
     * modify to use IEntityReader.
     */
    @Override
    public CompletionStage<OperationResult> selectByConditions(SelectByCondition in, Metadata metadata) {
        return asyncRead(() -> {

            String profile = extractProfile(metadata).orElse("");

            //check entityRef
            EntityClassRef entityClassRef = EntityClassHelper.toEntityClassRef(in.getEntity(), profile);
            IEntityClass entityClass;

            try {
                entityClass = checkedEntityClassRef(entityClassRef);
            } catch (Exception ex) {
                return exceptional(ex);
            }

            boolean success;
            try {
                success = tryRestoreTransaction(metadata);
            } catch (Exception ex) {
                return exceptional(ex);
            }
            if (!success) {
                OperationResult.newBuilder()
                    .setAffectedRow(0)
                    .setCode(OperationResult.Code.FAILED)
                    .setMessage("Restore transaction failure, transaction may have timed out.")
                    .buildPartial();
            }

            OperationResult result;
            try {

                Page page = null;
                int pageNo = in.getPageNo();
                int pageSize = in.getPageSize();
                page = new Page(pageNo, pageSize);
                Optional<Conditions> extraCondition = Optional.empty();
                if (in.hasTree()) {
                    SelectByTree tree = in.getTree();
                    Filters filters = tree.getFilters();
                    extraCondition = toConditions(entityClass, filters, metaManager);
                }

                ServiceSelectConfig.Builder serviceSelectConfigBuilder =
                    ServiceSelectConfig.Builder.anSearchConfig();
                //inject page
                serviceSelectConfigBuilder.withPage(page);
                //inject extra filter
                serviceSelectConfigBuilder.withFilter(extraCondition.orElseGet(Conditions::buildEmtpyConditions));

                List<FieldSortUp> sort = in.getSortList();
                //build sort
                if (sort != null && !sort.isEmpty()) {

                    logger.debug("Sort seq is {}", sort);

                    List<Tuple2<FieldSortUp, IEntityField>> sortFieldsList = sort.stream()
                        .map(x -> Tuple.of(x, toEntityField(entityClass, x.getField())))
                        .filter(x -> x._2().isPresent())
                        .map(x -> x.map2(Optional::get))
                        .collect(Collectors.toList());

                    int size = sortFieldsList.size();
                    if (size == 1) {
                        Tuple2<FieldSortUp, IEntityField> tuple2 = sortFieldsList.get(0);
                        serviceSelectConfigBuilder.withSort(toSort(tuple2));
                    } else if (size == 2) {
                        Tuple2<FieldSortUp, IEntityField> tuple21 = sortFieldsList.get(0);
                        serviceSelectConfigBuilder.withSort(toSort(tuple21));
                        Tuple2<FieldSortUp, IEntityField> tuple22 = sortFieldsList.get(1);
                        serviceSelectConfigBuilder.withSecondarySort(toSort(tuple22));
                    } else if (size >= 3) {
                        Tuple2<FieldSortUp, IEntityField> tuple21 = sortFieldsList.get(0);
                        serviceSelectConfigBuilder.withSort(toSort(tuple21));
                        Tuple2<FieldSortUp, IEntityField> tuple22 = sortFieldsList.get(1);
                        serviceSelectConfigBuilder.withSecondarySort(toSort(tuple22));
                        Tuple2<FieldSortUp, IEntityField> tuple23 = sortFieldsList.get(2);
                        serviceSelectConfigBuilder.withThridSort(toSort(tuple23));
                    }
                }
                Optional<Conditions> consOp = toConditions(
                    entityClass, in.getConditions(), in.getIdsList(), metaManager);

                OqsResult<Collection<IEntity>> entities =
                    entitySearchService
                        .selectByConditions(consOp.orElseGet(Conditions::buildEmtpyConditions), entityClassRef,
                            serviceSelectConfigBuilder.build());

                Collection<IEntity> retCollections = simplify(
                    metadata, entities.getValue().get(), in.getQueryFieldsList());

                result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.OK)
                    .addAllQueryResult(Optional.ofNullable(retCollections).orElseGet(Collections::emptyList)
                        .stream().filter(Objects::nonNull)
                        .map(EntityClassHelper::toEntityUp).collect(Collectors.toList()))
                    .setTotalRow(page == null || !page.isReady()
                        ? Optional.ofNullable(retCollections).orElseGet(Collections::emptyList).size()
                        : Long.valueOf(page.getTotalCount()).intValue())
                    .buildPartial();

            } catch (Exception e) {
                logger.error("{}", e);
                result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.EXCEPTION)
                    .setMessage(Optional.ofNullable(e.getMessage()).orElseGet(e::toString))
                    .buildPartial();
            } finally {

                extractTransaction(metadata).ifPresent(id -> {
                    transactionManager.unbind();
                });
            }

            return result;
        });
    }

    private Collection<IEntity> simplify(Metadata metadata,
                                         Collection<IEntity> rawEntities,
                                         List<QueryFieldsUp> projects) {
        Boolean isSimplify = extractSimplify(metadata).map(Boolean::parseBoolean).orElse(false);
        if (!isSimplify) {
            return rawEntities;
        } else {
            //do simplify
            Set<Long> idSet = projects.stream().map(QueryFieldsUp::getId).collect(Collectors.toSet());
            return rawEntities.stream().map(x -> {
                Collection<IValue> values = x.entityValue().values();
                values.removeIf(item -> !idSet.contains(item.getField().id()));
                return x;
            }).collect(Collectors.toList());
        }
    }

    @Override
    public CompletionStage<OperationResult> commit(TransactionUp in, Metadata metadata) {
        Long id = Long.parseLong(in.getId());
        OperationResult result = null;

        try {
            boolean success = transactionManagementService.restore(id);
            if (!success) {
                result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.FAILED)
                    .setMessage("Restore transaction failure, transaction may have timed out.")
                    .buildPartial();
            } else {
                transactionManagementService.commit();

                result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.OK)
                    .setMessage("Transaction committed successfully.")
                    .buildPartial();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            result = OperationResult.newBuilder()
                .setCode(OperationResult.Code.EXCEPTION)
                .setMessage(Optional.ofNullable(e.getMessage()).orElseGet(e::toString))
                .buildPartial();
        }
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletionStage<OperationResult> rollBack(TransactionUp in, Metadata metadata) {

        Long id = Long.parseLong(in.getId());
        OperationResult result = null;

        try {
            boolean success = transactionManagementService.restore(id);
            if (!success) {
                result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.FAILED)
                    .setMessage("Restore transaction failure, transaction may have timed out.")
                    .buildPartial();
            } else {
                transactionManagementService.rollback();
                result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.OK)
                    .setMessage("Transaction rollback successful.")
                    .buildPartial();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            result = OperationResult.newBuilder()
                .setCode(OperationResult.Code.EXCEPTION)
                .setMessage(Optional.ofNullable(e.getMessage()).orElseGet(e::toString))
                .buildPartial();
        }
        return CompletableFuture.completedFuture(result);
    }

    /**
     * 未实现.
     */
    @Override
    public CompletionStage<OperationResult> selectByTreeFilter(SelectByTree in, Metadata metadata) {
        return asyncRead(() -> {

            String profile = extractProfile(metadata).orElse("");

            //check entityRef
            EntityClassRef entityClassRef = EntityClassHelper.toEntityClassRef(in.getEntity(), profile);
            IEntityClass entityClass;

            try {
                entityClass = checkedEntityClassRef(entityClassRef);
            } catch (Exception ex) {
                return exceptional(ex);
            }

            boolean success;
            try {
                success = tryRestoreTransaction(metadata);
            } catch (Exception ex) {
                return exceptional(ex);
            }
            if (!success) {
                OperationResult.newBuilder()
                    .setAffectedRow(0)
                    .setCode(OperationResult.Code.FAILED)
                    .setMessage("Restore transaction failure, transaction may have timed out.")
                    .buildPartial();
            }

            OperationResult result;
            try {

                OqsResult<Collection<IEntity>> entities = null;

                int pageNo = in.getRange().getPageIndex();
                int pageSize = in.getRange().getPageSize();
                Page page = new Page(pageNo, pageSize);

                Optional<? extends IEntityField> sortField;

                List<SortNode> sortList = in.getSorts().getSortList();
                if (sortList.isEmpty()) {
                    sortField = Optional.empty();
                } else {
                    SortNode sortUp = sortList.get(0);
                    //get related field
                    sortField = entityClass.field(sortUp.getFieldId());
                }

                Sort sortParam = null;
                if (sortField.isPresent()) {
                    SortNode sortUp = sortList.get(0);
                    if (sortUp.getOrder() == SortNode.Order.asc) {
                        sortParam = Sort.buildAscSort(sortField.get());
                    } else {
                        sortParam = Sort.buildDescSort(sortField.get());
                    }
                }

                ServiceSelectConfig serviceSelectConfig = ServiceSelectConfig
                    .Builder.anSearchConfig()
                    .withSort(sortParam)
                    .withPage(page)
                    .build();

                Optional<Conditions> consOp = toConditions(
                    entityClass, in.getFilters(), metaManager);


                if (consOp.isPresent()) {
                    entities =
                        entitySearchService.selectByConditions(consOp.get(), entityClassRef, serviceSelectConfig);
                } else {
                    entities = entitySearchService.selectByConditions(
                        Conditions.buildEmtpyConditions(), entityClassRef, serviceSelectConfig);
                }

                result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.OK)
                    .addAllQueryResult(Optional.ofNullable(entities.getValue().get()).orElseGet(Collections::emptyList)
                        .stream().filter(Objects::nonNull)
                        .map(EntityClassHelper::toEntityUp).collect(Collectors.toList()))
                    .setTotalRow(page == null || !page.isReady()
                        ? Optional.ofNullable(entities.getValue().get()).orElseGet(Collections::emptyList).size()
                        : Long.valueOf(page.getTotalCount()).intValue())
                    .buildPartial();

            } catch (Exception e) {
                logger.error("{}", e);
                result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.EXCEPTION)
                    .setMessage(Optional.ofNullable(e.getMessage()).orElseGet(e::toString))
                    .buildPartial();
            } finally {

                extractTransaction(metadata).ifPresent(id -> {
                    transactionManager.unbind();
                });
            }

            return result;
        });
    }

    /**
     * SDK连接应该第一个调用的准备动作.
     */
    @Override
    public CompletionStage<OperationResult> prepare(EntityUp entityUp, Metadata metadata) {
        return asyncRead(() -> {

            try {
                Optional<String> appId = metadata.getText("appid");
                Optional<String> env = metadata.getText("env");
                if (appId.isPresent() && env.isPresent()) {
                    int need = metaManager.need(appId.get(), env.get());
                    return OperationResult
                        .newBuilder()
                        .setCode(OperationResult.Code.OK)
                        .setMessage("OK:" + need)
                        .build();
                } else {
                    return OperationResult
                        .newBuilder()
                        .setCode(OperationResult.Code.FAILED)
                        .setMessage("FAILED: not registered")
                        .build();
                }
            } catch (Exception ex) {
                return OperationResult
                    .newBuilder()
                    .setCode(OperationResult.Code.FAILED)
                    .setMessage(ex.getMessage())
                    .build();
            }
        });
    }

    @Override
    public CompletionStage<OperationResult> selectBySql(SelectBySql in, Metadata metadata) {
        return asyncRead(() -> {
            return OperationResult
                .newBuilder()
                .setCode(OperationResult.Code.OTHER)
                .setMessage("Not Implemented")
                .build();
        });
    }

    //TODO
    @Override
    public CompletionStage<OperationResult> compatible(CompatibleRequest compatibleRequest, Metadata metadata) {
        return asyncRead(() -> {
            return OperationResult
                .newBuilder()
                .setCode(OperationResult.Code.OTHER)
                .setMessage("Not Implemented")
                .build();
        });
    }

    @Override
    public CompletionStage<ChangelogResponseList> changelogList(ChangelogRequest changelogRequest, Metadata metadata) {
        return asyncChangelog(() -> {
            long objId = changelogRequest.getObjId();
            long entityClassId = changelogRequest.getEntityClassId();
            int pageSize = changelogRequest.getPageSize();
            int pageNo = changelogRequest.getPageNo();
            boolean isSelf = changelogRequest.getIsSelf();
            try {
                List<ChangeVersion> changeVersions = queryStorage.queryChangelog(objId, isSelf, pageNo, pageSize);
                return ChangelogResponseList.newBuilder().addAllResponse(changeVersions.stream().map(x ->
                    ChangelogResponse
                        .newBuilder()
                        .setComment(Optional.ofNullable(x.getComment()).orElse(""))
                        .setId(x.getId())
                        .setSource(x.getSource())
                        .setUsername(Optional.ofNullable(x.getUsername()).orElse(""))
                        .setVersion(x.getVersion())
                        .setTimestamp(x.getTimestamp())
                        .build()).collect(Collectors.toList())).build();
            } catch (SQLException e) {
                logger.error("{}");
            }

            return ChangelogResponseList.newBuilder().build();
        });
    }

    @Override
    public CompletionStage<OperationResult> replay(ReplayRequest replayRequest, Metadata metadata) {
        return asyncChangelog(() -> {
            boolean isSelf = replayRequest.getIsSelf();
            long entityClassId = replayRequest.getEntityClassId();
            long objId = replayRequest.getObjId();
            long version = replayRequest.getVersion();
            if (isSelf) {
                EntityDomain entityDomain = replayService.replaySimpleDomain(entityClassId, objId, version);
                return toOperationResult(entityDomain);
            } else {
                EntityAggDomain entityAggDomain = replayService.replayAggDomain(entityClassId, objId, version);
                return toOperationResult(entityAggDomain);
            }
        });
    }

    @Override
    public CompletionStage<ChangelogCountResponse> changelogCount(ChangelogCountRequest changelogCountRequest,
                                                                  Metadata metadata) {
        return asyncChangelog(() -> {
            List<Long> objIdList = changelogCountRequest.getObjIdList();
            boolean isSelf = changelogCountRequest.getIsSelf();
            try {
                Map<Long, Long> mapping = queryStorage.changeCountMapping(objIdList, isSelf);

                List<ChangelogCountSingle> singleList = mapping.entrySet().stream().map(entry -> {
                    ChangelogCountSingle changelogCountSingle = ChangelogCountSingle
                        .newBuilder()
                        .setCount(entry.getValue())
                        .setObjId(entry.getKey())
                        .build();
                    return changelogCountSingle;
                }).collect(Collectors.toList());

                ChangelogCountResponse changelogCountResponse = ChangelogCountResponse
                    .newBuilder()
                    .addAllCount(singleList)
                    .build();
                return changelogCountResponse;
            } catch (SQLException ex) {
                logger.error("{}", ex);
                return ChangelogCountResponse
                    .newBuilder()
                    .addAllCount(Collections.emptyList())
                    .build();
            }
        });
    }

    @Override
    public CompletionStage<OperationResult> expand(TransRequest transRequest, Metadata metadata) {
        return asyncRead(() -> {
            return OperationResult.newBuilder()
                .setCode(OperationResult.Code.OK)
                .setMessage("")
                .build();
        });
    }

    private boolean tryRestoreTransaction(Metadata metadata) throws SQLException {
        Optional<Long> txIdOp = extractTransaction(metadata);
        if (txIdOp.isPresent()) {
            long id = txIdOp.get();
            return transactionManagementService.restore(id);

        } else {

            return true;
        }
    }

    private Optional<Long> extractTransaction(Metadata metadata) {
        Optional<String> transactionId = metadata.getText("transaction-id");
        return transactionId.map(Long::valueOf);
    }

    private Optional<String> extractProfile(Metadata metadata) {
        Optional<String> profile = metadata.getText("profile");
        return profile;
    }

    private Optional<String> extractSimplify(Metadata metadata) {
        Optional<String> simplify = metadata.getText("simplify");
        return simplify;
    }

    private void logInfo(Metadata metadata, BiFunction<String, String, String> template) {
        String displayName = metadata.getText("display-name").orElse("noname");
        String userName = metadata.getText("username").orElse("noname");

        logger.info(template.apply(displayName, userName));
    }


    @Override
    public Source<LockResponse, NotUsed> communicate(Source<LockRequest, NotUsed> in, Metadata metadata) {
        String node = metadata.getText("node").orElse("dummy");
        return lockStateService.setupCommunication(in, node);
    }

    @Override
    public CompletionStage<LockResponse> test(LockRequest in, Metadata metadata) {
        throw new RuntimeException("Not Supported");
    }

    @Override
    public CompletionStage<LockResponse> tryAcquire(LockRequest in, Metadata metadata) {
        String node = metadata.getText("node").orElse("dummy");
        return lockStateService.tryAcquire(in, node);
    }

    @Override
    public CompletionStage<LockResponse> tryRelease(LockRequest in, Metadata metadata) {
        String node = metadata.getText("node").orElse("dummy");
        return lockStateService.tryRelease(in, node);
    }

    @Override
    public CompletionStage<LockResponse> addWaiter(LockRequest in, Metadata metadata) {
        String node = metadata.getText("node").orElse("dummy");
        return lockStateService.addWaiter(in, node);
    }
}
