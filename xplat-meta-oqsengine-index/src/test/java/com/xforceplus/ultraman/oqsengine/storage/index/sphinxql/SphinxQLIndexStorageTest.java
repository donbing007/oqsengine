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
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoShardTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.optimizer.DefaultSphinxQLQueryOptimizer;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.selector.TakeTurnsSelector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
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

    @Before
    public void before() throws Exception {


        Selector<DataSource> dataSourceSelector = buildDataSourceSelector("./src/test/resources/sql_index_storage.conf");


        truncate();

        // 等待加载完毕
        TimeUnit.SECONDS.sleep(1L);

        TransactionExecutor executor = new AutoShardTransactionExecutor(transactionManager);

        storage = new SphinxQLIndexStorage();
        ReflectionTestUtils.setField(storage, "writerDataSourceSelector", dataSourceSelector);
        ReflectionTestUtils.setField(storage, "searchDataSourceSelector", dataSourceSelector);
        ReflectionTestUtils.setField(storage, "transactionExecutor", executor);
        ReflectionTestUtils.setField(storage, "queryOptimizer", new DefaultSphinxQLQueryOptimizer());
        storage.setIndexTableName("oqsindex");
        storage.init();


        transactionManager.create();

        expectedEntitys = initData(storage, 10);

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

            } catch(SQLException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }

        });
    }

    @After
    public void after() throws Exception {

        transactionManager.finish();

        dataSourcePackage.close();
    }

    @Test
    public void testCase() throws Exception {
        buildCase().stream().forEach(c -> {

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

    private Collection<Case> buildCase() {
        return Arrays.asList(
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
        return new Entity(
            idGenerator.next(),
            new EntityClass(1, "test", fields),
            buildRandomValue(1, fields)
        );
    }

    private Collection<IEntityField> buildRandomFields(int size) {
        List<IEntityField> fields = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            long fieldId = idGenerator.next();
            fields.add(new Field(idGenerator.next(), "c" + fieldId,
                ("c" + fieldId).hashCode() % 2 == 1 ? FieldType.LONG : FieldType.STRING));
        }

        return fields;
    }

    private IEntityValue buildRandomValue(long id, Collection<IEntityField> fields) {
        Collection<IValue> values = fields.stream().map(f -> {

            switch (f.type()) {
                case STRING:
                    return new StringValue(f, buildRandomString(30));
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
