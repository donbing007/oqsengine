package com.xforceplus.ultraman.oqsengine.sdk.service.impl;

import akka.grpc.javadsl.SingleResponseRequestBuilder;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.reader.record.Record;
import com.xforceplus.ultraman.oqsengine.sdk.*;
import com.xforceplus.ultraman.oqsengine.sdk.event.EntityCreated;
import com.xforceplus.ultraman.oqsengine.sdk.event.EntityDeleted;
import com.xforceplus.ultraman.oqsengine.sdk.event.EntityUpdated;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityService;
import com.xforceplus.ultraman.oqsengine.sdk.service.*;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.MetadataRepository;
import com.xforceplus.ultraman.oqsengine.sdk.util.ConditionQueryRequestHelper;
import com.xforceplus.ultraman.oqsengine.sdk.util.context.ContextDecorator;
import com.xforceplus.ultraman.oqsengine.sdk.util.flow.FlowRegistry;
import com.xforceplus.ultraman.oqsengine.sdk.util.flow.QueueFlow;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionQueryRequest;
import com.xforceplus.xplat.galaxy.framework.context.ContextService;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.sdk.util.EntityClassToGrpcConverter.*;
import static com.xforceplus.xplat.galaxy.framework.context.ContextKeys.StringKeys.*;

/**
 * main service for entity
 */
public class EntityServiceImpl implements EntityService {

    private final MetadataRepository metadataRepository;

    private final EntityServiceClient entityServiceClient;

    private final ContextService contextService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private HandleValueService handlerValueService;

    @Autowired
    private HandleQueryValueService handleQueryValueService;

    @Autowired
    private HandleResultValueService handleResultValueService;

    @Autowired
    private RetryRegistry retryRegistry;

    @Autowired
    private FlowRegistry flowRegistry;

    @Value("${xplat.oqsengine.sdk.cas.retry.max-attempts:2}")
    private int maxAttempts;

    @Value("${xplat.oqsengine.sdk.cas.retry.delay:100}")
    private int delay;

    @Value("${xplat.oqsengine.sdk.strict.range:true}")
    private boolean isRangeStrict;

    private Logger logger = LoggerFactory.getLogger(EntityService.class);

    public EntityServiceImpl(MetadataRepository metadataRepository, EntityServiceClient entityServiceClient, ContextService contextService) {
        this.metadataRepository = metadataRepository;
        this.entityServiceClient = entityServiceClient;
        this.contextService = contextService;
    }

    @Override
    public Optional<IEntityClass> load(String boId) {

        String tenantId = contextService.get(TENANTID_KEY);
        String appCode = contextService.get(APPCODE);

        return metadataRepository.load(tenantId, appCode, boId);
    }

    @Override
    public Optional<IEntityClass> load(String boId, String version) {

        String tenantId = contextService.get(TENANTID_KEY);
        String appCode = contextService.get(APPCODE);

        return metadataRepository.load(tenantId, appCode, boId, version);
    }

    @Override
    public Optional<IEntityClass> loadByCode(String bocode) {

        String tenantId = contextService.get(TENANTID_KEY);
        String appCode = contextService.get(APPCODE);

        return metadataRepository.loadByCode(tenantId, appCode, bocode);
    }

    @Override
    public Optional<IEntityClass> loadByCode(String bocode, String version) {

        String tenantId = contextService.get(TENANTID_KEY);
        String appCode = contextService.get(APPCODE);

        return metadataRepository.loadByCode(tenantId, appCode, bocode, version);
    }

    @Override
    public <T> Either<String, T> transactionalExecute(Callable<T> supplier) {
        //maybe timeout
        OperationResult result = entityServiceClient
                .begin(TransactionUp.newBuilder().build()).toCompletableFuture().join();

        if (result.getCode() == OperationResult.Code.OK) {
            logger.info("Transaction create success with id:{}", result.getTransactionResult());
            contextService.set(TRANSACTION_KEY, result.getTransactionResult());
            logger.debug("set currentService with {}", result.getTransactionResult());
            try {
                T t = supplier.call();
                CompletableFuture<Either<String, T>> commitedT = entityServiceClient.commit(TransactionUp.newBuilder()
                        .setId(result.getTransactionResult())
                        .build())
                        .exceptionally(ex -> {
                            logger.error("Transaction with id:{} failed to commit with exception {}", result.getTransactionResult(), ex.getMessage());
                            return OperationResult
                                    .newBuilder()
                                    .setCode(OperationResult.Code.EXCEPTION)
                                    .setMessage(ex.getMessage())
                                    .buildPartial();
                        }).thenApply(x -> {
                            if (x.getCode() == OperationResult.Code.OK) {
                                logger.info("Transaction with id:{} has committed successfully ", result.getTransactionResult());
                                return Either.<String, T>right(t);
                            } else {
                                logger.error("Transaction with id:{} failed to commit", result.getTransactionResult());
                                return Either.<String, T>left("事务提交失败:" + x.getMessage());
                            }
                        }).toCompletableFuture();

                return commitedT.join();
            } catch (Exception ex) {
                //maybe timeout
                try {
                    entityServiceClient.rollBack(TransactionUp.newBuilder()
                            .setId(result.getTransactionResult())
                            .build()).toCompletableFuture().join();
                    return Either.left(ex.getMessage());
                } catch (Exception bindEx) {
                    return Either.left(bindEx.getMessage());
                }
            } finally {
                //remove TRANSACTION_KEY
                logger.info("remove currentService {} with {}", TRANSACTION_KEY.name(), result.getTransactionResult());
                contextService.set(TRANSACTION_KEY, null);
            }
        } else {
            return Either.left("事务创建失败");
        }
    }

    @Override
    public Either<String, Map<String, Object>> findOne(IEntityClass entityClass, long id) {
        String transId = contextService.get(TRANSACTION_KEY);

        SingleResponseRequestBuilder<EntityUp, OperationResult> queryResultBuilder = entityServiceClient.selectOne();

        if (transId != null) {
            logger.info("Query with Transaction id:{} ", transId);
            queryResultBuilder = queryResultBuilder.addHeader("transaction-id", transId);
        } else {
            logger.debug("Query without Transaction");
        }

        OperationResult queryResult = queryResultBuilder.invoke(toEntityUp(entityClass, id))
                .toCompletableFuture().join();

        if (queryResult.getCode() == OperationResult.Code.OK) {
            if (queryResult.getTotalRow() > 0) {

                return Either.right(
                        handleResultValueService.toRecord(entityClass, queryResult.getQueryResultList().get(0))
                                .toMap(null));
            } else {
                return Either.left("未查询到记录");
            }
        } else {
            return Either.left(queryResult.getMessage());
        }
    }

    @Override
    public <T> Either<String, T> retryExecute(String key, Supplier<Either<String, T>> supplier) {

        QueueFlow<Either<String, T>> queueFlow = flowRegistry.flow(key);
        CompletableFuture<Either<String, T>> future = new CompletableFuture<>();

        //this is fixed by only retry for update and delete

        RetryConfig config = RetryConfig.<Either<String, T>>custom()
                .maxAttempts(maxAttempts)
                .waitDuration(Duration.ofMillis(delay))
                .retryOnResult(response -> response == null || (response.isLeft() && "CONFLICT".equalsIgnoreCase(response.getLeft())))
                .build();

        Retry retry = retryRegistry.retry("retry-" + UUID.randomUUID().toString(), config);

        retry.getEventPublisher().onRetry(retryEvt -> {
            logger.info("Trigger Retry {} attempts {} left {}", retryEvt.getName(), retryEvt.getNumberOfRetryAttempts()
                    , maxAttempts - retryEvt.getNumberOfRetryAttempts());
        });

        queueFlow.feed(Tuple.of(future, Retry.decorateSupplier(retry, ContextDecorator.decorateSupplier(contextService, supplier))));

        //TODO
        return future.join();
    }

    /**
     * //TODO transaction
     * return affect row
     *
     * @param entityClass
     * @param id
     * @return
     */
    @Override
    public Either<String, Integer> deleteOne(IEntityClass entityClass, Long id) {

        String transId = contextService.get(TRANSACTION_KEY);
        SingleResponseRequestBuilder<EntityUp, OperationResult> removeBuilder = entityServiceClient.remove();
        if (transId != null) {
            logger.info("delete with Transaction id:{} ", transId);
            removeBuilder = removeBuilder.addHeader("transaction-id", transId);
        }

        //monitor delete action
        String userDisplayName = contextService.get(USER_DISPLAYNAME);
        String userName = contextService.get(USERNAME);

        if (userDisplayName != null) {
            removeBuilder = removeBuilder.addHeader("display-name", userDisplayName);
        }

        if (userName != null) {
            removeBuilder = removeBuilder.addHeader("username", userDisplayName);
        }


        OperationResult updateResult =
                removeBuilder.invoke(toEntityUp(entityClass, id))
                        .toCompletableFuture().join();

        if (updateResult.getCode() == OperationResult.Code.OK) {
            int rows = updateResult.getAffectedRow();

            if (rows > 0) {
                publisher.publishEvent(buildDeleteEvent(entityClass, id));
            }

            return Either.right(rows);
        } else {
            //indicates
            return Either.left(updateResult.getMessage());
        }
    }

    private Map<String, String> getContext() {
        Map<String, String> map = new HashMap<>();
        map.put(TENANTID_KEY.name(), contextService.get(TENANTID_KEY));
        map.put(TENANTCODE_KEY.name(), contextService.get(TENANTCODE_KEY));
        map.put(USERNAME.name(), contextService.get(USERNAME));
        map.put(USER_DISPLAYNAME.name(), contextService.get(USER_DISPLAYNAME));
        return map;
    }

    @Override
    public Either<String, Integer> updateById(IEntityClass entityClass, Long id, Map<String, Object> body) {

        String transId = contextService.get(TRANSACTION_KEY);
        SingleResponseRequestBuilder<EntityUp, OperationResult> replaceBuilder = entityServiceClient.replace();
        if (transId != null) {
            logger.info("updateById with Transaction id:{} ", transId);
            replaceBuilder = replaceBuilder.addHeader("transaction-id", transId);
        }
        //处理系统字段的逻辑-add by wz
//        body = entityMetaHandler.updateFill(entityClass,body);

        List<ValueUp> valueUps = handlerValueService.handlerValue(entityClass, body, OperationType.UPDATE);

        OperationResult updateResult = replaceBuilder
                .invoke(toEntityUp(entityClass, id, valueUps))
                .toCompletableFuture().join();

        if (updateResult.getCode() == OperationResult.Code.OK) {
            int rows = updateResult.getAffectedRow();

            if (rows > 0) {
                publisher.publishEvent(buildUpdatedEvent(entityClass, id, body));
            }

            return Either.right(rows);
        } else {
            //indicates
            return Either.left(updateResult.getMessage());
        }
    }

    @Override
    public Either<String, Integer> updateByCondition(IEntityClass entityClass, ConditionQueryRequest condition, Map<String, Object> body) {
        String transId = contextService.get(TRANSACTION_KEY);

        SingleResponseRequestBuilder<SelectByCondition, OperationResult> requestBuilder = entityServiceClient.replaceByCondition();
        if (transId != null) {
            logger.info("updateByCondition with Transaction id:{} ", transId);
            requestBuilder = requestBuilder.addHeader("transaction-id", transId);
        }

        List<ValueUp> valueUps = handlerValueService.handlerValue(entityClass, body, OperationType.UPDATE);
        ConditionsUp conditionsUp = Optional.ofNullable(condition)
                .map(ConditionQueryRequest::getConditions)
                .map(x ->
                        handleQueryValueService
                                .handleQueryValue(entityClass, condition.getConditions(), OperationType.QUERY))
                .orElseGet(() -> ConditionsUp.newBuilder().build());

        SelectByCondition sbc = toUpdateSelection(entityClass, () -> toEntityUp(entityClass, null, valueUps), condition, conditionsUp);

        OperationResult updateResult = requestBuilder
                .invoke(sbc)
                .toCompletableFuture().join();

        if (updateResult.getCode() == OperationResult.Code.OK) {
            int rows = updateResult.getAffectedRow();

            if (rows > 0) {
                updateResult.getIdsList().forEach(x -> {
                    publisher.publishEvent(buildUpdatedEvent(entityClass, x, body));
                });
            }

            return Either.right(rows);
        } else {
            //indicates
            return Either.left(updateResult.getMessage());
        }
    }

    @Override
    public Either<String, Integer> replaceById(IEntityClass entityClass, Long id, Map<String, Object> body) {
        String transId = contextService.get(TRANSACTION_KEY);
        SingleResponseRequestBuilder<EntityUp, OperationResult> replaceBuilder = entityServiceClient.replace();
        if (transId != null) {
            logger.info("replaceById with Transaction id:{} ", transId);
            replaceBuilder = replaceBuilder.addHeader("transaction-id", transId);
        }

        replaceBuilder = replaceBuilder.addHeader("mode", "replace");

        //处理系统字段的逻辑-add by wz
//        body = entityMetaHandler.updateFill(entityClass,body);

        List<ValueUp> valueUps = handlerValueService.handlerValue(entityClass, body, OperationType.REPLACE);

        OperationResult updateResult = replaceBuilder
                .invoke(toEntityUp(entityClass, id, valueUps))
                .toCompletableFuture().join();

        if (updateResult.getCode() == OperationResult.Code.OK) {
            int rows = updateResult.getAffectedRow();

            if (rows > 0) {
                publisher.publishEvent(buildUpdatedEvent(entityClass, id, body));
            }

            return Either.right(rows);
        } else {
            //indicates
            return Either.left(updateResult.getMessage());
        }
    }


    /**
     * Record has all field and not filtered;
     * for export
     *
     * @param entityClass
     * @param condition
     * @return
     */
    @Override
    public Either<String, Tuple2<Integer, List<Record>>> findRecordsByCondition(IEntityClass entityClass, List<Long> ids, ConditionQueryRequest condition) {

        String transId = contextService.get(TRANSACTION_KEY);

        SingleResponseRequestBuilder<SelectByCondition, OperationResult> requestBuilder = entityServiceClient.selectByConditions();

        ConditionQueryRequest finalRequest = ConditionQueryRequestHelper.build(ids, condition);

        if (isRangeStrict) {
            boolean noRange = (finalRequest.getPageSize() == null || finalRequest.getPageNo() == null);
            if (noRange) {
                return Either.left("[STRICT-MODE]: RangeSearch Without Range");
            }
        }

        if (transId != null) {
            logger.info("findRecordsByCondition with Transaction id:{} ", transId);
            requestBuilder = requestBuilder.addHeader("transaction-id", transId);
        }

        /**
         * to ConditionsUp
         */
        ConditionsUp conditionsUp = Optional.ofNullable(finalRequest)
                .map(ConditionQueryRequest::getConditions)
                .map(x -> {
                    return handleQueryValueService
                            .handleQueryValue(entityClass, x, OperationType.QUERY);
                })
                .orElseGet(() -> ConditionsUp.newBuilder().build());

        /**
         * condition
         */
        OperationResult result = requestBuilder.invoke(toSelectByCondition(entityClass, finalRequest, conditionsUp))
                .toCompletableFuture().join();

        if (result.getCode() == OperationResult.Code.OK) {
            List<Record> repList = result.getQueryResultList()
                    .stream()
                    .map(x -> {
                        return handleResultValueService.toRecord(entityClass, x);
                    }).collect(Collectors.toList());
            Tuple2<Integer, List<Record>> queryResult = Tuple.of(result.getTotalRow(), repList);
            return Either.right(queryResult);
        } else {
            return Either.left(result.getMessage());
        }
    }


    /**
     * query
     *
     * @param entityClass
     * @param condition
     * @return
     */
    @Override
    public Either<String, Tuple2<Integer, List<Map<String, Object>>>> findByCondition(IEntityClass entityClass
            , ConditionQueryRequest condition) {

        return findByConditionWithIds(entityClass, null, condition);
    }

    @Override
    public Either<String, Tuple2<Integer, List<Map<String, Object>>>> findByConditionWithIds(IEntityClass entityClass
            , List<Long> ids, ConditionQueryRequest condition) {


        return findRecordsByCondition(entityClass, ids, condition).map(tuple -> {

            List<Map<String, Object>> mapResult = tuple._2().stream().map(record -> record.toMap(Optional.ofNullable(condition)
                    .map(ConditionQueryRequest::getStringKeys)
                    .orElseGet(Collections::emptySet)))
                    .collect(Collectors.toList());
            return Tuple.of(tuple._1(), mapResult);
        });
    }

    @Override
    public Either<String, Long> create(IEntityClass entityClass, Map<String, Object> body) {

        String transId = contextService.get(TRANSACTION_KEY);

        SingleResponseRequestBuilder<EntityUp, OperationResult> buildBuilder = entityServiceClient.build();

        if (transId != null) {
            logger.info("create with Transaction id:{} ", transId);
            buildBuilder = buildBuilder.addHeader("transaction-id", transId);
        }

        List<ValueUp> valueUps = handlerValueService.handlerValue(entityClass, body, OperationType.CREATE);

        OperationResult createResult = buildBuilder
                .invoke(toEntityUp(entityClass, null, valueUps))
                .toCompletableFuture().join();

        if (createResult.getCode() == OperationResult.Code.OK) {
            if (createResult.getIdsList().size() < 1) {
                return Either.left("未获得结果");
            } else {
                Long id = createResult.getIdsList().get(0);

                publisher.publishEvent(buildCreatedEvent(entityClass, id, body));

                return Either.right(id);
            }
        } else {
            //indicates
            return Either.left(createResult.getMessage());
        }
    }

    @Override
    public Integer count(IEntityClass entityClass, ConditionQueryRequest condition) {
        String transId = contextService.get(TRANSACTION_KEY);


        SingleResponseRequestBuilder<SelectByCondition, OperationResult> requestBuilder = entityServiceClient.selectByConditions();

        if (transId != null) {
            logger.info("count with Transaction id:{} ", transId);
            requestBuilder = requestBuilder.addHeader("transaction-id", transId);
        }

        OperationResult result = requestBuilder.invoke(toSelectByCondition(entityClass, null, condition))
                .toCompletableFuture().join();

        if (result.getCode() == OperationResult.Code.OK) {
            return result.getTotalRow();
        } else {
            return 0;
        }
    }

    @Override
    public List<IEntityClass> loadSonByCode(String bocode, String tenantId) {
        return this.loadSonByCode(bocode, tenantId, null);
    }

    @Override
    public List<IEntityClass> loadSonByCode(String bocode, String tenantId, String version) {

        if (StringUtils.isEmpty(tenantId)) {
            tenantId = contextService.get(TENANTID_KEY);
        }
        String appCode = contextService.get(APPCODE);

        if (version == null) {
            return metadataRepository.findSubEntitiesByCode(tenantId, appCode, bocode);
        } else {
            return metadataRepository.findSubEntitiesByCode(tenantId, appCode, bocode, version);
        }
    }

    @Override
    public List<IEntityClass> getEntityClasss() {
        return metadataRepository.findAllEntities();
    }

    /**
     * TODO move to another file
     * event related
     *
     * @param entityClass
     * @param id
     * @return
     */
    private EntityDeleted buildDeleteEvent(IEntityClass entityClass, Long id) {
        String code = entityClass.code();
        Map<String, String> context = getContext();
        return new EntityDeleted(code, id, context);
    }

    private EntityCreated buildCreatedEvent(IEntityClass entityClass, Long id, Map<String, Object> data) {
        String code = entityClass.code();
        Map<String, String> context = getContext();
        return new EntityCreated(code, id, data, context);
    }

    private EntityUpdated buildUpdatedEvent(IEntityClass entityClass, Long id, Map<String, Object> data) {
        String code = entityClass.code();
        Map<String, String> context = getContext();
        return new EntityUpdated(code, id, data, context);
    }
}
