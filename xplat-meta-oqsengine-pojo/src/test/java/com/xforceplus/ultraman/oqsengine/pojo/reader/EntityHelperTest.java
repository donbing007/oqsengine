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
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.GraphLayout;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertTrue;

public class EntityHelperTest {

    public IEntityClass entityClass;
    private IEntityClass entityClass1;

    @Before
    public void initEntityclass(){

        entityClass = gen();

    }

    private EntityClass gen(){
        EntityField rel1Field = new EntityField(2001L, "rel1.id", FieldType.LONG);
        EntityField relDupField = new EntityField(2004L, "rel1Dup.id", FieldType.LONG);
        EntityField rel2Field = new EntityField(3001L, "rel2.id", FieldType.LONG);
        EntityField rel2DupField = new EntityField(3004L, "rel2Dup.id", FieldType.LONG);
        EntityField rel3Field = new EntityField(4001L, "rel3.id", FieldType.LONG);

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
        return new EntityClass(
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

        long time1 = System.currentTimeMillis();
        IEntityClassReader reader = new IEntityClassReader(entityClass);
        long time2 = System.currentTimeMillis();
        System.out.println(time2 - time1);
        long time3 = System.currentTimeMillis();
        IEntityClassReader reader2 = new IEntityClassReader(entityClass);
        long time4 = System.currentTimeMillis();
        System.out.println(time4 - time3);

        IEntityClass entityClass2 = gen();

        long time5 = System.currentTimeMillis();
        IEntityClassReader reader3 = new IEntityClassReader(entityClass2);
        long time6 = System.currentTimeMillis();
        System.out.println(time6 - time5);

        IEntityField entityField = new EntityField(2002L, "fieldA2", FieldType.STRING);
        IEntityField field = new ColumnField("rel1.fieldA2", entityField, null);
        assertTrue("got related field", reader.getRelatedOriginalField(field).isPresent());


    }

    @Test
    public void testTime(){
        long time1 = System.currentTimeMillis();
        for(int i = 0 ; i < 10000 ; i ++) {
            Optional<IEntityField> field = entityClass.field("fieldA2");
        }
        long time2 = System.currentTimeMillis();
        System.out.println(time2 - time1);

        IEntityClassReader reader = new IEntityClassReader(entityClass);
        long time3 = System.currentTimeMillis();
        for(int i = 0 ; i < 10000 ; i ++) {
            reader.column("fieldA2");
        }
        long time4 = System.currentTimeMillis();
        System.out.println(time4 - time3);

//        System.out.println(ClassLayout.parseClass(IEntityClassReader.class).toPrintable(reader));
//        System.out.println(GraphLayout.parseInstance(reader).toPrintable());
        System.out.println(GraphLayout.parseInstance(reader).toFootprint());
        System.out.println(ClassLayout.parseInstance(reader).toPrintable());


//        System.out.println(ClassLayout.parseClass(IEntityClass.class).toPrintable(entityClass));
//        System.out.println(GraphLayout.parseInstance(entityClass).toPrintable());
        System.out.println(GraphLayout.parseInstance(entityClass).toFootprint());
    }

    @Test
    public void testMemMap(){
        Map<String, Object> map = new HashMap<>();
        Object a = new Object();
        String ab = "";
        Object ref = new Object();
        map.put("a", ab);
        map.put("b", "2");
        map.put("c", ref);
        map.put("d", ref);
//        map.put("b", ab);
        System.out.println(GraphLayout.parseInstance(map).toFootprint());
        System.out.println(GraphLayout.parseInstance(map).totalSize());
    }

    @Test
    public void testMem(){

        System.out.println(ClassLayout.parseClass(String.class).toPrintable());
        System.out.println(ClassLayout.parseClass(String.class).toPrintable("1234567890"));


        System.out.println(GraphLayout.parseInstance("1234567890").toPrintable());
        System.out.println(GraphLayout.parseInstance("1234567890").toFootprint());
    }
}
