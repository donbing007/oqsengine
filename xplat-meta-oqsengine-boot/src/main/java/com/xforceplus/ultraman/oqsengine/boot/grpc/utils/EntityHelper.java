package com.xforceplus.ultraman.oqsengine.boot.grpc.utils;

import static com.xforceplus.ultraman.oqsengine.pojo.utils.OptionalHelper.ofEmptyStr;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.ValueWithEmpty;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.sdk.EntityUp;
import com.xforceplus.ultraman.oqsengine.sdk.FieldUp;
import com.xforceplus.ultraman.oqsengine.sdk.RelationUp;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Entity helper.
 */
public class EntityHelper {

    /**
     * 构造IEntityField 实例.
     */
    public static IEntityField toEntityField(FieldUp fieldUp) {
        return new EntityField(
            fieldUp.getId(),
            fieldUp.getCode(),
            FieldType.valueOf(fieldUp.getFieldType()),
            FieldConfig.build()
                .searchable(ofEmptyStr(fieldUp.getSearchable()).map(Boolean::valueOf).orElse(false))
                .max(ofEmptyStr(fieldUp.getMaxLength()).map(String::valueOf).map(Long::parseLong).orElse(-1L))
                .min(ofEmptyStr(fieldUp.getMinLength()).map(String::valueOf).map(Long::parseLong).orElse(-1L))
                .precision(fieldUp.getPrecision())
                .identifie(fieldUp.getIdentifier())
        );
    }

    /**
     * 构造关系实例.
     */
    public static Relation toEntityRelation(RelationUp relationUp) {
        return new Relation(relationUp.getName(),
            relationUp.getRelatedEntityClassId(),
            relationUp.getRelationType(),
            relationUp.getIdentity(),
            relationUp.hasEntityField() ? toEntityField(relationUp.getEntityField()) : null);
    }

    /**
     * 构造IEntityClass实例.
     */
    public static IEntityClass toEntityClass(EntityUp entityUp) {

        boolean hasExtendedClass =
            entityUp.hasField(EntityUp.getDescriptor().findFieldByNumber(EntityUp.EXTENDENTITYCLASS_FIELD_NUMBER));

        //Long id, String code, String relation, List<IEntityClass> entityClasss,
        // IEntityClass extendEntityClass, List<Field> fields
        IEntityClass entityClass = new EntityClass(
            entityUp.getId(),
            entityUp.getCode(),
            entityUp.getRelationList().stream().map(EntityHelper::toEntityRelation).collect(Collectors.toList()),
            entityUp.getEntityClassesList().stream().map(EntityHelper::toRawEntityClass).collect(Collectors.toList()),
            hasExtendedClass ? toRawEntityClass(entityUp.getExtendEntityClass()) : null,
            entityUp.getFieldsList().stream().map(EntityHelper::toEntityField).collect(Collectors.toList())
        );
        return entityClass;
    }


    /**
     * to typed value.
     */
    public static List<IValue> toTypedValue(IEntityField entityField, String value) {
        if (value != null && ValueWithEmpty.isEmpty(value)) {
            return Collections.singletonList(new EmptyTypedValue(entityField));
        } else {
            return entityField.type().toTypedValue(entityField, value)
                    .map(Collections::singletonList).orElseGet(Collections::emptyList);
        }
    }

    /**
     * 构造IEntityClass 实例.
     */
    public static IEntityClass toRawEntityClass(EntityUp entityUp) {
        return new EntityClass(
            entityUp.getId(),
            entityUp.getCode(),
            null,
            Collections.emptyList(),
            null,
            entityUp.getFieldsList().stream().map(EntityHelper::toEntityField).collect(Collectors.toList())
        );
    }
}
