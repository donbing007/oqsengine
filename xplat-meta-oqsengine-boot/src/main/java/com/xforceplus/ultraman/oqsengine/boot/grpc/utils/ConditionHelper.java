package com.xforceplus.ultraman.oqsengine.boot.grpc.utils;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsRelation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.sdk.ConditionsUp;
import com.xforceplus.ultraman.oqsengine.sdk.FieldConditionUp;
import io.vavr.Tuple2;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.boot.grpc.utils.EntityClassHelper.isRelatedField;

/**
 * TODO build condition
 */
public class ConditionHelper {

    //TODO error handler
    private Conditions toOneConditions(
              Optional<IEntityField> fieldOp
            , long relationId
            , FieldConditionUp fieldCondition
            , IEntityClass mainClass) {

        Conditions conditions = null;

        if (fieldOp.isPresent()) {
            FieldConditionUp.Op op = fieldCondition.getOperation();

            IEntityField originField = fieldOp.get();

            //in order
            List<String> nonNullValueList = fieldCondition
                    .getValuesList()
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            //return if field with invalid
            if (nonNullValueList.isEmpty()) {
                throw new RuntimeException("Field: " + originField + " Value is Missing");
            }

            switch (op) {
                case eq:
                    conditions = new Conditions(
                            new Condition(
                                    isRelatedField(tuple) ?
                                            EntityClassRef.Builder.anEntityClassRef().withEntityClassId(tuple._1().getEntityClassId()).build() : null
                                    , originField
                                    , ConditionOperator.EQUALS
                                    , toTypedValue(originField, nonNullValueList.get(0)).toArray(new IValue[]{})));
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

        if (conditions == null) {
            throw new RuntimeException("Condition is invalid " + fieldCondition);
        }

        return conditions;
    }


    private static   toOneConditions(){

    }

    public static Optional<Conditions> toConditions(IEntityClass mainClass, ConditionsUp conditionsUp, List<Long> ids) {

        Optional<Conditions> conditions = conditionsUp.getFieldsList().stream().map(x -> {
            /**
             * turn alias field to columnfield
             */
            long fieldId = x.getField().getId();

            Optional<Tuple2<OqsRelation, IEntityField>> fieldById = EntityClassHelper.findFieldById(mainClass, fieldId);

            return toOneConditions(fieldById, x, mainClass);
        }).filter(Objects::nonNull).reduce((a, b) -> a.addAnd(b, true));

        //remove special behavior for ids
//        //Remove Empty ids judgment
        if (ids != null && !ids.isEmpty()) {
            Conditions conditionsIds =
                new Conditions(new Condition(
                          EntityField.ID_ENTITY_FIELD
                        , ConditionOperator.MULTIPLE_EQUALS
                        , ids.stream().map(x -> new LongValue(EntityField.ID_ENTITY_FIELD, x)).toArray(IValue[]::new)));

            if (conditions.isPresent()) {
               return conditions.map(x -> x.addAnd(conditionsIds., true));
            } else {
               return Optional.of(conditionsIds);
            }
        }
        return conditions;
    }
}
