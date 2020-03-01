package com.xforceplus.ultraman.oqsengine.sdk.service;

import akka.grpc.javadsl.SingleResponseRequestBuilder;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;
import com.xforceplus.ultraman.oqsengine.sdk.*;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.MetadataRepository;
import com.xforceplus.ultraman.oqsengine.sdk.util.IEntityClassHelper;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.*;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.xforceplus.ultraman.oqsengine.sdk.util.OptionalHelper.combine;

@Service
public class EntityService {

    private final MetadataRepository metadataRepository;

    private final EntityServiceClient entityServiceClient;

    private final ContextService contextService;

    public EntityService(MetadataRepository metadataRepository, EntityServiceClient entityServiceClient, ContextService contextService) {
        this.metadataRepository = metadataRepository;
        this.entityServiceClient = entityServiceClient;
        this.contextService = contextService;
    }

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

    public Either<String, Map<String, Object>> findOne(EntityClass entityClass, Long id) {
        String transId = contextService.get(ContextService.StringKeys.TransactionKey);

        SingleResponseRequestBuilder<EntityUp, OperationResult> queryResultBuilder = entityServiceClient.selectOne();

        if(transId != null) {
            queryResultBuilder.addHeader("transaction-id", transId);
        }

        OperationResult queryResult = queryResultBuilder.invoke(toEntityUpBuilder(entityClass, id).build())
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

        //add parent
        if(entityClass.extendEntityClass() != null){
            IEntityClass parent = entityClass.extendEntityClass();
            EntityUp parentUp = toRawEntityUp(parent);
            builder.setExtendEntityClass(parentUp);
        }

        //add obj id
        if(id != null) {
            builder.setObjId(id);
        }

        //add relation
        builder.addAllRelation(entityClass.relations().stream().map(rel -> {
            return RelationUp.newBuilder()
                    .setEntityField(toFieldUp(rel.getEntityField()))
                    .setName(rel.getName())
                    .setRelationType(rel.getRelationType())
                    .setIdentity(rel.isIdentity())
                    .setRelatedEntityClassId(rel.getEntityClassId())
                    .build();
        }).collect(Collectors.toList()));

        builder.setId(entityClass.id())
                .setCode(entityClass.code())
                .addAllFields(entityClass.fields().stream().map(this::toFieldUp).collect(Collectors.toList()));

        if(entityClass.entityClasss() != null && !entityClass.entityClasss().isEmpty()){
            builder.addAllEntityClasses(entityClass.entityClasss().stream()
                    .map(this::toRawEntityUp).collect(Collectors.toList()));
        }

        return builder;
    }

    private FieldUp toFieldUp(IEntityField field){
        FieldUp.Builder builder =
                FieldUp.newBuilder()
                .setCode(field.name())
                .setFieldType(field.type().name())
                .setId(field.id());
        if(field.config() != null){
            builder.setSearchable(String.valueOf(field.config().isSearchable()));
            builder.setMaxLength(String.valueOf(field.config().getMax()));
            builder.setMinLength(String.valueOf(field.config().getMin()));
        }
        return builder.build();
    }

    /**
     * TODO check
     * @param entityClass
     * @param body
     * @return
     */
    public EntityUp toEntityUp(EntityClass entityClass, Long id,  Map<String, Object> body){
        //build entityUp
        EntityUp.Builder builder = toEntityUpBuilder(entityClass, id);

        List<ValueUp> values = body.entrySet().stream()
                .map(entry -> {
                    String key = entry.getKey();
                    Optional<IEntityField> fieldOp = getKeyFromEntityClass(entityClass, key);
                    Optional<IEntityField> fieldOpRel = getKeyFromRelation(entityClass, key);
                    Optional<IEntityField> fieldOpParent = getKeyFromParent(entityClass, key);

                    Optional<IEntityField> fieldFinal = combine(fieldOp, fieldOpParent, fieldOpRel);

                    return fieldFinal.map(field -> {
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

    private Optional<IEntityField> getKeyFromRelation(EntityClass entityClass, String key) {
        return entityClass.relations().stream().filter(x ->  x.getName().equals(key)).map(Relation::getEntityField).findFirst();
    }

    //TODO sub search
    private Optional<IEntityField> getKeyFromEntityClass(EntityClass entityClass, String key ){
        return entityClass.field(key);
    }

    private Optional<IEntityField> getKeyFromParent(EntityClass entityClass, String key ){
        return Optional.ofNullable(entityClass.extendEntityClass()).flatMap(x -> x.field(key));
    }

    //TODO
    private Map<String, Object> toResultMap(EntityClass entityClass, EntityUp up) {

        Map<String, Object> map = new HashMap<>();
        if(!StringUtils.isEmpty(up.getObjId())){
            map.put("id", String.valueOf(up.getObjId()));
        }

        up.getValuesList().forEach(entry -> {
            IEntityClassHelper.findFieldById(entityClass, entry.getFieldId()).ifPresent(field -> {
                if(field.type() == FieldType.BOOLEAN) {
                    map.put(field.name(), Boolean.valueOf(entry.getValue()));
                } else {
                    map.put(field.name(), entry.getValue());
                }


            });
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
            removeBuilder.addHeader("transaction-id", transId);
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
    public Either<String, Tuple2<Integer, List<Map<String, Object>>>> findByCondition(EntityClass entityClass, ConditionQueryRequest condition) {

        String transId = contextService.get(ContextService.StringKeys.TransactionKey);

        SelectByCondition.Builder select = SelectByCondition
                .newBuilder();

        if(condition.getPageNo() != null){
            select.setPageNo(condition.getPageNo());
        }

        if(condition.getPageSize() != null ){
            select.setPageSize(condition.getPageSize());
        }

        if(condition.getConditions() != null){
            select.setConditions(toConditionsUp(entityClass, condition.getConditions()));
        }

        if(condition.getSort() != null){
            select.addAllSort(toSortUp(condition.getSort()));
        }

        select.setEntity(toEntityUp(entityClass));

        EntityItem entityItem = condition.getEntity();
        if( entityItem != null ){
            select.addAllQueryFields(toQueryFields(entityClass, entityItem));
        }

        SelectByCondition selectByCondition = select.build();

        SingleResponseRequestBuilder<SelectByCondition, OperationResult> requestBuilder = entityServiceClient.selectByConditions();

        if(transId != null){
            requestBuilder.addHeader("transaction-id", transId);
        }

        OperationResult result = requestBuilder.invoke(selectByCondition)
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

    private List<QueryFieldsUp> toQueryFields(IEntityClass entityClass, EntityItem entityItem) {

        Stream<QueryFieldsUp> fieldsUp = entityItem.getFields()
                .stream()
                .map(entityClass::field)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(x -> QueryFieldsUp
                        .newBuilder()
                            .setCode(x.name())
                            .setEntityId(entityClass.id())
                            .setId(x.id())
                        .build());

        Stream<QueryFieldsUp> fieldsUpFrom = entityItem.getEntities()
                .stream()
                .flatMap(subEntityItem -> {

                    Optional<IEntityClass> subEntityClassOp = entityClass.entityClasss().stream()
                            .filter(ec -> {
                                return subEntityItem.getCode().equalsIgnoreCase(ec.code());
                            }).findFirst();

                    return subEntityClassOp.map(iEntityClass -> subEntityItem.getFields().stream()
                            .map(iEntityClass::field)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(x -> QueryFieldsUp
                                    .newBuilder()
                                    .setCode(x.name())
                                    .setEntityId(iEntityClass.id())
                                    .setId(x.id())
                                    .build())).orElseGet(Stream::empty);
                });

        return Stream.concat(fieldsUp, fieldsUpFrom).collect(Collectors.toList());
    }

    private List<FieldSortUp> toSortUp(List<FieldSort> sort) {
        return sort.stream().map(x -> {
            return FieldSortUp.newBuilder()
                    .setCode(x.getField())
                    .setOrder(FieldSortUp.Order.valueOf(x.getOrder()))
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * flatten all the conditions to field condition
     * @param conditions
     * @return
     */
    private ConditionsUp toConditionsUp(EntityClass entityClass, Conditions conditions) {
        ConditionsUp.Builder conditionsUpBuilder = ConditionsUp.newBuilder();

        Stream<Optional<FieldConditionUp>> fieldInMainStream = Optional.ofNullable(conditions.getFields())
                            .orElseGet(Collections::emptyList).stream().map(fieldCondition -> {
            return toFieldCondition(entityClass, fieldCondition);
        });

        /**
         * should find from relation currently not in sub entity
         */
//        Stream<Optional<FieldConditionUp>> fieldInSubStream = conditions.getEntities().stream().flatMap(entityCondition -> {
//            return toFieldCondition(entityClass, entityCondition);
//        } );


        //from relation to condition
        Stream<Optional<FieldConditionUp>> fieldInRelationStream = conditions
                .getEntities()
                .stream().flatMap(entityCondition -> {
            return toFieldConditionFromRel(entityClass, entityCondition);
        });

        conditionsUpBuilder.addAllFields(Stream.concat(fieldInMainStream, fieldInRelationStream)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()));
        return conditionsUpBuilder.build();
    }


    private Optional<FieldConditionUp> toFieldCondition(IEntityClass entityClass, FieldCondition fieldCondition){

        Optional<IEntityField> fieldOp = entityClass.field(fieldCondition.getCode());

        return fieldOp.map(x -> FieldConditionUp.newBuilder()
                .setCode(fieldCondition.getCode())
                .setOperation(FieldConditionUp.Op.valueOf(fieldCondition.getOperation().name()))
                .addAllValues(Optional.ofNullable(fieldCondition.getValue()).orElseGet(Collections::emptyList))
                .setField(toFieldUp(fieldOp.get()))
                .build());
    }

    private Stream<? extends Optional<FieldConditionUp>> toFieldConditionFromRel(EntityClass entityClass, SubFieldCondition entityCondition) {
        return entityClass.relations().stream()
                .map(rel -> {
                     Optional<FieldCondition> fieldConditionOp = entityCondition.getFields()
                            .stream()
                            .filter(enc -> {
                                String code = entityCondition.getCode() + "." +enc.getCode();
                                return rel.getEntityField().name().equalsIgnoreCase(code);
                            }).findFirst();
                    return fieldConditionOp.map(fieldCon -> Tuple.of(fieldCon, rel));
                }).map(tuple -> tuple.map(this::toFieldCondition));
    }

    private FieldConditionUp toFieldCondition(Tuple2<FieldCondition, Relation> tuple) {
        FieldCondition fieldCondition = tuple._1();
        IEntityField entityField = tuple._2().getEntityField();

        return FieldConditionUp.newBuilder()
                .setCode(fieldCondition.getCode())
                .setOperation(FieldConditionUp.Op.valueOf(fieldCondition.getOperation().name()))
                .addAllValues(Optional.ofNullable(fieldCondition.getValue()).orElseGet(Collections::emptyList))
                .setField(toFieldUp(entityField))
                .build();
    }

    private Stream<Optional<FieldConditionUp>> toFieldCondition(EntityClass entityClass, SubFieldCondition subFieldCondition){
        return entityClass.entityClasss().stream()
                .filter(x -> x.code().equals( subFieldCondition.getCode()))
                .flatMap(entity -> subFieldCondition
                        .getFields()
                        .stream()
                        .map(subField -> toFieldCondition(entity, subField)));
    }

    private Map<String, Object> filterItem(Map<String, Object> values, String mainEntityCode, EntityItem entityItem){

        if(entityItem == null ){
            return values;
        }

        Map<String, Object> newResult = new HashMap<>();

        //setup main
        entityItem.getFields().forEach(x -> {
            Object value  = values.get(x);
            if(value != null){
                newResult.put(x, value);
            }

            Object otherValue = values.get(mainEntityCode + "." + x);

            if(otherValue != null){
                newResult.put(x, value);
            }
        });

        entityItem.getEntities().forEach(subEntity -> {
            subEntity.getFields().forEach(field -> {
                String subKey = subEntity.getCode() + "." + field;
                Object value = values.get(subKey);
                if(value != null){
                    newResult.put(subKey, value);
                }
            });
        });
        return newResult;
    }
}
