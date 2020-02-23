package com.xforceplus.ultraman.oqsengine.sdk.service;

import akka.grpc.javadsl.SingleResponseRequestBuilder;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.sdk.*;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.MetadataRepository;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionQueryRequest;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.Conditions;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.EntityItem;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class EntityService {

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private EntityServiceClient entityServiceClient;

    @Autowired
    private ContextService contextService;

//    @Autowired(required = false)
//    private TransactionTemplate template;
//
//    @Autowired
//    private TransactionManager transactionManager;

    public Optional<EntityClass> load(String tenantId, String appCode, String boId){
        return metadataRepository.load(tenantId, appCode, boId);
    }

//    //TODO
    public <T> Either<String, T> transactionalExecute(Callable<T> supplier){
//        if(TransactionSynchronizationManager.isActualTransactionActive() && template != null){
//            String id = contextService.get(new ContextService.TransactionKey());
//            if(id != null){
//
//            }
//            return null;
//        }else{
//            return supplier.get();
//        }
        OperationResult result = entityServiceClient
                .commit(TransactionUp.newBuilder().build()).toCompletableFuture().join();

        if(result.getCode() == OperationResult.Code.OK){
            contextService.set(ContextService.StringKeys.TransactionKey, result.getTransactionResult());
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

    public Either<String, Map<String, String>> findOne(EntityClass entityClass, Long id) {
        String transId = contextService.get(ContextService.StringKeys.TransactionKey);

        SingleResponseRequestBuilder<EntityUp, OperationResult> queryResultBuilder = entityServiceClient.selectOne();

        if(transId != null) {
            queryResultBuilder.addHeader("transaction-id", transId);
        }

        OperationResult queryResult = queryResultBuilder.invoke(toEntityUpBuilder(entityClass, id).build())
                .toCompletableFuture().join();

        if(queryResult.getCode() == OperationResult.Code.OK){
            return Either.right(toResultMap(queryResult.getQueryResultList().get(0)));
        }else{
            return Either.left(queryResult.getMessage());
        }
    }

    private EntityUp toEntityUp(IEntityClass entityClass) {
        return toEntityUpBuilder(entityClass, null).build();
    }

    private EntityUp toRawEntityUp(IEntityClass entity){
        return EntityUp.newBuilder()
                .setId(entity.id())
                .setCode(entity.code())
                .addAllFields(entity.fields().stream().map(this::toFieldUp).collect(Collectors.toList()))
                .build();
    }

    private EntityUp.Builder toEntityUpBuilder(IEntityClass entityClass, Long id){

        EntityUp.Builder builder = EntityUp.newBuilder();

        if(entityClass.extendEntityClass() != null){
            //this entityClass is a subEntity so we get the extend entity first
            IEntityClass parent = entityClass.extendEntityClass();
            EntityUp parentUp = toRawEntityUp(parent);
            builder.addEntityClasses(parentUp);
        }

        if(id == null) {
            builder.setObjId(id);
        }

        builder.setId(entityClass.id())
                .setCode(entityClass.code())
                .addAllFields(entityClass.fields().stream().map(this::toFieldUp).collect(Collectors.toList()));

        if(entityClass.entityClasss() != null && entityClass.entityClasss().isEmpty()){
            builder.addAllEntityClasses(entityClass.entityClasss().stream().map(this::toRawEntityUp).collect(Collectors.toList()));
        }

        return builder;
    }

    private FieldUp toFieldUp(IEntityField field){
        return FieldUp.newBuilder()
                .setCode(field.name())
                .setFieldType(field.type().name())
                .setId(field.id())
                .build();
    }

    /**
     * TODO check
     * @param entityClass
     * @param body
     * @return
     */
    private EntityUp toEntityUp(EntityClass entityClass, Long id,  Map<String, Object> body){
        EntityUp.Builder builder = toEntityUpBuilder(entityClass, id);

        builder.getFieldsList();

        List<ValueUp> values = body.entrySet().stream()
                .map(entry -> {
                    String key = entry.getKey();
                    Optional<IEntityField> fieldOp = getKeyFromEntityClass(entityClass, key);

                    return fieldOp.map(field -> {
                        return ValueUp.newBuilder()
                                .setFieldId(field.id())
                                .setFieldType(field.type().getType())
                                .setValue(entry.getValue().toString())
                                .build();
                    });
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        builder.addAllValues(values);
        return builder.build();
    }

    //TODO sub search
    private Optional<IEntityField> getKeyFromEntityClass(EntityClass entityClass, String key ){
        return entityClass.field(key);
    }

    //TODO
    private Map<String, String> toResultMap(EntityUp up) {

        Map<String, String> map = new HashMap<>();

        up.getValuesList().forEach(entry -> {
            map.put(entry.getName(), entry.getValue());
        });
        return map;
    }

    /**
     * //TODO transaction
     * return affect row
     * @param entityClass
     * @param id
     * @return
     */
    public Either<String, Integer> deleteOne(EntityClass entityClass, Long id) {

        String transId = contextService.get(ContextService.StringKeys.TransactionKey);
        SingleResponseRequestBuilder<EntityUp, OperationResult> removeBuilder = entityServiceClient.remove();
        if(transId != null){
            removeBuilder.addHeader("transaction-id", "1");
        }

        OperationResult updateResult =
                removeBuilder.invoke(toEntityUpBuilder(entityClass, id).build())
                .toCompletableFuture().join();

        if(updateResult.getCode() == OperationResult.Code.OK){
            int rows = updateResult.getAffectedRow();
            return Either.right(rows);
        }else{
            //indicates
            return Either.left(updateResult.getMessage());
        }
    }

    public Either<String, Integer> updateById(EntityClass entityClass, Long id, Map<String, Object> body){

        String transId = contextService.get(ContextService.StringKeys.TransactionKey);
        SingleResponseRequestBuilder<EntityUp, OperationResult> replaceBuilder = entityServiceClient.replace();
        if(transId != null){
            replaceBuilder.addHeader("transaction-id", transId);
        }

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

    //TODO current return long id
    public Either<String, Long> create(EntityClass entityClass, Map<String, Object> body) {

        String transId = contextService.get(ContextService.StringKeys.TransactionKey);
        SingleResponseRequestBuilder<EntityUp, OperationResult> buildBuilder = entityServiceClient.build();

        if(transId != null){
            buildBuilder.addHeader("transaction-id", transId);
        }

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

    /**
     * query
     * @param entityClass
     * @param condition
     * @return
     */
    public Either<String, Tuple2<Integer, List<Map<String, String>>>> findByCondition(EntityClass entityClass, ConditionQueryRequest condition) {

        String transId = contextService.get(ContextService.StringKeys.TransactionKey);

        SelectByCondition.Builder select = SelectByCondition
                .newBuilder();

        if(condition.getPageNo() != null){
            select.setPageNo(condition.getPageNo());
        }

        if(condition.getPageSize() != null ){
            select.setPageSize(condition.getPageSize());
        }

//        if(condition.getConditions() != null){
//            select.setConditions(toConditionsUp(condition.getConditions()));
//        }

//        if(condition.getSort() != null){
//            select.addAllSort(toSortUp(condition.getSort()));
//        }

        SelectByCondition selectByCondition = select.build();

        OperationResult result = entityServiceClient.selectByConditions(selectByCondition)
                .toCompletableFuture().join();

        if(result.getCode() == OperationResult.Code.OK) {
            List<Map<String, String>> repList = result.getQueryResultList()
                    .stream()
                    .map(x -> {
                        Map<String, String> resultMap = toResultMap(x);
                        return filterItem(resultMap, x.getCode(), condition.getEntity());
                    }).collect(Collectors.toList());
            Tuple2<Integer, List<Map<String, String>>> queryResult = Tuple.of(result.getTotalRow(), repList);
            return Either.right(queryResult);
        }else{
            return Either.left(result.getMessage());
        }
    }

//    private ConditionsUp toConditionsUp(Conditions conditions) {
//        ConditionsUp conditionsUpBuilder = ConditionsUp.
//        return null;
//    }

    private Map<String, String> filterItem(Map<String, String> values, String mainEntityCode, EntityItem entityItem){


        Map<String, String> newResult = new HashMap<>();

        //setup main
        entityItem.getFields().forEach(x -> {
            String value  = values.get(x);
            if(value != null){
                newResult.put(x, value);
            }

            String otherValue = values.get(mainEntityCode + "." + x);

            if(otherValue != null){
                newResult.put(x, value);
            }
        });

        entityItem.getEntities().forEach(subEntity -> {
            subEntity.getFields().forEach(field -> {
                String subKey = subEntity.getCode() + "." + field;
                String value = values.get(subKey);
                if(value != null){
                    newResult.put(subKey, value);
                }
            });
        });
        return newResult;
    }
}
