package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * field like Relation
 * relation name is the alias for the entity
 */
public enum FieldLikeRelationType {

    ONE2ONE("onetoone", rel -> {

        return toField(rel, FieldType.LONG, "id", true, false, true);
    }, true),

    ONE2MANY("onetomany", rel -> {

        return toField(rel, FieldType.LONG, "id", true, false, false);
    }, false),

    MANY2ONE("manytoone", rel -> {

        return toField(rel, FieldType.LONG, "id", true, false, true);
    }, true),

    MULTI_VALUES("multivalues", rel -> {

        return toField(rel, FieldType.STRINGS, "id", true, false, true);
    }, true);

    private String name;

    private boolean ownerSide;

    private Function<Relation, IEntityField> fieldTransformer;

    FieldLikeRelationType(String name, Function<Relation, IEntityField> fieldTransformer, boolean ownerSide) {
        this.fieldTransformer = fieldTransformer;
        this.name = name;
        this.ownerSide = ownerSide;
    }

    public boolean isOwnerSide() {
        return ownerSide;
    }

    public IEntityField getField(Relation rel) {
        return fieldTransformer.apply(rel);
    }

    /**
     * concat fieldName
     * owner side => related field code . name
     * !=> owner field code .name
     * e.g
     * A 1:1 B | A N:1 B
     * A B.id
     * --------
     * A 1:N B
     * B A.id
     **/
    public static IEntityField toField(
            Relation relation
            , FieldType fieldType
            , String defaultName
            , boolean searchable
            , boolean isIdentifier
            , boolean ownerSide) {


        //determine which is the related field code
        String relationName = relation.getName();
        String relatedEntityName;
        if (relationName == null || relationName.isEmpty()) {
            relatedEntityName = relation.getEntityClassName();
        } else {
            relatedEntityName = relationName;
        }

        String fieldName = (ownerSide ? relatedEntityName : relation.getRelOwnerClassName())
                .concat(".").concat(defaultName);

        Long fieldId = relation.getId();

        //TODO isIdentifier should always false
        //TODO searchable should always true
        FieldConfig fieldConfig = FieldConfig
                .build()
                .searchable(searchable)
                .identifie(isIdentifier);

        return new EntityField(fieldId, fieldName, fieldType, fieldConfig);
    }

    public static Optional<FieldLikeRelationType> from(String name) {
        return Stream.of(FieldLikeRelationType.values())
                .filter(x -> x.name.equalsIgnoreCase(name))
                .findFirst();
    }
}
