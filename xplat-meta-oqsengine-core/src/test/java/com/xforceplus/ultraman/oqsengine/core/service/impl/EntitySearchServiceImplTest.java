package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.BooleanValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * EntitySearchServiceImpl Tester.
 *
 * @author <Authors name>
 * @version 1.0 03/01/2020
 * @since <pre>Mar 1, 2020</pre>
 */
public class EntitySearchServiceImplTest {

    private LongIdGenerator idGenerator = new IncreasingOrderLongIdGenerator(1);

    private final Collection<IEntityField> childFields = Arrays.asList(
        new Field(idGenerator.next(), "c4", FieldType.STRING),
        new Field(idGenerator.next(), "c5", FieldType.LONG),
        new Field(idGenerator.next(), "c6", FieldType.BOOLEAN)
    );

    private final Collection<IEntityField> parentFields = Arrays.asList(
        new Field(idGenerator.next(), "c1", FieldType.STRING),
        new Field(idGenerator.next(), "c2", FieldType.LONG),
        new Field(idGenerator.next(), "c3", FieldType.BOOLEAN)
    );

    private IEntityClass parentEntityClass;
    private IEntityClass childEntityClass;

    private Map<Long, IEntity> expectedEntity;

    private MasterStorage masterStorage;
    private IndexStorage indexStorage;

    private EntitySearchServiceImpl instance;

    @Before
    public void before() throws Exception {

        parentEntityClass = buildIEntityClass(null);
        childEntityClass = buildIEntityClass(parentEntityClass);

        expectedEntity = new HashMap();

        for (int i = 0; i < 3; i++) {
            buildEntity(false);
        }

        for (int i = 0; i < 3; i++) {
            buildEntity(true);
        }


        masterStorage = mock(MasterStorage.class);
        expectedEntity.values().stream().forEach(e -> {
            try {
                when(masterStorage.select(e.id(), e.entityClass())).thenReturn(Optional.of(e));
            } catch (SQLException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        });

        indexStorage = mock(IndexStorage.class);

        instance = new EntitySearchServiceImpl();
        ReflectionTestUtils.setField(instance, "masterStorage", masterStorage);
        ReflectionTestUtils.setField(instance, "indexStorage", indexStorage);
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: selectOne(long id, IEntityClass entityClass)
     */
    @Test
    public void testSelectOne() throws Exception {
        expectedEntity.values().stream().forEach(e -> {
            Optional<IEntity> selectEntityOp;
            try {
                selectEntityOp = instance.selectOne(e.id(), e.entityClass());


                if (e.entityClass().extendEntityClass() == null) {
                    Assert.assertEquals(e, selectEntityOp.get());
                } else {

                    IEntity child = expectedEntity.get(e.id());
                    IEntity parent = expectedEntity.get(child.family().parent());

                    Collection<IValue> childValues = child.entityValue().values();
                    child.entityValue().clear()
                        .addValues(parent.entityValue().values())
                        .addValues(childValues);


                    Assert.assertEquals(child, selectEntityOp.get());
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        });
    }

    private IEntityClass buildIEntityClass(IEntityClass parentEntityClass) {

        long classId = idGenerator.next();
        if (parentEntityClass != null) {
            return new EntityClass(
                classId, "class-" + classId, null, null, parentEntityClass, childFields);
        } else {
            return new EntityClass(classId, "class-" + classId, parentFields);
        }
    }

    private void buildEntity(boolean extend) {
        if (extend) {
            long parentId = idGenerator.next();
            long childId = idGenerator.next();
            expectedEntity.put(parentId, new Entity(
                parentId, parentEntityClass,
                buildValues(parentEntityClass), new EntityFamily(0, childId), 0));

            expectedEntity.put(childId, new Entity(
                childId, childEntityClass,
                buildValues(childEntityClass), new EntityFamily(parentId, 0), 0));


        } else {

            Entity entity = new Entity(
                idGenerator.next(),
                parentEntityClass,
                buildValues(parentEntityClass)
            );

            expectedEntity.put(entity.id(), entity);

        }
    }

    private IEntityValue buildValues(IEntityClass entityClass) {
        Collection<IValue> values = entityClass.fields().stream().map(f -> {
            switch (f.type()) {
                case STRING:
                    return new StringValue(f, buildRandomString(10));
                case LONG:
                    return new LongValue(f, (long) buildRandomLong(0, 100));
                case BOOLEAN:
                    return new BooleanValue(f, buildRandomLong(0, 100) > 50 ? true : false);
            }
            return null;
        }).collect(Collectors.toList());

        return new EntityValue(entityClass.id()).addValues(values);
    }

    private String buildRandomString(int size) {
        StringBuilder buff = new StringBuilder();
        Random rand = new Random(47);
        for (int i = 0; i < size; i++) {
            buff.append(rand.nextInt(26) + 'a');
        }
        return buff.toString();
    }

    private int buildRandomLong(int min, int max) {
        Random random = new Random();

        return random.nextInt(max) % (max - min + 1) + min;
    }

} 
