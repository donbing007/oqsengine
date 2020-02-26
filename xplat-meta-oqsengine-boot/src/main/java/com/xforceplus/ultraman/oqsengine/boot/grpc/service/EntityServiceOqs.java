package com.xforceplus.ultraman.oqsengine.boot.grpc.service;

import akka.grpc.javadsl.Metadata;
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
import com.xforceplus.ultraman.oqsengine.sdk.*;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.time.LocalDateTime;
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
        } catch (SQLException e) {
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
        try {
            long id = entityManagementService.build(toEntity(in));
            result = OperationResult.newBuilder().addIds(id).setCode(OperationResult.Code.OK).buildPartial();
        } catch (SQLException e) {
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

        try {
            Optional<IEntity> ds = entitySearchService.selectOne(in.getObjId(), toEntityClass(in));
            if(ds.isPresent()) {
                //side effect
                updateEntity(ds.get(), toEntity(in));
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
        } catch (SQLException e) {
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
            entityManagementService.delete(toEntity(in));
            result = OperationResult.newBuilder()
                    .setAffectedRow(1)
                    .setCode(OperationResult.Code.OK)
                    .buildPartial();
        } catch (SQLException e) {
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
            Optional<IEntity> ds = entitySearchService.selectOne(in.getObjId(), toEntityClass(in));
            if(ds.isPresent()) {
                result = OperationResult
                        .newBuilder()
                        .setCode(OperationResult.Code.OK)
                        .addQueryResult(toEntityUp(ds.get()))
                        .setTotalRow(1)
                        .buildPartial();
            }else{
                result = OperationResult
                        .newBuilder()
                        .setCode(OperationResult.Code.OK)
                        .setTotalRow(0)
                        .buildPartial();
            }
        } catch (SQLException e) {
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


            if(sort != null && !sort.isEmpty()){
                entities = entitySearchService.selectByConditions(toConditions(conditions), toEntityClass(entityUp), page);
            }else{

                Sort sortParam;
                FieldSortUp sortUp = sort.get(0);
                if(sortUp.getOrder() == FieldSortUp.Order.asc) {
                    sortParam = Sort.buildAscSort(toEntityField(sortUp.getField()));
                }else{
                    sortParam = Sort.buildDescSort(toEntityField(sortUp.getField()));
                }

                entities = entitySearchService.selectByConditions(null, toEntityClass(entityUp), sortParam, page);
            }

            result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.OK)
                    .addAllQueryResult(entities.stream().map(this::toEntityUp).collect(Collectors.toList()))
                    .setTotalRow(entities.size()).buildPartial();

        } catch (SQLException e) {
            logger.error("{}", e);
            result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.EXCEPTION)
                    .setMessage(e.getMessage())
                    .buildPartial();
        }

        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletionStage<OperationResult> commit(TransactionUp in, Metadata metadata) {
        Long id = Long.valueOf(in.getId());
        OperationResult result = null;

        try {

            transactionManager.rebind(id);
            transactionManagementService.commit();
            result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.OK)
                    .setMessage("事务提交成功")
                    .buildPartial();
        } catch (SQLException e) {
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

        Long id = Long.valueOf(in.getId());
        OperationResult result = null;

        try {
            transactionManager.rebind(id);
            transactionManagementService.rollback();
            result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.OK)
                    .setMessage("事务提交成功")
                    .buildPartial();
        } catch (SQLException e) {
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

        builder.addAllValues(entity.entityValue().values().stream()
                .map(this::toValueUp)
                .collect(Collectors.toList()));
        return builder.build();
    }

    private ValueUp toValueUp(IValue value){
        //TODO format?
        IEntityField field = value.getField();
        return ValueUp.newBuilder()
                .setValue(value.getValue().toString())
                .setName(field.name())
                .setFieldId(field.id())
                .setFieldType(field.type().name())
                .build();
    }

    //TODO version
    private IEntity toEntity(EntityUp in){
        return new Entity(in.getObjId(), toEntityClass(in), toEntityValue(in));
    }

    //TODO
    private IEntityFamily toEntityFamily(EntityUp in) {
        return null;
    }

    //TODO
    private Conditions toConditions(ConditionsUp conditionsUp){
//
//        conditionsUp.getFieldsList().stream().map(x -> {
//
//
//
//            Condition condition = new Condition(toEntityField(x.getField()),toConditionOp(x.getOperation()), x.getVA);
//            return condition;
//        });


       return null;
    }

//    private ConditionOperator toConditionOp(FieldConditionUp.Op op) {
//
//    }

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

    private IEntityValue toEntityValue(EntityUp entityUp){
        List<IValue> valueList = entityUp.getValuesList().stream()
                .map(y -> {
                    return toTypedValue(y.getName(), y.getFieldId(), y.getFieldType(), y.getValue());
                }).collect(Collectors.toList());
        EntityValue entityValue = new EntityValue(entityUp.getId());
        entityValue.addValues(valueList);
        return entityValue;
    }

    private IValue toTypedValue(String name, Long id, String fieldType, String value){
        try {
            Objects.requireNonNull(value, "值不能为空");
            FieldType fieldTypeE = FieldType.valueOf(fieldType.toUpperCase());
            //TODO fix field
            IEntityField entityField = new Field(id, name, fieldTypeE);
            IValue retValue = null;
            switch(fieldTypeE){
                case LONG:
                    retValue = new LongValue(entityField, Long.valueOf(value));
                    break;
                case DATATIME:
                    retValue = new DateTimeValue(entityField, LocalDateTime.parse(value));
                    break;
                case ENUM:
                    retValue = new EnumValue(entityField, value);
                    break;
                case BOOLEAN:
                    retValue = new BooleanValue(entityField, Boolean.valueOf(value));
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

    //TODO
    private Field toEntityField(FieldUp fieldUp){
        return new Field(
                  fieldUp.getId()
                , fieldUp.getName()
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
    private IEntityClass toEntityClass(EntityUp entityUp){
        //Long id, String code, String relation, List<IEntityClass> entityClasss, IEntityClass extendEntityClass, List<Field> fields
        IEntityClass entityClass = new EntityClass(
                entityUp.getId()
                , entityUp.getCode()
                , null
                , entityUp.getEntityClassesList().stream().map(this::toRawEntityClass).collect(Collectors.toList())
                , toRawEntityClass(entityUp.getExtendEntityClass())
                , entityUp.getFieldsList().stream().map(this::toEntityField).collect(Collectors.toList())
        );
        return entityClass;
    }
}
