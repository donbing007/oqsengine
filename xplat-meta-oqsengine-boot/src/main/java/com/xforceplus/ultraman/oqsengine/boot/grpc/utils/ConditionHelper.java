package com.xforceplus.ultraman.oqsengine.boot.grpc.utils;

import static com.xforceplus.ultraman.oqsengine.boot.grpc.utils.EntityClassHelper.findFieldById;

import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.sdk.ConditionsUp;
import com.xforceplus.ultraman.oqsengine.sdk.FieldConditionUp;
import com.xforceplus.ultraman.oqsengine.sdk.FieldUp;
import com.xforceplus.ultraman.oqsengine.sdk.FilterNode;
import com.xforceplus.ultraman.oqsengine.sdk.Filters;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 条件构造帮助.
 */
public class ConditionHelper {

    private static Logger logger = LoggerFactory.getLogger(ConditionHelper.class);


    private static List<IValue> toTypedValue(IEntityField entityField, String value) {
        return entityField.type().toTypedValue(entityField, value).map(Collections::singletonList)
            .orElseGet(Collections::emptyList);
    }

    //TODO error handler
    private static Conditions toOneConditions(
        Optional<Tuple2<IEntityClass, IEntityField>> fieldOp, FieldConditionUp fieldCondition) {

        Conditions conditions = null;


        long relationId = fieldCondition.getRelationId();

        if (fieldOp.isPresent()) {
            FieldConditionUp.Op op = fieldCondition.getOperation();

            Tuple2<IEntityClass, IEntityField> tuple = fieldOp.get();
            IEntityField originField = tuple._2();
            IEntityClass mainClass = tuple._1();

            //in order
            List<String> nonNullValueList = fieldCondition
                .getValuesList()
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            EntityClassRef classRef = fieldOp.get()._1.ref();

            switch (op) {
                case eq:
                    conditions = new Conditions(
                        new Condition(
                            classRef,
                            originField,
                            ConditionOperator.EQUALS,
                            relationId,
                            toTypedValue(originField, nonNullValueList.get(0)).toArray(new IValue[] {})));
                    break;
                case ne:
                    conditions = new Conditions(new Condition(
                        classRef,
                        originField,
                        ConditionOperator.NOT_EQUALS,
                        relationId,
                        toTypedValue(originField, nonNullValueList.get(0)).toArray(new IValue[] {})));
                    break;
                case ge:
                    conditions = new Conditions(new Condition(
                        classRef,
                        originField,
                        ConditionOperator.GREATER_THAN_EQUALS,
                        relationId,
                        toTypedValue(originField, nonNullValueList.get(0)).toArray(new IValue[] {})));
                    break;
                case gt:
                    conditions = new Conditions(new Condition(
                        classRef,
                        originField,
                        ConditionOperator.GREATER_THAN,
                        relationId,
                        toTypedValue(originField, nonNullValueList.get(0)).toArray(new IValue[] {})));
                    break;
                case ge_le:
                    if (nonNullValueList.size() > 1) {
                        Condition left = new Condition(
                            classRef,
                            originField,
                            ConditionOperator.GREATER_THAN_EQUALS,
                            relationId,
                            toTypedValue(originField, nonNullValueList.get(0)).toArray(new IValue[] {}));

                        Condition right = new Condition(
                            classRef,
                            originField,
                            ConditionOperator.LESS_THAN_EQUALS,
                            relationId,
                            toTypedValue(originField, nonNullValueList.get(1)).toArray(new IValue[] {}));

                        conditions = new Conditions(left).addAnd(right);

                    } else {
                        logger.warn("required value more then 2, fallback to ge");
                        conditions = new Conditions(new Condition(
                            classRef,
                            originField,
                            ConditionOperator.GREATER_THAN_EQUALS,
                            relationId,
                            toTypedValue(originField, nonNullValueList.get(0)).toArray(new IValue[] {})));
                    }
                    break;
                case gt_le:
                    if (nonNullValueList.size() > 1) {
                        Condition left = new Condition(
                            classRef,
                            originField,
                            ConditionOperator.GREATER_THAN,
                            relationId,
                            toTypedValue(originField, nonNullValueList.get(0)).toArray(new IValue[] {}));

                        Condition right = new Condition(
                            classRef,
                            originField,
                            ConditionOperator.LESS_THAN_EQUALS,
                            relationId,
                            toTypedValue(originField, nonNullValueList.get(1)).toArray(new IValue[] {}));

                        conditions = new Conditions(left).addAnd(right);

                    } else {
                        logger.warn("required value more then 2, fallback to gt");
                        conditions = new Conditions(new Condition(
                            classRef,
                            originField,
                            ConditionOperator.GREATER_THAN,
                            relationId,
                            toTypedValue(originField, nonNullValueList.get(0)).toArray(new IValue[] {})));
                    }
                    break;
                case ge_lt:
                    if (nonNullValueList.size() > 1) {
                        Condition left = new Condition(
                            classRef,
                            originField,
                            ConditionOperator.GREATER_THAN_EQUALS,
                            relationId,
                            toTypedValue(originField, nonNullValueList.get(0)).toArray(new IValue[] {}));

                        Condition right = new Condition(
                            classRef,
                            originField,
                            ConditionOperator.LESS_THAN,
                            relationId,
                            toTypedValue(originField, nonNullValueList.get(1)).toArray(new IValue[] {}));

                        conditions = new Conditions(left).addAnd(right);

                    } else {
                        logger.warn("required value more then 2, fallback to ge");
                        conditions = new Conditions(new Condition(
                            classRef,
                            originField,
                            ConditionOperator.GREATER_THAN_EQUALS,
                            relationId,
                            toTypedValue(originField, nonNullValueList.get(0)).toArray(new IValue[] {})));
                    }
                    break;
                case gt_lt:
                    if (nonNullValueList.size() > 1) {
                        Condition left = new Condition(
                            classRef,
                            originField,
                            ConditionOperator.GREATER_THAN,
                            relationId,
                            toTypedValue(originField, nonNullValueList.get(0)).toArray(new IValue[] {}));

                        Condition right = new Condition(
                            classRef,
                            originField,
                            ConditionOperator.LESS_THAN,
                            relationId,
                            toTypedValue(originField, nonNullValueList.get(1)).toArray(new IValue[] {}));
                        conditions = new Conditions(left).addAnd(right);

                    } else {
                        logger.warn("required value more then 2, fallback to ge");
                        conditions = new Conditions(new Condition(
                            classRef,
                            originField,
                            ConditionOperator.GREATER_THAN_EQUALS,
                            relationId,
                            toTypedValue(originField, nonNullValueList.get(0)).toArray(new IValue[] {})));
                    }
                    break;
                case le:
                    conditions = new Conditions(new Condition(
                        classRef,
                        originField,
                        ConditionOperator.LESS_THAN_EQUALS,
                        relationId,
                        toTypedValue(originField, nonNullValueList.get(0)).toArray(new IValue[] {})));
                    break;
                case lt:
                    conditions = new Conditions(new Condition(
                        classRef,
                        originField,
                        ConditionOperator.LESS_THAN,
                        relationId,
                        toTypedValue(originField, nonNullValueList.get(0)).toArray(new IValue[] {})));
                    break;
                case in:
                    conditions = new Conditions(
                        new Condition(
                            classRef,
                            originField,
                            ConditionOperator.MULTIPLE_EQUALS,
                            relationId,
                            nonNullValueList.stream().flatMap(x ->
                                toTypedValue(originField, x).stream()).toArray(IValue[]::new)
                        )
                    );

                    break;
                case ni:
                    if (nonNullValueList.size() == 1) {
                        conditions = new Conditions(new Condition(
                            classRef,
                            originField,
                            ConditionOperator.NOT_EQUALS,
                            relationId,
                            toTypedValue(originField, nonNullValueList.get(0)).toArray(new IValue[] {})));
                    } else {
                        conditions = new Conditions(new Condition(
                            classRef,
                            originField,
                            ConditionOperator.NOT_EQUALS,
                            relationId,
                            toTypedValue(originField, nonNullValueList.get(0)).toArray(new IValue[] {})));

                        Conditions finalConditions = conditions;
                        EntityClassRef finalClassRef = classRef;
                        nonNullValueList.stream().skip(1).forEach(x -> {
                            finalConditions.addAnd(new Conditions(new Condition(
                                finalClassRef,
                                originField,
                                ConditionOperator.NOT_EQUALS,
                                relationId,
                                toTypedValue(originField, x).toArray(new IValue[] {}))), false);
                        });

                        conditions = finalConditions;
                    }
                    break;
                case like:
                    conditions = new Conditions(new Condition(
                        classRef,
                        originField,
                        ConditionOperator.LIKE,
                        relationId,
                        toTypedValue(originField, nonNullValueList.get(0)).toArray(new IValue[] {})));
                    break;
                case nil:
                    conditions = new Conditions(
                        new Condition(
                            classRef,
                            originField,
                            ConditionOperator.IS_NULL,
                            relationId, new EmptyTypedValue(originField))
                    );
                    break;
                case exists:
                    conditions = new Conditions(
                        new Condition(
                            classRef,
                            originField,
                            ConditionOperator.IS_NOT_NULL,
                            relationId, new EmptyTypedValue(originField))
                    );
                    break;
                default:
            }
        }

        if (conditions == null) {
            throw new RuntimeException("Condition is invalid " + fieldCondition);
        }

        return conditions;
    }

    /**
     * relation can be inherited.
     */
    private static Optional<Relationship> findRelation(IEntityClass mainClass, long relationId) {
        Optional<Relationship> relationOp = mainClass.relationship().stream()
            .filter(rel -> rel.getId() == relationId)
            .findFirst();
        if (!relationOp.isPresent()) {
            if (mainClass.father().isPresent()) {
                return findRelation(mainClass.father().get(), relationId);
            } else {
                return Optional.empty();
            }
        } else {
            return relationOp;
        }
    }

    /**
     * find field within entityField.
     */
    private static Optional<Tuple2<IEntityClass, IEntityField>> findFieldWithInEntityClass(IEntityClass mainClass,
                                                                                           FieldUp field,
                                                                                           MetaManager manager) {

        /*
        如果是identifier,那么返回预定义类型.
         */
        if (field.getIdentifier()) {
            return Optional.of(Tuple.of(mainClass, EntityField.ID_ENTITY_FIELD));
        }

        IEntityClass targetEntityClass = mainClass;
        String profile = mainClass.ref().getProfile();
        long fieldId = field.getId();
        long originEntityClassId = field.getOwnerClassId();
        if (originEntityClassId > 0 && originEntityClassId != mainClass.id()) {
            Optional<IEntityClass> targetOp = manager.load(originEntityClassId, profile);
            if (targetOp.isPresent()) {
                targetEntityClass = targetOp.get();
            } else {
                logger.error("Field's owner {} is missing", originEntityClassId);
            }
        } else {
            if (originEntityClassId <= 0) {
                logger.warn("Field {} not contains ownerId", field);
            }
        }

        Optional<IEntityField> fieldOp = findFieldById(targetEntityClass, fieldId);
        IEntityClass finalClass = targetEntityClass;
        return fieldOp.map(x -> Tuple.of(finalClass, x));
    }

    /**
     * build Condtitons.
     */
    public static Optional<Conditions> toConditions(IEntityClass mainClass, Filters filters, MetaManager manager) {
        if (!filters.isInitialized()) {
            return Optional.empty();
        } else {
            //and clause
            List<FilterNode> filterList = filters.getNodesList();
            return filterList.stream()
                .map(x -> {
                    return toConditions(mainClass, x, manager);
                }).filter(Objects::nonNull)
                .reduce((a, b) -> a.addAnd(b, true));
        }
    }

    private static Conditions toConditions(IEntityClass mainClass, FilterNode node, MetaManager manager) {
        if (node.getNodeType() == 0) {
            //condition
            List<FilterNode> nodesList = node.getNodesList();

            if (FilterNode.Operator.or == node.getOperator()) {
                //or
                return nodesList.stream()
                    .filter(x -> x.getNodeType() == 0)
                    .map(x -> ConditionHelper.toConditions(mainClass, x, manager))
                    .reduce((a, b) -> a.addOr(b, true)).orElseThrow(() -> new RuntimeException("no conditions in OR"));
            } else if (FilterNode.Operator.and == node.getOperator()) {
                //and
                return nodesList.stream()
                    .filter(x -> x.getNodeType() == 0)
                    .map(x -> ConditionHelper.toConditions(mainClass, x, manager))
                    .reduce((a, b) -> a.addAnd(b, true))
                    .orElseThrow(() -> new RuntimeException("no conditions in AND"));
            } else {

                //field node
                Optional<FilterNode> filterNode = extractFieldNode(node);
                List<FilterNode> values = extractValueNode(node);

                Conditions conditions = null;
                if (filterNode.isPresent()) {
                    FilterNode fieldNode = filterNode.get();
                    /*
                     * field related.
                     */
                    long relationId = fieldNode.getRelationId();
                    FieldUp fieldUp = fieldNode.getFieldUp();
                    Optional<Tuple2<IEntityClass, IEntityField>> fieldOp =
                        findFieldOp(relationId, fieldUp, mainClass, mainClass.ref().getProfile(), manager);
                    /*
                     * build field condition.
                     */
                    FieldConditionUp fieldCondition = FieldConditionUp.newBuilder()
                        .setRelationId(relationId)
                        .setOperation(FieldConditionUp.Op.valueOf(node.getOperator().name()))
                        .setField(fieldUp)
                        .addAllValues(values.stream().map(x -> x.getPayload()).collect(Collectors.toList()))
                        .build();

                    /*
                     * TODO  Optional<Tuple2<IEntityClass, IEntityField>> fieldOp
                     *             , FieldConditionUp fieldCondition.
                     */
                    conditions = toOneConditions(fieldOp, fieldCondition);
                }

                if (conditions == null) {
                    throw new RuntimeException(
                        "Condition is invalid with field "
                            + filterNode
                            + " "
                            + node.getOperator()
                            + " values:"
                            + values);
                }

                return conditions;
            }
        }

        return null;
    }


    /**
     * if a condition has relation id than we should find in related class or in main class
     * should consider following cases:
     * 1 search field in main's sub / parent.
     * 2 search related 'sub / parent.
     */
    public static Optional<Conditions> toConditions(IEntityClass mainClass, ConditionsUp conditionsUp, List<Long> ids,
                                                    MetaManager manager) {

        Optional<Conditions> conditions = conditionsUp.getFieldsList().stream().map(x -> {

            FieldUp field = x.getField();
            Optional<Tuple2<IEntityClass, IEntityField>> fieldOp = Optional.empty();

            fieldOp = findFieldOp(x.getRelationId(), field, mainClass, mainClass.ref().getProfile(), manager);

            return toOneConditions(fieldOp, x);
        }).reduce((a, b) -> a.addAnd(b, true));

        //remove special behavior for ids
        //Remove Empty ids judgment
        if (ids != null && !ids.isEmpty()) {
            Conditions conditionsIds =
                new Conditions(new Condition(
                    EntityField.ID_ENTITY_FIELD,
                    ConditionOperator.MULTIPLE_EQUALS,
                    ids.stream().map(x -> new LongValue(EntityField.ID_ENTITY_FIELD, x)).toArray(IValue[]::new)));

            if (conditions.isPresent()) {
                return conditions.map(x -> x.addAnd(conditionsIds, true));
            } else {
                return Optional.of(conditionsIds);
            }
        }
        return conditions;
    }

    private static Optional<FilterNode> extractFieldNode(FilterNode node) {
        return node.getNodesList()
            .stream()
            .filter(x -> x.getNodeType() == 1)
            .findFirst();
    }

    private static List<FilterNode> extractValueNode(FilterNode node) {
        return node.getNodesList()
            .stream()
            .filter(x -> x.getNodeType() == 2)
            .collect(Collectors.toList());
    }

    /**
     * check if relation is belong to current entityclass.
     *
     * @param relation relation
     * @param entityClass entityClass
     */
    private static boolean isRelationBelongsToEntityClass(Relationship relation, IEntityClass entityClass) {

        boolean isMatched = entityClass.id() == relation.getLeftEntityClassId();

        if (!isMatched) {
            IEntityClass ptr = entityClass;
            while (ptr.father() != null && ptr.father().isPresent()) {
                ptr = ptr.father().get();
                if (relation.getLeftEntityClassId() == ptr.id()) {
                    isMatched = true;
                    break;
                }
            }
        }

        return isMatched;
    }

    private static Optional<Tuple2<IEntityClass, IEntityField>> findFieldOp(long relationId, FieldUp field,
                                                                            IEntityClass mainClass,
                                                                            String profile,
                                                                            MetaManager manager) {

        Optional<Tuple2<IEntityClass, IEntityField>> fieldOp = Optional.empty();

        //TODO relation is inherited
        if (relationId > 0) {
            //find in related
            Optional<Relationship> relationOp = findRelation(mainClass, relationId);

            if (relationOp.isPresent()) {
                Relationship relation = relationOp.get();

                if (isRelationBelongsToEntityClass(relation, mainClass)) {
                    Optional<IEntityClass> relatedEntityClassOp =
                        manager.load(relation.getRightEntityClassId(), profile);
                    if (relatedEntityClassOp.isPresent()) {
                        fieldOp = findFieldWithInEntityClass(relatedEntityClassOp.get(), field, manager);
                    } else {
                        logger.error("related EntityClass {} is missing", relation.getRightEntityClassId());
                    }
                }
            }
        } else {
            //find in main
            fieldOp = findFieldWithInEntityClass(mainClass, field, manager);
        }
        return fieldOp;
    }
}