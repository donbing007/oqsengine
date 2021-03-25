package com.xforceplus.ultraman.oqsengine.boot.grpc.utils;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsRelation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.sdk.EntityUp;
import io.vavr.Tuple;
import io.vavr.Tuple2;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.boot.grpc.utils.EntityHelper.toTypedValue;

/**
 * new helper
 */
public class EntityClassHelper {

    /**
     * convert entityUp to EntityRef
     *
     * @param entityUp
     * @return
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

    public static IEntity toEntity(EntityClassRef entityClassRef, IEntityClass entityClass, EntityUp in) {
        return Entity.Builder.anEntity()
                .withId(in.getObjId())
                .withEntityClassRef(entityClassRef)
                .withEntityValue(toEntityValue(entityClass, in))
                .build();
    }

    public static boolean isRelatedField( Tuple2<OqsRelation, IEntityField> tuple ) {
        return tuple._1 != null;
    }

    /**
     * TODO test
     *
     * @param entityClass
     * @param id
     * @return
     */
    public static Optional<Tuple2<OqsRelation, IEntityField>> findFieldById(IEntityClass entityClass, long id) {
        Optional<IEntityField> field = entityClass.field(id);
        if (field.isPresent()) {
            return Optional.of(Tuple.of(null, field.get()));
        }

        Optional<Tuple2<OqsRelation, IEntityField>> firstField = entityClass.oqsRelations().stream()
                .filter(x -> x.getRelOwnerClassId() == entityClass.id())
                .map(x -> {
                    IEntityClass relatedEntityClass = x.getEntityClass();
                    Optional<IEntityField> entityField = relatedEntityClass.field(x.getId());
                    return entityField.map(entityF -> {
                        return Tuple.of(x, entityF);
                    });
                }).filter(Optional::isPresent).map(Optional::get).findFirst();

        return firstField;
    }

    /**
     * entityUp -> IEntityValue
     *
     * @param entityClass
     * @param entityUp
     * @return
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
}
