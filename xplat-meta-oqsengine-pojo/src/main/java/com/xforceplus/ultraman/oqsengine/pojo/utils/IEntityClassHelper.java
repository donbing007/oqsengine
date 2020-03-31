package com.xforceplus.ultraman.oqsengine.pojo.utils;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;
import io.vavr.Tuple;
import io.vavr.Tuple2;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static com.xforceplus.ultraman.oqsengine.pojo.utils.OptionalHelper.combine;

/**
 * a helper for entityClass
 */
public class IEntityClassHelper {

    /**
     * find field from entity fields and entityParent fields
     *
     * @param entityClass
     * @param fieldId
     * @return
     */
    public static Optional<IEntityField> findFieldById(IEntityClass entityClass, long fieldId) {

        Optional<IEntityField> entityFieldOp = entityClass.field(fieldId);
        Optional<IEntityField> entityClassFromParent = Optional.ofNullable(entityClass.extendEntityClass())
                .flatMap(parent -> parent.field(fieldId));

        return combine(entityFieldOp, entityClassFromParent);
    }

    public static Optional<IEntityField> findFieldByCode(IEntityClass entityClass, String code) {

        Optional<IEntityField> entityFieldOp = entityClass.field(code);
        Optional<IEntityField> entityClassFromParent = Optional.ofNullable(entityClass.extendEntityClass())
                .flatMap(parent -> parent.field(code));

        return combine(entityFieldOp, entityClassFromParent);
    }

    public static Optional<IEntityField> findFieldInRel(Relation relation, Long id) {
        if (relation.getEntityField() != null) {
            return Optional.ofNullable(relation.getEntityField());
        } else {
            return Optional.empty();
        }
    }

    public static Optional<Tuple2<IEntityClass, IEntityField>> findFieldByIdInAll(IEntityClass entityClass, long fieldId) {
        Stream<Optional<Tuple2<IEntityClass, IEntityField>>> field =
                Stream.of(findFieldById(entityClass, fieldId).map(x -> Tuple.of(entityClass, x)));
        Stream<Optional<Tuple2<IEntityClass, IEntityField>>> subStream = Optional.ofNullable(entityClass.entityClasss())
                .orElseGet(Collections::emptyList).stream()
                .map(x -> findFieldById(x, fieldId).map(y -> Tuple.of(x, y)));
        Stream<Optional<Tuple2<IEntityClass, IEntityField>>> relStream = Optional.ofNullable(entityClass.relations())
                .orElseGet(Collections::emptyList).stream()
                .map(x -> findFieldInRel(x, fieldId).map(y -> Tuple.of(entityClass, y)));
        return Stream.concat(relStream, Stream.concat(field, subStream)).filter(Optional::isPresent).map(Optional::get).findFirst();
    }
}
