package com.xforceplus.ultraman.oqsengine.storage.master.utils;

import com.github.javafaker.Faker;
import com.google.common.collect.Lists;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.SnowflakeLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MetaInitialization;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MockMetaManagerHolder;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.mock.MasterDBInitialization;
import com.xforceplus.ultraman.oqsengine.storage.pojo.EntityPackage;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.MysqlContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * EntityUpdateTimeRangeIteratorTest.
 *
 * @author dongbin
 * @version 0.1 2022/8/18 18:13
 * @since 1.8
 */
@ExtendWith({RedisContainer.class, MysqlContainer.class})
public class EntityUpdateTimeRangeIteratorTest {

    private IEntityField l0LongField = EntityField.Builder.anEntityField()
        .withId(1000)
        .withFieldType(FieldType.LONG)
        .withName("l0-long")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l0StringField = EntityField.Builder.anEntityField()
        .withId(1001)
        .withFieldType(FieldType.STRING)
        .withName("l0-string")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l0StringsField = EntityField.Builder.anEntityField()
        .withId(1003)
        .withFieldType(FieldType.STRINGS)
        .withName("l0-strings")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityClass entityClass = EntityClass.Builder.anEntityClass()
        .withId(1)
        .withLevel(0)
        .withCode("l0")
        .withField(l0LongField)
        .withField(l0StringField)
        .withField(l0StringsField)
        .build();

    private Date startDate;
    private Date endDate;

    private DataSource dataSource;
    private MetaManager metaManager;

    private MasterStorage masterStorage;

    private LongIdGenerator idGenerator = new SnowflakeLongIdGenerator(new StaticNodeIdGenerator(0));

    private Faker faker = new Faker(Locale.CHINA);


    /**
     * before.
     *
     * @throws Exception exception
     */
    @BeforeEach
    public void setUp() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, 0, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        startDate = calendar.getTime();

        calendar.set(2022, 11, 30, 0, 0, 0);
        endDate = calendar.getTime();

        dataSource = MasterDBInitialization.getInstance().getDataSource();
        MockMetaManagerHolder.initEntityClassBuilder(Lists.newArrayList(entityClass));
        metaManager = MetaInitialization.getInstance().getMetaManager();

        masterStorage = MasterDBInitialization.getInstance().getMasterStorage();
    }

    @AfterEach
    public void after() throws Exception {
        InitializationHelper.clearAll();
        InitializationHelper.destroy();
    }

    /**
     * 测试时间范围不正确的异常.
     */
    @Test
    public void testTimeRangeIncorrect() throws Exception {
        Assertions.assertThrows(RuntimeException.class, () -> {
            EntityUpdateTimeRangeIterator.Builder.anEntityIterator()
                .withStartTime(System.currentTimeMillis())
                .withEndTime(System.currentTimeMillis() - 1000)
                .withDataSource(dataSource)
                .withEntityClass(entityClass)
                .witherTableName("oqsbigentity")
                .withMetaManager(metaManager)
                .build();
        });
    }

    /**
     * 测试正确迭代对象.
     */
    @Test
    public void testIterator() throws Exception {
        int size = 3030;
        buildEntities(size);
        Set<Long> ids = new HashSet<>();
        try (EntityUpdateTimeRangeIterator iterator = EntityUpdateTimeRangeIterator.Builder.anEntityIterator()
            .withStartTime(startDate.getTime())
            .withEndTime(endDate.getTime())
            .withDataSource(dataSource)
            .withEntityClass(entityClass)
            .witherTableName("oqsbigentity")
            .withMetaManager(metaManager)
            .build()) {

            while (iterator.hasNext()) {
                ids.add(iterator.next().getId());
            }
        }

        Assertions.assertEquals(size, ids.size());
    }

    /**
     * 测试只有一个实例.
     */
    @Test
    public void testOneIterator() throws Exception {
        int size = 1;
        buildEntities(size);
        Set<Long> ids = new HashSet<>();
        try (EntityUpdateTimeRangeIterator iterator = EntityUpdateTimeRangeIterator.Builder.anEntityIterator()
            .withStartTime(startDate.getTime())
            .withEndTime(endDate.getTime())
            .withDataSource(dataSource)
            .withEntityClass(entityClass)
            .witherTableName("oqsbigentity")
            .withMetaManager(metaManager)
            .build()) {

            while (iterator.hasNext()) {
                ids.add(iterator.next().getId());
            }
        }

        Assertions.assertEquals(size, ids.size());
    }

    private void buildEntities(int size) throws Exception {
        EntityPackage entityPackage = new EntityPackage();
        for (int i = 0; i < size; i++) {
            entityPackage.put(buildIEntity(), entityClass);
        }

        masterStorage.build(entityPackage);
    }

    private IEntity buildIEntity() {
        return Entity.Builder.anEntity()
            .withId(idGenerator.next())
            .withVersion(0)
            .withMajor(0)
            .withMaintainid(0)
            .withEntityClassRef(entityClass.ref())
            .withTime(faker.date().between(startDate, endDate).getTime())
            .withValue(
                new LongValue(l0LongField, faker.random().nextLong())
            )
            .withValue(
                new StringValue(l0StringField, faker.animal().name())
            )
            .withValue(
                new StringsValue(l0StringsField, faker.color().name(), faker.currency().name())
            ).build();
    }
}