package com.xforceplus.ultraman.oqsengine.boot.grpc.service;

import akka.grpc.javadsl.Metadata;
import com.xforceplus.ultraman.oqsengine.boot.utils.EntityHelper;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.TransactionManagementService;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.*;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IEntityClassHelper;
import com.xforceplus.ultraman.oqsengine.sdk.*;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.boot.utils.OptionalHelper.ofEmptyStr;

@Component
public class EntityServiceOqs implements EntityServicePowerApi {

    @Autowired(required = false)
    private EntityManagementService entityManagementService;

    @Autowired(required = false)
    private EntitySearchService entitySearchService;

    @Autowired(required = false)
    private TransactionManagementService transactionManagementService;

    @Autowired(required = false)
    private TransactionManager transactionManager;


    private Logger logger = LoggerFactory.getLogger(EntityServicePowerApi.class);

    @Override
    public CompletionStage<OperationResult> begin(TransactionUp in, Metadata metadata) {
        try {
            long transId = transactionManagementService.begin();

            return CompletableFuture.completedFuture(OperationResult.newBuilder()
                    .setCode(OperationResult.Code.OK)
                    .setTransactionResult(String.valueOf(transId)).buildPartial());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{}", e);

            return CompletableFuture.completedFuture(
                    OperationResult.newBuilder()
                    .setCode(OperationResult.Code.EXCEPTION)
                    .setMessage(e.getMessage())
                    .buildPartial());
        }
    }

    @Override
    public CompletionStage<OperationResult> build(EntityUp in, Metadata metadata) {
        extractTransaction(metadata).ifPresent(id -> transactionManager.rebind(id));

        OperationResult result;

        IEntityClass entityClass = toEntityClass(in);

        try {
            long id = entityManagementService.build(toEntity(entityClass, in));
            result = OperationResult.newBuilder().addIds(id).setCode(OperationResult.Code.OK).buildPartial();
        } catch (Exception e) {
            logger.error("{}", e);
            result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.EXCEPTION)
                    .setMessage(e.getMessage())
                    .buildPartial();
        }

        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletionStage<OperationResult> replace(EntityUp in, Metadata metadata) {

        extractTransaction(metadata).ifPresent(id -> transactionManager.rebind(id));

        OperationResult result;

        IEntityClass entityClass = toEntityClass(in);

        try {
            Optional<IEntity> ds = entitySearchService.selectOne(in.getObjId(), entityClass);
            if(ds.isPresent()) {

                //side effect
                updateEntity(ds.get(), toEntity(entityClass, in));
                entityManagementService.replace(ds.get());
                result = OperationResult.newBuilder()
                        .setAffectedRow(1)
                        .setCode(OperationResult.Code.OK)
                        .buildPartial();
            } else {
                result = OperationResult.newBuilder()
                        .setCode(OperationResult.Code.FAILED)
                        .setMessage("没有找到该记录")
                        .buildPartial();
            }
        } catch (Exception e) {
            logger.error("{}", e);
            result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.EXCEPTION)
                    .setMessage(e.getMessage())
                    .buildPartial();
        }

        return CompletableFuture.completedFuture(result);
    }

    //TODO test
    private void updateEntity(IEntity src, IEntity update) {
        src.entityValue().addValues(update.entityValue().values());
    }

    @Override
    public CompletionStage<OperationResult> remove(EntityUp in, Metadata metadata) {
        extractTransaction(metadata).ifPresent(id -> transactionManager.rebind(id));

        OperationResult result;

        try{
            IEntityClass entityClass = toEntityClass(in);

            //find one
            Optional<IEntity> op = entitySearchService.selectOne(in.getObjId(), entityClass);

            if(op.isPresent()){
                IEntity entity = op.get();
                entityManagementService.delete(entity);
                result = OperationResult.newBuilder()
                        .setAffectedRow(1)
                        .setCode(OperationResult.Code.OK)
                        .buildPartial();
            }else{
                result = OperationResult.newBuilder()
                        .setAffectedRow(0)
                        .setCode(OperationResult.Code.OK)
                        .buildPartial();
            }
        } catch (Exception e) {
            logger.error("{}", e);
            result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.EXCEPTION)
                    .setMessage(e.getMessage())
                    .buildPartial();
        }

        return CompletableFuture.completedFuture(result);
    }


    @Override
    public CompletionStage<OperationResult> selectOne(EntityUp in, Metadata metadata) {
        extractTransaction(metadata).ifPresent(id -> transactionManager.rebind(id));

        OperationResult result;

        try {

            IEntityClass entityClass = toEntityClass(in);

            Optional<IEntity> ds = entitySearchService.selectOne(in.getObjId(), entityClass);

            if(ds.isPresent()){
                if(ds.get().family() != null && ds.get().family().parent() > 0 && entityClass.extendEntityClass() != null){
                    Optional<IEntity> parentDS = entitySearchService.selectOne(ds.get().family().parent(), entityClass.extendEntityClass());

                    parentDS.ifPresent(x ->
                            ds.ifPresent(y -> leftAppend(y, x)));
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
            logger.error("{}", e);
            result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.EXCEPTION)
                    .setMessage(e.getMessage())
                    .buildPartial();
        }

        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletionStage<OperationResult> selectByConditions(SelectByCondition in, Metadata metadata) {

        OperationResult result;
        try {
            Collection<IEntity> entities = null;

            List<FieldSortUp> sort = in.getSortList();

            ConditionsUp conditions = in.getConditions();

            EntityUp entityUp = in.getEntity();

            int pageNo = in.getPageNo();
            int pageSize = in.getPageSize();
            Page page = new Page(pageNo, pageSize);

            IEntityClass entityClass = toEntityClass(entityUp);

            Long mainEntityId = entityClass.id();

            //check if has sub query for more details
            List<QueryFieldsUp> queryField = in.getQueryFieldsList();

            Optional<IEntityField> sortField;

            if(sort == null || sort.isEmpty()) {
                sortField = Optional.empty();
            }else{
                FieldSortUp sortUp = sort.get(0);
                //get related field
                sortField = IEntityClassHelper.findFieldByCode(entityClass, sortUp.getCode());
            }

            if(!sortField.isPresent()){
                Optional<Conditions> consOp = toConditions(entityClass, conditions);
                if(consOp.isPresent()){
                    entities = entitySearchService.selectByConditions(consOp.get(), entityClass, page);
                }else{
                    entities = entitySearchService.selectByConditions(Conditions.buildEmtpyConditions(), entityClass, page);
                }
            }else {
                FieldSortUp sortUp = sort.get(0);
                Sort sortParam;
                if(sortUp.getOrder() == FieldSortUp.Order.asc) {
                    sortParam = Sort.buildAscSort(sortField.get());
                }else{
                    sortParam = Sort.buildDescSort(sortField.get());
                }

                Optional<Conditions> consOp = toConditions(entityClass, conditions);
                if(consOp.isPresent()){
                    entities = entitySearchService.selectByConditions(consOp.get(), entityClass, sortParam, page);

                }else{
                    entities = entitySearchService.selectByConditions(Conditions.buildEmtpyConditions(), entityClass, page);
                }
            }

            //extend entities
            Map<Long, List<QueryFieldsUp>> mappedQueryFields = queryField.stream()
                                                                .collect(Collectors.groupingBy(QueryFieldsUp::getEntityId));



            Optional.ofNullable(entities).orElseGet(Collections::emptyList)
                    .stream().filter(Objects::nonNull).forEach(entity -> {
                mappedQueryFields.keySet().stream()
                        .filter(key -> !key.equals(mainEntityId))
                        .forEach(subEntityClassId -> {
                            Optional<IEntityClass> iEntityClassOp = getRelatedEntityClassById(entityClass, subEntityClassId);
                            Optional<IEntityField> relationFieldOp = findRelationField(entityClass, subEntityClassId);

                            if(iEntityClassOp.isPresent() && relationFieldOp.isPresent()){
                               Optional<IValue> subObjRelated = entity
                                       .entityValue().getValue(relationFieldOp.get().id());
                               if(subObjRelated.isPresent()){
                                   try {

                                       IValue subId = subObjRelated.get();
                                       //how to judge this is the primary key
                                       if(subId instanceof LongValue){
                                            //id
                                            Optional<IEntity> leftEntity = entitySearchService.selectOne(subId.valueToLong(), iEntityClassOp.get());
                                            leftEntity.ifPresent(left ->
                                                    leftAppend(entity, left));

                                       }else{
                                           logger.warn("not support now");
                                       }

                                    }catch(Exception ex){
                                        logger.error("{}", ex);
                                    }
                               }
                            }
                        });
            });




            result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.OK)
                    .addAllQueryResult(Optional.ofNullable(entities).orElseGet(Collections::emptyList)
                            .stream().filter(Objects::nonNull).map(this::toEntityUp).collect(Collectors.toList()))
                    .setTotalRow(Optional.ofNullable(entities).orElseGet(Collections::emptyList).size())
                    .buildPartial();

        } catch (Exception e) {
            logger.error("{}", e);
            result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.EXCEPTION)
                    .setMessage(e.getMessage())
                    .buildPartial();
        }
        return CompletableFuture.completedFuture(result);
    }

    private Optional<IEntityClass> getRelatedEntityClassById(IEntityClass entityClass, long subEntityClassId){
        return entityClass.entityClasss().stream().filter(x -> x.id() == subEntityClassId).findFirst();
    }

    /**
     *
     * @param entity
     * @param leftEntity
     */
    private void leftAppend(IEntity entity, IEntity leftEntity){
        entity.entityValue().addValues(leftEntity.entityValue().values());
    }

    /**
     * only one to one || many to one
     * @param entityClass
     * @param subEntityClassId
     * @return
     */
    private Optional<IEntityField> findRelationField(IEntityClass entityClass, long subEntityClassId){
        return entityClass.relations()
                .stream()
                .filter(rel -> ("onetoone".equalsIgnoreCase(rel.getRelationType())
                        || "manytoone".equalsIgnoreCase(rel.getRelationType()))
                        && rel.getEntityClassId() == subEntityClassId
                )
                .map(Relation::getEntityField)
                .findFirst();
    }

    @Override
    public CompletionStage<OperationResult> commit(TransactionUp in, Metadata metadata) {
        Long id = Long.parseLong(in.getId());
        OperationResult result = null;

        try {

            transactionManager.rebind(id);
            transactionManagementService.commit();
            result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.OK)
                    .setMessage("事务提交成功")
                    .buildPartial();
        } catch (Exception e) {
            logger.error("{}", e);
            result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.EXCEPTION)
                    .setMessage(e.getMessage())
                    .buildPartial();
        }
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletionStage<OperationResult> rollBack(TransactionUp in, Metadata metadata) {

        Long id = Long.parseLong(in.getId());
        OperationResult result = null;

        try {
            transactionManager.rebind(id);
            transactionManagementService.rollback();
            result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.OK)
                    .setMessage("事务提交成功")
                    .buildPartial();
        } catch (Exception e) {
            logger.error("{}", e);
            result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.EXCEPTION)
                    .setMessage(e.getMessage())
                    .buildPartial();
        }
        return CompletableFuture.completedFuture(result);
    }

    private Optional<Long> extractTransaction(Metadata metadata){
        Optional<String> transactionId =  metadata.getText("transaction-id");
        return transactionId.map(Long::valueOf);
    }

    private EntityUp toEntityUp(IEntity entity){
        EntityUp.Builder builder = EntityUp.newBuilder();

        builder.setObjId(entity.id());
        builder.addAllValues(entity.entityValue().values().stream()
                .map(this::toValueUp)
                .collect(Collectors.toList()));
        return builder.build();
    }

    private ValueUp toValueUp(IValue value){
        //TODO format?
        IEntityField field = value.getField();
        return ValueUp.newBuilder()
                .setValue(toValueStr(value))
                .setName(field.name())
                .setFieldId(field.id())
                .setFieldType(field.type().name())
                .build();
    }

    private String toValueStr(IValue value){
        String retVal;
        switch(value.getField().type()){
            case DATETIME:
                retVal = String.valueOf(((LocalDateTime)value.getValue()).atZone(DateTimeValue.zoneId).toInstant().toEpochMilli());
                break;
            case LONG:
            case ENUM:
            case BOOLEAN:
            case STRING:
            default:
                retVal = value.getValue().toString();
                break;
        }

        return retVal;
    }

    //TODO version
    private IEntity toEntity(IEntityClass entityClass, EntityUp in){
        return new Entity(in.getObjId(), entityClass, toEntityValue(entityClass, in));
    }

    //TODO
    private IEntityFamily toEntityFamily(EntityUp in) {
        return null;
    }

    //To condition
    private  Optional<Conditions> toConditions(IEntityClass entityClass, ConditionsUp conditionsUp){

        Optional<Conditions> conditions =conditionsUp.getFieldsList().stream().map(x -> {
            return toOneConditions(entityClass, x);
        }).reduce((a,b) -> a.addOr(b, true));

       return conditions;
    }

    //TODO error handler
    private Conditions toOneConditions(IEntityClass entityClass, FieldConditionUp fieldCondition){

        Optional<IEntityField> fieldOp = getFieldFromEntityClass(entityClass, fieldCondition.getField().getId());
        Conditions conditions = null;

        if(fieldOp.isPresent()) {
            FieldConditionUp.Op op = fieldCondition.getOperation();

            switch (op) {
                case eq:

                    conditions = new Conditions(new Condition(fieldOp.get()
                            , ConditionOperator.EQUALS
                            , toTypedValue(fieldOp.get()
                                , fieldCondition.getValues(0))));
                    break;
                case ne:
                    conditions = new Conditions(new Condition(fieldOp.get()
                            , ConditionOperator.NOT_EQUALS
                            , toTypedValue(fieldOp.get()
                            , fieldCondition.getValues(0))));
                    break;
                case ge:
                    conditions = new Conditions(new Condition(fieldOp.get()
                            , ConditionOperator.GREATER_THAN_EQUALS
                            , toTypedValue(fieldOp.get()
                            , fieldCondition.getValues(0))));
                    break;
                case gt:
                    conditions = new Conditions(new Condition(fieldOp.get()
                            , ConditionOperator.GREATER_THAN
                            , toTypedValue(fieldOp.get()
                            , fieldCondition.getValues(0))));
                    break;
                case ge_le:
                    if (fieldCondition.getValuesCount() > 1) {
                        Condition left = new Condition(fieldOp.get()
                                , ConditionOperator.GREATER_THAN_EQUALS
                                , toTypedValue(fieldOp.get()
                                , fieldCondition.getValues(0)));

                        Condition right = new Condition(fieldOp.get()
                                , ConditionOperator.MINOR_THAN_EQUALS
                             , toTypedValue(fieldOp.get()
                                , fieldCondition.getValues(1)));

                        conditions = new Conditions(left).addAnd(right);

                    }else{
                        logger.error("required value more then 2");
                    }
                    break;
                case gt_le:
                    if (fieldCondition.getValuesCount() > 1) {
                        Condition left = new Condition(fieldOp.get()
                                , ConditionOperator.GREATER_THAN
                                , toTypedValue(fieldOp.get()
                                , fieldCondition.getValues(0)));

                        Condition right = new Condition(fieldOp.get()
                                , ConditionOperator.MINOR_THAN_EQUALS
                                , toTypedValue(fieldOp.get()
                                , fieldCondition.getValues(1)));


                        conditions = new Conditions(left).addAnd(right);

                    }else{
                        logger.error("required value more then 2");
                    }
                    break;
                case ge_lt:
                    if (fieldCondition.getValuesCount() > 1) {
                        Condition left = new Condition(fieldOp.get()
                                , ConditionOperator.GREATER_THAN_EQUALS
                                , toTypedValue(fieldOp.get()
                                , fieldCondition.getValues(0)));

                        Condition right = new Condition(fieldOp.get()
                                , ConditionOperator.MINOR_THAN
                                , toTypedValue(fieldOp.get()
                                , fieldCondition.getValues(1)));


                        conditions = new Conditions(left).addAnd(right);

                    }else{
                        logger.error("required value more then 2");
                    }
                    break;
                case le:
                    conditions = new Conditions(new Condition(fieldOp.get()
                            , ConditionOperator.MINOR_THAN_EQUALS
                            , toTypedValue(fieldOp.get()
                            , fieldCondition.getValues(0))));
                    break;
                case lt:
                    conditions = new Conditions(new Condition(fieldOp.get()
                            , ConditionOperator.MINOR_THAN
                            , toTypedValue(fieldOp.get()
                            , fieldCondition.getValues(0))));
                    break;
                case in:
                    if(fieldCondition.getValuesCount() == 1 ){
                        conditions = new Conditions(new Condition(fieldOp.get()
                                , ConditionOperator.EQUALS
                                , toTypedValue(fieldOp.get()
                                , fieldCondition.getValues(0))));
                    }else{
                        conditions = new Conditions(new Condition(fieldOp.get()
                                , ConditionOperator.EQUALS
                                , toTypedValue(fieldOp.get()
                                , fieldCondition.getValues(0))));

                        Conditions finalConditions = conditions;
                        fieldCondition.getValuesList().stream().skip(1).forEach(x -> {
                            finalConditions.addOr(new Conditions(new Condition(fieldOp.get()
                                    , ConditionOperator.EQUALS
                                    , toTypedValue(fieldOp.get()
                                    , x))), false);
                        });

                        conditions = finalConditions;
                    }
                    break;
                case ni:
                    if(fieldCondition.getValuesCount() == 1 ){
                        conditions = new Conditions(new Condition(fieldOp.get()
                                , ConditionOperator.NOT_EQUALS
                                , toTypedValue(fieldOp.get()
                                , fieldCondition.getValues(0))));
                    }else{
                        conditions = new Conditions(new Condition(fieldOp.get()
                                , ConditionOperator.NOT_EQUALS
                                , toTypedValue(fieldOp.get()
                                , fieldCondition.getValues(0))));

                        Conditions finalConditions = conditions;
                        fieldCondition.getValuesList().stream().skip(1).forEach(x -> {
                            finalConditions.addAnd(new Conditions(new Condition(fieldOp.get()
                                    , ConditionOperator.NOT_EQUALS
                                    , toTypedValue(fieldOp.get()
                                    , x))), false);
                        });

                        conditions = finalConditions;
                    }
                    break;
                case like:
                    conditions = new Conditions(new Condition(fieldOp.get()
                            , ConditionOperator.LIKE
                            , toTypedValue(fieldOp.get()
                            , fieldCondition.getValues(0))));
                    break;
            }
        }

        return conditions;
    }

    //TODO currently only id
    private Optional<IEntityField> getFieldFromEntityClass(IEntityClass entityClass, Long id){
        return IEntityClassHelper.findFieldById(entityClass, id);
    }

    private IEntityClass toRawEntityClass(EntityUp entityUp){
        return new EntityClass(
                entityUp.getId()
                , entityUp.getCode()
                , null
                , Collections.emptyList()
                , null
                , entityUp.getFieldsList().stream().map(this::toEntityField).collect(Collectors.toList())
        );
    }

    private IEntityValue toEntityValue(IEntityClass entityClass, EntityUp entityUp){
        List<IValue> valueList = entityUp.getValuesList().stream()
                .map(y -> {
                    return toTypedValue(entityClass, y.getFieldId(), y.getValue());
                }).filter(Objects::nonNull).collect(Collectors.toList());
        EntityValue entityValue = new EntityValue(entityUp.getId());
        entityValue.addValues(valueList);
        return entityValue;
    }

    private IValue toTypedValue(IEntityField entityField, String value){
        try {
            Objects.requireNonNull(value, "value值不能为空");
            Objects.requireNonNull(entityField, "field值不能为空");
            IValue retValue = null;
            switch (entityField.type()) {
                case LONG:
                    retValue = new LongValue(entityField, Long.parseLong(value));
                    break;
                case DATETIME:
                    //DATETIME is a timestamp
                    Instant instant = Instant.ofEpochMilli(Long.parseLong(value));
                    retValue = new DateTimeValue(entityField, LocalDateTime.ofInstant(instant, DateTimeValue.zoneId));
                    break;
                case ENUM:
                    retValue = new EnumValue(entityField, value);
                    break;
                case BOOLEAN:
                    retValue = new BooleanValue(entityField, Boolean.parseBoolean(value));
                    break;
                default:
                    retValue = new StringValue(entityField, value);
            }
            return retValue;
        }catch (Exception ex){
            logger.error("{}", ex);
            throw new RuntimeException("类型转换失败 " + ex.getMessage());
        }
    }

    private IValue toTypedValue(IEntityClass entityClass, Long id, String value){
        try {
            Objects.requireNonNull(value, "值不能为空");
            Optional<IEntityField> fieldOp = entityClass.field(id);
            if(entityClass.extendEntityClass() != null && !fieldOp.isPresent()){
                fieldOp = entityClass.extendEntityClass().field(id);
            }
            if(fieldOp.isPresent()) {
                return toTypedValue(fieldOp.get(), value);
            } else {
                logger.error("不存在对应的field id:{}", id);
                return null;
            }
        }catch (Exception ex){
            logger.error("{}", ex);
            throw new RuntimeException("类型转换失败 " + ex.getMessage());
        }
    }

    //TODO
    private Field toEntityField(FieldUp fieldUp){
        return new Field(
                  fieldUp.getId()
                , fieldUp.getCode()
                , FieldType.valueOf(fieldUp.getFieldType())
                , FieldConfig.build()
                    .searchable(ofEmptyStr(fieldUp.getSearchable())
                            .map(Boolean::valueOf).orElse(false))
                    .max(ofEmptyStr(fieldUp.getMaxLength())
                            .map(String::valueOf)
                            .map(Long::parseLong).orElse(-1L))
                    .min(ofEmptyStr(fieldUp.getMinLength()).map(String::valueOf)
                            .map(Long::parseLong).orElse(-1L))
               );
    }


    private Relation toEntityRelation(RelationUp relationUp){
        return new Relation(relationUp.getName()
                         , relationUp.getRelatedEntityClassId()
                         , relationUp.getRelationType()
                         , relationUp.getIdentity()
                         , toEntityField(relationUp.getEntityField()));
    }

    private IEntityClass toEntityClass(EntityUp entityUp){

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
}
