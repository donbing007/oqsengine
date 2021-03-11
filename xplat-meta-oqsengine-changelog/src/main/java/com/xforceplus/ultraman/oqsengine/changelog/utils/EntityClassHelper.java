package com.xforceplus.ultraman.oqsengine.changelog.utils;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldLikeRelationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.oqs.OqsRelation;
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
     * what is a associated relation
     * same name or name end with 'MTO'
     *
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
