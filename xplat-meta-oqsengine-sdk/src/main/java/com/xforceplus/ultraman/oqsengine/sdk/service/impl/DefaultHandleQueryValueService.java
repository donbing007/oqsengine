package com.xforceplus.ultraman.oqsengine.sdk.service.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.reader.IEntityClassReader;
import com.xforceplus.ultraman.oqsengine.sdk.ConditionsUp;
import com.xforceplus.ultraman.oqsengine.sdk.FieldConditionUp;
import com.xforceplus.ultraman.oqsengine.sdk.service.HandleQueryValueService;
import com.xforceplus.ultraman.oqsengine.sdk.service.OperationType;
import com.xforceplus.ultraman.oqsengine.sdk.service.operation.QuerySideFieldOperationHandler;
import com.xforceplus.ultraman.oqsengine.sdk.service.operation.TriFunction;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.Conditions;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.FieldCondition;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.xforceplus.ultraman.oqsengine.sdk.util.EntityClassToGrpcConverter.toFieldUp;

/**
 * Query Handler chain
 *
 * @see com.xforceplus.ultraman.oqsengine.sdk.service.operation.QuerySideFieldOperationHandler
 */
public class DefaultHandleQueryValueService implements HandleQueryValueService {

    private Logger logger = LoggerFactory.getLogger(HandleQueryValueService.class);

    final
    List<QuerySideFieldOperationHandler> querySideFieldOperationHandler;

    public DefaultHandleQueryValueService(List<QuerySideFieldOperationHandler> querySideFieldOperationHandler) {
        this.querySideFieldOperationHandler = querySideFieldOperationHandler;
    }

    @Override
    public ConditionsUp handleQueryValue(IEntityClass entityClass, Conditions conditions, OperationType phase) {

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

                    //will fail if value is empty
                    return fieldOp.map(field -> {

                        return FieldConditionUp.newBuilder()
                                .setCode(field.name())
                                .setOperation(FieldConditionUp.Op.valueOf(fieldCondition.getOperation().name()))
                                .addAllValues(doHandle(field, fieldCondition.getValue()))
                                .setField(toFieldUp(field))
                                .build();

                    });
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        conditionsUpBuilder.addAllFields(fieldConditionUps);
        return conditionsUpBuilder.build();
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
                    .map(x -> x.apply(field, value, phase))
                    .orElse(value);

        } catch (Exception ex) {
            logger.error("{}", ex);
            return null;
        }
    }
}
