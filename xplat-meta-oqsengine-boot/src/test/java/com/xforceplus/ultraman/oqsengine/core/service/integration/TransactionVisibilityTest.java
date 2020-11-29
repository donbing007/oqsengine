package com.xforceplus.ultraman.oqsengine.core.service.integration;

import com.xforceplus.ultraman.oqsengine.boot.OqsengineBootApplication;
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
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EnumValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
public class TransactionVisibilityTest extends AbstractContainerTest {

    @Resource(name = "masterDataSource")
    private DataSource masterDataSource;

    @Resource
    private CommitIdStatusService commitIdStatusService;

    @Resource
    private EntitySearchService entitySearchService;

    @Resource
    private EntityManagementService entityManagementService;

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
        Connection conn = masterDataSource.getConnection();
        Statement stat = conn.createStatement();
        stat.executeUpdate("truncate table oqsbigentity");
        stat.close();
        conn.close();
    }

    /**
     * 测试不断的更新已有数据,并立即查询后的结果.
     *
     * @throws Exception
     */
    @Test
    public void testBuildRead() throws Exception {
        IEntity newFatherEntity = new Entity(0, childClass, new EntityValue(0)
            .addValue(new LongValue(fatherClass.field("c1").get(), 100000L))
            .addValue(new EnumValue(childClass.field("c3").get(), "0"))
        );

        newFatherEntity = entityManagementService.build(newFatherEntity);
        Assert.assertTrue(newFatherEntity.id() != 0);
        for (int i = 0; i < 100; i++) {
            newFatherEntity.entityValue().addValue(
                new EnumValue(childClass.field("c3").get(), Long.toString(i))
            );
            ResultStatus status = entityManagementService.replace(newFatherEntity);
            Assert.assertTrue(ResultStatus.SUCCESS == status);

            Collection<IEntity> entities = entitySearchService.selectByConditions(
                Conditions.buildEmtpyConditions().addAnd(
                    new Condition(
                        childClass.field("c3").get(),
                        ConditionOperator.EQUALS,
                        new EnumValue(childClass.field("c3").get(), Long.toString(i))
                    )
                ),
                childClass,
                Page.newSinglePage(100)
            );
            newFatherEntity = entities.stream().findFirst().get();

            Assert.assertEquals(1, entities.size());
            Assert.assertEquals(Long.toString(i), entities.stream()
                .findFirst().get().entityValue().getValue("c3").get().valueToString());
        }

        TimeUnit.SECONDS.sleep(1);

        Assert.assertEquals(0, commitIdStatusService.size());
    }
}
