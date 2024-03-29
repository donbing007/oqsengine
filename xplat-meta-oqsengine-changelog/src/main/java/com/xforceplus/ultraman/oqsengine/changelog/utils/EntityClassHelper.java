package com.xforceplus.ultraman.oqsengine.changelog.utils;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * helper for entityclass
 */
public class EntityClassHelper {

    private final static String MTO = "MTO";

    /**
     * find out propagation relation include
     * two parts
     * 1 strong relation part
     */
    public static List<Relationship> findPropagationRelation(IEntityClass entityClass) {
        List<Relationship> relationships = new LinkedList<>();
        entityClass.relationship().forEach(oqsRelation -> {
            if (!isRelationOwner(entityClass.id(), oqsRelation) && oqsRelation.isStrong()) {
                relationships.add(oqsRelation);
            }
        });

        return relationships;
    }

    /**
     * A -> B  1:n
     */
    public static List<Relationship> findNextRelation(IEntityClass entityClass) {
        List<Relationship> relationships = new LinkedList<>();
        entityClass.relationship().forEach(oqsRelation -> {
            if (isRelationOwner(entityClass.id(), oqsRelation)) {
                relationships.add(oqsRelation);
            }
        });

        return relationships;
    }

    /**
     * find associated entityClassId
     */
    public static Long findIdAssociatedEntityClassId(Relationship relationship) {
        if (relationship.getRelationType() == Relationship.RelationType.MANY_TO_ONE
            || relationship.getRelationType() == Relationship.RelationType.ONE_TO_ONE) {
            return relationship.getRightEntityClassId();
        } else {
            return relationship.getLeftEntityClassId();
        }
    }

    public static Optional<Relationship> findRelationWithFieldId(IEntityClass entityClass, long fieldId) {
        return entityClass.relationship().stream().filter(x -> x.getEntityField().id() == fieldId).findFirst();
    }


    /**
     * what is a associated relation
     * same name or name end with 'MTO'
     * first relation is relation
     * second relation is associated relation
     */
    public static List<Tuple2<Relationship, Relationship>> findAssociatedRelations(IEntityClass entityClass) {

        List<Tuple2<Relationship, Relationship>> oqsRelations = new LinkedList<>();
        Map<String, Relationship> temp = new HashMap<>();
        entityClass.relationship().forEach(oqsRelation -> {

            String relName = oqsRelation.getCode();

            if (relName.endsWith(MTO)) {
                relName = relName.substring(0, relName.length() - MTO.length());
            }

            Relationship retRelationship = temp.putIfAbsent(relName, oqsRelation);
            if (retRelationship != null) {
                //find out which is associate
                if (retRelationship.getRelationType() == Relationship.RelationType.MANY_TO_ONE
                    && isRelationOwner(entityClass.id(), retRelationship)) {
                    oqsRelations.add(Tuple.of(oqsRelation, retRelationship));
                } else if (retRelationship.getRelationType() == Relationship.RelationType.MANY_TO_ONE
                    && isRelationOwner(entityClass.id(), oqsRelation)) {
                    oqsRelations.add(Tuple.of(retRelationship, oqsRelation));
                }
            } else {
                temp.put(relName, oqsRelation);
            }
        });

        return oqsRelations;
    }

    private static boolean isRelationOwner(long id, Relationship relation) {
        return id == relation.getLeftEntityClassId();
    }
}