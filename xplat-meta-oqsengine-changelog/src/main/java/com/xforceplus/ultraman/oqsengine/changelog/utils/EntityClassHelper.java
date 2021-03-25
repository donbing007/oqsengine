package com.xforceplus.ultraman.oqsengine.changelog.utils;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldLikeRelationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsRelation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
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
     *   1 strong relation part
     * @param entityClass
     * @return
     */
    public static List<OqsRelation> findPropagationRelation(IEntityClass entityClass){
        List<OqsRelation> oqsRelations = new LinkedList<>();
        entityClass.oqsRelations().forEach(oqsRelation -> {
            if(!isRelationOwner(entityClass.id(), oqsRelation) && oqsRelation.isStrong()){
                oqsRelations.add(oqsRelation);
            }
        });

        return oqsRelations;
    }

    /**
     * A -> B  1:n
     *
     */
    public static List<OqsRelation> findNextRelation(IEntityClass entityClass) {
        List<OqsRelation> oqsRelations = new LinkedList<>();
        entityClass.oqsRelations().forEach(oqsRelation -> {
            if(isRelationOwner(entityClass.id(), oqsRelation)){
                oqsRelations.add(oqsRelation);
            }
        });

        return oqsRelations;
    }

    /**
     * find
     * @param oqsRelation
     * @return
     */
    public static Long findIdAssociatedEntityClassId(OqsRelation oqsRelation){
        if(oqsRelation.getRelationType().equalsIgnoreCase(FieldLikeRelationType.MANY2ONE.getName())
                || oqsRelation.getRelationType().equalsIgnoreCase(FieldLikeRelationType.ONE2ONE.getName())){
            return oqsRelation.getEntityClassId();
        } else {
            return oqsRelation.getRelOwnerClassId();
        }
    }

    public static Optional<OqsRelation> findRelationWithFieldId(IEntityClass entityClass, long fieldId){
        return entityClass.oqsRelations().stream().filter(x -> x.getEntityField().id() == fieldId).findFirst();
    }


    /**
     * what is a associated relation
     * same name or name end with 'MTO'
     * first relation is relation
     * second relation is associated relation
     * @param entityClass
     * @return
     */
    public static List<Tuple2<OqsRelation, OqsRelation>> findAssociatedRelations(IEntityClass entityClass){

        List<Tuple2<OqsRelation, OqsRelation>> oqsRelations = new LinkedList<>();
        Map<String, OqsRelation> temp = new HashMap<>();
        entityClass.oqsRelations().forEach(oqsRelation -> {

            String relName = oqsRelation.getName();

            if(relName.endsWith(MTO)){
                relName = relName.substring(0, relName.length() - MTO.length());
            }

            OqsRelation retOqsRelation = temp.putIfAbsent(relName, oqsRelation);
            if(retOqsRelation != null){
                String manyToOne = FieldLikeRelationType.MANY2ONE.getName();
                //find out which is associate
                if(retOqsRelation.getRelationType().equalsIgnoreCase(manyToOne)
                        && isRelationOwner(entityClass.id(), retOqsRelation)){
                    oqsRelations.add(Tuple.of(oqsRelation, retOqsRelation));
                }else if(oqsRelation.getRelationType().equalsIgnoreCase(manyToOne)
                        && isRelationOwner(entityClass.id(), oqsRelation)){
                    oqsRelations.add(Tuple.of(retOqsRelation, oqsRelation));
                }
            } else {
                temp.put(relName, oqsRelation);
            }
        });

        return oqsRelations;
    }

    private static boolean isRelationOwner(long id, OqsRelation relation){
        return id == relation.getRelOwnerClassId();
    }

}
