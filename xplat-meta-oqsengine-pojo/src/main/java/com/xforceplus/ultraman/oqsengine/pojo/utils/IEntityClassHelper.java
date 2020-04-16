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
    @Deprecated
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
        if (relation.getEntityField() != null && relation.getEntityField().id() == id) {
            return Optional.ofNullable(relation.getEntityField());
        } else {
            return Optional.empty();
        }
    }

    public static Optional<IEntityField> findFieldInRel(Relation relation, String code) {
        if (relation.getEntityField() != null && relation.getEntityField().name().equals(code)) {
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

    /**
     * Field <-- origin
     * <-- relation
     * <-- subfield
     * find field in entityClass by Code
     * support x.x will
     *
     * @return
     */
    public static Optional<IEntityField> findFieldByCodeInAll(IEntityClass entityClass, String code) {
        //first find in entityclass self 's field

        Optional<IEntityField> fieldInMain = entityClass.field(code);

        Optional<IEntityField> fieldInParent = Optional.ofNullable(entityClass.extendEntityClass()).flatMap(x -> x.field(code));

        Optional<IEntityField> fieldInRel = entityClass.relations().stream().map(x -> findFieldInRel(x, code))
                .findFirst().filter(Optional::isPresent).map(Optional::get);

        String[] splitCode = code.split("\\.");

        Optional<IEntityField> fieldInRelOther;
        if (splitCode.length > 1) {
            // field exists in Related EntityClass
            fieldInRelOther = entityClass.entityClasss()
                    .stream()
                    .filter(x -> x.code().equals(splitCode[0]))
                    .findFirst().map(x -> x.field(splitCode[1])).filter(Optional::isPresent).map(Optional::get);
        } else {
            fieldInRelOther = Optional.empty();
        }

        return OptionalHelper.combine(fieldInMain, fieldInParent, fieldInRel, fieldInRelOther);
    }
}
