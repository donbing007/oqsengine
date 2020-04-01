package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * field like
 */
public enum FieldLikeRelationType {

    ONE2ONE("onetoone", rel -> {

        return toField(rel, FieldType.LONG, "id", true, true, true);
    }, true),

    ONE2MANY("onetomany", rel -> {

        return toField(rel, FieldType.LONG, "id", true, true, false);
    }, false),

    MANY2ONE("manytoone", rel -> {

        return toField(rel, FieldType.LONG, "id", true, true, true);
    }, true),

    MULTI_VALUES("multivalues", rel -> {

        return toField(rel, FieldType.STRINGS, "ids", true, false, true);
    }, true);

    private String name;

    private boolean ownerSide;

    private Function<Relation, IEntityField> fieldTransformer;

    FieldLikeRelationType(String name, Function<Relation, IEntityField> fieldTransformer, boolean ownerSide) {
        this.fieldTransformer = fieldTransformer;
        this.name = name;
        this.ownerSide = ownerSide;
    }

    public boolean isOwnerSide(){
        return ownerSide;
    }

    public IEntityField getField(Relation rel) {
        return fieldTransformer.apply(rel);
    }

    public static IEntityField toField(
            Relation relation
            , FieldType fieldType
            , String defaultName
            , boolean searchable
            , boolean isIdentifier
            , boolean ownerSide) {

        //relation name as field name
        String fieldName = relation.getName();
        if (fieldName == null || "".equals(fieldName)) {
            fieldName = defaultName;
        }

        //concat fieldname
        // owner side => related field code . name
        //           !=> owner field code .name
        fieldName = (!ownerSide ? relation.getRelOwnerClassName() : relation.getEntityClassName())
                .concat(".").concat(fieldName);

        Long fieldId = relation.getId();

        FieldConfig fieldConfig = FieldConfig
                .build()
                .searchable(searchable)
                .identifie(isIdentifier);

        return new Field(fieldId, fieldName, fieldType, fieldConfig);
    }

    public static Optional<FieldLikeRelationType> from(String name) {
        return Stream.of(FieldLikeRelationType.values())
                .filter(x -> x.name.equalsIgnoreCase(name))
                .findFirst();
    }
}
