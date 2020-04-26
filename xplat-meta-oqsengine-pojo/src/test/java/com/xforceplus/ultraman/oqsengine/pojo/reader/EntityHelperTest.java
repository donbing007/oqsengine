package com.xforceplus.ultraman.oqsengine.pojo.reader;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.ColumnField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IEntityClassHelper;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class EntityHelperTest {

    public IEntityClass entityClass;

    @Before
    public void initEntityclass(){

        EntityField rel1Field = new EntityField(2001L, "rel1.id", FieldType.LONG);
        EntityField relDupField = new EntityField(2004L, "rel1Dup.id", FieldType.LONG);
        EntityField rel2Field = new EntityField(3001L, "rel2.id", FieldType.LONG);
        EntityField rel2DupField = new EntityField(3004L, "rel2Dup.id", FieldType.LONG);
        EntityField rel3Field = new EntityField(4001L, "rel3.ids", FieldType.LONG);

        Relation rel1 = new Relation("rel1", 2L
                , "OneToOne"
                , false, rel1Field);

        Relation rel1Dup = new Relation("rel1Dup", 2L
                , "OneToOne"
                , false, relDupField);

        Relation rel2 = new Relation("rel2", 3L
                , "OneToMany"
                , false, rel2Field);

        Relation rel2Dup = new Relation("rel2Dup", 3L
                , "OneToMany"
                , false, rel2DupField);

        Relation rel3 = new Relation("rel3", 4L
                , "MultiValues"
                , false, rel3Field);


        IEntityClass relatedEntityA = new EntityClass(2L,"one2one-entity"
                , Arrays.asList(new EntityField(2002L, "fieldA2", FieldType.STRING)
                              , new EntityField(2003L, "fieldA3", FieldType.DECIMAL)));

        IEntityClass relatedEntityB = new EntityClass(3L,"one2many-entity"
                , Arrays.asList(new EntityField(3002L, "fieldB2", FieldType.STRING)
                              , new EntityField(3003L, "fieldB3", FieldType.LONG)));

        IEntityClass relatedEntityC = new EntityClass(4L,"multivalues-entity"
                , Arrays.asList(new EntityField(4002L, "fieldC2", FieldType.STRING)
                              , new EntityField(4003L, "fieldC3", FieldType.LONG)));


        IEntityClass parentEntity = new EntityClass(11L, "parent-entity"
                , Arrays.asList(new EntityField(11002L, "fieldC2", FieldType.STRING)
                              , new EntityField(11003L, "fieldC3", FieldType.LONG)
                              , new EntityField(11004L, "fieldC3", FieldType.LONG)
        ));

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
                , Arrays.asList(rel1, rel1Dup, rel2, rel2Dup, rel3)
                , Arrays.asList(relatedEntityA, relatedEntityB, relatedEntityC)
                , parentEntity
                , Arrays.asList(new EntityField(1001L, "field1", FieldType.LONG)));

    }

    @Test
    public void findField(){
        assertTrue("related field can be read", IEntityClassHelper.findFieldByCodeInAll(entityClass, "one2one-entity.fieldA2").isPresent());
    }

    @Test
    public void fieldReaderTest(){
        IEntityClassReader reader = new IEntityClassReader(entityClass);
        assertTrue("related column can be read", reader.column("rel1.fieldA2").isPresent());
    }

    @Test
    public void fieldReaderWillWarning(){
        IEntityClassReader reader = new IEntityClassReader(entityClass);
        assertTrue("related column can be read", reader.column("fieldC3").isPresent());
    }

    @Test
    public void testAllFields(){
        new IEntityClassReader(entityClass).fields().forEach(System.out::println);
    }

    @Test
    public void testZip(){
        Map<String, Object> map = new HashMap<>();
        map.put("what", 1);
        map.put("fieldC3", 3);

        new IEntityClassReader(entityClass)
                .zipValue(map).forEach(System.out::println);
    }

    @Test
    public void testRelated(){
        IEntityClassReader reader = new IEntityClassReader(entityClass);
        IEntityField entityField = new EntityField(2002L, "fieldA2", FieldType.STRING);
        IEntityField field = new ColumnField("rel1.fieldA2", entityField);
        assertTrue("got related field", reader.getRelatedOriginalField(field).isPresent());
    }
}
