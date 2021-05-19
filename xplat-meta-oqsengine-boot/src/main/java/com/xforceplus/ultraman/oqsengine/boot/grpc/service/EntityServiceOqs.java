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

import akka.grpc.javadsl.Metadata;
import com.xforceplus.ultraman.oqsengine.boot.grpc.utils.EntityClassHelper;
import com.xforceplus.ultraman.oqsengine.changelog.ReplayService;
import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangeVersion;
import com.xforceplus.ultraman.oqsengine.changelog.domain.EntityAggDomain;
import com.xforceplus.ultraman.oqsengine.changelog.domain.EntityDomain;
import com.xforceplus.ultraman.oqsengine.changelog.storage.query.QueryStorage;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.TransactionManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.ServiceSelectConfig;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.sdk.ChangelogCountRequest;
import com.xforceplus.ultraman.oqsengine.sdk.ChangelogCountResponse;
import com.xforceplus.ultraman.oqsengine.sdk.ChangelogCountSingle;
import com.xforceplus.ultraman.oqsengine.sdk.ChangelogRequest;
import com.xforceplus.ultraman.oqsengine.sdk.ChangelogResponse;
import com.xforceplus.ultraman.oqsengine.sdk.ChangelogResponseList;
import com.xforceplus.ultraman.oqsengine.sdk.CompatibleRequest;
import com.xforceplus.ultraman.oqsengine.sdk.ConditionsUp;
import com.xforceplus.ultraman.oqsengine.sdk.EntityServicePowerApi;
import com.xforceplus.ultraman.oqsengine.sdk.EntityUp;
import com.xforceplus.ultraman.oqsengine.sdk.FieldSortUp;
import com.xforceplus.ultraman.oqsengine.sdk.Filters;
import com.xforceplus.ultraman.oqsengine.sdk.OperationResult;
import com.xforceplus.ultraman.oqsengine.sdk.QueryFieldsUp;
import com.xforceplus.ultraman.oqsengine.sdk.ReplayRequest;
import com.xforceplus.ultraman.oqsengine.sdk.SelectByCondition;
import com.xforceplus.ultraman.oqsengine.sdk.SelectBySql;
import com.xforceplus.ultraman.oqsengine.sdk.SelectByTree;
import com.xforceplus.ultraman.oqsengine.sdk.TransRequest;
import com.xforceplus.ultraman.oqsengine.sdk.TransactionUp;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.CacheEventHandler;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
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

    @Autowired
    private CacheEventHandler cacheEventHandler;

    private static final String ENTITYCLASS_NOT_FOUND = "Requested EntityClass not found in current OqsEngine";

    @Autowired
    private MetaManager metaManager;

    @Resource(name = "callReadThreadPool")
    private ExecutorService asyncReadDispatcher;

    @Resource(name = "callWriteThreadPool")
    private ExecutorService asyncWriteDispatcher;

    @Resource(name = "callChangelogThreadPool")
    private ExecutorService asyncChangelogDispatcher;

    @Autowired(required = false)
    private QueryStorage queryStorage;

    @Autowired(required = false)
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
        Optional<IEntityClass> entityClassOp = metaManager.load(entityClassRef);
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
                com.xforceplus.ultraman.oqsengine.core.service.pojo.OperationResult operationResult =
                    entityManagementService.build(entity);
                long txId = operationResult.getTxId();
                long version = operationResult.getVersion();
                ResultStatus resultStatus = operationResult.getResultStatus();
                if (resultStatus == ResultStatus.SUCCESS) {
                    OperationResult.Builder builder = OperationResult.newBuilder()
                        .addIds(entity.id())
                        .addIds(txId)
                        .addIds(version);

                    result = builder.setCode(OperationResult.Code.OK).buildPartial();
                } else {
                    throw new RuntimeException(resultStatus.name() + ":" + operationResult.getMessage());
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
     * fill all empty field to empty.
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

            String profile = extractProfile(metadata).orElse("");

            //check entityRef
            EntityClassRef entityClassRef = EntityClassHelper.toEntityClassRef(in, profile);
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
                com.xforceplus.ultraman.oqsengine.core.service.pojo.OperationResult operationResult =
                    entityManagementService.replace(entity);
                long txId = operationResult.getTxId();
                int version = operationResult.getVersion();
                ResultStatus replaceStatus = operationResult.getResultStatus();
                switch (replaceStatus) {
                    case SUCCESS:
                        result = OperationResult.newBuilder()
                            .setAffectedRow(1)
                            .setCode(OperationResult.Code.OK)
                            .addIds(txId)
                            .addIds(version)
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
                    case NOT_FOUND:
                        //send to sdk
                        result = OperationResult.newBuilder()
                            .setAffectedRow(0)
                            .setCode(OperationResult.Code.FAILED)
                            .setMessage(notFound("No record found."))
                            .buildPartial();
                        break;

                    default:
                        //unreachable code
                        result = OperationResult.newBuilder()
                            .setAffectedRow(0)
                            .setCode(OperationResult.Code.FAILED)
                            .setMessage(
                                other(String.format("Unknown response status %s.",
                                    replaceStatus != null ? replaceStatus.name() : "NULL")))
                            .buildPartial();
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
                    sortField = toEntityField(entityClass, sortUp.getField());
                }

                if (!sortField.isPresent()) {
                    Optional<Conditions> consOp = toConditions(entityClass, conditions, in.getIdsList(), metaManager);
                    if (consOp.isPresent()) {
                        entities = entitySearchService.selectByConditions(consOp.get(), entityClassRef, page);
                    } else {
                        entities = entitySearchService.selectByConditions(
                            Conditions.buildEmtpyConditions(), entityClassRef, page);
                    }
                } else {
                    FieldSortUp sortUp = sort.get(0);
                    Sort sortParam;
                    if (sortUp.getOrder() == FieldSortUp.Order.asc) {
                        sortParam = Sort.buildAscSort(sortField.get());
                    } else {
                        sortParam = Sort.buildDescSort(sortField.get());
                    }

                    Optional<Conditions> consOp = toConditions(entityClass, conditions, in.getIdsList(), metaManager);
                    if (consOp.isPresent()) {
                        entities =
                            entitySearchService.selectByConditions(consOp.get(), entityClassRef, sortParam, page);
                    } else {
                        entities = entitySearchService.selectByConditions(
                            Conditions.buildEmtpyConditions(), entityClassRef, page);
                    }
                }

                //----------------------------
                AtomicInteger affected = new AtomicInteger(0);
                Optional<String> mode = metadata.getText("mode");
                if (!entities.isEmpty()) {

                    entities.forEach(entityResult -> {

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

                Entity targetEntity =
                    Entity.Builder.anEntity().withId(in.getObjId()).withEntityClassRef(entityClassRef).build();

                if (!Boolean.parseBoolean(force)) {
                    com.xforceplus.ultraman.oqsengine.core.service.pojo.OperationResult operationResult =
                        entityManagementService.delete(targetEntity);
                    long txId = operationResult.getTxId();
                    long version = operationResult.getVersion();
                    ResultStatus deleteStatus = operationResult.getResultStatus();

                    switch (deleteStatus) {
                        case SUCCESS:
                            result = OperationResult.newBuilder()
                                .setAffectedRow(1)
                                .setCode(OperationResult.Code.OK)
                                .addIds(txId)
                                .addIds(version)
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
                        case NOT_FOUND:
                            //send to sdk
                            result = OperationResult.newBuilder()
                                .setAffectedRow(0)
                                .setCode(OperationResult.Code.OK)
                                .buildPartial();
                            break;
                        default:
                            //unreachable code
                            result = OperationResult.newBuilder()
                                .setAffectedRow(0)
                                .setCode(OperationResult.Code.FAILED)
                                .setMessage(
                                    other(String.format("Unknown response status %s.",
                                        deleteStatus != null ? deleteStatus.name() : "NULL")))
                                .buildPartial();
                    }
                } else {
                    com.xforceplus.ultraman.oqsengine.core.service.pojo.OperationResult operationResult =
                        entityManagementService.delete(targetEntity);
                    long txId = operationResult.getTxId();
                    long version = operationResult.getVersion();
                    ResultStatus resultStatus = operationResult.getResultStatus();
                    switch (resultStatus) {
                        case SUCCESS:
                            result = OperationResult.newBuilder()
                                .setAffectedRow(1)
                                .setCode(OperationResult.Code.OK)
                                .addIds(txId)
                                .addIds(version)
                                .buildPartial();
                            break;
                        case NOT_FOUND:
                            //send to sdk
                            result = OperationResult.newBuilder()
                                .setAffectedRow(0)
                                .setCode(OperationResult.Code.OK)
                                .buildPartial();
                            break;
                        default:
                            //unreachable code
                            result = OperationResult.newBuilder()
                                .setAffectedRow(0)
                                .setCode(OperationResult.Code.FAILED)
                                .setMessage(
                                    other(String.format("Unknown response status %s.",
                                        resultStatus != null ? resultStatus.name() : "NULL")))
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

                Optional<IEntity> ds = entitySearchService.selectOne(in.getObjId(), entityClassRef);

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

                Page page = null;

                List<FieldSortUp> sort = in.getSortList();


                int pageNo = in.getPageNo();
                int pageSize = in.getPageSize();
                page = new Page(pageNo, pageSize);

                Optional<? extends IEntityField> sortField;

                if (sort == null || sort.isEmpty()) {
                    sortField = Optional.empty();
                } else {
                    FieldSortUp sortUp = sort.get(0);
                    //get related field
                    sortField = toEntityField(entityClass, sortUp.getField());
                }

                Optional<Conditions> extraCondition = Optional.empty();
                if (in.hasTree()) {
                    SelectByTree tree = in.getTree();
                    Filters filters = tree.getFilters();
                    extraCondition = toConditions(entityClass, filters, metaManager);
                }

                Sort sortParam = Sort.buildOutOfSort();
                if (sortField.isPresent()) {
                    FieldSortUp sortUp = sort.get(0);

                    if (sortUp.getOrder() == FieldSortUp.Order.asc) {
                        sortParam = Sort.buildAscSort(sortField.get());
                    } else {
                        sortParam = Sort.buildDescSort(sortField.get());
                    }
                }

                ServiceSelectConfig serviceSelectConfig = ServiceSelectConfig
                    .Builder.anSearchConfig()
                    .withSort(sortParam)
                    .withPage(page)
                    .withFilter(extraCondition.orElseGet(Conditions::buildEmtpyConditions))
                    .build();

                Optional<Conditions> consOp = toConditions(
                    entityClass, in.getConditions(), in.getIdsList(), metaManager);

                if (consOp.isPresent()) {

                    entities = entitySearchService.selectByConditions(consOp.get(), entityClassRef, serviceSelectConfig);
                } else {
                    entities = entitySearchService.selectByConditions(
                        Conditions.buildEmtpyConditions(), entityClassRef, serviceSelectConfig);
                }

                result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.OK)
                    .addAllQueryResult(Optional.ofNullable(entities).orElseGet(Collections::emptyList)
                        .stream().filter(Objects::nonNull)
                        .map(EntityClassHelper::toEntityUp).collect(Collectors.toList()))
                    .setTotalRow(page == null || !page.isReady()
                        ? Optional.ofNullable(entities).orElseGet(Collections::emptyList).size()
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
     * 未实现.
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
     * SDK连接应该第一个调用的准备动作.
     */
    @Override
    public CompletionStage<OperationResult> prepare(EntityUp entityUp, Metadata metadata) {
        return asyncRead(() -> {

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
            long txId = transRequest.getTxId();
            //ver is 0
            long ver = transRequest.getVer();
            long objId = transRequest.getObjId();
            int transType = transRequest.getTransType();
            String type = transRequest.getType();
            EventType eventType = null;
            if (!StringUtils.isEmpty(type)) {
                eventType = EventType.valueOf(type);
            }
            Collection<String> payloads = cacheEventHandler
                .eventsQuery(txId, objId, ver == 0 ? null : Long.valueOf(ver).intValue(),
                    eventType == null ? null : eventType.ordinal());
            return OperationResult.newBuilder()
                .setCode(OperationResult.Code.OK)
                .setMessage("[" + payloads.stream().collect(Collectors.joining(",")) + "]")
                .build();
        });
    }

    private Optional<Long> extractTransaction(Metadata metadata) {
        Optional<String> transactionId = metadata.getText("transaction-id");
        return transactionId.map(Long::valueOf);
    }

    private Optional<String> extractProfile(Metadata metadata) {
        Optional<String> profile = metadata.getText("profile");
        return profile;
    }

    private void logInfo(Metadata metadata, BiFunction<String, String, String> template) {
        String displayName = metadata.getText("display-name").orElse("noname");
        String userName = metadata.getText("username").orElse("noname");

        logger.info(template.apply(displayName, userName));
    }
}
