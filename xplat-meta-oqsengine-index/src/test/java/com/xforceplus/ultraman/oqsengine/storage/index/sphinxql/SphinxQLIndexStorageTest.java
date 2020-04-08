package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql;

import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.*;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoShardTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.optimizer.DefaultSphinxQLQueryOptimizer;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.selector.TakeTurnsSelector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.sql.SphinxQLTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * SphinxQLIndexStorage Tester.
 *
 * @author <Authors name>
 * @version 1.0 02/26/2020
 * @since <pre>Feb 26, 2020</pre>
 */
public class SphinxQLIndexStorageTest {


    private TransactionManager transactionManager = new DefaultTransactionManager(
        new IncreasingOrderLongIdGenerator(0));
    private LongIdGenerator idGenerator = new IncreasingOrderLongIdGenerator(1);
    private SphinxQLIndexStorage storage;
    private List<IEntity> expectedEntitys;
    private DataSourcePackage dataSourcePackage;
    // 所有数据都会有的字段,用以选择所有数据
    private IEntityField fixFieldAll = new Field(100000, "all", FieldType.BOOLEAN);
    // 所有数据都会有的字段,用以范围选择.值为随机数字.
    private IEntityField fixFieldRange = new Field(100001, "range", FieldType.LONG);
    // 所有数据都会有的负数字符串.
    private IEntityField fixStringNumber = new Field(100002, "Negative string", FieldType.STRING);

    private IEntityField fixStringsField = new Field(100003, "strings", FieldType.STRINGS);


    @Before
    public void before() throws Exception {


        Selector<DataSource> dataSourceSelector = buildDataSourceSelector("./src/test/resources/sql_index_storage.conf");


        truncate();

        // 等待加载完毕
        TimeUnit.SECONDS.sleep(1L);

        TransactionExecutor executor = new AutoShardTransactionExecutor(
            transactionManager, SphinxQLTransactionResource.class);

        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new SphinxQLDecimalStorageStrategy());

        DefaultSphinxQLQueryOptimizer optimizer = new DefaultSphinxQLQueryOptimizer();
        optimizer.setStorageStrategy(storageStrategyFactory);
        optimizer.init();

        storage = new SphinxQLIndexStorage();
        ReflectionTestUtils.setField(storage, "writerDataSourceSelector", dataSourceSelector);
        ReflectionTestUtils.setField(storage, "searchDataSourceSelector", dataSourceSelector);
        ReflectionTestUtils.setField(storage, "transactionExecutor", executor);
        ReflectionTestUtils.setField(storage, "queryOptimizer", optimizer);
        ReflectionTestUtils.setField(storage, "storageStrategyFactory", storageStrategyFactory);
        storage.setIndexTableName("oqsindex");
//        storage.init();


        transactionManager.create();

        expectedEntitys = initData(storage, 10);

        // 确认没有事务.
        Assert.assertFalse(transactionManager.getCurrent().isPresent());

    }

    private void truncate() {
        List<DataSource> ds = dataSourcePackage.getIndexWriter();
        ds.stream().forEach(d -> {
            try {
                Connection conn = d.getConnection();
                boolean autocommit = conn.getAutoCommit();
                conn.setAutoCommit(true);

                Statement st = conn.createStatement();
                st.executeUpdate("TRUNCATE RTINDEX oqsindex");

                st.close();

                conn.setAutoCommit(autocommit);

                conn.close();

            } catch (SQLException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }

        });
    }

    @After
    public void after() throws Exception {

        Optional<Transaction> t = transactionManager.getCurrent();
        if (t.isPresent()) {
            Transaction tx = t.get();
            if (!tx.isCompleted()) {
                tx.rollback();
            }
        }

        transactionManager.finish();

        truncate();

        dataSourcePackage.close();
    }

    @Test
    public void testReplaceAttribute() throws Exception {
        transactionManager.create();

        IEntity expectedEntity = expectedEntitys.stream().findAny().get();
        IValue value = expectedEntity.entityValue().values().stream().filter(
            v -> v.getField().id() == fixStringNumber.id()).findFirst().get();

        IEntityField field = value.getField();
        IValue newValue = new StringValue(field, "-100");

        expectedEntity.entityValue().addValue(newValue);

        storage.replaceAttribute(expectedEntity.entityValue());

        transactionManager.getCurrent().get().commit();
        transactionManager.finish();

        Conditions conditions = Conditions.buildEmtpyConditions();
        conditions.addAnd(new Condition(field, ConditionOperator.EQUALS, newValue));

        Collection<EntityRef> refs = storage.select(
            conditions, expectedEntity.entityClass(), null, Page.newSinglePage(100));

        Assert.assertEquals(1, refs.size());
        Assert.assertEquals(expectedEntity.id(), refs.stream().findFirst().get().getId());
        Assert.assertEquals(expectedEntity.family().parent(), refs.stream().findFirst().get().getPref());
        Assert.assertEquals(expectedEntity.family().child(), refs.stream().findFirst().get().getCref());

    }

    @Test
    public void testDeleteSuccess() throws Exception {

        transactionManager.create();

        expectedEntitys.stream().forEach(e -> {
            try {

                storage.delete(e);


            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        });

        transactionManager.getCurrent().get().commit();
        transactionManager.finish();

        Collection<EntityRef> refs = storage.select(
            new Conditions(new Condition(fixFieldAll, ConditionOperator.EQUALS, new BooleanValue(fixFieldAll, true))),
            expectedEntitys.stream().findFirst().get().entityClass(),
            null,
            Page.newSinglePage(100)
        );

        Assert.assertEquals(0, refs.size());
    }

    @Test
    public void testDeleteFailure() throws Exception {

        transactionManager.create();

        expectedEntitys.stream().forEach(e -> {
            try {

                storage.delete(e);


            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        });

        // 无条件 rollback.
        transactionManager.getCurrent().get().rollback();
        transactionManager.finish();

        Collection<EntityRef> refs = storage.select(
            new Conditions(new Condition(fixFieldAll, ConditionOperator.EQUALS, new BooleanValue(fixFieldAll, true))),
            expectedEntitys.stream().findFirst().get().entityClass(),
            null,
            Page.newSinglePage(100)
        );

        Assert.assertEquals(expectedEntitys.size(), refs.size());
    }

    @Test
    public void testSelectCase() throws Exception {
        // 确认没有事务.
        Assert.assertFalse(transactionManager.getCurrent().isPresent());

        // 每一个都以独立事务运行.
        buildSelectCase().stream().forEach(c -> {

            Collection<EntityRef> refs = null;
            try {
                refs = storage.select(c.conditions, c.entityClass, null, c.page);
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }

            c.check.test(refs);
        });

    }

    private static class Case {
        private Conditions conditions;
        private IEntityClass entityClass;
        private Page page;
        private Predicate<? super Collection<EntityRef>> check;

        public Case(Conditions conditions, IEntityClass entityClass, Page page,
                    Predicate<? super Collection<EntityRef>> check) {
            this.conditions = conditions;
            this.entityClass = entityClass;
            this.page = page;
            this.check = check;
        }
    }

    private Collection<Case> buildSelectCase() {
        return Arrays.asList(
            // Negative string, example -1.
            new Case(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        fixStringNumber,
                        ConditionOperator.EQUALS,
                        new StringValue(fixStringNumber, "-1")
                    )
                ),
                expectedEntitys.stream().findFirst().get().entityClass(),
                Page.newSinglePage(100),
                refs -> {

                    Assert.assertEquals(expectedEntitys.size(), refs.size());

                    return true;
                }
            )
            ,
            // =
            new Case(
                new Conditions(new Condition(
                    expectedEntitys.stream().skip(3)
                        .findFirst().get().entityValue().values().stream().findFirst().get().getField(),
                    ConditionOperator.EQUALS,
                    expectedEntitys.stream().skip(3)
                        .findFirst().get().entityValue().values().stream().findFirst().get()
                )),
                expectedEntitys.stream().findFirst().get().entityClass(),
                Page.newSinglePage(10),
                refs -> {
                    Assert.assertEquals(1, refs.size());
                    Assert.assertEquals(expectedEntitys.stream().skip(3).findFirst().get().id(),
                        refs.stream().findFirst().get().getId());
                    return true;
                }
            ),
            // !=
            new Case(
                new Conditions(new Condition(
                    expectedEntitys.stream().skip(1)
                        .findFirst().get().entityValue().values().stream().findFirst().get().getField(),
                    ConditionOperator.NOT_EQUALS,
                    expectedEntitys.stream().skip(1)
                        .findFirst().get().entityValue().values().stream().findFirst().get()
                )),
                expectedEntitys.stream().findFirst().get().entityClass(),
                Page.newSinglePage(100),
                refs -> {

                    Assert.assertEquals(expectedEntitys.size() - 1, refs.size());
                    List<IEntity> onlyOne = expectedEntitys.stream().filter(
                        e -> e.id() == refs.stream().findFirst().get().getId()
                    ).collect(Collectors.toList());

                    Assert.assertEquals(1, onlyOne.size());

                    return true;
                }
            ),
            // = !=
            new Case(
                new Conditions(new Condition(
                    expectedEntitys.stream().skip(1)
                        .findFirst().get().entityValue().values().stream().findFirst().get().getField(),
                    ConditionOperator.NOT_EQUALS,
                    expectedEntitys.stream().skip(1)
                        .findFirst().get().entityValue().values().stream().findFirst().get()
                )).addAnd(
                    new Condition(
                        expectedEntitys.stream().skip(2)
                            .findFirst().get().entityValue().values().stream().findFirst().get().getField(),
                        ConditionOperator.EQUALS,
                        expectedEntitys.stream().skip(2)
                            .findFirst().get().entityValue().values().stream().findFirst().get()
                    )
                ),
                expectedEntitys.stream().findFirst().get().entityClass(),
                Page.newSinglePage(100),
                refs -> {

                    Assert.assertEquals(1, refs.size());
                    List<IEntity> onlyOne = expectedEntitys.stream().filter(
                        e -> e.id() == refs.stream().findFirst().get().getId()
                    ).collect(Collectors.toList());

                    Assert.assertEquals(1, onlyOne.size());

                    return true;
                }
            ),
            // = page
            new Case(
                new Conditions(new Condition(
                    expectedEntitys.stream().skip(3)
                        .findFirst().get().entityValue().values().stream().findFirst().get().getField(),
                    ConditionOperator.EQUALS,
                    expectedEntitys.stream().skip(3)
                        .findFirst().get().entityValue().values().stream().findFirst().get()
                )),
                expectedEntitys.stream().findFirst().get().entityClass(),
                new Page(1, 100),
                refs -> {
                    Assert.assertEquals(1, refs.size());
                    Assert.assertEquals(expectedEntitys.stream().skip(3).findFirst().get().id(),
                        refs.stream().findFirst().get().getId());
                    return true;
                }
            ),
            // >=
            new Case(
                new Conditions(new Condition(
                    fixFieldRange,
                    ConditionOperator.GREATER_THAN,
                    expectedEntitys.stream().skip(1)
                        .findFirst().get().entityValue().getValue(fixFieldRange.name()).get()
                )).addAnd(
                    new Condition(
                        expectedEntitys.stream().skip(2)
                            .findFirst().get().entityValue().values().stream().findFirst().get().getField(),
                        ConditionOperator.EQUALS,
                        expectedEntitys.stream().skip(2)
                            .findFirst().get().entityValue().values().stream().findFirst().get()
                    )
                ),
                expectedEntitys.stream().findFirst().get().entityClass(),
                Page.newSinglePage(100),
                refs -> {

                    IValue<Long> oneCondition = expectedEntitys.stream().skip(1)
                        .findFirst().get().entityValue().getValue(fixFieldRange.name()).get();
                    IValue twoCondition = expectedEntitys.stream().skip(2)
                        .findFirst().get().entityValue().values().stream().findFirst().get();

                    int expectedSize = expectedEntitys.stream().filter(e -> {
                        IValue<Long> oneTarget = e.entityValue().getValue(fixFieldRange.name()).get();
                        IValue twoTarget = e.entityValue().values().stream().findFirst().get();


                        return oneTarget.valueToLong() > oneCondition.valueToLong() && twoTarget.equals(twoCondition);

                    }).collect(Collectors.toList()).size();
                    Assert.assertEquals(expectedSize, refs.size());
                    return true;
                }
            )
            ,
            // id in()
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            new Field(Long.MAX_VALUE, "id", FieldType.LONG, FieldConfig.build().identifie(true)),
                            ConditionOperator.MULTIPLE_EQUALS,
                            new LongValue(
                                new Field(
                                    Long.MAX_VALUE, "id", FieldType.LONG, FieldConfig.build().identifie(true)),
                                expectedEntitys.get(0).id()),
                            new LongValue(
                                new Field(
                                    Long.MAX_VALUE, "id", FieldType.LONG, FieldConfig.build().identifie(true)),
                                expectedEntitys.get(1).id())
                        )
                    ).addAnd(
                    new Condition(fixFieldAll, ConditionOperator.EQUALS, new BooleanValue(fixFieldAll, true))
                ),
                expectedEntitys.stream().findFirst().get().entityClass(),
                Page.newSinglePage(100),
                refs -> {

                    Assert.assertEquals(2, refs.size());
                    Assert.assertEquals(expectedEntitys.get(0).id(), refs.stream().findFirst().get().getId());
                    Assert.assertEquals(expectedEntitys.get(1).id(), refs.stream().skip(1).findFirst().get().getId());
                    return true;
                }
            )
            ,
            // attribute in()
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        fixFieldAll,
                        ConditionOperator.MULTIPLE_EQUALS,
                        new BooleanValue(fixFieldAll, true),
                        new BooleanValue(fixFieldAll, false)
                    )),
                expectedEntitys.get(0).entityClass(),
                Page.newSinglePage(100),
                refs -> {
                    Assert.assertEquals(expectedEntitys.size(), refs.size());
                    return true;
                }
            )
            ,
            // all
            new Case(
                Conditions.buildEmtpyConditions(),
                expectedEntitys.stream().findFirst().get().entityClass(),
                Page.newSinglePage(100),
                refs -> {
                    Assert.assertEquals(expectedEntitys.size(), refs.size());

                    refs.stream().forEach(r -> {
                        Assert.assertEquals(1,
                            expectedEntitys.stream().filter(e -> e.id() == r.getId()).collect(Collectors.toList()).size());
                    });

                    return true;
                }
            )
            ,
            // strings eq
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            fixStringsField,
                            ConditionOperator.EQUALS,
                            new StringsValue(fixStringsField, new String[]{"500002"}))
                    ),
                expectedEntitys.get(0).entityClass(),
                Page.newSinglePage(100),
                refs -> {
                    Assert.assertEquals(expectedEntitys.size(), refs.size());

                    return true;
                }
            )
            ,
            // strings not eq
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            fixStringsField,
                            ConditionOperator.NOT_EQUALS,
                            new StringsValue(fixStringsField, new String[] {"500002"}))
                    ),
                expectedEntitys.get(0).entityClass(),
                Page.newSinglePage(100),
                refs -> {
                    Assert.assertEquals(0, refs.size());

                    return true;
                }
            )
            ,
            // enum meq
            new Case(
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            fixStringsField,
                            ConditionOperator.MULTIPLE_EQUALS,
                            new StringsValue(fixStringsField, new String[] {"500002"} ),
                            new StringsValue(fixStringsField, new String[] {"1"}))
                    ),
                expectedEntitys.get(0).entityClass(),
                Page.newSinglePage(100),
                refs -> {
                    Assert.assertEquals(expectedEntitys.size(), refs.size());

                    return true;
                }
            )
        );
    }

    // 初始化数据
    private List<IEntity> initData(SphinxQLIndexStorage storage, int size) throws Exception {
        List<IEntity> expectedEntitys = new ArrayList<>(size);
        for (int i = 1; i <= size; i++) {
            expectedEntitys.add(buildEntity());
        }

        try {
            expectedEntitys.stream().forEach(e -> {
                try {
                    storage.build(e);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex.getMessage(), ex);
                }
            });
        } catch (Exception ex) {
            transactionManager.getCurrent().get().rollback();
            throw ex;
        }

        //将事务正常提交,并从事务管理器中销毁事务.
        Transaction tx = transactionManager.getCurrent().get();
        tx.commit();
        transactionManager.finish();

        return expectedEntitys;
    }

    private IEntity buildEntity() {
        Collection<IEntityField> fields = buildRandomFields(3);
        long id = idGenerator.next();
        return new Entity(
            id,
            new EntityClass(1, "test", fields),
            buildRandomValue(id, fields)
        );
    }

    private Collection<IEntityField> buildRandomFields(int size) {
        List<IEntityField> fields = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            long fieldId = idGenerator.next();
            fields.add(
                new Field(idGenerator.next(),
                    "c" + fieldId,
                    randomFieldType()
                )
            );
        }

        // 一个固定的所有都有的字段.
        fields.add(fixFieldAll);
        fields.add(fixFieldRange);
        fields.add(fixStringNumber);
        fields.add(fixStringsField);

        return fields;
    }

    private FieldType randomFieldType() {
        long n = buildRandomLong(0, 100);
        if (n > 30) {
            return FieldType.STRING;
        } else if (n > 60) {
            return FieldType.LONG;
        } else {
            return FieldType.DECIMAL;
        }
    }

    private IEntityValue buildRandomValue(long id, Collection<IEntityField> fields) {
        Collection<IValue> values = fields.stream().map(f -> {

            if (f == fixFieldAll) {
                return new BooleanValue(f, true);
            }

            if (f == fixFieldRange) {
                return new LongValue(f, (long) buildRandomLong(10, 100000));
            }

            if (f == fixStringNumber) {
                return new StringValue(f, "-1");
            }

            if (f == fixStringsField) {
                return new StringsValue(fixStringsField, "1,2,3,500002,测试".split(","));
            }

            switch (f.type()) {
                case STRING:
                    return new StringValue(f, buildRandomString(30));
                case DECIMAL:
                    return new DecimalValue(
                        f,
                        new BigDecimal(
                            Long.toString(buildRandomLong(0, 10000)) + "." + Long.toString(buildRandomLong(0, 10000))
                        )
                    );
                default:
                    return new LongValue(f, (long) buildRandomLong(10, 100000));
            }
        }).collect(Collectors.toList());

        EntityValue value = new EntityValue(id);
        value.addValues(values);
        return value;
    }

    private String buildRandomString(int size) {
        StringBuilder buff = new StringBuilder();
        Random rand = new Random(47);
        for (int i = 0; i < size; i++) {
            buff.append(rand.nextInt(26) + 'a');
        }
        // 加上特殊的字符.
        buff.append('\\').append('@').append('\'').append('-').append('\"').append('(').append(')');
        return buff.toString();
    }

    private int buildRandomLong(int min, int max) {
        Random random = new Random();

        return random.nextInt(max) % (max - min + 1) + min;
    }

    private Selector<DataSource> buildDataSourceSelector(String file) {
        System.setProperty(DataSourceFactory.CONFIG_FILE, file);

        dataSourcePackage = DataSourceFactory.build();

        return new TakeTurnsSelector<>(dataSourcePackage.getIndexWriter());

    }
}
