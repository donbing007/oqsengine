package com.xforceplus.ultraman.oqsengine.sdk.service.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;
import com.xforceplus.ultraman.oqsengine.pojo.reader.IEntityClassReader;
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
import org.apache.commons.lang3.StringUtils;
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
 * Query Handler chain
 * @See com.xforceplus.ultraman.oqsengine.sdk.service.operation.QuerySideFieldOperationHandler
 *
 *
 */
public class DefaultHandleQueryValueService implements HandleQueryValueService {

    private Logger logger = LoggerFactory.getLogger(HandleQueryValueService.class);

    @Autowired
    List<QuerySideFieldOperationHandler> querySideFieldOperationHandler;

    @Override
    public ConditionsUp handleQueryValue(EntityClass entityClass, Conditions conditions, OperationType phase) {

        ConditionsUp.Builder conditionsUpBuilder = ConditionsUp.newBuilder();


        IEntityClassReader reader = new IEntityClassReader(entityClass);

        //entites to query
        Stream<Tuple2<String, FieldCondition>> subFieldConditionStream
                = Optional.ofNullable(conditions).map(Conditions::getEntities)
                          .orElseGet(Collections::emptyList)
                          .stream()
                          .flatMap(x -> x.getFields().stream().map(field -> Tuple.of(x.getCode(), field)));

        //condition self to ;
        Stream<Tuple2<String, FieldCondition>> fieldConditionStream
                = Optional.ofNullable(conditions).map(Conditions::getFields)
                  .orElseGet(Collections::emptyList)
                  .stream().map(x -> Tuple.of("", x));


        List<FieldConditionUp> fieldConditionUps = Stream.concat(fieldConditionStream, subFieldConditionStream)
                .map(x -> {

                    String code = x._1();
                    FieldCondition fieldCondition = x._2();

                    String combinedName = StringUtils.isEmpty(code) ?
                            fieldCondition.getCode()
                            : code + "." + fieldCondition.getCode();

                    Optional<? extends IEntityField> fieldOp = reader.column(combinedName);


                    return fieldOp.map(field ->

                            FieldConditionUp.newBuilder()
                                    .setCode(field.name())
                                    .setOperation(FieldConditionUp.Op.valueOf(fieldCondition.getOperation().name()))
                                    .addAllValues(doHandle(field, fieldCondition.getValue()))
                                    .setField(toFieldUp(field))
                                    .build());
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        conditionsUpBuilder.addAllFields(fieldConditionUps);
        return conditionsUpBuilder.build();
    }

//    private Stream<? extends Optional<FieldConditionUp>> toFieldConditionFromRel(EntityClass entityClass
//            , SubFieldCondition entityCondition) {
//
//        return entityClass.relations().stream()
//            .map(rel -> {
//                Optional<FieldCondition> fieldConditionOp = entityCondition.getFields()
//                    .stream()
//                    .filter(enc -> {
//                        String code = entityCondition.getCode() + "." + enc.getCode();
//                        return rel.getEntityField().name().equalsIgnoreCase(code);
//                    }).findFirst();
//                return fieldConditionOp.map(fieldCon -> Tuple.of(fieldCon, rel));
//            }).map(tuple -> tuple.map(this::toFieldCondition));
//    }

//    /**
//     * mapping field condition to real field
//     * @param entityClass
//     * @param fieldCondition
//     * @return
//     */
//    private Optional<FieldConditionUp> toFieldCondition(EntityClass entityClass, FieldCondition fieldCondition) {
//
//        Optional<IEntityField> fieldOp = IEntityClassHelper.findFieldByCode(entityClass, fieldCondition.getCode());
//
//        return fieldOp.map(x -> FieldConditionUp.newBuilder()
//            .setCode(fieldCondition.getCode())
//            .setOperation(Optional.ofNullable(fieldCondition.getOperation())
//                .map(Enum::name).map(FieldConditionUp.Op::valueOf).orElse(eq))
//            .addAllValues(doHandle(x, fieldCondition.getValue()))
//            .setField(toFieldUp(fieldOp.get()))
//            .build());
//    }


//    private FieldConditionUp toFieldCondition(Tuple2<FieldCondition, Relation> tuple) {
//        FieldCondition fieldCondition = tuple._1();
//        IEntityField entityField = tuple._2().getEntityField();
//
//
//    }

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
