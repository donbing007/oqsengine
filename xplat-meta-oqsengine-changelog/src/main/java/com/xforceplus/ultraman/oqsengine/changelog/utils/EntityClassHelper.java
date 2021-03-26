package com.xforceplus.ultraman.oqsengine.changelog.utils;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsRelation;
import io.vavr.Tuple;
import io.vavr.Tuple2;

import java.util.*;

/**
 * helper for entityclass
 */
public class EntityClassHelper {

    private final static String MTO = "MTO";

    /**
     * find out propagation relation include
     * two parts
     * 1 strong relation part
     *
     * @param entityClass
     * @return
     */
    public static List<OqsRelation> findPropagationRelation(IEntityClass entityClass) {
        List<OqsRelation> oqsRelations = new LinkedList<>();
        entityClass.oqsRelations().forEach(oqsRelation -> {
            if (!isRelationOwner(entityClass.id(), oqsRelation) && oqsRelation.isStrong()) {
                oqsRelations.add(oqsRelation);
            }
        });

        return oqsRelations;
    }

    /**
     * A -> B  1:n
     */
    public static List<OqsRelation> findNextRelation(IEntityClass entityClass) {
        List<OqsRelation> oqsRelations = new LinkedList<>();
        entityClass.oqsRelations().forEach(oqsRelation -> {
            if (isRelationOwner(entityClass.id(), oqsRelation)) {
                oqsRelations.add(oqsRelation);
            }
        });

        return oqsRelations;
    }

    /**
     * find associated entityClassId
     *
     * @param oqsRelation
     * @return
     */
    public static Long findIdAssociatedEntityClassId(OqsRelation oqsRelation) {
        if (oqsRelation.getRelationType() == OqsRelation.RelationType.MANY_TO_ONE
                || oqsRelation.getRelationType() == OqsRelation.RelationType.ONE_TO_ONE) {
            return oqsRelation.getRightEntityClassId();
        } else {
            return oqsRelation.getLeftEntityClassId();
        }
    }

    public static Optional<OqsRelation> findRelationWithFieldId(IEntityClass entityClass, long fieldId) {
        return entityClass.oqsRelations().stream().filter(x -> x.getEntityField().id() == fieldId).findFirst();
    }


    /**
     * what is a associated relation
     * same name or name end with 'MTO'
     * first relation is relation
     * second relation is associated relation
     *
     * @param entityClass
     * @return
     */
    public static List<Tuple2<OqsRelation, OqsRelation>> findAssociatedRelations(IEntityClass entityClass) {

        List<Tuple2<OqsRelation, OqsRelation>> oqsRelations = new LinkedList<>();
        Map<String, OqsRelation> temp = new HashMap<>();
        entityClass.oqsRelations().forEach(oqsRelation -> {

            String relName = oqsRelation.getCode();

            if (relName.endsWith(MTO)) {
                relName = relName.substring(0, relName.length() - MTO.length());
            }

            OqsRelation retOqsRelation = temp.putIfAbsent(relName, oqsRelation);
            if (retOqsRelation != null) {
                //find out which is associate
                if (retOqsRelation.getRelationType() == OqsRelation.RelationType.MANY_TO_ONE
                        && isRelationOwner(entityClass.id(), retOqsRelation)) {
                    oqsRelations.add(Tuple.of(oqsRelation, retOqsRelation));
                } else if (retOqsRelation.getRelationType() == OqsRelation.RelationType.MANY_TO_ONE
                        && isRelationOwner(entityClass.id(), oqsRelation)) {
                    oqsRelations.add(Tuple.of(retOqsRelation, oqsRelation));
                }
            } else {
                temp.put(relName, oqsRelation);
            }
        });

        return oqsRelations;
    }

    private static boolean isRelationOwner(long id, OqsRelation relation) {
        return id == relation.getLeftEntityClassId();
    }
}