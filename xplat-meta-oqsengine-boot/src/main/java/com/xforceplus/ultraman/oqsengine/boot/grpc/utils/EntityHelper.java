package com.xforceplus.ultraman.oqsengine.boot.grpc.utils;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.reader.IEntityClassReader;
import com.xforceplus.ultraman.oqsengine.sdk.EntityUp;
import com.xforceplus.ultraman.oqsengine.sdk.FieldUp;
import com.xforceplus.ultraman.oqsengine.sdk.RelationUp;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.pojo.utils.OptionalHelper.ofEmptyStr;

/**
 * Entity helper
 */
public class EntityHelper {


    public static IEntityField toEntityField(FieldUp fieldUp) {
        return new EntityField(
                fieldUp.getId()
                , fieldUp.getCode()
                , FieldType.valueOf(fieldUp.getFieldType())
                , FieldConfig.build()
                .searchable(ofEmptyStr(fieldUp.getSearchable())
                        .map(Boolean::valueOf).orElse(false))
                .max(ofEmptyStr(fieldUp.getMaxLength())
                        .map(String::valueOf)
                        .map(Long::parseLong).orElse(-1L))
                .min(ofEmptyStr(fieldUp.getMinLength()).map(String::valueOf)
                        .map(Long::parseLong).orElse(-1L))
                .precision(fieldUp.getPrecision())
                .identifie(fieldUp.getIdentifier())
        );
    }


    public static Relation toEntityRelation(RelationUp relationUp) {
        return new Relation(relationUp.getName()
                , relationUp.getRelatedEntityClassId()
                , relationUp.getRelationType()
                , relationUp.getIdentity()
                , relationUp.hasEntityField() ? toEntityField(relationUp.getEntityField()) : null);
    }

    public static IEntityClass getSubEntityClass(EntityUp entityUp) {
        boolean hasSubClass = entityUp.hasField(EntityUp.getDescriptor().findFieldByNumber(EntityUp.SUBENTITYCLASS_FIELD_NUMBER));

        if (hasSubClass) {
            return toEntityClass(entityUp.getSubEntityClass());
        }

        return null;
    }

    public static IEntityClass toEntityClass(EntityUp entityUp) {

        boolean hasExtendedClass = entityUp.hasField(EntityUp.getDescriptor().findFieldByNumber(EntityUp.EXTENDENTITYCLASS_FIELD_NUMBER));

        //Long id, String code, String relation, List<IEntityClass> entityClasss, IEntityClass extendEntityClass, List<Field> fields
        IEntityClass entityClass = new EntityClass(
                entityUp.getId()
                , entityUp.getCode()
                , entityUp.getRelationList().stream()
                .map(EntityHelper::toEntityRelation)
                .collect(Collectors.toList())
                , entityUp.getEntityClassesList().stream()
                .map(EntityHelper::toRawEntityClass)
                .collect(Collectors.toList())
                , hasExtendedClass ? toRawEntityClass(entityUp.getExtendEntityClass()) : null
                , entityUp.getFieldsList().stream().map(EntityHelper::toEntityField).collect(Collectors.toList())
        );
        return entityClass;
    }


    //private helper
    public static List<IValue> toTypedValue(IEntityField entityField, String value) {
        return entityField.type().toTypedValue(entityField, value).map(Collections::singletonList).orElseGet(Collections::emptyList);
    }


    public static IEntityClass toRawEntityClass(EntityUp entityUp) {
        return new EntityClass(
                entityUp.getId()
                , entityUp.getCode()
                , null
                , Collections.emptyList()
                , null
                , entityUp.getFieldsList().stream().map(EntityHelper::toEntityField).collect(Collectors.toList())
        );
    }

    public static IEntityValue toEntityValue(IEntityClass entityClass, EntityUp entityUp) {

        IEntityClassReader reader = new IEntityClassReader(entityClass);

        List<IValue> valueList = entityUp.getValuesList().stream()
                .flatMap(y -> {
                    Optional<? extends IEntityField> entityFieldOp = reader.field(y.getFieldId()).map(AliasField::getOrigin);
                    return entityFieldOp
                            .map(x -> toTypedValue(x, y.getValue()))
                            .orElseGet(Collections::emptyList)
                            .stream();
                }).filter(Objects::nonNull).collect(Collectors.toList());
        EntityValue entityValue = new EntityValue(entityUp.getId());
        entityValue.addValues(valueList);
        return entityValue;
    }
}
