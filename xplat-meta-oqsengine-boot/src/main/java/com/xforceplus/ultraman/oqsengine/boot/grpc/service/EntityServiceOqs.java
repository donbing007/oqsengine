package com.xforceplus.ultraman.oqsengine.boot.grpc.service;

import akka.grpc.javadsl.Metadata;
import com.xforceplus.ultraman.oqsengine.boot.grpc.utils.EntityClassHelper;
import com.xforceplus.ultraman.oqsengine.changelog.ReplayService;
import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangeVersion;
import com.xforceplus.ultraman.oqsengine.changelog.storage.query.QueryStorage;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.TransactionManagementService;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsRelation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.pojo.reader.IEntityClassReader;
import com.xforceplus.ultraman.oqsengine.sdk.*;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import io.vavr.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import scala.annotation.meta.field;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.boot.grpc.utils.EntityClassHelper.isRelatedField;
import static com.xforceplus.ultraman.oqsengine.boot.grpc.utils.EntityClassHelper.toEntity;
import static com.xforceplus.ultraman.oqsengine.pojo.utils.OptionalHelper.ofEmptyStr;
import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;

/**
 * grpc server
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

    @Autowired
    private MetaManager metaManager;

    @Resource(name = "callReadThreadPool")
    private ExecutorService asyncReadDispatcher;

    @Resource(name = "callWriteThreadPool")
    private ExecutorService asyncWriteDispatcher;

    @Resource(name = "callChangelogThreadPool")
    private ExecutorService asyncChangelogDispatcher;

    @Resource
    private QueryStorage queryStorage;

    @Resource
    private ReplayService replayService;

    @Autowired
    private TransactionManager transactionManager;

    private Long buffer = 10_000L;

    private Logger logger = LoggerFactory.getLogger(EntityServiceOqs.class);

    private <T> CompletableFuture<T> asyncRead(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, asyncReadDispatcher);
    }

    private <T> CompletableFuture<T> asyncWrite(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, asyncWriteDispatcher);
    }

    private <T> CompletableFuture<T> asyncChangelog(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, asyncChangelogDispatcher);
    }

    @Override
    public CompletionStage<OperationResult> begin(TransactionUp in, Metadata metadata) {
        try {

            Optional<Integer> timeout = metadata.getText("timeout").map(Integer::parseInt);
            long transId;

            if (timeout.isPresent() && timeout.get() > 0) {
                transId = transactionManagementService.begin(timeout.get());
            } else {
                transId = transactionManagementService.begin();
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
     * TODO checkout the entityClassRef
     *
     * @param entityClassRef
     * @return
     */
    private IEntityClass checkedEntityClassRef(EntityClassRef entityClassRef) {
        Optional<IEntityClass> entityClassOp = metaManager.load(entityClassRef.getId());
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
     * create
     *
     * @param in
     * @param metadata
     * @return
     */
    @Override
    public CompletionStage<OperationResult> build(EntityUp in, Metadata metadata) {
        return asyncWrite(() -> {

            //check entityRef
            EntityClassRef entityClassRef = EntityClassHelper.toEntityClassRef(in);
            IEntityClass entityClass;
            try {
                entityClass = checkedEntityClassRef(entityClassRef);
            } catch (Exception ex) {
                return exceptional(ex);
            }

            if (extractTransaction(metadata).isPresent()) {
                Long id = extractTransaction(metadata).get();
                try {
                    transactionManagementService.restore(id);
                } catch (Exception ex) {
                    return exceptional(ex);
                }
            }

            OperationResult result;

            try {
                IEntity entity = toEntity(entityClassRef, entityClass, in);
                ResultStatus resultStatus = entityManagementService.build(entity);
                if (resultStatus == ResultStatus.SUCCESS) {
                    OperationResult.Builder builder = OperationResult.newBuilder()
                            .addIds(entity.id());
//TODO father and son
//                if (entity.family() != null && entity.family().parent() > 0) {
//                    builder.addIds(entity.family().parent());
//                }
                    result = builder.setCode(OperationResult.Code.OK).buildPartial();
                } else {
                    throw new RuntimeException();
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
     * fill all empty field to empty
     *
     * @param entity
     * @param entityClass
     */
    private void replaceEntity(IEntity entity, IEntityClass entityClass) {
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

            //check entityRef
            EntityClassRef entityClassRef = EntityClassHelper.toEntityClassRef(in);
            IEntityClass entityClass;
            try {
                entityClass = checkedEntityClassRef(entityClassRef);
            } catch (Exception ex) {
                return exceptional(ex);
            }

            if (extractTransaction(metadata).isPresent()) {
                Long id = extractTransaction(metadata).get();
                try {
                    transactionManagementService.restore(id);
                } catch (Exception e) {
                    logger.error("{}", e);
                    //fast fail
                    return OperationResult.newBuilder()
                            .setCode(OperationResult.Code.EXCEPTION)
                            .setMessage(Optional.ofNullable(e.getMessage()).orElseGet(e::toString))
                            .buildPartial();
                }
            }

            OperationResult result;

            try {
                Optional<String> mode = metadata.getText("mode");
                IEntity entity = toEntity(entityClassRef, entityClass, in);
                if (mode.filter("replace"::equals).isPresent()) {
                    replaceEntity(entity, entityClass);
                }

                //side effect
                ResultStatus replaceStatus = entityManagementService.replace(entity);
                switch (replaceStatus) {
                    case SUCCESS:
                        result = OperationResult.newBuilder()
                                .setAffectedRow(1)
                                .setCode(OperationResult.Code.OK)
                                .buildPartial();
                        break;
                    case CONFLICT:
                        //send to sdk
                        result = OperationResult.newBuilder()
                                .setAffectedRow(0)
                                .setCode(OperationResult.Code.OTHER)
                                .setMessage(ResultStatus.CONFLICT.name())
                                .buildPartial();
                        break;
                    default:
                        //unreachable code
                        result = OperationResult.newBuilder()
                                .setAffectedRow(0)
                                .setCode(OperationResult.Code.FAILED)
                                .setMessage(
                                        String.format("Unknown response status %s.",
                                                replaceStatus != null ? replaceStatus.name() : "NULL"))
                                .buildPartial();
                }


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

    @Override
    public CompletionStage<OperationResult> replaceByCondition(SelectByCondition in, Metadata metadata) {
        return asyncWrite(() -> {

            //check entityRef
            EntityClassRef entityClassRef = EntityClassHelper.toEntityClassRef(in.getEntity());
            IEntityClass entityClass;
            try {
                entityClass = checkedEntityClassRef(entityClassRef);
            } catch (Exception ex) {
                return exceptional(ex);
            }

            if (extractTransaction(metadata).isPresent()) {
                Long id = extractTransaction(metadata).get();
                try {
                    transactionManagementService.restore(id);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    //fast fail
                    return OperationResult.newBuilder()
                            .setCode(OperationResult.Code.EXCEPTION)
                            .setMessage(Optional.ofNullable(e.getMessage()).orElseGet(e::toString))
                            .buildPartial();
                }
            }

            OperationResult result;

            try {
                Collection<IEntity> entities = null;

                //check if has sub query for more details
                List<QueryFieldsUp> queryField = in.getQueryFieldsList();

                Page page = null;
                List<FieldSortUp> sort = in.getSortList();
                ConditionsUp conditions = in.getConditions();
                int pageNo = in.getPageNo();
                int pageSize = in.getPageSize();
                page = new Page(pageNo, pageSize);

                Optional<? extends IEntityField> sortField;

                if (sort == null || sort.isEmpty()) {
                    sortField = Optional.empty();
                } else {
                    FieldSortUp sortUp = sort.get(0);
                    //get related field
                    sortField = Optional.of(toEntityField(sortUp.getField()));
                }

                if (!sortField.isPresent()) {
                    Optional<Conditions> consOp = toConditions(entityClass, reader, conditions, in.getIdsList());
                    if (consOp.isPresent()) {
                        entities = entitySearchService.selectByConditions(consOp.get(), entityClass, page);
                    } else {
                        entities = entitySearchService.selectByConditions(
                                Conditions.buildEmtpyConditions(), entityClass, page);
                    }
                } else {
                    FieldSortUp sortUp = sort.get(0);
                    Sort sortParam;
                    if (sortUp.getOrder() == FieldSortUp.Order.asc) {
                        sortParam = Sort.buildAscSort(sortField.get());
                    } else {
                        sortParam = Sort.buildDescSort(sortField.get());
                    }

                    Optional<Conditions> consOp = toConditions(entityClass, reader, conditions, in.getIdsList());
                    if (consOp.isPresent()) {

                        entities = entitySearchService.selectByConditions(consOp.get(), entityClass, sortParam, page);

                    } else {
                        entities = entitySearchService.selectByConditions(
                                Conditions.buildEmtpyConditions(), entityClass, page);
                    }
                }

                //----------------------------
                AtomicInteger affected = new AtomicInteger(0);
                Optional<String> mode = metadata.getText("mode");
                if (!entities.isEmpty()) {

                    entities.forEach(entityResult -> {

                        IEntity entityInner = null;
                        if (mode.filter("replace"::equals).isPresent()) {
                            //need reset version here
                            entityInner = toEntity(entityClass, in.getEntity(), entityResult.version());
                        } else {
                            //reference here !! so cannot reuse the entities !!!
                            entityInner = entityResult;
                            updateEntity(entityInner, toEntity(entityClass, in.getEntity()));
                        }

                        //side effect
                        try {
                            entityManagementService.replace(entityInner);
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
                            .setMessage("No records have been updated.")
                            .setAffectedRow(0)
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


    @Override
    public CompletionStage<OperationResult> remove(EntityUp in, Metadata metadata) {
        return asyncWrite(() -> {

            if (extractTransaction(metadata).isPresent()) {
                Long id = extractTransaction(metadata).get();
                try {
                    transactionManagementService.restore(id);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    //fast fail
                    return OperationResult.newBuilder()
                            .setCode(OperationResult.Code.EXCEPTION)
                            .setMessage(Optional.ofNullable(e.getMessage()).orElseGet(e::toString))
                            .buildPartial();
                }
            }

            String force = "false";
            try {
                force = metadata.getText("force").orElse("false");
                String finalForce = force;
                logInfo(metadata, (displayname, username) ->
                        String.format("Attempt to delete %s:%s by %s:%s with %s",
                                in.getId(), in.getObjId(), displayname, username, finalForce));

            } catch (Exception ex) {
                logger.error("{}", ex);
            }


            OperationResult result;

            try {
                IEntityClass entityClass = toEntityClass(in);

                //find one
                Optional<IEntity> op = entitySearchService.selectOne(in.getObjId(), entityClass);

                if (op.isPresent()) {
                    IEntity entity = op.get();

                    if (!Boolean.parseBoolean(force)) {
                        ResultStatus deleteStatus = entityManagementService.delete(entity);

                        switch (deleteStatus) {
                            case SUCCESS:
                                result = OperationResult.newBuilder()
                                        .setAffectedRow(1)
                                        .setCode(OperationResult.Code.OK)
                                        .buildPartial();
                                break;
                            case CONFLICT:
                                //send to sdk
                                result = OperationResult.newBuilder()
                                        .setAffectedRow(0)
                                        .setCode(OperationResult.Code.OTHER)
                                        .setMessage(ResultStatus.CONFLICT.name())
                                        .buildPartial();
                                break;
                            default:
                                //unreachable code
                                result = OperationResult.newBuilder()
                                        .setAffectedRow(0)
                                        .setCode(OperationResult.Code.FAILED)
                                        .setMessage(
                                                String.format("Unknown response status %s.",
                                                        deleteStatus != null ? deleteStatus.name() : "NULL"))
                                        .buildPartial();
                        }
                    } else {
                        ResultStatus resultStatus = entityManagementService.deleteForce(entity);
                        switch (resultStatus) {
                            case SUCCESS:
                                result = OperationResult.newBuilder()
                                        .setAffectedRow(1)
                                        .setCode(OperationResult.Code.OK)
                                        .buildPartial();
                                break;
                            default:
                                //unreachable code
                                result = OperationResult.newBuilder()
                                        .setAffectedRow(0)
                                        .setCode(OperationResult.Code.FAILED)
                                        .setMessage(
                                                String.format("Unknown response status %s.",
                                                        resultStatus != null ? resultStatus.name() : "NULL"))
                                        .buildPartial();
                        }
                    }
                } else {
                    result = OperationResult.newBuilder()
                            .setAffectedRow(0)
                            .setCode(OperationResult.Code.OK)
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

    @Override
    public CompletionStage<OperationResult> selectOne(EntityUp in, Metadata metadata) {
        return asyncRead(() -> {

            if (extractTransaction(metadata).isPresent()) {
                Long id = extractTransaction(metadata).get();
                try {
                    transactionManagementService.restore(id);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    //fast fail
                    return OperationResult.newBuilder()
                            .setCode(OperationResult.Code.EXCEPTION)
                            .setMessage(Optional.ofNullable(e.getMessage()).orElseGet(e::toString))
                            .buildPartial();
                }
            }

            OperationResult result;

            try {

                IEntityClass entityClass = toEntityClass(in);

                IEntityClass subEntityClass = getSubEntityClass(in);

                Optional<IEntity> ds = entitySearchService.selectOne(in.getObjId(), entityClass);

                if (ds.isPresent()) {
                    if (ds.get().family() != null && ds.get().family().parent() > 0 && entityClass.father() != null) {
                        Optional<IEntity> parentDS = entitySearchService
                                .selectOne(ds.get().family().parent(), entityClass.father());

                        Optional<IEntity> finalDs = ds;
                        parentDS.ifPresent(x ->
                                finalDs.ifPresent(y -> leftAppend(y, x)));
                    } else if (ds.get().family() != null && ds.get().family().child() > 0 && subEntityClass != null) {
                        Optional<IEntity> childDs = entitySearchService
                                .selectOne(ds.get().family().child(), subEntityClass);

                        Optional<IEntity> finalDs = ds;
                        childDs.ifPresent(x ->
                                finalDs.ifPresent(y -> leftAppend(x, y)));
                        ds = childDs;
                    }
                }

                result = ds.map(entity -> OperationResult
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

    /**
     * TODO modify to use IEntityReader
     *
     * @param in
     * @param metadata
     * @return
     */
    @Override
    public CompletionStage<OperationResult> selectByConditions(SelectByCondition in, Metadata metadata) {
        return asyncRead(() -> {

            if (extractTransaction(metadata).isPresent()) {
                Long id = extractTransaction(metadata).get();
                try {
                    transactionManagementService.restore(id);
                } catch (Exception e) {
                    logger.error("{}", e);
                    //fast fail
                    return OperationResult.newBuilder()
                            .setCode(OperationResult.Code.EXCEPTION)
                            .setMessage(Optional.ofNullable(e.getMessage()).orElseGet(e::toString))
                            .buildPartial();
                }
            }

            OperationResult result;
            try {

                Collection<IEntity> entities = null;

                //check if has sub query for more details
                List<QueryFieldsUp> queryField = in.getQueryFieldsList();

                EntityUp entityUp = in.getEntity();

                IEntityClass entityClass = toEntityClass(entityUp);

                IEntityClassReader reader = new IEntityClassReader(entityClass);

                Long mainEntityId = entityClass.id();

                Page page = null;

                List<FieldSortUp> sort = in.getSortList();

                ConditionsUp conditions = in.getConditions();

                int pageNo = in.getPageNo();
                int pageSize = in.getPageSize();
                page = new Page(pageNo, pageSize);

                Optional<? extends IEntityField> sortField;

                if (sort == null || sort.isEmpty()) {
                    sortField = Optional.empty();
                } else {
                    FieldSortUp sortUp = sort.get(0);
                    //get related field
                    sortField = reader.column(sortUp.getCode());
                }

                if (!sortField.isPresent()) {
                    Optional<Conditions> consOp = toConditions(entityClass, reader, conditions, in.getIdsList());
                    if (consOp.isPresent()) {
                        entities = entitySearchService.selectByConditions(consOp.get(), entityClass, page);
                    } else {
                        entities = entitySearchService.selectByConditions(
                                Conditions.buildEmtpyConditions(), entityClass, page);
                    }
                } else {
                    FieldSortUp sortUp = sort.get(0);
                    Sort sortParam;
                    if (sortUp.getOrder() == FieldSortUp.Order.asc) {
                        sortParam = Sort.buildAscSort(sortField.get());
                    } else {
                        sortParam = Sort.buildDescSort(sortField.get());
                    }

                    Optional<Conditions> consOp = toConditions(entityClass, reader, conditions, in.getIdsList());
                    if (consOp.isPresent()) {
                        entities = entitySearchService.selectByConditions(
                                consOp.get(), entityClass, sortParam, page);
                    } else {
                        entities = entitySearchService.selectByConditions(
                                Conditions.buildEmtpyConditions(), entityClass, sortParam, page);
                    }
                }


                /**
                 * find extends entity from field
                 * field a
                 * field b.a
                 * field b.b
                 *
                 *  --> "" -> a
                 *      "b" ->
                 */

                /**
                 *  entities ->
                 */

                //extend entities
                Map<String, List<QueryFieldsUp>> mappedQueryFields = queryField.stream()
                        .collect(Collectors.groupingBy(x -> {
                            String code = x.getCode();
                            String[] relCode = code.split("\\.");
                            if (relCode.length > 1) {
                                return relCode[0];
                            } else {
                                return "";
                            }
                        }));

                /**
                 * find all related field and change all these IEntity to use mixed IValue
                 */
                Collection<IEntity> finalEntities = entities
                        .stream()
                        .map(iEntity -> {

                            if (iEntity != null) {

                                //find fieldName from ientity;
                                iEntity.entityValue().values().stream()
                                        .forEach(envValue -> {
                                            IEntityField field = envValue.getField();
                                            entityClass.field(field.id()).ifPresent(envValue::setField);
                                        });
                                iEntity.resetEntityValue(new MixedEntityValue(iEntity.entityValue()));
                            }
                            return iEntity;
                        }).filter(Objects::nonNull).collect(Collectors.toList());

                if (!entities.isEmpty()) {
                    mappedQueryFields.entrySet().stream()
                            .filter(x -> !StringUtils.isEmpty(x.getKey()))
                            .forEach(entry -> {
                                Optional<IEntityClass> searchableRelatedEntity = reader.getSearchableRelatedEntity(entry.getKey());
                                String relatedField = entry.getKey() + ".id";
                                Optional<? extends IEntityField> relationFieldOp = reader.column(relatedField);

                                if (searchableRelatedEntity.isPresent() && relationFieldOp.isPresent()) {

                                    //always assume this is long
                                    List<Long> values = finalEntities
                                            .stream()
                                            .map(entity -> entity.entityValue()
                                                    .getValue(relatedField).map(IValue::valueToLong))
                                            .filter(Optional::isPresent)
                                            .map(Optional::get)
                                            .distinct()
                                            .collect(Collectors.toList());


                                    if (!values.isEmpty()) {
                                        logger.info("Try to find related record for {}", searchableRelatedEntity.get().code());
                                        //in case idField is not absent build a dummy one;
                                        IEntityField idField = new EntityField(1, "dummy", FieldType.LONG, new FieldConfig().searchable(true).identifie(true));
                                        Conditions conditionsIds =
                                                new Conditions(new Condition(idField
                                                        , ConditionOperator.MULTIPLE_EQUALS
                                                        , values.stream().map(x -> new LongValue(idField, x)).toArray(IValue[]::new)));

                                        try {

                                            Collection<IEntity> iEntities = entitySearchService.selectByConditions(
                                                    conditionsIds,
                                                    searchableRelatedEntity.get(),
                                                    new Page(0, values.size()));

                                            //append value

                                            Map<Long, IEntity> leftEntities = iEntities.stream().collect(Collectors.toMap(IEntity::id, leftEntity -> leftEntity));

                                            finalEntities.stream().forEach(originEntity -> {
                                                Long id = originEntity.entityValue()
                                                        .getValue(relatedField).map(IValue::valueToLong).orElse(0L);

                                                if (leftEntities.get(id) != null && leftEntities.get(id).entityValue() != null) {
                                                    entry.getValue().forEach(queryFieldsUp -> {
                                                        leftEntities.get(id).entityValue().getValue(queryFieldsUp.getId()).ifPresent(value -> {
                                                            leftAppend(originEntity, entry.getKey(), value);
                                                        });
                                                    });
                                                }
                                            });

                                        } catch (SQLException ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                }
                            });
                }


                result = OperationResult.newBuilder()
                        .setCode(OperationResult.Code.OK)
                        .addAllQueryResult(Optional.ofNullable(entities).orElseGet(Collections::emptyList)
                                .stream().filter(Objects::nonNull).map(this::toEntityUp).collect(Collectors.toList()))
                        .setTotalRow(page == null || !page.isReady() ?
                                Optional.ofNullable(entities).orElseGet(Collections::emptyList).size() :
                                Long.valueOf(page.getTotalCount()).intValue())
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
     * @param entity
     * @param leftEntity
     */
    private void leftAppend(IEntity entity, IEntity leftEntity) {
        entity.entityValue().addValues(leftEntity.entityValue().values());
    }

    private void leftAppend(IEntity entity, String relName, IValue iValue) {

        IEntityField originField = iValue.getField();

        if (!originField.name().startsWith(relName.concat("."))) {
            iValue.setField(new ColumnField(relName + "." + originField.name(), originField, null));
        }
        entity.entityValue().addValue(iValue);
    }

    @Override
    public CompletionStage<OperationResult> commit(TransactionUp in, Metadata metadata) {
        Long id = Long.parseLong(in.getId());
        OperationResult result = null;

        try {

            transactionManagementService.restore(id);
            transactionManagementService.commit();
            result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.OK)
                    .setMessage("Transaction committed successfully.")
                    .buildPartial();
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
            transactionManagementService.restore(id);
            transactionManagementService.rollback();
            result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.OK)
                    .setMessage("Transaction rollback successful.")
                    .buildPartial();
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
     * TODO
     *
     * @param selectByTree
     * @param metadata
     * @return
     */
    @Override
    public CompletionStage<OperationResult> selectByTreeFilter(SelectByTree selectByTree, Metadata metadata) {
        return asyncRead(() -> {
            return OperationResult
                    .newBuilder()
                    .setCode(OperationResult.Code.UNRECOGNIZED)
                    .setMessage("Not Implemented")
                    .build();
        });
    }

    /**
     * @param entityUp
     * @param metadata
     * @return
     */
    @Override
    public CompletionStage<OperationResult> prepare(EntityUp entityUp, Metadata metadata) {
        return asyncRead(() -> {
            return OperationResult
                    .newBuilder()
                    .setCode(OperationResult.Code.UNRECOGNIZED)
                    .setMessage("Not Implemented")
                    .build();
        });
    }

    @Override
    public CompletionStage<OperationResult> selectBySql(SelectBySql in, Metadata metadata) {
        return asyncRead(() -> {
            return OperationResult
                    .newBuilder()
                    .setCode(OperationResult.Code.UNRECOGNIZED)
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
                    .setCode(OperationResult.Code.UNRECOGNIZED)
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
                                .setComment(x.getComment())
                                .setId(x.getId())
                                .setSource(x.getSource())
                                .setUsername(x.getUsername())
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
        return null;
    }

    @Override
    public CompletionStage<ChangelogCountResponse> changelogCount(ChangelogCountRequest changelogCountRequest, Metadata metadata) {
        return null;
    }

    private Optional<Long> extractTransaction(Metadata metadata) {
        Optional<String> transactionId = metadata.getText("transaction-id");
        return transactionId.map(Long::valueOf);
    }

    /**
     * @param metadata
     */
    private void logInfo(Metadata metadata, BiFunction<String, String, String> template) {
        String displayName = metadata.getText("display-name").orElse("noname");
        String userName = metadata.getText("username").orElse("noname");

        logger.info(template.apply(displayName, userName));
    }

    private EntityUp toEntityUp(IEntity entity) {
        EntityUp.Builder builder = EntityUp.newBuilder();

        builder.setObjId(entity.id());
        builder.addAllValues(entity.entityValue().values().stream()
                .map(this::toValueUp)
                .collect(Collectors.toList()));
        return builder.build();
    }

    private ValueUp toValueUp(IValue value) {
        //TODO format?
        IEntityField field = value.getField();
        return ValueUp.newBuilder()
                .setValue(toValueStr(value))
                .setName(field.name())
                .setFieldId(field.id())
                .setFieldType(field.type().name())
                .build();
    }

    private String toValueStr(IValue value) {
        String retVal
                = Match(value)
                .of(Case($(instanceOf(DateTimeValue.class)), x -> String.valueOf(x.valueToLong())),
                        Case($(), IValue::valueToString));
        return retVal;
    }



    private IEntityClass toRawEntityClass(EntityUp entityUp) {
        return new EntityClass(
                entityUp.getId()
                , entityUp.getCode()
                , null
                , Collections.emptyList()
                , null
                , entityUp.getFieldsList().stream().map(this::toEntityField).collect(Collectors.toList())
        );
    }


    //TODO
    private IEntityField toEntityField(FieldUp fieldUp) {
        return EntityField.Builder.anEntityField()
                .withId(fieldUp.getId())
                .withName(fieldUp.getCode())
                .withFieldType(FieldType.valueOf(fieldUp.getFieldType()))
                .withConfig(FieldConfig.build()
                        .searchable(ofEmptyStr(fieldUp.getSearchable())
                                .map(Boolean::valueOf).orElse(false))
                        .max(ofEmptyStr(fieldUp.getMaxLength())
                                .map(String::valueOf)
                                .map(Long::parseLong).orElse(-1L))
                        .min(ofEmptyStr(fieldUp.getMinLength()).map(String::valueOf)
                                .map(Long::parseLong).orElse(-1L))
                        .precision(fieldUp.getPrecision())
                        .identifie(fieldUp.getIdentifier())).build();
    }


    private Relation toEntityRelation(RelationUp relationUp) {
        return new Relation(relationUp.getName()
                , relationUp.getRelatedEntityClassId()
                , relationUp.getRelationType()
                , relationUp.getIdentity()
                , relationUp.hasEntityField() ? toEntityField(relationUp.getEntityField()) : null);
    }

    private IEntityClass getSubEntityClass(EntityUp entityUp) {
        boolean hasSubClass = entityUp.hasField(EntityUp.getDescriptor().findFieldByNumber(EntityUp.SUBENTITYCLASS_FIELD_NUMBER));

        if (hasSubClass) {
            return toEntityClass(entityUp.getSubEntityClass());
        }

        return null;
    }

    private IEntityClass toEntityClass(EntityUp entityUp) {

        boolean hasExtendedClass = entityUp.hasField(EntityUp.getDescriptor().findFieldByNumber(EntityUp.EXTENDENTITYCLASS_FIELD_NUMBER));

        //Long id, String code, String relation, List<IEntityClass> entityClasss, IEntityClass extendEntityClass, List<Field> fields
        IEntityClass entityClass = new EntityClass(
                entityUp.getId()
                , entityUp.getCode()
                , entityUp.getRelationList().stream()
                .map(this::toEntityRelation)
                .collect(Collectors.toList())
                , entityUp.getEntityClassesList().stream()
                .map(this::toRawEntityClass)
                .collect(Collectors.toList())
                , hasExtendedClass ? toRawEntityClass(entityUp.getExtendEntityClass()) : null
                , entityUp.getFieldsList().stream().map(this::toEntityField).collect(Collectors.toList())
        );
        return entityClass;
    }


    //private helper
    private List<IValue> toTypedValue(IEntityField entityField, String value) {
        return entityField.type().toTypedValue(entityField, value).map(Collections::singletonList).orElseGet(Collections::emptyList);
    }
}
