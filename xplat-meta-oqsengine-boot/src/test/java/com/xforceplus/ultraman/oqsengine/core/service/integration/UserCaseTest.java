package com.xforceplus.ultraman.oqsengine.core.service.integration;

import com.xforceplus.ultraman.oqsengine.boot.OqsengineBootApplication;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EnumValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.AbstractContainer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * @author dongbin
 * @version 0.1 2020/11/29 17:37
 * @since 1.8
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = OqsengineBootApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserCaseTest extends AbstractContainer {

    final Logger logger = LoggerFactory.getLogger(UserCaseTest.class);

    @Resource(name = "masterDataSource")
    private DataSource masterDataSource;

    @Resource(name = "indexWriteDataSourceSelector")
    private Selector<DataSource> indexWriteDataSourceSelector;

    @Resource
    private CommitIdStatusService commitIdStatusService;

    @Resource
    private EntitySearchService entitySearchService;

    @Resource
    private EntityManagementService entityManagementService;

    private static final int TEST_LOOPS = 10;

    IEntityClass fatherClass = new EntityClass(100, "father", Arrays.asList(
        new EntityField(123, "c1", FieldType.LONG, FieldConfig.build().searchable(true)),
        new EntityField(456, "c2", FieldType.STRING, FieldConfig.build().searchable(true))
    ));
    IEntityClass childClass = new EntityClass(200, "child", null, null, fatherClass, Arrays.asList(
        new EntityField(789, "c3", FieldType.ENUM, FieldConfig.build().searchable(true)),
        new EntityField(910, "c4", FieldType.BOOLEAN, FieldConfig.build().searchable(true))
    ));

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
        try (Connection conn = masterDataSource.getConnection()) {
            try (Statement stat = conn.createStatement()) {
                stat.executeUpdate("truncate table oqsbigentity");
            }
        }


        for (DataSource ds : indexWriteDataSourceSelector.selects()) {
            try (Connection conn = ds.getConnection()) {
                try (Statement stat = conn.createStatement()) {
                    stat.executeUpdate("truncate table oqsindex0");
                    stat.executeUpdate("truncate table oqsindex1");
                }
            }
        }
    }

    @Test
    public void testUpdateFatherSearchChild() throws Exception {
        IEntity childEntity = new Entity(0, childClass, new EntityValue(0)
            .addValue(new LongValue(fatherClass.field("c1").get(), 100000L))
            .addValue(new EnumValue(childClass.field("c3").get(), "0"))
        );
        childEntity = entityManagementService.build(childEntity);
        IEntity fatherEntity = entitySearchService.selectOne(childEntity.family().parent(), fatherClass).get();
        fatherEntity.entityValue().addValue(new StringValue(fatherClass.field("c2").get(), "newValue"));

        entityManagementService.replace(fatherEntity);

        Collection<IEntity> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions()
                .addAnd(new Condition(
                    fatherClass.field("c2").get(),
                    ConditionOperator.EQUALS,
                    new StringValue(fatherClass.field("c2").get(), "newValue")
                )),
            childClass,
            Page.newSinglePage(100)
        );

        Assert.assertEquals(1, entities.size());

    }

    /**
     * 更新后查询不等值总数匹配.
     *
     * @throws Exception
     */
    @Test
    public void testUpdateAfterNotEqCount() throws Exception {
        IEntity targetEntity = new Entity(0, childClass, new EntityValue(0)
            .addValue(new LongValue(fatherClass.field("c1").get(), 100000L))
            .addValue(new EnumValue(childClass.field("c3").get(), "0"))
        );
        entityManagementService.build(targetEntity);

        for (int i = 1; i <= TEST_LOOPS; i++) {
            targetEntity = entitySearchService.selectOne(targetEntity.id(), childClass).get();
            targetEntity.entityValue().addValue(
                new EnumValue(childClass.field("c3").get(), Long.toString(i))
            );
            ResultStatus status = entityManagementService.replace(targetEntity);
            Assert.assertTrue(ResultStatus.SUCCESS == status);

            Page page = Page.newSinglePage(100);
            Collection<IEntity> entities = entitySearchService.selectByConditions(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        childClass.field("c3").get(),
                        ConditionOperator.NOT_EQUALS,
                        new EnumValue(childClass.field("c3").get(), "0")
                    )
                ).addAnd(new Condition(
                    fatherClass.field("c1").get(),
                    ConditionOperator.EQUALS,
                    new LongValue(fatherClass.field("c1").get(), 100000L)
                )),
                childClass,
                page
            );

            Assert.assertEquals(1, page.getTotalCount());
        }

        TimeUnit.SECONDS.sleep(1);

        Assert.assertEquals(0, commitIdStatusService.size());

    }

    /**
     * 不断创建马上查询.
     */
    @Test
    public void testBuildAfterRead() throws Exception {
        IEntity newFatherEntity = new Entity(0, childClass, new EntityValue(0)
            .addValue(new LongValue(fatherClass.field("c1").get(), 100000L))
            .addValue(new EnumValue(childClass.field("c3").get(), "0"))
        );

        for (int i = 0; i < TEST_LOOPS; i++) {
            newFatherEntity = entityManagementService.build(newFatherEntity);
            IEntity selectEntity = entitySearchService.selectOne(newFatherEntity.id(), childClass).get();

            Assert.assertNotEquals(0, selectEntity.id());

            Assert.assertEquals(100000L, selectEntity.entityValue().getValue("c1").get().valueToLong());
            Assert.assertEquals("0", selectEntity.entityValue().getValue("c3").get().valueToString());
        }

        TimeUnit.SECONDS.sleep(1);

        Assert.assertEquals(0, commitIdStatusService.size());
    }

    @Test
    public void testBuildAfterDelete() throws Exception {
        IEntity childEntity = new Entity(0, childClass, new EntityValue(0)
            .addValue(new LongValue(fatherClass.field("c1").get(), 100000L))
            .addValue(new EnumValue(childClass.field("c3").get(), "0"))
        );
        for (int i = 0; i < TEST_LOOPS; i++) {
            childEntity = entityManagementService.build(childEntity);
            Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.deleteForce(childEntity));
        }

    }

    /**
     * 测试不断的更新已有数据,并立即查询后的结果.
     *
     * @throws Exception
     */
    @Test
    public void testUpdateAfterRead() throws Exception {
        IEntity newFatherEntity = new Entity(0, childClass, new EntityValue(0)
            .addValue(new LongValue(fatherClass.field("c1").get(), 100000L))
            .addValue(new EnumValue(childClass.field("c3").get(), "0"))
        );

        newFatherEntity = entityManagementService.build(newFatherEntity);
        Assert.assertTrue(newFatherEntity.id() != 0);
        for (int i = 0; i < TEST_LOOPS; i++) {
            newFatherEntity = entitySearchService.selectOne(newFatherEntity.id(), childClass).get();
            newFatherEntity.entityValue().addValue(
                new EnumValue(childClass.field("c3").get(), Long.toString(i))
            );
            ResultStatus status = entityManagementService.replace(newFatherEntity);
            Assert.assertTrue(ResultStatus.SUCCESS == status);

            Page page = Page.newSinglePage(100);
            Collection<IEntity> entities = entitySearchService.selectByConditions(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        childClass.field("c3").get(),
                        ConditionOperator.NOT_EQUALS,
                        new EnumValue(childClass.field("c3").get(), Long.toString(i))
                    )
                ),
                childClass,
                page
            );
            Assert.assertEquals(0, page.getTotalCount());

            Assert.assertEquals(0, entities.size());
        }

        TimeUnit.SECONDS.sleep(1);

        Assert.assertEquals(0, commitIdStatusService.size());
    }

    @Test
    public void testSort() throws Exception {
        IEntity e0 = new Entity(0, childClass, new EntityValue(0)
            .addValue(new LongValue(fatherClass.field("c1").get(), 100000L))
            .addValue(new EnumValue(childClass.field("c3").get(), "0"))
        );
        IEntity e1 = new Entity(0, childClass, new EntityValue(0)
            .addValue(new LongValue(fatherClass.field("c1").get(), 200000L))
            .addValue(new EnumValue(childClass.field("c3").get(), "0"))
        );
        IEntity e2 = new Entity(0, childClass, new EntityValue(0)
            .addValue(new LongValue(fatherClass.field("c1").get(), 300000L))
            .addValue(new EnumValue(childClass.field("c3").get(), "0"))
        );

        entityManagementService.build(e0);
        entityManagementService.build(e1);
        entityManagementService.build(e2);

        Collection<IEntity> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions().addAnd(
                new Condition(childClass.field("c3").get(), ConditionOperator.EQUALS,
                    new EnumValue(childClass.field("c3").get(), "0"))
            ),
            childClass, Sort.buildAscSort(fatherClass.field("c1").get()), Page.newSinglePage(100));

        Assert.assertEquals(3, entities.size());
        Assert.assertEquals(100000L,
            entities.stream().findFirst().get().entityValue().getValue("c1").get().valueToLong());
        Assert.assertEquals(200000L,
            entities.stream().skip(1).findFirst().get().entityValue().getValue("c1").get().valueToLong());
        Assert.assertEquals(300000L,
            entities.stream().skip(2).findFirst().get().entityValue().getValue("c1").get().valueToLong());
    }

    // 测试排序,但是记录中没有排序的值.应该使用默认值作为排序字段.
    @Test
    public void testSortButNoValue() throws Exception {
        Entity e0 = new Entity(0, childClass, new EntityValue(0)
            .addValue(new LongValue(fatherClass.field("c1").get(), 100000L))
            .addValue(new EnumValue(childClass.field("c3").get(), "0"))
        );
        IEntity e1 = new Entity(0, childClass, new EntityValue(0)
            .addValue(new LongValue(fatherClass.field("c1").get(), 200000L))
            .addValue(new EnumValue(childClass.field("c3").get(), "0"))
        );
        IEntity e2 = new Entity(0, childClass, new EntityValue(0)
            .addValue(new EnumValue(childClass.field("c3").get(), "0"))
        );

        entityManagementService.build(e0);
        entityManagementService.build(e1);
        entityManagementService.build(e2);

        Collection<IEntity> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions().addAnd(
                new Condition(childClass.field("c3").get(), ConditionOperator.EQUALS,
                    new EnumValue(childClass.field("c3").get(), "0"))
            ),
            childClass, Sort.buildAscSort(fatherClass.field("c1").get()), Page.newSinglePage(100));

        Assert.assertEquals(3, entities.size());

        Assert.assertFalse(entities.stream().findFirst().get().entityValue().getValue("c1").isPresent());

        Assert.assertEquals(100000L,
            entities.stream().skip(1).findFirst().get().entityValue().getValue("c1").get().valueToLong());
        Assert.assertEquals(200000L,
            entities.stream().skip(2).findFirst().get().entityValue().getValue("c1").get().valueToLong());
    }

    @Test
    public void testUpdateAfterCount() throws Exception {
        IEntity childEntity = new Entity(0, childClass, new EntityValue(0)
            .addValue(new LongValue(fatherClass.field("c1").get(), 200000L))
            .addValue(new EnumValue(childClass.field("c3").get(), "0"))
        );

        childEntity = entityManagementService.build(childEntity);
        for (int i = 0; i < TEST_LOOPS; i++) {
            childEntity = entitySearchService.selectOne(childEntity.id(), childClass).get();
            childEntity.entityValue().addValue(new LongValue(fatherClass.field("c1").get(), i));
            Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.replace(childEntity));

            Page page = new Page(1, 10);
            entitySearchService.selectByConditions(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        fatherClass.field("c1").get(),
                        ConditionOperator.EQUALS,
                        new LongValue(fatherClass.field("c1").get(), i))
                ),
                fatherClass,
                page
            );

            Assert.assertEquals(1, page.getPageCount());
        }
    }

    @Test
    public void testCount() throws Exception {
        for (int i = 0; i < 10; i++) {
            IEntity childEntity = new Entity(0, childClass, new EntityValue(0)
                .addValue(new LongValue(fatherClass.field("c1").get(), i))
                .addValue(new EnumValue(childClass.field("c3").get(), "0"))
            );

            entityManagementService.build(childEntity);
        }

        Page page = new Page(1, 1);
        Collection<IEntity> entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions().addAnd(
                new Condition(
                    fatherClass.field("c1").get(),
                    ConditionOperator.MULTIPLE_EQUALS,
                    new LongValue(fatherClass.field("c1").get(), 1),
                    new LongValue(fatherClass.field("c1").get(), 2),
                    new LongValue(fatherClass.field("c1").get(), 3),
                    new LongValue(fatherClass.field("c1").get(), 4),
                    new LongValue(fatherClass.field("c1").get(), 5),
                    new LongValue(fatherClass.field("c1").get(), 6),
                    new LongValue(fatherClass.field("c1").get(), 7),
                    new LongValue(fatherClass.field("c1").get(), 8),
                    new LongValue(fatherClass.field("c1").get(), 9)
                )
            ),
            fatherClass,
            page
        );

        Assert.assertEquals(1, entities.size());
        Assert.assertEquals(9, page.getTotalCount());

        page = new Page(1, 1);
        entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions().addAnd(
                new Condition(
                    fatherClass.field("c1").get(),
                    ConditionOperator.MULTIPLE_EQUALS,
                    new LongValue(fatherClass.field("c1").get(), 1),
                    new LongValue(fatherClass.field("c1").get(), 2),
                    new LongValue(fatherClass.field("c1").get(), 3),
                    new LongValue(fatherClass.field("c1").get(), 4),
                    new LongValue(fatherClass.field("c1").get(), 5),
                    new LongValue(fatherClass.field("c1").get(), 6),
                    new LongValue(fatherClass.field("c1").get(), 7),
                    new LongValue(fatherClass.field("c1").get(), 8),
                    new LongValue(fatherClass.field("c1").get(), 9)
                )
            ),
            fatherClass,
            page
        );
        Assert.assertEquals(1, entities.size());
        Assert.assertEquals(9, page.getTotalCount());

        page = new Page(1, 1);
        entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions().addAnd(
                new Condition(
                    fatherClass.field("c1").get(),
                    ConditionOperator.MULTIPLE_EQUALS,
                    new LongValue(fatherClass.field("c1").get(), 1),
                    new LongValue(fatherClass.field("c1").get(), 2),
                    new LongValue(fatherClass.field("c1").get(), 3),
                    new LongValue(fatherClass.field("c1").get(), 4),
                    new LongValue(fatherClass.field("c1").get(), 5),
                    new LongValue(fatherClass.field("c1").get(), 6),
                    new LongValue(fatherClass.field("c1").get(), 7),
                    new LongValue(fatherClass.field("c1").get(), 8),
                    new LongValue(fatherClass.field("c1").get(), 9)
                )
            ),
            fatherClass,
            page
        );
        Assert.assertEquals(1, entities.size());
        Assert.assertEquals(9, page.getTotalCount());

        page = new Page(1, 1);
        entities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions().addAnd(
                new Condition(
                    fatherClass.field("c1").get(),
                    ConditionOperator.MULTIPLE_EQUALS,
                    new LongValue(fatherClass.field("c1").get(), 1),
                    new LongValue(fatherClass.field("c1").get(), 2),
                    new LongValue(fatherClass.field("c1").get(), 3),
                    new LongValue(fatherClass.field("c1").get(), 4),
                    new LongValue(fatherClass.field("c1").get(), 5),
                    new LongValue(fatherClass.field("c1").get(), 6),
                    new LongValue(fatherClass.field("c1").get(), 7),
                    new LongValue(fatherClass.field("c1").get(), 8),
                    new LongValue(fatherClass.field("c1").get(), 9)
                )
            ),
            fatherClass,
            page
        );
        Assert.assertEquals(1, entities.size());
        Assert.assertEquals(9, page.getTotalCount());
    }
}
