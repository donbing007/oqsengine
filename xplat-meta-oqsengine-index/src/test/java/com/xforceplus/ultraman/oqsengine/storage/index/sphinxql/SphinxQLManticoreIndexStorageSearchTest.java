package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql;

import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.define.OperationType;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.mock.IndexInitialization;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import com.xforceplus.ultraman.oqsengine.storage.pojo.search.SearchConfig;
import com.xforceplus.ultraman.oqsengine.testcontainer.basic.AbstractContainerExtends;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 搜索测试.
 *
 * @author dongbin
 * @version 0.1 2021/05/18 15:51
 * @since 1.8
 */
public class SphinxQLManticoreIndexStorageSearchTest extends AbstractContainerExtends {

    private IEntityField baseStringField = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE)
        .withFieldType(FieldType.STRING)
        .withName("base-string")
        .withConfig(FieldConfig.build().searchable(true).fuzzyType(FieldConfig.FuzzyType.SEGMENTATION)).build();
    private IEntityClass baseEntityClass = EntityClass.Builder.anEntityClass()
        .withId(Long.MAX_VALUE)
        .withLevel(0)
        .withCode("base")
        .withField(baseStringField)
        .build();

    // 第一个子类.
    private IEntityField firstNameField = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE - 1)
        .withFieldType(FieldType.STRING)
        .withName("name")
        .withConfig(
            FieldConfig.Builder.anFieldConfig()
            .withSearchable(true)
            .withCrossSearch(true)
            .withFuzzyType(FieldConfig.FuzzyType.SEGMENTATION)
            .build()
        )
        .build();
    private IEntityClass firstEntityClass = EntityClass.Builder.anEntityClass()
        .withId(Long.MAX_VALUE - 1)
        .withLevel(1)
        .withCode("first")
        .withField(firstNameField)
        .withFather(baseEntityClass)
        .build();

    // 第二个子类.
    private IEntityField secondNameField = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE - 2)
        .withFieldType(FieldType.STRING)
        .withName("name")
        .withConfig(
            FieldConfig.Builder.anFieldConfig()
                .withSearchable(true)
                .withCrossSearch(true)
                .withFuzzyType(FieldConfig.FuzzyType.WILDCARD)
                .build()
        ).build();
    private IEntityClass secondEntityClass = EntityClass.Builder.anEntityClass()
        .withId(Long.MAX_VALUE - 2)
        .withLevel(1)
        .withCode("second")
        .withField(secondNameField)
        .withFather(baseEntityClass)
        .build();

    private Collection<OriginalEntity> expectedDatas;

    @BeforeEach
    public void before() throws Exception {
        Thread.sleep(1_000);
        initData();
    }

    @AfterEach
    public void after() throws Exception {
        InitializationHelper.clearAll();
    }

    @Test
    public void testSearch() throws Exception {
        for (Case c : buildCase()) {
            Collection<EntityRef> refs = IndexInitialization.getInstance().getIndexStorage().search(
                c.config,
                c.entityClasses
            );

            long[] expectedIds = c.expectedSupplie.get();
            Arrays.sort(expectedIds);
            Assertions.assertEquals(
                expectedIds.length, refs.size(), String.format("%s check length failed.", c.description));
            for (EntityRef ref : refs) {
                Assertions
                    .assertTrue(Arrays.binarySearch(expectedIds, ref.getId()) >= 0,
                        String.format("%s validation failed to find expected %d.", c.description, ref.getId()));
            }
        }
    }

    private Collection<Case> buildCase() {
        return Arrays.asList(
            new Case(
                "精确搜索,第一个子类第二个子类.",
                SearchConfig.Builder.anSearchConfig()
                    .withPage(Page.newSinglePage(100))
                    .withFuzzyType(FieldConfig.FuzzyType.NOT)
                    .withCode(firstNameField.name())
                    .withValue("第一个类的数据").build(),
                new IEntityClass[] {
                    firstEntityClass, secondEntityClass
                },
                () -> {
                    long[] ids = new long[10];
                    for (int i = 0; i < ids.length; i++) {
                        ids[i] = Long.MAX_VALUE - i;
                    }
                    return ids;
                }
            ),
            new Case(
                "分词搜索,只能搜索出一半.",
                SearchConfig.Builder.anSearchConfig()
                    .withPage(Page.newSinglePage(100))
                    .withFuzzyType(FieldConfig.FuzzyType.WILDCARD)
                    .withCode(firstNameField.name())
                    .withValue("一个类").build(),
                new IEntityClass[] {
                    firstEntityClass, secondEntityClass
                },
                () -> {
                    long[] ids = new long[5];
                    for (int i = 0; i < ids.length; i++) {
                        ids[i] = Long.MAX_VALUE - (i + 5);
                    }
                    return ids;
                }
            ),
            new Case(
                "所有数据,不指定元信息.",
                SearchConfig.Builder.anSearchConfig()
                    .withPage(Page.newSinglePage(100))
                    .withFuzzyType(FieldConfig.FuzzyType.NOT)
                    .withCode(firstNameField.name())
                    .withValue("第一个类的数据").build(),
                new IEntityClass[0],
                () -> {
                    long[] ids = new long[10];
                    for (int i = 0; i < ids.length; i++) {
                        ids[i] = Long.MAX_VALUE - i;
                    }
                    return ids;
                }
            )
        );
    }

    static class Case {
        private String description;
        private SearchConfig config;
        private IEntityClass[] entityClasses;
        private Supplier<long[]> expectedSupplie;

        public Case(String description, SearchConfig config, IEntityClass[] entityClasses,
                    Supplier<long[]> expectedSupplie) {
            this.description = description;
            this.config = config;
            this.entityClasses = entityClasses;
            this.expectedSupplie = expectedSupplie;
        }
    }

    private void initData() throws Exception {
        final int size = 10;
        expectedDatas = new ArrayList<>();
        OriginalEntity oe;
        int index = 0;
        int max = index + size / 2;
        for (; index < max; index++) {
            oe = OriginalEntity.Builder.anOriginalEntity()
                .withId(Long.MAX_VALUE - index)
                .withCommitid(0)
                .withDeleted(false)
                .withAttribute(buildAttributeKey(firstNameField.id()), "第一个类的数据")
                .withVersion(0)
                .withCreateTime(System.currentTimeMillis())
                .withUpdateTime(System.currentTimeMillis())
                .withOp(OperationType.CREATE.getValue())
                .withEntityClass(firstEntityClass)
                .build();
            expectedDatas.add(oe);
        }

        max = index + size / 2;
        for (; index < max; index++) {
            oe = OriginalEntity.Builder.anOriginalEntity()
                .withId(Long.MAX_VALUE - index)
                .withCommitid(0)
                .withDeleted(false)
                .withAttribute(buildAttributeKey(secondEntityClass.id()), "第一个类的数据")
                .withVersion(0)
                .withCreateTime(System.currentTimeMillis())
                .withUpdateTime(System.currentTimeMillis())
                .withOp(OperationType.CREATE.getValue())
                .withEntityClass(secondEntityClass)
                .build();
            expectedDatas.add(oe);
        }

        IndexInitialization.getInstance().getIndexStorage().saveOrDeleteOriginalEntities(expectedDatas);
    }

    private String buildAttributeKey(long id) {
        return String.format("%dS", id);
    }

}
