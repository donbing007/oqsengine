package com.xforceplus.ultraman.oqsengine.changelog;

import com.xforceplus.ultraman.oqsengine.changelog.utils.EntityClassHelper;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import io.vavr.Tuple2;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class HelperTest {

    @Test
    public void testFindAssociateRelation(){
        EntityClass example = EntityClass.Builder.anEntityClass()
                .withId(2L)
                .withRelations(Arrays.asList(
                        Relationship.Builder.anOqsRelation()
                                .withCode("IIO")
                                .withId(12123)
                                .withLeftEntityClassId(1L)
                                .withRightEntityClassId(2L)
                                .withIdentity(true)
                                .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                                .withBelongToOwner(true)
                                .build(),
                        Relationship.Builder.anOqsRelation()
                                .withId(1222323)
                                .withCode("IIO")
                                .withLeftEntityClassId(2L)
                                .withRightEntityClassId(1L)
                                .withIdentity(true)
                                .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                                .withBelongToOwner(true)
                                .build()
                        ,
                        Relationship.Builder.anOqsRelation()
                                .withCode("SELF")
                                .withId(666)
                                .withLeftEntityClassId(2L)
                                .withRightEntityClassId(2L)
                                .withIdentity(true)
                                .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                                .withBelongToOwner(false)
                                .build(),
                        Relationship.Builder.anOqsRelation()
                                .withCode("SELFMTO")
                                .withId(6667)
                                .withLeftEntityClassId(2L)
                                .withRightEntityClassId(2L)
                                .withIdentity(true)
                                .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                                .withBelongToOwner(true)
                                .build()
                ))
                .build();

        List<Tuple2<Relationship, Relationship>> associatedRelations = EntityClassHelper.findAssociatedRelations(example);
        associatedRelations.forEach(rel -> System.out.println("found:" + rel._1.getId() + " -> " + rel._2.getId()));
    }
}
