package com.xforceplus.ultraman.oqsengine.boot.grpc.utils;

import static com.xforceplus.ultraman.oqsengine.boot.grpc.utils.EntityHelper.toTypedValue;
import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.instanceOf;

import com.xforceplus.ultraman.oqsengine.changelog.domain.EntityAggDomain;
import com.xforceplus.ultraman.oqsengine.changelog.domain.EntityDomain;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsRelation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.sdk.EntityUp;
import com.xforceplus.ultraman.oqsengine.sdk.FieldUp;
import com.xforceplus.ultraman.oqsengine.sdk.OperationResult;
import com.xforceplus.ultraman.oqsengine.sdk.ValueUp;
import io.vavr.Tuple2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * new helper.
 */
public class EntityClassHelper {

    /**
     * convert entityUp to EntityRef.
     */
    public static EntityClassRef toEntityClassRef(EntityUp entityUp) {
        EntityClassRef entityClassRef = EntityClassRef
            .Builder
            .anEntityClassRef()
            .withEntityClassId(entityUp.getId())
            .withEntityClassCode(entityUp.getCode())
            .build();
        return entityClassRef;
    }

    /**
     * 构造实体实例.
     */
    public static IEntity toEntity(EntityClassRef entityClassRef, IEntityClass entityClass, EntityUp in) {
        return Entity.Builder.anEntity()
            .withId(in.getObjId())
            .withEntityClassRef(entityClassRef)
            .withEntityValue(toEntityValue(entityClass, in))
            .build();
    }

    public static boolean isRelatedField(Tuple2<OqsRelation, IEntityField> tuple) {
        return tuple._1 != null;
    }

    /**
     * 查找字段实例.
     */
    public static Optional<IEntityField> findFieldById(IEntityClass entityClass, long id) {
        //find current
        Optional<IEntityField> field = entityClass.field(id);
        if (field.isPresent()) {
            return field;
        }

        Optional<IEntityField> firstField = entityClass.oqsRelations().stream()
            .filter(x -> x.getLeftEntityClassId() == entityClass.id())
            .map(x -> {
                IEntityClass relatedEntityClass = x.getRightEntityClass();
                Optional<IEntityField> entityField = relatedEntityClass.field(x.getId());
                return entityField;
            }).filter(Optional::isPresent).map(Optional::get).findFirst();

        return firstField;
    }

    /**
     * 构造实体字段值实例.
     */
    private static EntityValue toEntityValue(IEntityClass entityClass, EntityUp entityUp) {
        List<IValue> valueList = entityUp.getValuesList().stream()
            .flatMap(y -> {
                //TODO cannot find field like this
                Optional<? extends IEntityField> entityFieldOp = entityClass.field(y.getFieldId());
                return entityFieldOp
                    .map(x -> toTypedValue(x, y.getValue()))
                    .orElseGet(Collections::emptyList)
                    .stream();
            }).filter(Objects::nonNull).collect(Collectors.toList());
        EntityValue entityValue = new EntityValue();
        entityValue.addValues(valueList);
        return entityValue;
    }

    /**
     * 构造Up实例.
     */
    public static EntityUp toEntityUp(IEntity entity) {
        EntityUp.Builder builder = EntityUp.newBuilder();

        builder.setObjId(entity.id());
        builder.addAllValues(entity.entityValue().values().stream()
            .map(EntityClassHelper::toValueUp)
            .collect(Collectors.toList()));
        builder.setId(entity.entityClassRef().getId());
        return builder.build();
    }

    private static ValueUp toValueUp(IValue value) {
        //TODO format?
        IEntityField field = value.getField();
        return ValueUp.newBuilder()
            .setValue(toValueStr(value))
            .setName(field.name())
            .setFieldId(field.id())
            .setFieldType(field.type().name())
            .build();
    }


    /**
     * 将 FieldUp转换成 IEntityField 实例.
     */
    public static Optional<IEntityField> toEntityField(IEntityClass entityClass, FieldUp fieldUp) {
        return entityClass.field(fieldUp.getId());
    }

    private static String toValueStr(IValue value) {
        String retVal
            = Match(value)
            .of(Case($(instanceOf(DateTimeValue.class)), x -> String.valueOf(x.valueToLong())),
                Case($(), IValue::valueToString));
        return retVal;
    }

    /**
     * 构造操作结果实例.
     */
    public static OperationResult toOperationResult(EntityDomain entityDomain) {
        return OperationResult.newBuilder()
            .setCode(OperationResult.Code.OK)
            .setTotalRow(1)
            .addQueryResult(toEntityUp(entityDomain.getEntity()))
            .build();
    }

    /**
     * 构造操作结果实例.
     */
    public static OperationResult toOperationResult(EntityAggDomain entityAggDomain) {
        List<EntityUp> result = new ArrayList();

        Queue<EntityAggDomain> queue = new LinkedList<>();

        queue.offer(entityAggDomain);

        while (!queue.isEmpty()) {
            EntityAggDomain next = queue.poll();
            next.getGraph().values().forEach(x -> x.forEach(queue::offer));
            IEntity rootEntity = next.getRootIEntity();
            result.add(toEntityUp(rootEntity));
        }

        return OperationResult.newBuilder()
            .setCode(OperationResult.Code.OK)
            .setTotalRow(result.size())
            .addAllQueryResult(result)
            .build();
    }
}
