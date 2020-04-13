package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;
import com.xforceplus.ultraman.oqsengine.pojo.reader.IEntityClassReader;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IEntityClassHelper;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class EntityHelperTest {

    IEntityClass entityClass;

    @Before
    public void initEntityclass(){

        Relation rel1 = new Relation("rel1", 2L
                , "OneToOne"
                , false, new Field(2001L, "relFieldA", FieldType.LONG));

        Relation rel2 = new Relation("rel2", 3L
                , "OneToMany"
                , false, new Field(3001L, "relFieldB", FieldType.LONG));

        Relation rel3 = new Relation("rel3", 4L
                , "MultiValues"
                , false, new Field(4001L, "relFieldC", FieldType.LONG));


        IEntityClass relatedEntityA = new EntityClass(2L,"one2one-entity"
                , Arrays.asList(new Field(2002L, "fieldA2", FieldType.STRING)
                              , new Field(2003L, "fieldA3", FieldType.DECIMAL)));

        IEntityClass relatedEntityB = new EntityClass(3L,"one2many-entity"
                , Arrays.asList(new Field(3002L, "fieldB2", FieldType.STRING)
                              , new Field(3003L, "fieldB3", FieldType.LONG)));

        IEntityClass relatedEntityC = new EntityClass(4L,"multivalues-entity"
                , Arrays.asList(new Field(4002L, "fieldC2", FieldType.STRING)
                              , new Field(4003L, "fieldC3", FieldType.LONG)));


        IEntityClass parentEntity = new EntityClass(11L, "parent-entity"
                , Arrays.asList(new Field(11002L, "fieldC2", FieldType.STRING)
                , new Field(11003L, "fieldC3", FieldType.LONG)));

        /**
         * Long id,
         * String code,
         * Collection<Relation> relations,
         * Collection<IEntityClass> entityClasss,
         * IEntityClass extendEntityClass,
         * Collection<IEntityField> fields
         */
        entityClass = new EntityClass(
                1L
                , "test-entity"
                , Arrays.asList(rel1, rel2, rel3)
                , Arrays.asList(relatedEntityA, relatedEntityB, relatedEntityC)
                , parentEntity
                , Arrays.asList(new Field(1001L, "field1", FieldType.LONG), new Field(4001L, "relFieldC", FieldType.LONG)));

    }

    @Test
    public void findField(){
        assertTrue("related field can be read", IEntityClassHelper.findFieldByCodeInAll(entityClass, "one2one-entity.fieldA2").isPresent());
    }

    @Test
    public void fieldReaderTest(){
        IEntityClassReader reader = new IEntityClassReader(entityClass);
        assertTrue("related field can be read", reader.field("one2one-entity.fieldA2").isPresent());
    }

}
