package com.xforceplus.ultraman.oqsengine.changelog;

import com.xforceplus.ultraman.oqsengine.changelog.utils.EntityClassHelper;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsRelation;
import io.vavr.Tuple2;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class HelperTest {

    @Test
    public void testFindAssociateRelation(){
        OqsEntityClass example = OqsEntityClass.Builder.anEntityClass()
                .withId(2L)
                .withRelations(Arrays.asList(
                        OqsRelation.Builder.anOqsRelation()
                                .withCode("IIO")
                                .withId(12123)
                                .withLeftEntityClassId(1L)
                                .withRightEntityClassId(2L)
                                .withIdentity(true)
                                .withRelationType(OqsRelation.RelationType.ONE_TO_MANY)
                                .withBelongToOwner(true)
                                .build(),
                        OqsRelation.Builder.anOqsRelation()
                                .withId(1222323)
                                .withCode("IIO")
                                .withLeftEntityClassId(2L)
                                .withRightEntityClassId(1L)
                                .withIdentity(true)
                                .withRelationType(OqsRelation.RelationType.MANY_TO_ONE)
                                .withBelongToOwner(true)
                                .build()
                        ,
                        OqsRelation.Builder.anOqsRelation()
                                .withCode("SELF")
                                .withId(666)
                                .withLeftEntityClassId(2L)
                                .withRightEntityClassId(2L)
                                .withIdentity(true)
                                .withRelationType(OqsRelation.RelationType.ONE_TO_MANY)
                                .withBelongToOwner(false)
                                .build(),
                        OqsRelation.Builder.anOqsRelation()
                                .withCode("SELFMTO")
                                .withId(6667)
                                .withLeftEntityClassId(2L)
                                .withRightEntityClassId(2L)
                                .withIdentity(true)
                                .withRelationType(OqsRelation.RelationType.MANY_TO_ONE)
                                .withBelongToOwner(true)
                                .build()
                ))
                .build();

        List<Tuple2<OqsRelation, OqsRelation>> associatedRelations = EntityClassHelper.findAssociatedRelations(example);
        associatedRelations.forEach(rel -> System.out.println("found:" + rel._1.getId() + " -> " + rel._2.getId()));
    }
}
