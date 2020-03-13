package com.xforceplus.ultraman.oqsengine.sdk.service.impl;

import akka.grpc.javadsl.SingleResponseRequestBuilder;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IEntityClassHelper;
import com.xforceplus.ultraman.oqsengine.sdk.*;
import com.xforceplus.ultraman.oqsengine.sdk.handler.EntityMetaHandler;
import com.xforceplus.ultraman.oqsengine.sdk.service.ContextService;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityService;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.MetadataRepository;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionQueryRequest;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.EntityItem;
import com.xforceplus.xplat.galaxy.framework.context.ContextKeys;
import com.xforceplus.xplat.galaxy.framework.context.ContextService;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.sdk.util.EntityClassToGrpcConverter.*;
import static com.xforceplus.xplat.galaxy.framework.context.ContextKeys.StringKeys.*;

public class EntityServiceImpl implements EntityService {

    private final MetadataRepository metadataRepository;

    private final EntityServiceClient entityServiceClient;

    private final ContextService contextService;

    @Autowired
    private EntityMetaHandler entityMetaHandler;

    public EntityServiceImpl(MetadataRepository metadataRepository, EntityServiceClient entityServiceClient, ContextService contextService) {
        this.metadataRepository = metadataRepository;
        this.entityServiceClient = entityServiceClient;
        this.contextService = contextService;
    }


    @Override
    public Optional<EntityClass> load(String boId){

        String tenantId = contextService.get(TENANTID_KEY);
        String appCode  = contextService.get(APPCODE);

        return metadataRepository.load(tenantId, appCode, boId);
    }

    @Override
    public Optional<EntityClass> loadByCode(String bocode) {
        String tenantId = contextService.get(TENANTID_KEY);
        String appCode  = contextService.get(APPCODE);

        return metadataRepository.loadByCode(tenantId, appCode, bocode);
    }

    @Override
    public <T> Either<String, T> transactionalExecute(Callable<T> supplier){
        OperationResult result = entityServiceClient
                .commit(TransactionUp.newBuilder().build()).toCompletableFuture().join();

        if(result.getCode() == OperationResult.Code.OK){
            contextService.set(TRANSACTION_KEY, result.getTransactionResult());
            try {
                T t = supplier.call();
                CompletableFuture<T> commitedT = entityServiceClient.commit(TransactionUp.newBuilder()
                        .setId(result.getTransactionResult())
                        .build()).thenApply(x -> {
                            if(x.getCode() == OperationResult.Code.OK){
                                return t;
                            }else{
                                throw new RuntimeException("事务提交失败");
                            }}).toCompletableFuture();

                if(commitedT.isCompletedExceptionally()){
                    return Either.left("事务提交失败");
                }else {
                    return Either.right(commitedT.join());
                }
            }catch(Exception ex){
                entityServiceClient.rollBack(TransactionUp.newBuilder()
                        .setId(result.getTransactionResult())
                        .build());
                return Either.left(ex.getMessage());
            }
        }else{
            return Either.left("事务创建失败");
        }
    }

    @Override
    public Either<String, Map<String, Object>> findOne(EntityClass entityClass, long id) {
        String transId = contextService.get(TRANSACTION_KEY);

        SingleResponseRequestBuilder<EntityUp, OperationResult> queryResultBuilder = entityServiceClient.selectOne();

        if(transId != null) {
            queryResultBuilder.addHeader("transaction-id", transId);
        }

        OperationResult queryResult = queryResultBuilder.invoke(toEntityUp(entityClass, id))
                .toCompletableFuture().join();

        if( queryResult.getCode() == OperationResult.Code.OK ){
            if(queryResult.getTotalRow() > 0) {
                return Either.right(toResultMap(entityClass, queryResult.getQueryResultList().get(0)));
            } else {
                return Either.left("未查询到记录");
            }
        }else{
            return Either.left(queryResult.getMessage());
        }
    }

    /**
     * //TODO transaction
     * return affect row
     * @param entityClass
     * @param id
     * @return
     */
    @Override
    public Either<String, Integer> deleteOne(EntityClass entityClass, Long id) {

        String transId = contextService.get(TRANSACTION_KEY);
        SingleResponseRequestBuilder<EntityUp, OperationResult> removeBuilder = entityServiceClient.remove();
        if(transId != null){
            removeBuilder.addHeader("transaction-id", transId);
        }

        OperationResult updateResult =
                removeBuilder.invoke(toEntityUp(entityClass, id))
                        .toCompletableFuture().join();

        if(updateResult.getCode() == OperationResult.Code.OK){
            int rows = updateResult.getAffectedRow();
            return Either.right(rows);
        }else{
            //indicates
            return Either.left(updateResult.getMessage());
        }
    }

    @Override
    public Either<String, Integer> updateById(EntityClass entityClass, Long id, Map<String, Object> body){

        String transId = contextService.get(TRANSACTION_KEY);
        SingleResponseRequestBuilder<EntityUp, OperationResult> replaceBuilder = entityServiceClient.replace();
        if(transId != null){
            replaceBuilder.addHeader("transaction-id", transId);
        }
        //处理系统字段的逻辑-add by wz
        body = entityMetaHandler.updateFill(entityClass,body);

        OperationResult updateResult = entityServiceClient.replace()
                .invoke(toEntityUp(entityClass, id, body))
                .toCompletableFuture().join();

        if(updateResult.getCode() == OperationResult.Code.OK){
            int rows = updateResult.getAffectedRow();
            return Either.right(rows);
        }else{
            //indicates
            return Either.left(updateResult.getMessage());
        }
    }

    /**
     * query
     * @param entityClass
     * @param condition
     * @return
     */
    @Override
    public Either<String, Tuple2<Integer, List<Map<String, Object>>>> findByCondition(EntityClass entityClass, ConditionQueryRequest condition) {

        String transId = contextService.get(TRANSACTION_KEY);


        SingleResponseRequestBuilder<SelectByCondition, OperationResult> requestBuilder = entityServiceClient.selectByConditions();

        if(transId != null){
            requestBuilder.addHeader("transaction-id", transId);
        }

        OperationResult result = requestBuilder.invoke(toSelectByCondition(entityClass, condition))
                .toCompletableFuture().join();

        if(result.getCode() == OperationResult.Code.OK) {
            List<Map<String, Object>> repList = result.getQueryResultList()
                    .stream()
                    .map(x -> {
                        Map<String, Object> resultMap = toResultMap(entityClass, x);
                        return filterItem(resultMap, x.getCode(), condition.getEntity());
                    }).collect(Collectors.toList());
            Tuple2<Integer, List<Map<String, Object>>> queryResult = Tuple.of(result.getTotalRow(), repList);
            return Either.right(queryResult);
        }else{
            return Either.left(result.getMessage());
        }
    }

    @Override
    public Either<String, Long> create(EntityClass entityClass, Map<String, Object> body) {

        String transId = contextService.get(TRANSACTION_KEY);

        SingleResponseRequestBuilder<EntityUp, OperationResult> buildBuilder = entityServiceClient.build();

        if(transId != null){
            buildBuilder.addHeader("transaction-id", transId);
        }
        //处理系统字段的逻辑-add by wz
        body = entityMetaHandler.insertFill(entityClass,body);

        OperationResult createResult = buildBuilder
                .invoke(toEntityUp(entityClass, null, body))
                .toCompletableFuture().join();

        if(createResult.getCode() == OperationResult.Code.OK){
            if(createResult.getIdsList().size() < 1 ) {
                return Either.left("未获得结果");
            }else{
                return Either.right(createResult.getIdsList().get(0));
            }
        }else{
            //indicates
            return Either.left(createResult.getMessage());
        }
    }

    @Override
    public Integer count(EntityClass entityClass, ConditionQueryRequest condition) {
        String transId = contextService.get(TRANSACTION_KEY);


        SingleResponseRequestBuilder<SelectByCondition, OperationResult> requestBuilder = entityServiceClient.selectByConditions();

        if(transId != null){
            requestBuilder.addHeader("transaction-id", transId);
        }

        OperationResult result = requestBuilder.invoke(toSelectByCondition(entityClass, condition))
                .toCompletableFuture().join();

        if(result.getCode() == OperationResult.Code.OK) {
            return result.getTotalRow();
        }else{
            return 0;
        }
    }
}
