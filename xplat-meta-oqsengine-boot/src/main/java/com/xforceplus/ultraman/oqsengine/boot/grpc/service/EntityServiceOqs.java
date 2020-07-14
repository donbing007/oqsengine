package com.xforceplus.ultraman.oqsengine.boot.grpc.service;

import akka.grpc.javadsl.Metadata;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.TransactionManagementService;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.pojo.reader.IEntityClassReader;
import com.xforceplus.ultraman.oqsengine.sdk.*;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    @Resource(name = "callThreadPool")
    private ExecutorService asyncDispatcher;

    @Autowired
    private TransactionManager transactionManager;

    private Logger logger = LoggerFactory.getLogger(EntityServicePowerApi.class);

    private <T> CompletableFuture<T> async(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, asyncDispatcher);
    }

    @Override
    public CompletionStage<OperationResult> begin(TransactionUp in, Metadata metadata) {
        try {
            long transId = transactionManagementService.begin();

            return CompletableFuture.completedFuture(OperationResult.newBuilder()
                    .setCode(OperationResult.Code.OK)
                    .setTransactionResult(String.valueOf(transId)).buildPartial());
        } catch (Exception e) {
            logger.error("{}", e);

            return CompletableFuture.completedFuture(
                    OperationResult.newBuilder()
                            .setCode(OperationResult.Code.EXCEPTION)
                            .setMessage(Optional.ofNullable(e.getMessage()).orElseGet(e::toString))
                            .buildPartial());
        }
    }

    @Override
    public CompletionStage<OperationResult> build(EntityUp in, Metadata metadata) {

        return async(() -> {

            extractTransaction(metadata).ifPresent(id -> {
                try {
                    transactionManagementService.restore(id);
                } catch (Exception e) {
                    logger.error("{}", e);
                }
            });

            OperationResult result;

            IEntityClass entityClass = toEntityClass(in);

            try {
                IEntity entity = entityManagementService.build(toEntity(entityClass, in));
                OperationResult.Builder builder = OperationResult.newBuilder()
                        .addIds(entity.id());

                if (entity.family() != null && entity.family().parent() > 0) {
                    builder.addIds(entity.family().parent());
                }

                result = builder.setCode(OperationResult.Code.OK).buildPartial();
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
    public CompletionStage<OperationResult> replace(EntityUp in, Metadata metadata) {
        return async(() -> {
            extractTransaction(metadata).ifPresent(id -> {
                try {
                    transactionManagementService.restore(id);
                } catch (Exception e) {
                    logger.error("{}", e);
                }
            });

            OperationResult result;
            IEntityClass entityClass = toEntityClass(in);

            try {

                Optional<String> mode = metadata.getText("mode");
                Optional<IEntity> ds = entitySearchService.selectOne(in.getObjId(), entityClass);
                if (ds.isPresent()) {
                    IEntity entity = null;
                    if (mode.filter("replace"::equals).isPresent()) {
                        //reset the version
                        entity = toEntity(entityClass, in, ds.get().version());
                    } else {
                        entity = ds.get();
                        updateEntity(entity, toEntity(entityClass, in));
                    }
                    //side effect
                    ResultStatus replaceStatus = entityManagementService.replace(entity);

                    switch(replaceStatus){
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
                                    .setMessage("产生了未知的错误")
                                    .buildPartial();
                    }
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
        return async(() -> {
            extractTransaction(metadata).ifPresent(id -> {
                try {
                    transactionManagementService.restore(id);
                } catch (Exception e) {
                    logger.error("{}", e);
                }
            });

            OperationResult result;

            try {
                IEntityClass entityClass = toEntityClass(in.getEntity());
                //---------------------------------
                Collection<IEntity> entities = null;

                //check if has sub query for more details
                List<QueryFieldsUp> queryField = in.getQueryFieldsList();

                IEntityClassReader reader = new IEntityClassReader(entityClass);

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
                        entities = entitySearchService.selectByConditions(Conditions.buildEmtpyConditions(), entityClass, page);
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
                        entities = entitySearchService.selectByConditions(Conditions.buildEmtpyConditions(), entityClass, page);
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
                            logger.error("{}", e);
                        }
                    });

                    result = OperationResult.newBuilder()
                            .setAffectedRow(affected.intValue())
                            .setCode(OperationResult.Code.OK)
                            .buildPartial();
                } else {
                    result = OperationResult.newBuilder()
                            .setCode(OperationResult.Code.OK)
                            .setMessage("没有更新任何记录")
                            .setAffectedRow(0)
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

    //TODO test
    private void updateEntity(IEntity src, IEntity update) {
        src.entityValue().addValues(update.entityValue().values());
    }

    @Override
    public CompletionStage<OperationResult> remove(EntityUp in, Metadata metadata) {
        return async(() -> {
            extractTransaction(metadata).ifPresent(id -> {
                try {
                    transactionManagementService.restore(id);
                } catch (Exception e) {
                    logger.error("{}", e);
                }
            });

            OperationResult result;

            try {
                IEntityClass entityClass = toEntityClass(in);

                //find one
                Optional<IEntity> op = entitySearchService.selectOne(in.getObjId(), entityClass);

                if (op.isPresent()) {
                    IEntity entity = op.get();
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
                                        .setMessage("产生了未知的错误")
                                        .buildPartial();
                        }
                } else {
                    result = OperationResult.newBuilder()
                            .setAffectedRow(0)
                            .setCode(OperationResult.Code.OK)
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
    public CompletionStage<OperationResult> selectOne(EntityUp in, Metadata metadata) {
        return async(() -> {
            extractTransaction(metadata).ifPresent(id -> {
                try {
                    transactionManagementService.restore(id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            OperationResult result;

            try {

                IEntityClass entityClass = toEntityClass(in);

                IEntityClass subEntityClass = getSubEntityClass(in);

                Optional<IEntity> ds = entitySearchService.selectOne(in.getObjId(), entityClass);

                if (ds.isPresent()) {
                    if (ds.get().family() != null && ds.get().family().parent() > 0 && entityClass.extendEntityClass() != null) {
                        Optional<IEntity> parentDS = entitySearchService
                                .selectOne(ds.get().family().parent(), entityClass.extendEntityClass());

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
     * TODO modify to use IEntityReader
     *
     * @param in
     * @param metadata
     * @return
     */
    @Override
    public CompletionStage<OperationResult> selectByConditions(SelectByCondition in, Metadata metadata) {
        return async(() -> {

            extractTransaction(metadata).ifPresent(id -> {
                try {
                    transactionManagementService.restore(id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

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
                        entities = entitySearchService.selectByConditions(Conditions.buildEmtpyConditions(), entityClass, page);
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
                        entities = entitySearchService.selectByConditions(Conditions.buildEmtpyConditions(), entityClass, page);
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
                            //find fieldName from ientity;
                            iEntity.entityValue().values().stream()
                                    .forEach(envValue -> {
                                        IEntityField field = envValue.getField();
                                        entityClass.field(field.id()).ifPresent(envValue::setField);
                                    });
                            iEntity.resetEntityValue(new MixedEntityValue(iEntity.entityValue()));
                            return iEntity;
                        }).collect(Collectors.toList());

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
                                            Collection<IEntity> iEntities = entitySearchService.selectByConditions(conditionsIds, searchableRelatedEntity.get(), new Page(0, values.size()));

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
                        .setTotalRow(page == null ?
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

    private Optional<IEntityClass> getRelatedEntityClassById(IEntityClass entityClass, long subEntityClassId) {
        return entityClass.entityClasss().stream().filter(x -> x.id() == subEntityClassId).findFirst();
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

//    /**
//     * TODO
//     * related
//     * only one to one || many to one
//     *
//     * @param entityClass
//     * @param subEntityClassId
//     * @return
//     */
//    private Optional<IEntityField> findRelationField(IEntityClass entityClass, long subEntityClassId) {
//        return entityClass.relations()
//                .stream()
//                .filter(rel -> ("onetoone".equalsIgnoreCase(rel.getRelationType())
//                        || "manytoone".equalsIgnoreCase(rel.getRelationType()))
//                        && rel.getEntityClassId() == subEntityClassId
//                )
//                .map(Relation::getEntityField)
//                .findFirst();
//    }

    @Override
    public CompletionStage<OperationResult> commit(TransactionUp in, Metadata metadata) {
        Long id = Long.parseLong(in.getId());
        OperationResult result = null;

        try {

            transactionManagementService.restore(id);
            transactionManagementService.commit();
            result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.OK)
                    .setMessage("事务提交成功")
                    .buildPartial();
        } catch (Exception e) {
            logger.error("{}", e);
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
                    .setMessage("事务提交成功")
                    .buildPartial();
        } catch (Exception e) {
            logger.error("{}", e);
            result = OperationResult.newBuilder()
                    .setCode(OperationResult.Code.EXCEPTION)
                    .setMessage(Optional.ofNullable(e.getMessage()).orElseGet(e::toString))
                    .buildPartial();
        }
        return CompletableFuture.completedFuture(result);
    }

    private Optional<Long> extractTransaction(Metadata metadata) {
        Optional<String> transactionId = metadata.getText("transaction-id");
        return transactionId.map(Long::valueOf);
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

    //TODO version
    private IEntity toEntity(IEntityClass entityClass, EntityUp in) {
        return new Entity(in.getObjId(), entityClass, toEntityValue(entityClass, in));
    }

    private IEntity toEntity(IEntityClass entityClass, EntityUp in, int version) {
        return new Entity(in.getObjId(), entityClass, toEntityValue(entityClass, in), version);
    }

    private Optional<Conditions> toConditions(IEntityClass mainClass, IEntityClassReader reader
            , ConditionsUp conditionsUp, List<Long> ids) {
        Optional<Conditions> conditions = conditionsUp.getFieldsList().stream().map(x -> {
            /**
             * turn alias field to columnfield
             */
            Optional<AliasField> field = reader.field(x.getField().getId());
            return toOneConditions(field.flatMap(f -> reader.column(f.firstName())), x, mainClass);
        }).filter(Objects::nonNull).reduce((a, b) -> a.addAnd(b, true));

        if (ids != null && !ids.isEmpty()) {

//            Optional<IEntityField> idField = IEntityClassHelper.findFieldByCode(entityClass, "id");
            Optional<IEntityField> idField = reader.column("id").map(ColumnField::getOriginObject);
            Optional<Conditions> conditionsIds = idField.map(field -> {
                return new Conditions(new Condition(field
                        , ConditionOperator.MULTIPLE_EQUALS
                        , ids.stream().map(x -> new LongValue(field, x)).toArray(IValue[]::new)));
            });

            if (conditions.isPresent()) {
                if (conditionsIds.isPresent()) {
                    return conditions.map(x -> x.addAnd(conditionsIds.get(), true));
                }
            } else {
                return conditionsIds;
            }
        }
        return conditions;
    }

    private boolean isRelatedField(ColumnField columnField, IEntityClass mainClass) {
        IEntityClass entityClass = columnField.originEntityClass();

        if (mainClass.extendEntityClass() != null) {
            return mainClass.id() != entityClass.id() && mainClass.extendEntityClass().id() != entityClass.id();
        } else {
            return entityClass.id() != mainClass.id();
        }
    }

    //TODO error handler
    private Conditions toOneConditions(Optional<ColumnField> fieldOp, FieldConditionUp fieldCondition, IEntityClass mainClass) {

        Conditions conditions = null;

        if (fieldOp.isPresent()) {
            FieldConditionUp.Op op = fieldCondition.getOperation();

            ColumnField columnField = fieldOp.get();
            IEntityField originField = columnField;

            //in order
            List<String> nonNullValueList = fieldCondition
                    .getValuesList()
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            //return if field with invalid
            if (nonNullValueList.isEmpty()) {
                conditions = Conditions.buildEmtpyConditions();
                return conditions;
            }

            switch (op) {
                case eq:
                    conditions = new Conditions(new Condition(
                            isRelatedField(columnField, mainClass) ? columnField.originEntityClass() : null
                            , originField
                            , ConditionOperator.EQUALS
                            , toTypedValue(fieldOp.get()
                            , nonNullValueList.get(0)).toArray(new IValue[]{})));
                    break;
                case ne:
                    conditions = new Conditions(new Condition(
                            isRelatedField(columnField, mainClass) ? columnField.originEntityClass() : null
                            , originField
                            , ConditionOperator.NOT_EQUALS
                            , toTypedValue(fieldOp.get()
                            , nonNullValueList.get(0)).toArray(new IValue[]{})));
                    break;
                case ge:
                    conditions = new Conditions(new Condition(
                            isRelatedField(columnField, mainClass) ? columnField.originEntityClass() : null
                            , originField
                            , ConditionOperator.GREATER_THAN_EQUALS
                            , toTypedValue(fieldOp.get()
                            , nonNullValueList.get(0)).toArray(new IValue[]{})));
                    break;
                case gt:
                    conditions = new Conditions(new Condition(
                            isRelatedField(columnField, mainClass) ? columnField.originEntityClass() : null
                            , originField
                            , ConditionOperator.GREATER_THAN
                            , toTypedValue(fieldOp.get()
                            , nonNullValueList.get(0)).toArray(new IValue[]{})));
                    break;
                case ge_le:
                    if (nonNullValueList.size() > 1) {
                        Condition left = new Condition(
                                isRelatedField(columnField, mainClass) ? columnField.originEntityClass() : null
                                , originField
                                , ConditionOperator.GREATER_THAN_EQUALS
                                , toTypedValue(fieldOp.get()
                                , nonNullValueList.get(0)).toArray(new IValue[]{}));

                        Condition right = new Condition(
                                isRelatedField(columnField, mainClass) ? columnField.originEntityClass() : null
                                , originField
                                , ConditionOperator.LESS_THAN_EQUALS
                                , toTypedValue(fieldOp.get()
                                , nonNullValueList.get(1)).toArray(new IValue[]{}));

                        conditions = new Conditions(left).addAnd(right);

                    } else {
                        logger.warn("required value more then 2, fallback to ge");
                        conditions = new Conditions(new Condition(
                                isRelatedField(columnField, mainClass) ? columnField.originEntityClass() : null
                                , originField
                                , ConditionOperator.GREATER_THAN_EQUALS
                                , toTypedValue(fieldOp.get()
                                , nonNullValueList.get(0)).toArray(new IValue[]{})));
                    }
                    break;
                case gt_le:
                    if (nonNullValueList.size() > 1) {
                        Condition left = new Condition(
                                isRelatedField(columnField, mainClass) ? columnField.originEntityClass() : null
                                , originField
                                , ConditionOperator.GREATER_THAN
                                , toTypedValue(fieldOp.get()
                                , nonNullValueList.get(0)).toArray(new IValue[]{}));

                        Condition right = new Condition(
                                isRelatedField(columnField, mainClass) ? columnField.originEntityClass() : null
                                , originField
                                , ConditionOperator.LESS_THAN_EQUALS
                                , toTypedValue(fieldOp.get()
                                , nonNullValueList.get(1)).toArray(new IValue[]{}));


                        conditions = new Conditions(left).addAnd(right);

                    } else {
                        logger.warn("required value more then 2, fallback to gt");
                        conditions = new Conditions(new Condition(
                                isRelatedField(columnField, mainClass) ? columnField.originEntityClass() : null
                                , originField
                                , ConditionOperator.GREATER_THAN
                                , toTypedValue(fieldOp.get()
                                , nonNullValueList.get(0)).toArray(new IValue[]{})));
                    }
                    break;
                case ge_lt:
                    if (nonNullValueList.size() > 1) {
                        Condition left = new Condition(
                                isRelatedField(columnField, mainClass) ? columnField.originEntityClass() : null
                                , originField
                                , ConditionOperator.GREATER_THAN_EQUALS
                                , toTypedValue(fieldOp.get()
                                , nonNullValueList.get(0)).toArray(new IValue[]{}));

                        Condition right = new Condition(
                                isRelatedField(columnField, mainClass) ? columnField.originEntityClass() : null
                                , originField
                                , ConditionOperator.LESS_THAN
                                , toTypedValue(fieldOp.get()
                                , nonNullValueList.get(1)).toArray(new IValue[]{}));


                        conditions = new Conditions(left).addAnd(right);

                    } else {
                        logger.warn("required value more then 2, fallback to ge");
                        conditions = new Conditions(new Condition(
                                isRelatedField(columnField, mainClass) ? columnField.originEntityClass() : null
                                , originField
                                , ConditionOperator.GREATER_THAN_EQUALS
                                , toTypedValue(fieldOp.get()
                                , nonNullValueList.get(0)).toArray(new IValue[]{})));
                    }
                    break;
                case gt_lt:
                    if (nonNullValueList.size() > 1) {
                        Condition left = new Condition(
                                isRelatedField(columnField, mainClass) ? columnField.originEntityClass() : null
                                , originField
                                , ConditionOperator.GREATER_THAN
                                , toTypedValue(fieldOp.get()
                                , nonNullValueList.get(0)).toArray(new IValue[]{}));

                        Condition right = new Condition(
                                isRelatedField(columnField, mainClass) ? columnField.originEntityClass() : null
                                , originField
                                , ConditionOperator.LESS_THAN
                                , toTypedValue(fieldOp.get()
                                , nonNullValueList.get(1)).toArray(new IValue[]{}));


                        conditions = new Conditions(left).addAnd(right);

                    } else {
                        logger.warn("required value more then 2, fallback to ge");
                        conditions = new Conditions(new Condition(
                                isRelatedField(columnField, mainClass) ? columnField.originEntityClass() : null
                                , originField
                                , ConditionOperator.GREATER_THAN_EQUALS
                                , toTypedValue(fieldOp.get()
                                , nonNullValueList.get(0)).toArray(new IValue[]{})));
                    }
                    break;
                case le:
                    conditions = new Conditions(new Condition(
                            isRelatedField(columnField, mainClass) ? columnField.originEntityClass() : null
                            , originField
                            , ConditionOperator.LESS_THAN_EQUALS
                            , toTypedValue(fieldOp.get()
                            , nonNullValueList.get(0)).toArray(new IValue[]{})));
                    break;
                case lt:
                    conditions = new Conditions(new Condition(
                            isRelatedField(columnField, mainClass) ? columnField.originEntityClass() : null
                            , originField
                            , ConditionOperator.LESS_THAN
                            , toTypedValue(fieldOp.get()
                            , nonNullValueList.get(0)).toArray(new IValue[]{})));
                    break;
                case in:
                    conditions = new Conditions(
                            new Condition(
                                    isRelatedField(columnField, mainClass) ? columnField.originEntityClass() : null
                                    , originField
                                    , ConditionOperator.MULTIPLE_EQUALS
                                    , nonNullValueList.stream().flatMap(x -> toTypedValue(fieldOp.get(), x).stream())
                                    .toArray(IValue[]::new)
                            )
                    );
                    break;
                case ni:
                    if (nonNullValueList.size() == 1) {
                        conditions = new Conditions(new Condition(
                                isRelatedField(columnField, mainClass) ? columnField.originEntityClass() : null
                                , originField
                                , ConditionOperator.NOT_EQUALS
                                , toTypedValue(fieldOp.get()
                                , nonNullValueList.get(0)).toArray(new IValue[]{})));
                    } else {
                        conditions = new Conditions(new Condition(
                                isRelatedField(columnField, mainClass) ? columnField.originEntityClass() : null
                                , originField
                                , ConditionOperator.NOT_EQUALS
                                , toTypedValue(fieldOp.get()
                                , nonNullValueList.get(0)).toArray(new IValue[]{})));

                        Conditions finalConditions = conditions;
                        nonNullValueList.stream().skip(1).forEach(x -> {
                            finalConditions.addAnd(new Conditions(new Condition(
                                    isRelatedField(columnField, mainClass) ? columnField.originEntityClass() : null
                                    , originField
                                    , ConditionOperator.NOT_EQUALS
                                    , toTypedValue(fieldOp.get()
                                    , x).toArray(new IValue[]{}))), false);
                        });

                        conditions = finalConditions;
                    }
                    break;
                case like:
                    conditions = new Conditions(new Condition(
                            isRelatedField(columnField, mainClass) ? columnField.originEntityClass() : null
                            , originField
                            , ConditionOperator.LIKE
                            , toTypedValue(fieldOp.get()
                            , nonNullValueList.get(0)).toArray(new IValue[]{})));
                    break;
                default:

            }
        }

        return conditions;
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

    private IEntityValue toEntityValue(IEntityClass entityClass, EntityUp entityUp) {

        IEntityClassReader reader = new IEntityClassReader(entityClass);

        List<IValue> valueList = entityUp.getValuesList().stream()
                .flatMap(y -> {
                    Optional<? extends IEntityField> entityFieldOp = reader.field(y.getFieldId()).map(AliasField::getOrigin);
                    return entityFieldOp
                            .map(x -> toTypedValue(x, y.getValue()))
                            .orElseGet(Collections::emptyList)
                            .stream();
                }).filter(Objects::nonNull).collect(Collectors.toList());
        EntityValue entityValue = new EntityValue(entityUp.getId());
        entityValue.addValues(valueList);
        return entityValue;
    }

    //TODO
    private IEntityField toEntityField(FieldUp fieldUp) {
        return new EntityField(
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
                .precision(fieldUp.getPrecision())
                .identifie(fieldUp.getIdentifier())
        );
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
