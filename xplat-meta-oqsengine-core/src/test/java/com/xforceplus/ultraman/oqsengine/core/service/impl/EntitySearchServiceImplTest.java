package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.BooleanValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * EntitySearchServiceImpl Tester.
 *
 * @author <Authors name>
 * @version 1.0 03/01/2020
 * @since <pre>Mar 1, 2020</pre>
 */
public class EntitySearchServiceImplTest {

    final Logger logger = LoggerFactory.getLogger(EntitySearchServiceImplTest.class);
    private LongIdGenerator idGenerator = new IncreasingOrderLongIdGenerator(1);
    private ExecutorService threadPool;

    private final Collection<IEntityField> childFields = Arrays.asList(
        new EntityField(idGenerator.next(), "c4", FieldType.STRING),
        new EntityField(idGenerator.next(), "c5", FieldType.LONG),
        new EntityField(idGenerator.next(), "c6", FieldType.BOOLEAN)
    );

    private final Collection<IEntityField> parentFields = Arrays.asList(
        new EntityField(idGenerator.next(), "c1", FieldType.STRING),
        new EntityField(idGenerator.next(), "c2", FieldType.LONG),
        new EntityField(idGenerator.next(), "c3", FieldType.BOOLEAN),
        new EntityField(idGenerator.next(), "rel0.id", FieldType.LONG)
    );

    private final Collection<IEntityField> driverFields0 = Arrays.asList(
        new EntityField(idGenerator.next(), "rel0.name", FieldType.STRING),
        new EntityField(idGenerator.next(), "rel0.age", FieldType.LONG)
    );

    private final Collection<IEntityField> driverFields1 = Arrays.asList(
        new EntityField(idGenerator.next(), "rel1.name", FieldType.STRING),
        new EntityField(idGenerator.next(), "rel1.age", FieldType.LONG)
    );

    private IEntityClass parentEntityClass;
    private IEntityClass childEntityClass;
    private IEntityClass driverEntityClass0;
    private IEntityClass driverEntityClass1;
    private IEntityClass notExistDriverEntityClass;

    private Map<Long, IEntity> masterEntities;
    private Map<IEntityClass, Collection<EntityRef>> indexEntities;

    private MasterStorage masterStorage;
    private MockSelectHistoryIndexStorage indexStorage;

    private EntitySearchServiceImpl instance;

    @Before
    public void before() throws Exception {

        parentEntityClass = buildIEntityClass(null, parentFields);
        childEntityClass = buildIEntityClass(parentEntityClass, childFields);
        driverEntityClass0 = buildIEntityClass(null, driverFields0);
        driverEntityClass1 = buildIEntityClass(null, driverFields1);
        notExistDriverEntityClass = buildIEntityClass(null, driverFields0);

        masterEntities = buildMasterEntities();
        indexEntities = buildIndexEntityes();

        masterStorage = new MockMasterStorage(masterEntities.values());

        indexStorage = new MockSelectHistoryIndexStorage(indexEntities);

        threadPool = Executors.newFixedThreadPool(3);

        instance = new EntitySearchServiceImpl();
        ReflectionTestUtils.setField(instance, "masterStorage", masterStorage);
        ReflectionTestUtils.setField(instance, "indexStorage", indexStorage);
        ReflectionTestUtils.setField(instance, "threadPool", threadPool);
        instance.init();
    }

    private Map<IEntityClass, Collection<EntityRef>> buildIndexEntityes() {
        Map<IEntityClass, Collection<EntityRef>> refs = new HashMap<>();
        refs.put(
            driverEntityClass0,
            masterEntities.values().stream()
                .filter(e -> e.entityClass().equals(driverEntityClass0))
                .map(e -> new EntityRef(e.id(), e.family().parent(), e.family().child()))
                .collect(Collectors.toList())
        );

        refs.put(
            driverEntityClass1,
            masterEntities.values().stream()
                .filter(e -> e.entityClass().equals(driverEntityClass1))
                .map(e -> new EntityRef(e.id(), e.family().parent(), e.family().child()))
                .collect(Collectors.toList())
        );

        return refs;
    }

    private Map<Long, IEntity> buildMasterEntities() {
        Map<Long, IEntity> entities = new HashMap();
        IEntity[] newEntities;
        for (int i = 0; i < 3; i++) {
            newEntities = buildEntity(parentEntityClass);
            for (IEntity e : newEntities) {
                entities.put(e.id(), e);
            }
        }

        for (int i = 0; i < 3; i++) {
            newEntities = buildEntity(childEntityClass);
            for (IEntity e : newEntities) {
                entities.put(e.id(), e);
            }
        }

        for (int i = 0; i < 3; i++) {
            newEntities = buildEntity(driverEntityClass0);
            for (IEntity e : newEntities) {
                entities.put(e.id(), e);
            }
        }

        for (int i = 0; i < 3; i++) {
            newEntities = buildEntity(driverEntityClass1);
            for (IEntity e : newEntities) {
                entities.put(e.id(), e);
            }
        }

        return entities;
    }

    @After
    public void after() throws Exception {
        threadPool.shutdown();
    }

    /**
     * Method: selectOne(long id, IEntityClass entityClass)
     */
    @Test
    public void testSelectOne() throws Exception {
        masterEntities.values().stream().forEach(e -> {
            Optional<IEntity> selectEntityOp;
            try {
                selectEntityOp = instance.selectOne(e.id(), e.entityClass());


                if (e.entityClass().extendEntityClass() == null) {
                    Assert.assertEquals(e, selectEntityOp.get());
                } else {

                    IEntity child = masterEntities.get(e.id());
                    IEntity parent = masterEntities.get(child.family().parent());

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

    @Test
    public void testSelectWrongParent() throws Exception {
        IEntity useEntity = masterEntities.values().stream().filter(
            e -> e.entityClass().extendEntityClass() != null).findFirst().get();
        // 不存在的家族信息.
        useEntity.resetFamily(new EntityFamily(0, 0));

        try {
            instance.selectOne(useEntity.id(), useEntity.entityClass()).get();
            Assert.fail("The SQLException was expected to be thrown, but it didn't.");
        } catch (SQLException ex) {
        }
    }

    @Test
    public void testSelectWrongParentQuery() throws Exception {
        IEntity useEntity = masterEntities.values().stream().filter(
            e -> e.entityClass().extendEntityClass() != null).findFirst().get();
        // 不存在的家族信息.
        useEntity.resetFamily(new EntityFamily(Long.MAX_VALUE, 0));

        try {
            instance.selectOne(useEntity.id(), useEntity.entityClass()).get();
            Assert.fail("The SQLException was expected to be thrown, but it didn't.");
        } catch (SQLException ex) {
        }
    }

    @Test
    public void testselectMultiple() throws Exception {

        long[] requestIds = masterEntities.values().stream().filter(
            e -> e.entityClass() == childEntityClass
        ).mapToLong(e -> e.id()).toArray();
        Collection<IEntity> entities = instance.selectMultiple(requestIds, childEntityClass);

        for (IEntity entity : entities) {

            IEntity child = masterEntities.get(entity.id());
            IEntity parent = masterEntities.get(child.family().parent());

            Collection<IValue> childValues = child.entityValue().values();
            child.entityValue().clear()
                .addValues(parent.entityValue().values())
                .addValues(childValues);

            Assert.assertEquals(child, entity);
        }
    }

    @Test
    public void testJoinOverTheMaximumDriver() throws Exception {
        Conditions conditions = Conditions.buildEmtpyConditions()
            .addAnd(
                new Condition(
                    new EntityField(idGenerator.next(), "c4", FieldType.STRING),
                    ConditionOperator.EQUALS,
                    new StringValue(new EntityField(idGenerator.next(), "c4", FieldType.STRING), "v1")
                )
            ).addAnd(
                new Condition(
                    driverEntityClass0,
                    new EntityField(idGenerator.next(), "rel0.age", FieldType.LONG),
                    ConditionOperator.EQUALS,
                    new LongValue(new EntityField(idGenerator.next(), "rel0.age", FieldType.LONG), 100)
                )
            ).addOr(
                new Condition(
                    driverEntityClass1,
                    new EntityField(idGenerator.next(), "rel1.name", FieldType.STRING),
                    ConditionOperator.EQUALS,
                    new StringValue(new EntityField(idGenerator.next(), "rel1.name", FieldType.STRING), "v2")
                )
            );

        try {
            instance.selectByConditions(conditions, childEntityClass, Page.newSinglePage(100));
            Assert.fail("An exception \"exceeding the maximum number of driver entities\" error was expected, but it did not.");
        } catch (SQLException ex) {

        }
    }

    @Test
    public void testJoinOverTheMaximumDriverLen() throws Exception {
        instance.setMaxJoinDriverLineNumber(2);
        Conditions conditions = Conditions.buildEmtpyConditions()
            .addAnd(
                new Condition(
                    new EntityField(idGenerator.next(), "c4", FieldType.STRING),
                    ConditionOperator.EQUALS,
                    new StringValue(new EntityField(idGenerator.next(), "c4", FieldType.STRING), "v1")
                )
            ).addAnd(
                new Condition(
                    driverEntityClass0,
                    new EntityField(idGenerator.next(), "rel0.age", FieldType.LONG),
                    ConditionOperator.EQUALS,
                    new LongValue(new EntityField(idGenerator.next(), "rel0.age", FieldType.LONG), 100)
                )
            );

        try {
            instance.selectByConditions(conditions, childEntityClass, Page.newSinglePage(100));
            Assert.fail("The driver entity exceeded the maximum expected to throw an exception, but did not.");
        } catch (SQLException ex) {

        }

    }

    @Test
    public void testJoinSelect() throws Exception {
        buildJoinCase().stream().forEach(j -> {
            try {
                instance.selectByConditions(j.conditions, j.reusltEntityClass, j.sort, j.page);
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }

            List<SelectHistory> history = new ArrayList<>(indexStorage.getHistories());
            List<String> expectedSelect = new ArrayList<>(j.expectedStrings);
            Assert.assertEquals(expectedSelect.size(), history.size());
            for (int i = 0; i < expectedSelect.size(); i++) {
                logger.info(history.get(i).toString());
                Assert.assertEquals(expectedSelect.get(i), history.get(i).toString());
            }

            indexStorage.reset();
        });
    }

    /**
     * 预期为查询条件的字符串表示.例如如下.
     * class-13.rel0.name = "driver-v1".13.asc:false|des:true|outoforder:true.empty:true|single:false|ready:false
     * 组成如下.
     * {entityClassName}.{fieldName} = {conditionValue}.{entityClassId}.{sort:asc}|{sort:des}|{sort:outoforder}.{page:empty}|{page:single}|{page:ready}
     */
    private Collection<JoinCase> buildJoinCase() {
        return Arrays.asList(
            new JoinCase(
                parentEntityClass,
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                            driverEntityClass0,
                        new EntityField(driverFields0.stream().findFirst().get().id(), "rel0.name", FieldType.STRING),
                            ConditionOperator.EQUALS,
                            new StringValue(new EntityField(idGenerator.next(), "rel0.name", FieldType.STRING), "driver-v1")
                        )
                    )
                ,
                Page.newSinglePage(100),
                Sort.buildOutOfSort(),
                Arrays.asList(
                    driverEntityClass0.code()
                        + ".rel0.name = \"driver-v1\"."
                        + driverEntityClass0.id()
                        + ".asc:false|des:true|outoforder:true.empty:false|single:false|ready:true",

                    driverEntityClass0.code()
                        + ".rel0.name = \"driver-v1\"."
                        + driverEntityClass0.id()
                        + ".asc:false|des:true|outoforder:true.empty:false|single:true|ready:true",

                    "rel0.id IN ("
                        + masterEntities.values().stream()
                        .filter(e -> e.entityClass().equals(driverEntityClass0))
                        .map(e -> Long.toString(e.id()))
                        .collect(Collectors.joining(", "))
                        + ")."
                        + parentEntityClass.id()
                        + ".asc:false|des:true|outoforder:true.empty:false|single:true|ready:true"
                )
            )
            ,
            new JoinCase(
                parentEntityClass,
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                            driverEntityClass0,
                        new EntityField(driverFields0.stream().findFirst().get().id(), "rel0.name", FieldType.STRING),
                            ConditionOperator.EQUALS,
                            new StringValue(new EntityField(idGenerator.next(), "rel0.name", FieldType.STRING), "driver-v1")
                        )
                    )
                    .addAnd(
                        new Condition(
                            driverEntityClass0,
                            new EntityField(driverFields0.stream().skip(1).findFirst().get().id(), "rel0.age", FieldType.LONG),
                            ConditionOperator.EQUALS,
                            new LongValue(new EntityField(idGenerator.next(), "rel0.age", FieldType.LONG), 100)
                        )
                    ),
                Page.newSinglePage(100),
                Sort.buildOutOfSort(),
                Arrays.asList(
                    driverEntityClass0.code()
                        + ".rel0.name = \"driver-v1\" AND " + driverEntityClass0.code() + ".rel0.age = 100."
                        + driverEntityClass0.id()
                        + ".asc:false|des:true|outoforder:true.empty:false|single:false|ready:true",

                    driverEntityClass0.code()
                        + ".rel0.name = \"driver-v1\" AND " + driverEntityClass0.code() + ".rel0.age = 100."
                        + driverEntityClass0.id()
                        + ".asc:false|des:true|outoforder:true.empty:false|single:true|ready:true",

                    "rel0.id IN ("
                        + masterEntities.values().stream()
                        .filter(e -> e.entityClass().equals(driverEntityClass0))
                        .map(e -> Long.toString(e.id()))
                        .collect(Collectors.joining(", "))
                        + ")."
                        + parentEntityClass.id()
                        + ".asc:false|des:true|outoforder:true.empty:false|single:true|ready:true"
                )
            )
            ,
            // 驱动 entity 没有数据.
            new JoinCase(
                parentEntityClass,
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            notExistDriverEntityClass,
                            new EntityField(driverFields0.stream().findFirst().get().id(), "rel0.name", FieldType.STRING),
                            ConditionOperator.EQUALS,
                            new StringValue(new EntityField(idGenerator.next(), "rel0.name", FieldType.STRING), "driver-v1")
                        )
                    ),
                Page.newSinglePage(100),
                Sort.buildOutOfSort(),
                Arrays.asList(
                    notExistDriverEntityClass.code()
                        + ".rel0.name = \"driver-v1\"."
                        + notExistDriverEntityClass.id()
                        + ".asc:false|des:true|outoforder:true.empty:false|single:false|ready:true"
                )
            )
        );
    }

    private class JoinCase {
        private IEntityClass reusltEntityClass;
        private Conditions conditions;
        private Page page;
        private Sort sort;
        private Collection<String> expectedStrings;

        public JoinCase(
            IEntityClass reusltEntityClass,
            Conditions conditions,
            Page page, Sort sort, Collection<String> expectedStrings) {
            this.reusltEntityClass = reusltEntityClass;
            this.conditions = conditions;
            this.page = page;
            this.sort = sort;
            this.expectedStrings = expectedStrings;
        }
    }

    private IEntityClass buildIEntityClass(IEntityClass parentEntityClass, Collection<IEntityField> fields) {

        long classId = idGenerator.next();
        if (parentEntityClass != null) {
            return new EntityClass(
                classId, "class-" + classId, null, null, parentEntityClass, fields);
        } else {
            return new EntityClass(classId, "class-" + classId, fields);
        }
    }

    private IEntity[] buildEntity(IEntityClass entityClass) {
        if (entityClass.extendEntityClass() != null) {
            long parentId = idGenerator.next();
            long childId = idGenerator.next();

            return new Entity[]{
                new Entity(
                    childId,
                    entityClass,
                    buildValues(entityClass),
                    new EntityFamily(parentId, 0),
                    0)
                ,
                new Entity(
                    parentId,
                    entityClass.extendEntityClass(),
                    buildValues(entityClass.extendEntityClass()),
                    new EntityFamily(0, childId), 0)
            };

        } else {

            return new Entity[]{new Entity(
                idGenerator.next(),
                entityClass,
                buildValues(entityClass))};
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

    private class MockMasterStorage implements MasterStorage {

        private Collection<IEntity> entities;

        public MockMasterStorage(Collection<IEntity> entities) {
            this.entities = entities;
        }

        @Override
        public Optional<IEntity> selectOne(long id, IEntityClass entityClass) throws SQLException {
            return entities.stream().filter(e -> e.id() == id && e.entityClass().equals(entityClass)).findFirst();
        }

        @Override
        public Collection<IEntity> selectMultiple(Map<Long, IEntityClass> ids) throws SQLException {
            Collection<IEntity> results = new ArrayList(ids.size());
            for (IEntity e : entities) {
                if (ids.containsKey(e.id()) && e.entityClass().equals(ids.get(e.id()))) {
                    results.add(e);
                }
            }
            return results;
        }

        @Override
        public int synchronize(long id, long child) throws SQLException {
            return 0;
        }

        @Override
        public int build(IEntity entity) throws SQLException {
            return 0;
        }

        @Override
        public int replace(IEntity entity) throws SQLException {
            return 0;
        }

        @Override
        public int delete(IEntity entity) throws SQLException {
            return 0;
        }
    }

    private class MockSelectHistoryIndexStorage implements IndexStorage {

        private Collection<SelectHistory> histories = new ArrayList<>();

        private Map<IEntityClass, Collection<EntityRef>> pool;

        public MockSelectHistoryIndexStorage(Map<IEntityClass, Collection<EntityRef>> pool) {
            this.pool = pool;
        }

        public Collection<SelectHistory> getHistories() {
            return histories;
        }

        public void reset() {
            histories.clear();
        }

        @Override
        public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, Sort sort, Page page)
            throws SQLException {
            histories.add(new SelectHistory(conditions, entityClass, sort, page));

            Collection<EntityRef> refs = pool.get(entityClass);
            if (refs == null) {
                page.setTotalCount(0);
            } else {
                page.setTotalCount(refs.size());
            }
            if (page.isEmptyPage()) {
                refs = null;
            }

            return refs == null ? Collections.emptyList() : refs;
        }

        @Override
        public void replaceAttribute(IEntityValue attribute) throws SQLException {

        }

        @Override
        public int build(IEntity entity) throws SQLException {
            return 0;
        }

        @Override
        public int replace(IEntity entity) throws SQLException {
            return 0;
        }

        @Override
        public int delete(IEntity entity) throws SQLException {
            return 0;
        }
    }

    private class SelectHistory {
        private Conditions conditions;
        private IEntityClass entityClass;
        private Sort sort;
        private Page page;

        public SelectHistory(Conditions conditions, IEntityClass entityClass, Sort sort, Page page) {
            this.conditions = conditions;
            this.entityClass = entityClass;
            this.sort = sort;
            this.page = page;
        }

        @Override
        public String toString() {
            StringBuilder buff = new StringBuilder();
            buff.append(conditions.toString())
                .append(".")
                .append(entityClass.id())
                .append(".asc:")
                .append(sort.isAsc()).append("|des:").append(sort.isDes()).append("|outoforder:").append(sort.isOutOfOrder())
                .append(".empty:");
            buff.append(page.isEmptyPage()).append("|single:").append(page.isSinglePage()).append("|ready:").append(page.isReady());
            return buff.toString();
        }
    }
} 
