package com.xforceplus.ultraman.oqsengine.sdk.util;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IEntityClassHelper;
import com.xforceplus.ultraman.oqsengine.sdk.*;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.*;
import io.vavr.Tuple;
import io.vavr.Tuple2;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.xforceplus.ultraman.oqsengine.pojo.utils.OptionalHelper.combine;


public class EntityClassToGrpcConverter {

    public static EntityUp toEntityUp(IEntityClass entityClass) {
        return toEntityUpBuilder(entityClass, null).build();
    }

    public static EntityUp toEntityUp(IEntityClass entityClass, long id) {
        return toEntityUpBuilder(entityClass, id).build();
    }

    private static EntityUp toRawEntityUp(IEntityClass entity){
        return EntityUp.newBuilder()
                .setId(entity.id())
                .setCode(entity.code())
                .addAllFields(entity.fields().stream().map(EntityClassToGrpcConverter::toFieldUp).collect(Collectors.toList()))
                .build();
    }



    /**
     * TODO check
     * @param entityClass
     * @param body
     * @return
     */
    public static EntityUp toEntityUp(EntityClass entityClass, Long id, Map<String, Object> body){
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

    public static SelectByCondition toSelectByCondition(EntityClass entityClass, ConditionQueryRequest condition){
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

        return select.build();
    }

    /**
     * EntityClass to entityUp builder
     * @param entityClass
     * @param id
     * @return
     */
    private static EntityUp.Builder toEntityUpBuilder(IEntityClass entityClass, Long id){

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
                .addAllFields(entityClass.fields().stream().map(EntityClassToGrpcConverter::toFieldUp).collect(Collectors.toList()));

        if(entityClass.entityClasss() != null && !entityClass.entityClasss().isEmpty()){
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


    private static Optional<IEntityField> getKeyFromRelation(EntityClass entityClass, String key) {
        return entityClass.relations().stream().filter(x ->  x.getName().equals(key)).map(Relation::getEntityField).findFirst();
    }

    //TODO sub search
    private static Optional<IEntityField> getKeyFromEntityClass(EntityClass entityClass, String key ){
        return entityClass.field(key);
    }

    private static Optional<IEntityField> getKeyFromParent(EntityClass entityClass, String key ){
        return Optional.ofNullable(entityClass.extendEntityClass()).flatMap(x -> x.field(key));
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


    private static Optional<FieldConditionUp> toFieldCondition(IEntityClass entityClass, FieldCondition fieldCondition){

        Optional<IEntityField> fieldOp = IEntityClassHelper.findFieldByCode(entityClass, fieldCondition.getCode());

        return fieldOp.map(x -> FieldConditionUp.newBuilder()
                .setCode(fieldCondition.getCode())
                .setOperation(FieldConditionUp.Op.valueOf(fieldCondition.getOperation().name()))
                .addAllValues(Optional.ofNullable(fieldCondition.getValue()).orElseGet(Collections::emptyList))
                .setField(toFieldUp(fieldOp.get()))
                .build());
    }


    private static Stream<? extends Optional<FieldConditionUp>> toFieldConditionFromRel(EntityClass entityClass, SubFieldCondition entityCondition) {
        return entityClass.relations().stream()
                .map(rel -> {
                    Optional<FieldCondition> fieldConditionOp = entityCondition.getFields()
                            .stream()
                            .filter(enc -> {
                                String code = entityCondition.getCode() + "." +enc.getCode();
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


    private static Stream<Optional<FieldConditionUp>> toFieldCondition(EntityClass entityClass, SubFieldCondition subFieldCondition){
        return entityClass.entityClasss().stream()
                .filter(x -> x.code().equals( subFieldCondition.getCode()))
                .flatMap(entity -> subFieldCondition
                        .getFields()
                        .stream()
                        .map(subField -> toFieldCondition(entity, subField)));
    }

    private static FieldUp toFieldUp(IEntityField field){
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



}
