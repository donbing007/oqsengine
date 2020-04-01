package com.xforceplus.ultraman.oqsengine.sdk.util;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ValueConditionNode;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IEntityClassHelper;
import com.xforceplus.ultraman.oqsengine.sdk.*;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.*;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.xforceplus.ultraman.oqsengine.pojo.utils.OptionalHelper.combine;
import static com.xforceplus.ultraman.oqsengine.sdk.FieldConditionUp.Op.*;

/**
 * static converter
 */
public class EntityClassToGrpcConverter {

    public static EntityUp toEntityUp(IEntityClass entityClass) {
        return toEntityUpBuilder(entityClass, null).build();
    }

    public static EntityUp toEntityUp(IEntityClass entityClass, long id) {
        return toEntityUpBuilder(entityClass, id).build();
    }

    public static EntityUp toRawEntityUp(IEntityClass entity) {
        return EntityUp.newBuilder()
                .setId(entity.id())
                .setCode(entity.code())
                .addAllFields(entity.fields().stream().map(EntityClassToGrpcConverter::toFieldUp).collect(Collectors.toList()))
                .build();
    }

    /**
     * TODO check
     *
     * @param entityClass
     * @param body
     * @return
     */
    @Deprecated
    public static EntityUp toEntityUp(EntityClass entityClass, Long id, Map<String, Object> body) {
        //build entityUp
        EntityUp.Builder builder = toEntityUpBuilder(entityClass, id);

        List<ValueUp> values = body.entrySet().stream()
                .map(entry -> {
                    String key = entry.getKey();
                    Optional<IEntityField> fieldOp = getKeyFromEntityClass(entityClass, key);
                    Optional<IEntityField> fieldOpRel = getKeyFromRelation(entityClass, key);
                    Optional<IEntityField> fieldOpParent = getKeyFromParent(entityClass, key);

                    Optional<IEntityField> fieldFinal = combine(fieldOp, fieldOpParent, fieldOpRel);

                    //filter null obj
                    if (entry.getValue() == null) {
                        return Optional.<ValueUp>empty();
                    }

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

    public static EntityUp toEntityUp(EntityClass entityClass, Long id, List<ValueUp> valueList) {
        //build entityUp
        EntityUp.Builder builder = toEntityUpBuilder(entityClass, id);
        builder.addAllValues(valueList);
        return builder.build();
    }


    public static SelectByCondition toSelectByCondition(EntityClass entityClass
            , EntityItem entityItem
            , com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions conditions
            , Sort sort, Page page) {
        SelectByCondition.Builder select = SelectByCondition
                .newBuilder();

        if (page != null) {
            select.setPageNo(Long.valueOf(page.getIndex()).intValue());
            select.setPageSize(Long.valueOf(page.getPageSize()).intValue());
        }

        if (sort != null) {
            select.addSort(toSortUp(sort.getField(), sort.isAsc()));
        }

        select.setEntity(toEntityUp(entityClass));

        if (conditions != null) {
            select.setConditions(toConditionsUp(conditions));
        }

        if (entityItem != null) {
            select.addAllQueryFields(toQueryFields(entityClass, entityItem));
        }

        return select.build();
    }

    public static SelectByCondition toSelectByCondition(EntityClass entityClass, List<Long> ids, ConditionQueryRequest condition) {
        SelectByCondition.Builder select = SelectByCondition
                .newBuilder();

        if (condition.getPageNo() != null) {
            select.setPageNo(condition.getPageNo());
        }

        if (condition.getPageSize() != null) {
            select.setPageSize(condition.getPageSize());
        }

        if (condition.getConditions() != null) {
            select.setConditions(toConditionsUp(entityClass, condition.getConditions()));
        }

        if (condition.getSort() != null) {
            select.addAllSort(toSortUp(condition.getSort()));
        }

        select.setEntity(toEntityUp(entityClass));

        EntityItem entityItem = condition.getEntity();
        if (entityItem != null) {
            select.addAllQueryFields(toQueryFields(entityClass, entityItem));
        }

        if (ids != null) {
            select.addAllIds(ids);
        }

        return select.build();
    }


    /**
     * new
     * @param entityClass
     * @param ids
     * @param condition
     * @return
     */
    public static SelectByCondition toSelectByCondition(EntityClass entityClass, List<Long> ids, ConditionQueryRequest condition, ConditionsUp conditionsUp) {
        SelectByCondition.Builder select = SelectByCondition
                .newBuilder();

        if (condition.getPageNo() != null) {
            select.setPageNo(condition.getPageNo());
        }

        if (condition.getPageSize() != null) {
            select.setPageSize(condition.getPageSize());
        }

        if (condition.getConditions() != null) {
            select.setConditions(conditionsUp);
        }

        if (condition.getSort() != null) {
            select.addAllSort(toSortUp(condition.getSort()));
        }

        select.setEntity(toEntityUp(entityClass));

        EntityItem entityItem = condition.getEntity();
        if (entityItem != null) {
            select.addAllQueryFields(toQueryFields(entityClass, entityItem));
        }

        if (ids != null) {
            select.addAllIds(ids);
        }

        return select.build();
    }

    /**
     * EntityClass to entityUp builder
     *
     * @param entityClass
     * @param id
     * @return
     */
    public static EntityUp.Builder toEntityUpBuilder(IEntityClass entityClass, Long id) {

        EntityUp.Builder builder = EntityUp.newBuilder();

        //add parent
        if (entityClass.extendEntityClass() != null) {
            IEntityClass parent = entityClass.extendEntityClass();
            EntityUp parentUp = toRawEntityUp(parent);
            builder.setExtendEntityClass(parentUp);
        }

        //add obj id
        if (id != null) {
            builder.setObjId(id);
        }

        //add relation
        //relation may has no field
        builder.addAllRelation(entityClass.relations().stream().map(rel -> {
            RelationUp.Builder relation =  RelationUp.newBuilder();

            if(rel.getEntityField() != null){
                relation.setEntityField(toFieldUp(rel.getEntityField()));
            }

            relation.setName(Optional.ofNullable(rel.getName()).orElse(""));
            relation.setRelationType(rel.getRelationType());

            if(rel.isIdentity()){
                relation.setIdentity(rel.isIdentity());
            }

            relation.setRelatedEntityClassId(rel.getEntityClassId());
            return relation.build();
        }).collect(Collectors.toList()));

        builder.setId(entityClass.id())
                .setCode(entityClass.code())
                .addAllFields(entityClass.fields().stream().map(EntityClassToGrpcConverter::toFieldUp).collect(Collectors.toList()));

        if (entityClass.entityClasss() != null && !entityClass.entityClasss().isEmpty()) {
            builder.addAllEntityClasses(entityClass.entityClasss().stream()
                    .map(EntityClassToGrpcConverter::toRawEntityUp).collect(Collectors.toList()));
        }

        return builder;
    }

    private static List<QueryFieldsUp> toQueryFields(IEntityClass entityClass, EntityItem entityItem) {

        Stream<QueryFieldsUp> fieldsUp = Optional.ofNullable(entityItem)
                .map(EntityItem::getFields)
                .orElseGet(Collections::emptyList)
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

        Stream<QueryFieldsUp> fieldsUpFrom = Optional.ofNullable(entityItem)
                .map(EntityItem::getEntities).orElseGet(Collections::emptyList)
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


    public static Optional<IEntityField> getKeyFromRelation(EntityClass entityClass, String key) {
        return entityClass.relations().stream().filter(x -> x.getName().equals(key)).map(Relation::getEntityField).findFirst();
    }

    //TODO sub search
    public static Optional<IEntityField> getKeyFromEntityClass(EntityClass entityClass, String key) {
        return entityClass.field(key);
    }

    public static Optional<IEntityField> getKeyFromParent(EntityClass entityClass, String key) {
        return Optional.ofNullable(entityClass.extendEntityClass()).flatMap(x -> x.field(key));
    }

    private static FieldSortUp toSortUp(IEntityField field, boolean isAsc) {
        return FieldSortUp.newBuilder()
                .setCode(field.name())
                .setOrder(isAsc ? FieldSortUp.Order.asc : FieldSortUp.Order.desc)
                .build();
    }

    private static List<FieldSortUp> toSortUp(List<FieldSort> sort) {
        return sort.stream().map(x -> {
            return FieldSortUp.newBuilder()
                    .setCode(x.getField())
                    .setOrder(FieldSortUp.Order.valueOf(x.getOrder()))
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * flatten all the conditions to field condition
     *
     * @param conditions
     * @return
     */
    private static ConditionsUp toConditionsUp(EntityClass entityClass, Conditions conditions) {
        ConditionsUp.Builder conditionsUpBuilder = ConditionsUp.newBuilder();

        Stream<Optional<FieldConditionUp>> fieldInMainStream = Optional.ofNullable(conditions.getFields())
                .orElseGet(Collections::emptyList).stream().map(fieldCondition -> {
                    return toFieldCondition(entityClass, fieldCondition);
                });

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

    private static ConditionsUp toConditionsUp(com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions conditions) {

        ConditionsUp.Builder conditionsUpBuilder = ConditionsUp.newBuilder();
        conditionsUpBuilder.addAllFields(conditions.collection().stream().filter(com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions::isValueNode)
                .map(x -> ((ValueConditionNode) x).getCondition())
                .map(EntityClassToGrpcConverter::toFieldCondition)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()));
        return conditionsUpBuilder.build();
    }

    private static FieldConditionUp.Op toConditionOp(ConditionOperator conditionOperator) {
        FieldConditionUp.Op op;
        switch (conditionOperator) {
            case GREATER_THAN:
                op = gt;
                break;
            case GREATER_THAN_EQUALS:
                op = ge;
                break;
            case LIKE:
                op = like;
                break;
            case EQUALS:
                op = eq;
                break;
            case NOT_EQUALS:
                op = ne;
                break;
            case LESS_THAN_EQUALS:
                op = le;
                break;
            case LESS_THAN:
                op = lt;
                break;
            default:
                op = eq;
        }
        return op;
    }

    private static Optional<FieldConditionUp> toFieldCondition(Condition condition) {

        IEntityField field = condition.getField();
        IValue value = condition.getValue();
        ConditionOperator operator = condition.getOperator();
        //check
        if (value == null || field == null || operator == null) {
            return Optional.empty();
        }

        FieldConditionUp fieldCondition = FieldConditionUp.newBuilder()
                .setCode(field.name())
                .setOperation(toConditionOp(operator))
                .addValues(value.valueToString())
                .setField(toFieldUp(field))
                .build();

        return Optional.of(fieldCondition);
    }

    /**
     * robust if op is not present return with eq
     * filter any null value in value list
     *
     * @param entityClass
     * @param fieldCondition
     * @return
     */
    private static Optional<FieldConditionUp> toFieldCondition(IEntityClass entityClass, FieldCondition fieldCondition) {

        Optional<IEntityField> fieldOp = IEntityClassHelper.findFieldByCode(entityClass, fieldCondition.getCode());

        return fieldOp.map(x -> FieldConditionUp.newBuilder()
                .setCode(fieldCondition.getCode())
                .setOperation(Optional.ofNullable(fieldCondition.getOperation()).map(Enum::name).map(FieldConditionUp.Op::valueOf).orElse(eq))
                .addAllValues(Optional.ofNullable(fieldCondition.getValue()).orElseGet(Collections::emptyList).stream().filter(Objects::nonNull).collect(Collectors.toList()))
                .setField(toFieldUp(fieldOp.get()))
                .build());
    }


    private static Stream<? extends Optional<FieldConditionUp>> toFieldConditionFromRel(EntityClass entityClass, SubFieldCondition entityCondition) {
        return entityClass.relations().stream()
                .map(rel -> {
                    Optional<FieldCondition> fieldConditionOp = entityCondition.getFields()
                            .stream()
                            .filter(enc -> {
                                String code = entityCondition.getCode() + "." + enc.getCode();
                                return rel.getEntityField().name().equalsIgnoreCase(code);
                            }).findFirst();
                    return fieldConditionOp.map(fieldCon -> Tuple.of(fieldCon, rel));
                }).map(tuple -> tuple.map(EntityClassToGrpcConverter::toFieldCondition));
    }

    private static FieldConditionUp toFieldCondition(Tuple2<FieldCondition, Relation> tuple) {
        FieldCondition fieldCondition = tuple._1();
        IEntityField entityField = tuple._2().getEntityField();

        return FieldConditionUp.newBuilder()
                .setCode(fieldCondition.getCode())
                .setOperation(FieldConditionUp.Op.valueOf(fieldCondition.getOperation().name()))
                .addAllValues(Optional.ofNullable(fieldCondition.getValue()).orElseGet(Collections::emptyList))
                .setField(toFieldUp(entityField))
                .build();
    }


    private static Stream<Optional<FieldConditionUp>> toFieldCondition(EntityClass entityClass, SubFieldCondition subFieldCondition) {
        return entityClass.entityClasss().stream()
                .filter(x -> x.code().equals(subFieldCondition.getCode()))
                .flatMap(entity -> subFieldCondition
                        .getFields()
                        .stream()
                        .map(subField -> toFieldCondition(entity, subField)));
    }

    public static FieldUp toFieldUp(IEntityField field) {
        FieldUp.Builder builder =
                FieldUp.newBuilder()
                        .setCode(field.name())
                        .setFieldType(field.type().name())
                        .setId(field.id());
        if (field.config() != null) {
            builder.setSearchable(String.valueOf(field.config().isSearchable()));
            builder.setMaxLength(String.valueOf(field.config().getMax()));
            builder.setMinLength(String.valueOf(field.config().getMin()));
            builder.setPrecision(field.config().getPrecision());
        }
        return builder.build();
    }

    public static Map<String, Object> toResultMap(EntityClass entityClass
            , EntityClass subEntityClass, EntityUp up) {

        Map<String, Object> map = new HashMap<>();

        up.getValuesList().forEach(entry -> {
            Optional<Tuple2<IEntityClass, IEntityField>> fieldByIdInAll = IEntityClassHelper
                    .findFieldByIdInAll(entityClass, entry.getFieldId());
            Optional<Tuple2<IEntityClass, IEntityField>> subField = IEntityClassHelper.findFieldById(subEntityClass, entry.getFieldId())
                    .map(x -> Tuple.of(subEntityClass, x));
            combine(fieldByIdInAll, subField).ifPresent(tuple2 -> {
                IEntityField field = tuple2._2();
                IEntityClass entity = tuple2._1();
                String fieldName = null;
                if (entityClass.id() != entity.id()) {
                    fieldName = entity.code() + "." + field.name();
                } else {
                    fieldName = field.name();
                }

                if (field.type() == FieldType.BOOLEAN) {

                    map.put(fieldName, Boolean.valueOf(entry.getValue()));
                } else {
                    map.put(fieldName, entry.getValue());
                }
            });
        });

        if (!StringUtils.isEmpty(up.getObjId())) {
            map.put("id", String.valueOf(up.getObjId()));
        }

        return map;
    }


    //TODO
    public static Map<String, Object> toResultMap(EntityClass entityClass, EntityUp up) {

        Map<String, Object> map = new HashMap<>();


        up.getValuesList().forEach(entry -> {
            IEntityClassHelper.findFieldByIdInAll(entityClass, entry.getFieldId()).ifPresent(tuple2 -> {
                IEntityField field = tuple2._2();
                IEntityClass entity = tuple2._1();
                String fieldName = null;
                if (entityClass.id() != entity.id()) {
                    fieldName = entity.code() + "." + field.name();
                } else {
                    fieldName = field.name();
                }

                if (field.type() == FieldType.BOOLEAN) {

                    map.put(fieldName, Boolean.valueOf(entry.getValue()));
                } else {
                    map.put(fieldName, entry.getValue());
                }
            });
        });

        if (!StringUtils.isEmpty(up.getObjId())) {
            map.put("id", String.valueOf(up.getObjId()));
        }

        return map;
    }

    public static Map<String, Object> filterItem(Map<String, Object> values, String mainEntityCode, EntityItem entityItem) {

        if (entityItem == null || entityItem.getEntities() == null || entityItem.getEntities().isEmpty()) {
            return values;
        }

        Map<String, Object> newResult = new HashMap<>();

        //setup main
        entityItem.getFields().forEach(x -> {
            Object value = values.get(x);
            if (value != null) {
                newResult.put(x, value);
            }

            Object otherValue = values.get(mainEntityCode + "." + x);

            if (otherValue != null) {
                newResult.put(x, value);
            }
        });

        entityItem.getEntities().forEach(subEntity -> {
            subEntity.getFields().forEach(field -> {
                String subKey = subEntity.getCode() + "." + field;
                Object value = values.get(subKey);
                if (value != null) {
                    newResult.put(subKey, value);
                }
            });
        });
        return newResult;
    }


}
