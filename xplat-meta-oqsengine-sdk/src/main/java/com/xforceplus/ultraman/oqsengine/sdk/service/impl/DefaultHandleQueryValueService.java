package com.xforceplus.ultraman.oqsengine.sdk.service.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IEntityClassHelper;
import com.xforceplus.ultraman.oqsengine.sdk.ConditionsUp;
import com.xforceplus.ultraman.oqsengine.sdk.FieldConditionUp;
import com.xforceplus.ultraman.oqsengine.sdk.service.HandleQueryValueService;
import com.xforceplus.ultraman.oqsengine.sdk.service.OperationType;
import com.xforceplus.ultraman.oqsengine.sdk.service.operation.QuerySideFieldOperationHandler;
import com.xforceplus.ultraman.oqsengine.sdk.service.operation.TriFunction;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.Conditions;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.FieldCondition;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.SubFieldCondition;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.xforceplus.ultraman.oqsengine.sdk.FieldConditionUp.Op.eq;
import static com.xforceplus.ultraman.oqsengine.sdk.util.EntityClassToGrpcConverter.toFieldUp;

/**
 * TODO
 */
public class DefaultHandleQueryValueService implements HandleQueryValueService {

    private Logger logger = LoggerFactory.getLogger(HandleQueryValueService.class);

    @Autowired
    List<QuerySideFieldOperationHandler> querySideFieldOperationHandler;

    @Override
    public ConditionsUp handleQueryValue(EntityClass entityClass, Conditions conditions, OperationType phase) {

        ConditionsUp.Builder conditionsUpBuilder = ConditionsUp.newBuilder();

        Stream<Optional<FieldConditionUp>> fieldInMainStream = Optional
            .ofNullable(conditions)
            .map(Conditions::getFields)
            .orElseGet(Collections::emptyList).stream().map(fieldCondition -> {
                return toFieldCondition(entityClass, fieldCondition);
            });


        //from relation to condition
        Stream<Optional<FieldConditionUp>> fieldInRelationStream = Optional.ofNullable(conditions)
            .map(x -> x.getEntities())
            .orElseGet(Collections::emptyList)
            .stream().flatMap(entityCondition -> {
                return toFieldConditionFromRel(entityClass, entityCondition);
            });

        conditionsUpBuilder.addAllFields(Stream.concat(fieldInMainStream, fieldInRelationStream)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList()));
        return conditionsUpBuilder.build();


    }

    private Stream<? extends Optional<FieldConditionUp>> toFieldConditionFromRel(EntityClass entityClass, SubFieldCondition entityCondition) {

        return entityClass.relations().stream()
            .map(rel -> {
                Optional<FieldCondition> fieldConditionOp = entityCondition.getFields()
                    .stream()
                    .filter(enc -> {
                        String code = entityCondition.getCode() + "." + enc.getCode();
                        return rel.getEntityField().name().equalsIgnoreCase(code);
                    }).findFirst();
                return fieldConditionOp.map(fieldCon -> Tuple.of(fieldCon, rel));
            }).map(tuple -> tuple.map(this::toFieldCondition));
    }

    /**
     * @param entityClass
     * @param fieldCondition
     * @return
     */
    private Optional<FieldConditionUp> toFieldCondition(EntityClass entityClass, FieldCondition fieldCondition) {

        Optional<IEntityField> fieldOp = IEntityClassHelper.findFieldByCode(entityClass, fieldCondition.getCode());

        return fieldOp.map(x -> FieldConditionUp.newBuilder()
            .setCode(fieldCondition.getCode())
            .setOperation(Optional.ofNullable(fieldCondition.getOperation())
                .map(Enum::name).map(FieldConditionUp.Op::valueOf).orElse(eq))
            .addAllValues(doHandle(x, fieldCondition.getValue()))
            .setField(toFieldUp(fieldOp.get()))
            .build());
    }


    private FieldConditionUp toFieldCondition(Tuple2<FieldCondition, Relation> tuple) {
        FieldCondition fieldCondition = tuple._1();
        IEntityField entityField = tuple._2().getEntityField();

        return FieldConditionUp.newBuilder()
            .setCode(fieldCondition.getCode())
            .setOperation(FieldConditionUp.Op.valueOf(fieldCondition.getOperation().name()))
            .addAllValues(doHandle(entityField, fieldCondition.getValue()))
            .setField(toFieldUp(entityField))
            .build();
    }


    private List<String> doHandle(IEntityField field, List<String> origin) {
        return Optional.ofNullable(origin)
            .orElseGet(Collections::emptyList).stream()
            .filter(Objects::nonNull)
            .map(input -> pipeline(input, field, OperationType.QUERY))
            .filter(Objects::nonNull)
            .map(Object::toString)
            .collect(Collectors.toList());
    }

    private Object pipeline(Object value, IEntityField field, OperationType phase) {

        try {
            return querySideFieldOperationHandler.stream()
                .sorted()
                .map(x -> (TriFunction) x)
                .reduce(TriFunction::andThen)
                .get()
                .apply(field, value, phase);
        } catch (Exception ex) {
            logger.error("{}", ex);
            return null;
        }
    }
}
