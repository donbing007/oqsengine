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
                                .withName("IIO")
                                .withId(12123)
                                .withRelOwnerClassId(1L)
                                .withEntityClassId(2L)
                                .withRelationType("onetomany")
                                .build(),
                        OqsRelation.Builder.anOqsRelation()
                                .withId(1222323)
                                .withName("IIO")
                                .withRelOwnerClassId(2L)
                                .withEntityClassId(1L)
                                .withRelationType("manytoone")
                                .build()
                        ,
                        OqsRelation.Builder.anOqsRelation()
                                .withName("SELF")
                                .withId(666)
                                .withRelOwnerClassId(2L)
                                .withEntityClassId(2L)
                                .withRelationType("onetomany")
                                .build(),
                        OqsRelation.Builder.anOqsRelation()
                                .withName("SELFMTO")
                                .withId(6667)
                                .withRelOwnerClassId(2L)
                                .withEntityClassId(2L)
                                .withRelationType("manytoone")
                                .build()
                ))
                .build();

        List<Tuple2<OqsRelation, OqsRelation>> associatedRelations = EntityClassHelper.findAssociatedRelations(example);
        associatedRelations.forEach(rel -> System.out.println("found:" + rel._1.getId() + " -> " + rel._2.getId()));
    }
}
