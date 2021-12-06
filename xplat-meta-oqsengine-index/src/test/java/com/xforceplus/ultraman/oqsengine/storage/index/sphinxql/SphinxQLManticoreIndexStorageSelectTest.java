package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql;

import com.alibaba.fastjson.JSONArray;
import com.xforceplus.ultraman.oqsengine.common.map.MapUtils;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.AttachmentCondition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.define.OperationType;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.mock.IndexInitialization;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.ManticoreContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * 索引查询测试.
 *
 * @author dongbin
 * @version 0.1 2021/3/10 14:01
 * @since 1.8
 */
@ExtendWith({RedisContainer.class, ManticoreContainer.class})
public class SphinxQLManticoreIndexStorageSelectTest {

    /*
    使用的字段名和其id.
    l2-string 9223372036854775802
    l2-time 9223372036854775801
    l2-enum 9223372036854775800
    l2-dec 9223372036854775799
    l1-long 9223372036854775804
    l1-string 9223372036854775803
    l0-long 9223372036854775807
    l0-string 9223372036854775806
    l0-strings 9223372036854775805
     */

    //-------------level 0--------------------
    private IEntityField l0LongField = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE)
        .withFieldType(FieldType.LONG)
        .withName("l0-long")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l0StringField = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE - 1)
        .withFieldType(FieldType.STRING)
        .withName("l0-string")
        .withConfig(FieldConfig.build().searchable(true).fuzzyType(FieldConfig.FuzzyType.SEGMENTATION)).build();
    private IEntityField l0StringsField = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE - 2)
        .withFieldType(FieldType.STRINGS)
        .withName("l0-strings")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityClass l0EntityClass = EntityClass.Builder.anEntityClass()
        .withId(Long.MAX_VALUE)
        .withLevel(0)
        .withCode("l0")
        .withField(l0LongField)
        .withField(l0StringField)
        .withField(l0StringsField)
        .build();

    //-------------level 1--------------------
    private IEntityField l1LongField = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE - 3)
        .withFieldType(FieldType.LONG)
        .withName("l1-long")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l1StringField = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE - 4)
        .withFieldType(FieldType.STRING)
        .withName("l1-string")
        .withConfig(FieldConfig.Builder.anFieldConfig()
            .withSearchable(true)
            .withFuzzyType(FieldConfig.FuzzyType.WILDCARD)
            .withWildcardMinWidth(3).withWildcardMaxWidth(7).build()).build();
    private IEntityClass l1EntityClass = EntityClass.Builder.anEntityClass()
        .withId(Long.MAX_VALUE - 1)
        .withLevel(1)
        .withCode("l1")
        .withField(l1LongField)
        .withField(l1StringField)
        .withFather(l0EntityClass)
        .build();

    //-------------level 2--------------------
    private IEntityField l2StringField = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE - 5)
        .withFieldType(FieldType.STRING)
        .withName("l2-string")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l2TimeField = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE - 6)
        .withFieldType(FieldType.DATETIME)
        .withName("l2-time")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l2EnumField = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE - 7)
        .withFieldType(FieldType.ENUM)
        .withName("l2-enum")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l2DecField = EntityField.Builder.anEntityField()
        .withId(Long.MAX_VALUE - 8)
        .withFieldType(FieldType.DECIMAL)
        .withName("l2-dec")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityClass l2EntityClass = EntityClass.Builder.anEntityClass()
        .withId(Long.MAX_VALUE - 2)
        .withLevel(2)
        .withCode("l2")
        .withField(l2StringField)
        .withField(l2TimeField)
        .withField(l2EnumField)
        .withField(l2DecField)
        .withFather(l1EntityClass)
        .build();

    /**
     * 初始化.
     */
    @BeforeEach
    public void before() throws Exception {

        // 等待加载完毕
        TimeUnit.SECONDS.sleep(1L);

        initData();
    }

    @AfterEach
    public void after() throws Exception {
        InitializationHelper.clearAll();
        InitializationHelper.destroy();
    }

    /**
     * 测试多级排序.
     */
    @Test
    public void testSort() throws Exception {
        // 预期的结果.
        List<EntityRef> expectRefs = Arrays.asList(
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775807L).withOrderValue("100").withSecondOrderValue("-2388650104").build(),
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775799L).withOrderValue("113").withSecondOrderValue("1046465945666").build(),
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775800L).withOrderValue("117").withSecondOrderValue("163808529746").build(),
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775804L).withOrderValue("119").withSecondOrderValue("517935305040").build(),
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775803L).withOrderValue("121").withSecondOrderValue("-121983894946").build(),
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775801L).withOrderValue("153").withSecondOrderValue("998526632168").build(),
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775805L).withOrderValue("162").withSecondOrderValue("-44449910669").build(),
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775806L).withOrderValue("162").withSecondOrderValue("975409638673").build(),
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775798L).withOrderValue("190").withSecondOrderValue("-196941314110").build(),
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775802L).withOrderValue("193").withSecondOrderValue("761631653530").build()
        );
        SelectConfig config = SelectConfig.Builder.anSelectConfig()
            .withSort(Sort.buildAscSort(l2EntityClass.field("l1-long").get()))
            .withSecondarySort(Sort.buildAscSort(l2EntityClass.field("l2-time").get()))
            .withPage(Page.newSinglePage(100)).build();

        SphinxQLManticoreIndexStorage storage = IndexInitialization.getInstance().getIndexStorage();
        List<EntityRef> refs = new ArrayList<>(storage.select(
            Conditions.buildEmtpyConditions(),
            l2EntityClass,
            config
        ));

        Assertions.assertEquals(expectRefs.size(), refs.size());

        for (int i = 0; i < refs.size(); i++) {
            EntityRef ref = refs.get(i);
            EntityRef expectedRef = expectRefs.get(i);
            Assertions.assertEquals(expectedRef.getId(), ref.getId());
            Assertions.assertEquals(expectedRef.getOrderValue(), ref.getOrderValue());
            Assertions.assertEquals(expectedRef.getSecondOrderValue(), ref.getSecondOrderValue());
        }


        expectRefs = Arrays.asList(
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775807L).withOrderValue("100").build(),
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775799L).withOrderValue("113").build(),
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775800L).withOrderValue("117").build(),
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775804L).withOrderValue("119").build(),
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775803L).withOrderValue("121").build(),
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775801L).withOrderValue("153").build(),
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775805L).withOrderValue("162").build(),
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775806L).withOrderValue("162").build(),
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775798L).withOrderValue("190").build(),
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775802L).withOrderValue("193").build()
        );
        config = SelectConfig.Builder.anSelectConfig()
            .withSort(Sort.buildAscSort(l2EntityClass.field("l1-long").get()))
            .withPage(Page.newSinglePage(100)).build();

        refs = new ArrayList<>(storage.select(
            Conditions.buildEmtpyConditions(),
            l2EntityClass,
            config
        ));

        Assertions.assertEquals(expectRefs.size(), refs.size());

        for (int i = 0; i < refs.size(); i++) {
            EntityRef ref = refs.get(i);
            EntityRef expectedRef = expectRefs.get(i);
            Assertions.assertEquals(expectedRef.getId(), ref.getId());
            Assertions.assertEquals(expectedRef.getOrderValue(), ref.getOrderValue());
        }

        // 单个浮点字段.
        expectRefs = Arrays.asList(
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775801L).withOrderValue("5910.412727589000000000").build(),
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775798L).withOrderValue("13354.099203446200000000").build(),
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775807L).withOrderValue("16507.168923797500000000").build(),
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775804L).withOrderValue("25155.917007414000000000").build(),
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775800L).withOrderValue("30472.189513653100000000").build(),
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775806L).withOrderValue("37574.332230891300000000").build(),
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775802L).withOrderValue("41077.388130479000000000").build(),
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775803L).withOrderValue("56388.810397345000000000").build(),
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775799L).withOrderValue("61894.976727655200000000").build(),
            EntityRef.Builder.anEntityRef()
                .withId(9223372036854775805L).withOrderValue("84730.454610377100000000").build()
        );
        config = SelectConfig.Builder.anSelectConfig()
            .withSort(Sort.buildAscSort(l2EntityClass.field("l2-dec").get()))
            .withPage(Page.newSinglePage(100)).build();

        refs = new ArrayList<>(storage.select(
            Conditions.buildEmtpyConditions(),
            l2EntityClass,
            config
        ));

        Assertions.assertEquals(expectRefs.size(), refs.size());

        for (int i = 0; i < refs.size(); i++) {
            EntityRef ref = refs.get(i);
            EntityRef expectedRef = expectRefs.get(i);
            Assertions.assertEquals(expectedRef.getId(), ref.getId());
            Assertions.assertEquals(expectedRef.getOrderValue(), ref.getOrderValue());
        }
    }

    /**
     * 测试如果使用Page.emptyPage()实例做为分页,那么page的数据总量还是会被填充的.
     */
    @Test
    public void testEmptyPageCount() throws Exception {
        Page page = Page.emptyPage();
        IndexInitialization.getInstance().getIndexStorage().select(
            Conditions.buildEmtpyConditions(),
            l2EntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withPage(page)
                .withCommitId(0)
                .withSort(Sort.buildAscSort(l2EntityClass.field("l1-long").get()))
                .build()
        );
        Assertions.assertEquals(page.getTotalCount(), expectedDatas.size());
    }

    @Test
    public void testNegativeNumber() throws Exception {
        OriginalEntity entity = OriginalEntity.Builder.anOriginalEntity()
            .withId(123L)
            .withEntityClass(l2EntityClass)
            .withCreateTime(System.currentTimeMillis())
            .withUpdateTime(System.currentTimeMillis())
            .withDeleted(false)
            .withOp(OperationType.CREATE.getValue())
            .withAttributes(
                MapUtils.asMap(
                    l2TimeField.id() + "L",
                    LocalDateTime.of(1970, 1, 1, 0, 0, 0)
                        .atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli()
                )
            ).build();

        IndexInitialization.getInstance().getIndexStorage().saveOrDeleteOriginalEntities(Arrays.asList(entity));


        Collection<EntityRef> refs = IndexInitialization.getInstance().getIndexStorage().select(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        l2TimeField,
                        ConditionOperator.EQUALS,
                        new DateTimeValue(l2TimeField, LocalDateTime.of(1970, 1, 1, 0, 0, 0))
                    )
                ),
            l2EntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withPage(Page.newSinglePage(100))
                .withSort(Sort.buildOutOfSort())
                .build()
        );

        Assertions.assertEquals(1, refs.size());
        Assertions.assertEquals(123, refs.stream().findFirst().get().getId());
    }


    /**
     * 测试模糊和精确是否会互相影响.
     * 预期两者不会相互影响.分词字段对于除like以外都应该是不可见的.
     */
    @Test
    public void testFuzzyAndEqInfluence() throws Exception {
        List<OriginalEntity> entities = new ArrayList(3);
        long baseId = 1000;
        for (int i = 0; i < 3; i++) {

            entities.add(OriginalEntity.Builder.anOriginalEntity()
                .withId(baseId++)
                .withEntityClass(l2EntityClass)
                .withCreateTime(System.currentTimeMillis())
                .withUpdateTime(System.currentTimeMillis())
                .withDeleted(false)
                .withOp(OperationType.CREATE.getValue())
                .withAttributes(
                    MapUtils.asMap(
                        l1StringField.id() + "S", "scan" + i
                    )
                ).build());
        }

        OriginalEntity scanEntity = OriginalEntity.Builder.anOriginalEntity()
            .withId(baseId++)
            .withEntityClass(l2EntityClass)
            .withCreateTime(System.currentTimeMillis())
            .withUpdateTime(System.currentTimeMillis())
            .withDeleted(false)
            .withOp(OperationType.CREATE.getValue())
            .withAttributes(
                MapUtils.asMap(
                    l1StringField.id() + "S", "scan"
                )
            ).build();
        entities.add(scanEntity);

        IndexInitialization.getInstance().getIndexStorage().saveOrDeleteOriginalEntities(entities);

        Collection<EntityRef> refs = IndexInitialization.getInstance().getIndexStorage().select(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        l1StringField,
                        ConditionOperator.EQUALS,
                        new StringValue(l1StringField, "scan")
                    )
                ),
            l2EntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withPage(Page.newSinglePage(100)).build()
        );

        Assertions.assertEquals(1, refs.size());
        Assertions.assertEquals(scanEntity.getId(), refs.stream().findFirst().get().getId());

        // 查询所有包含scan的,应该包含所有新加入的记录.
        refs = IndexInitialization.getInstance().getIndexStorage().select(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        l1StringField,
                        ConditionOperator.LIKE,
                        new StringValue(l1StringField, "scan")
                    )
                ),
            l2EntityClass,
            SelectConfig.Builder.anSelectConfig()
                .withPage(Page.newSinglePage(100))
                .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                .build()
        );

        Assertions.assertEquals(4, refs.size());
        List<Long> expecteIds = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            expecteIds.add((long) (1000 + i));
        }

        for (EntityRef ref : refs) {
            expecteIds.remove(ref.getId());
        }
        Assertions.assertEquals(0, expecteIds.size());
    }

    @Test
    public void testSelect() throws Exception {
        Collection<EntityRef> refs = null;
        SphinxQLManticoreIndexStorage storage = IndexInitialization.getInstance().getIndexStorage();
        for (Case c : buildSelectCases()) {
            try {
                refs = storage.select(c.conditions, c.entityClass, c.selectConfig);
            } catch (Exception ex) {
                String msg = String.format("%s faild, message is %s.", c.description, ex.getMessage());
                throw new Exception(msg, ex);
            }

            long[] expectedIds = c.expectedIds;
            if (expectedIds == null) {
                expectedIds = expectedDatas.stream().mapToLong(oe -> oe.getId()).toArray();
            }
            Arrays.sort(expectedIds);
            Assertions.assertEquals(
                expectedIds.length, refs.size(), String.format("%s check length failed.", c.description));
            for (EntityRef ref : refs) {
                Assertions
                    .assertTrue(Arrays.binarySearch(expectedIds, ref.getId()) >= 0,
                        String.format("%s validation failed to find expected %d.", c.description, ref.getId()));
            }

            if (c.otherCheck != null) {
                String r = (String) c.otherCheck.apply(new SelectResult(refs, c.selectConfig.getPage()));
                if (r != null && r.length() > 0) {
                    Assertions.fail(String.format("%s validation failed due to %s.", c.description, r));
                }
            }
        }
    }

    private Collection<Case> buildSelectCases() {
        return Arrays.asList(
            new Case(
                "attahcment - (l1-long) 100",
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new AttachmentCondition(
                            l2EntityClass.field("l1-long").get(),
                            true,
                            "100"
                        )
                    ),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(100)).build(),
                new long[] {
                    9223372036854775807L
                }
            ),
            new Case(
                "attahcment - (l0-string) 方致远",
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new AttachmentCondition(
                            l2EntityClass.field("l0-string").get(),
                            true,
                            "方致远"
                        )
                    ),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(100)).build(),
                new long[] {
                    9223372036854775798L
                }
            ),
            new Case(
                "all",
                Conditions.buildEmtpyConditions(),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(20)).build(),
                new long[] {
                    Long.MAX_VALUE, Long.MAX_VALUE - 1, Long.MAX_VALUE - 2, Long.MAX_VALUE - 3, Long.MAX_VALUE - 4,
                    Long.MAX_VALUE - 5, Long.MAX_VALUE - 6, Long.MAX_VALUE - 7, Long.MAX_VALUE - 8, Long.MAX_VALUE - 9
                }
            ),
            new Case(
                "string .- symbol",
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            l2EntityClass.field("l1-string").get(),
                            ConditionOperator.EQUALS,
                            new StringValue(l2EntityClass.field("l1-string").get(), "15796500901.-12")
                        )
                    ),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig().build(),
                new long[] {
                    9223372036854775798L
                }
            ),
            new Case(
                "id in (not exist)",
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            EntityField.ID_ENTITY_FIELD,
                            ConditionOperator.MULTIPLE_EQUALS,
                            new LongValue(EntityField.ID_ENTITY_FIELD, 10000000L)
                        )
                    ),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildAscSort(l2EntityClass.field("l1-long").get()))
                    .build(),
                new long[0],
                r -> {

                    if (!r.refs.isEmpty()) {
                        return String.format("No data is expected, but there is %d data.", r.refs.size());
                    }

                    return null;
                }
            ),
            new Case(
                "sort with value",
                Conditions.buildEmtpyConditions(),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildAscSort(l2EntityClass.field("l1-long").get()))
                    .build(),
                null,
                r -> {
                    for (EntityRef ref : r.refs) {
                        if (ref.getOrderValue() == null) {
                            return "The sort is expected to return the value of the sort, but it does not.";
                        }
                    }
                    return null;
                }
            ),
            new Case(
                "empty",
                Conditions.buildEmtpyConditions(),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.emptyPage())
                    .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[0],
                r -> {
                    if (!r.refs.isEmpty()) {
                        return String.format("No data is expected, but there is %d data.", r.refs.size());
                    }

                    if (r.page.getTotalCount() != expectedDatas.size()) {
                        return String.format("The expected data total is %d, but it is actually up %d.",
                            expectedDatas.size(), r.page.getTotalCount());
                    }

                    return null;
                }
            ),
            new Case(
                "order by id asc",
                Conditions.buildEmtpyConditions(),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                null,
                r -> isSorted(r.refs, false) ? null : "Not expecting descending order"
            ),
            new Case(
                "order by id desc",
                Conditions.buildEmtpyConditions(),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                null,
                r -> isSorted(r.refs, true) ? null : "Not expecting descending order"
            ),
            new Case(
                "first page",
                Conditions.buildEmtpyConditions(),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(new Page(1, 5))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[] {
                    Long.MAX_VALUE, Long.MAX_VALUE - 1, Long.MAX_VALUE - 2, Long.MAX_VALUE - 3, Long.MAX_VALUE - 4
                }
            ),
            new Case(
                "last page",
                Conditions.buildEmtpyConditions(),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(new Page(2, 5))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                null
            ),
            new Case(
                "long eq",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l0-long").get(),
                        ConditionOperator.EQUALS,
                        new LongValue(l2EntityClass.field("l0-long").get(), 5932795534250140672L)
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[] {
                    Long.MAX_VALUE - 4
                }
            ),
            new Case(
                "long not equals",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l0-long").get(),
                        ConditionOperator.NOT_EQUALS,
                        new LongValue(l2EntityClass.field("l0-long").get(), 5932795534250140672L)
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[] {
                    Long.MAX_VALUE, Long.MAX_VALUE - 1, Long.MAX_VALUE - 2, Long.MAX_VALUE - 3,
                    Long.MAX_VALUE - 5, Long.MAX_VALUE - 6, Long.MAX_VALUE - 7, Long.MAX_VALUE - 8,
                    Long.MAX_VALUE - 9
                }
            ),
            new Case(
                "long >",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l0-long").get(),
                        ConditionOperator.GREATER_THAN,
                        new LongValue(l2EntityClass.field("l0-long").get(), 3065636258020209152L)
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[] {
                    Long.MAX_VALUE, Long.MAX_VALUE - 1, Long.MAX_VALUE - 2, Long.MAX_VALUE - 4,
                    Long.MAX_VALUE - 5, Long.MAX_VALUE - 6, Long.MAX_VALUE - 8, Long.MAX_VALUE - 9
                }
            ),
            new Case(
                "long <",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l0-long").get(),
                        ConditionOperator.LESS_THAN,
                        new LongValue(l2EntityClass.field("l0-long").get(), 3065636258020209152L)
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[] {
                    Long.MAX_VALUE - 3,
                }
            ),
            new Case(
                "long >=",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l0-long").get(),
                        ConditionOperator.GREATER_THAN_EQUALS,
                        new LongValue(l2EntityClass.field("l0-long").get(), 3065636258020209152L)
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[] {
                    Long.MAX_VALUE, Long.MAX_VALUE - 1, Long.MAX_VALUE - 2, Long.MAX_VALUE - 4,
                    Long.MAX_VALUE - 5, Long.MAX_VALUE - 6, Long.MAX_VALUE - 7, Long.MAX_VALUE - 8,
                    Long.MAX_VALUE - 9
                }
            ),
            new Case(
                "long <=",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l0-long").get(),
                        ConditionOperator.LESS_THAN_EQUALS,
                        new LongValue(l2EntityClass.field("l0-long").get(), 3065636258020209152L)
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[] {
                    Long.MAX_VALUE - 3, Long.MAX_VALUE - 7,
                }
            ),
            new Case(
                "long in",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l0-long").get(),
                        ConditionOperator.MULTIPLE_EQUALS,
                        new LongValue(l2EntityClass.field("l0-long").get(), 3065636258020209152L),
                        new LongValue(l2EntityClass.field("l0-long").get(), 8044778060371018752L)
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[] {
                    Long.MAX_VALUE, Long.MAX_VALUE - 7,
                }
            ),
            new Case(
                "String eq",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l1-string").get(),
                        ConditionOperator.EQUALS,
                        new StringValue(l2EntityClass.field("l1-string").get(), "15841884807")
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[] {
                    Long.MAX_VALUE - 1
                }
            ),
            new Case(
                "String no eq",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l1-string").get(),
                        ConditionOperator.NOT_EQUALS,
                        new StringValue(l2EntityClass.field("l1-string").get(), "15841884807")
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[] {
                    Long.MAX_VALUE, Long.MAX_VALUE - 2, Long.MAX_VALUE - 4, Long.MAX_VALUE - 9,
                    Long.MAX_VALUE - 5, Long.MAX_VALUE - 6, Long.MAX_VALUE - 7, Long.MAX_VALUE - 8,
                    Long.MAX_VALUE - 3
                }
            ),
            new Case(
                "String like 熊鹤轩 (熊鹤轩)",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l0-string").get(),
                        ConditionOperator.LIKE,
                        new StringValue(l2EntityClass.field("l0-string").get(), "熊鹤轩")
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[] {
                    Long.MAX_VALUE - 2
                }
            ),
            new Case(
                "String like 18159301250 (301)",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l1-string").get(),
                        ConditionOperator.LIKE,
                        new StringValue(l2EntityClass.field("l1-string").get(), "301")
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[] {
                    Long.MAX_VALUE - 2
                }
            ),
            new Case(
                "String like 18159301250 (3012)",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l1-string").get(),
                        ConditionOperator.LIKE,
                        new StringValue(l2EntityClass.field("l1-string").get(), "3012")
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[] {
                    Long.MAX_VALUE - 2
                }
            ),
            new Case(
                "String like 18159301250 (30125)",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l1-string").get(),
                        ConditionOperator.LIKE,
                        new StringValue(l2EntityClass.field("l1-string").get(), "30125")
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[] {
                    Long.MAX_VALUE - 2
                }
            ),
            new Case(
                "String like 18159301250 (301250)",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l1-string").get(),
                        ConditionOperator.LIKE,
                        new StringValue(l2EntityClass.field("l1-string").get(), "301250")
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[] {
                    Long.MAX_VALUE - 2
                }
            ),
            new Case(
                "String like 18159301250 (9301250)",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l1-string").get(),
                        ConditionOperator.LIKE,
                        new StringValue(l2EntityClass.field("l1-string").get(), "9301250")
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[] {
                    Long.MAX_VALUE - 2
                }
            ),
            new Case(
                "decimal eq",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l2-dec").get(),
                        ConditionOperator.EQUALS,
                        new DecimalValue(l2EntityClass.field("l2-dec").get(), new BigDecimal("13354.0992034462"))
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[] {
                    Long.MAX_VALUE - 9
                }
            ),
            new Case(
                "decimal not eq",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l2-dec").get(),
                        ConditionOperator.NOT_EQUALS,
                        new DecimalValue(l2EntityClass.field("l2-dec").get(), new BigDecimal("13354.0992034462"))
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[] {
                    Long.MAX_VALUE, Long.MAX_VALUE - 1, Long.MAX_VALUE - 2, Long.MAX_VALUE - 3,
                    Long.MAX_VALUE - 4, Long.MAX_VALUE - 5, Long.MAX_VALUE - 6, Long.MAX_VALUE - 7,
                    Long.MAX_VALUE - 8
                }
            ),
            new Case(
                "decimal >",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l2-dec").get(),
                        ConditionOperator.GREATER_THAN,
                        new DecimalValue(l2EntityClass.field("l2-dec").get(), new BigDecimal("13354.0992034462"))
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[] {
                    Long.MAX_VALUE, Long.MAX_VALUE - 1, Long.MAX_VALUE - 2, Long.MAX_VALUE - 3,
                    Long.MAX_VALUE - 4, Long.MAX_VALUE - 5, Long.MAX_VALUE - 7, Long.MAX_VALUE - 8
                }
            ),
            new Case(
                "decimal between",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l2-dec").get(),
                        ConditionOperator.GREATER_THAN,
                        new DecimalValue(l2EntityClass.field("l2-dec").get(), new BigDecimal("13354.0992034462"))
                    ))
                    .addAnd(new Condition(
                        l2EntityClass.field("l2-dec").get(),
                        ConditionOperator.LESS_THAN,
                        new DecimalValue(l2EntityClass.field("l2-dec").get(), new BigDecimal("61894.0"))
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[] {
                    Long.MAX_VALUE, Long.MAX_VALUE - 1, Long.MAX_VALUE - 3,
                    Long.MAX_VALUE - 4, Long.MAX_VALUE - 5, Long.MAX_VALUE - 7
                }
            ),
            new Case(
                "strings eq",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l0-strings").get(),
                        ConditionOperator.EQUALS,
                        new StringsValue(l2EntityClass.field("l0-strings").get(), "fuchsia")
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[] {
                    Long.MAX_VALUE - 3
                }
            ),
            new Case(
                "strings in",
                Conditions.buildEmtpyConditions()
                    .addAnd(new Condition(
                        l2EntityClass.field("l0-strings").get(),
                        ConditionOperator.MULTIPLE_EQUALS,
                        new StringsValue(l2EntityClass.field("l0-strings").get(), "teal"),
                        new StringsValue(l2EntityClass.field("l0-strings").get(), "fuchsia")
                    )),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[] {
                    Long.MAX_VALUE, Long.MAX_VALUE - 3
                }
            ),
            new Case(
                "update time desc",
                Conditions.buildEmtpyConditions(),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.UPDATE_TIME_FILED))
                    .build(),
                new long[] {
                    Long.MAX_VALUE, Long.MAX_VALUE - 1, Long.MAX_VALUE - 2, Long.MAX_VALUE - 3,
                    Long.MAX_VALUE - 4, Long.MAX_VALUE - 5, Long.MAX_VALUE - 6, Long.MAX_VALUE - 7,
                    Long.MAX_VALUE - 8, Long.MAX_VALUE - 9
                }
            ),
            new Case(
                "l0-long or l0-long",
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            l2EntityClass.field("l0-long").get(),
                            ConditionOperator.EQUALS,
                            new LongValue(l2EntityClass.field("l0-long").get(), 3065636258020209152L)
                        )
                    )
                    .addOr(
                        new Condition(
                            l2EntityClass.field("l0-long").get(),
                            ConditionOperator.EQUALS,
                            new LongValue(l2EntityClass.field("l0-long").get(), 8044778060371018752L)
                        )
                    ),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[] {
                    Long.MAX_VALUE, Long.MAX_VALUE - 7,
                }
            ),
            new Case(
                "l0-long eq or l0-long no eq",
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            l2EntityClass.field("l0-long").get(),
                            ConditionOperator.EQUALS,
                            new LongValue(l2EntityClass.field("l0-long").get(), 3065636258020209152L)
                        )
                    )
                    .addOr(
                        new Condition(
                            l2EntityClass.field("l0-long").get(),
                            ConditionOperator.NOT_EQUALS,
                            new LongValue(l2EntityClass.field("l0-long").get(), 8044778060371018752L)
                        )
                    ),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[] {
                    Long.MAX_VALUE - 1, Long.MAX_VALUE - 2, Long.MAX_VALUE - 3, Long.MAX_VALUE - 4,
                    Long.MAX_VALUE - 5, Long.MAX_VALUE - 6, Long.MAX_VALUE - 7, Long.MAX_VALUE - 8,
                    Long.MAX_VALUE - 9
                }
            ),
            new Case(
                "l0-long > or l0-long =",
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            l2EntityClass.field("l0-long").get(),
                            ConditionOperator.GREATER_THAN,
                            new LongValue(l2EntityClass.field("l0-long").get(), 5120579758453153792L)
                        )
                    )
                    .addOr(
                        new Condition(
                            l2EntityClass.field("l0-long").get(),
                            ConditionOperator.EQUALS,
                            new LongValue(l2EntityClass.field("l0-long").get(), 2305210501598537472L)
                        )
                    ),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .build(),
                new long[] {
                    Long.MAX_VALUE - 3, Long.MAX_VALUE - 4, Long.MAX_VALUE - 6, Long.MAX_VALUE - 8, Long.MAX_VALUE - 9,
                    Long.MAX_VALUE - 5, Long.MAX_VALUE - 2, Long.MAX_VALUE
                }
            ),
            new Case(
                "l0-long > or l0-long = (filter l0-long !=)",
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            l2EntityClass.field("l0-long").get(),
                            ConditionOperator.GREATER_THAN,
                            new LongValue(l2EntityClass.field("l0-long").get(), 5120579758453153792L)
                        )
                    )
                    .addOr(
                        new Condition(
                            l2EntityClass.field("l0-long").get(),
                            ConditionOperator.EQUALS,
                            new LongValue(l2EntityClass.field("l0-long").get(), 2305210501598537472L)
                        )
                    ),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .withDataAccessFitlerCondtitons(
                        Conditions.buildEmtpyConditions()
                            .addAnd(
                                new Condition(
                                    l2EntityClass.field("l0-long").get(),
                                    ConditionOperator.NOT_EQUALS,
                                    new LongValue(l2EntityClass.field("l0-long").get(), 8044778060371018752L)
                                )
                            )
                    )
                    .build(),
                new long[] {
                    Long.MAX_VALUE - 3, Long.MAX_VALUE - 4, Long.MAX_VALUE - 6, Long.MAX_VALUE - 8, Long.MAX_VALUE - 9,
                    Long.MAX_VALUE - 5, Long.MAX_VALUE - 2
                }
            ),
            new Case(
                "l0-long = and l0-long = (filter l0-long !=)",
                Conditions.buildEmtpyConditions()
                    .addAnd(
                        new Condition(
                            l2EntityClass.field("l0-long").get(),
                            ConditionOperator.EQUALS,
                            new LongValue(l2EntityClass.field("l0-long").get(), 5120579758453153792L)
                        )
                    )
                    .addAnd(
                        new Condition(
                            l2EntityClass.field("l1-long").get(),
                            ConditionOperator.EQUALS,
                            new LongValue(l2EntityClass.field("l0-long").get(), 162L)
                        )
                    ),
                l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(Page.newSinglePage(1000))
                    .withSort(Sort.buildDescSort(EntityField.ID_ENTITY_FIELD))
                    .withDataAccessFitlerCondtitons(
                        Conditions.buildEmtpyConditions()
                            .addAnd(
                                new Condition(
                                    l2EntityClass.field("l0-long").get(),
                                    ConditionOperator.NOT_EQUALS,
                                    new LongValue(l2EntityClass.field("l0-long").get(), 5120579758453153792L)
                                )
                            )
                    )
                    .build(),
                new long[] {
                }
            )
        );
    }

    // 判断是否有序
    private boolean isSorted(Collection<EntityRef> refs, boolean desc) {
        EntityRef lastRef = null;
        for (EntityRef ref : refs) {
            if (lastRef == null) {
                lastRef = ref;
            }

            int result = ref.compareTo(lastRef);
            // 降序判断
            if (desc) {

                if (result > 0) {
                    return false;
                }

            } else {
                // 升序判断

                if (result < 0) {
                    return false;
                }
            }
        }

        return true;
    }

    private static class SelectResult {
        private Collection<EntityRef> refs;
        private Page page;

        public SelectResult(Collection<EntityRef> refs, Page page) {
            this.refs = refs;
            this.page = page;
        }
    }

    private static class Case {
        private String description;
        private Conditions conditions;
        private IEntityClass entityClass;
        private SelectConfig selectConfig;
        private long[] expectedIds;
        private Function<? super SelectResult, ? super String> otherCheck;

        public Case(String description, Conditions conditions, IEntityClass entityClass, SelectConfig selectConfig,
                    long[] expectedIds) {
            this.description = description;
            this.conditions = conditions;
            this.entityClass = entityClass;
            this.selectConfig = selectConfig;
            this.expectedIds = expectedIds;
        }

        public Case(
            String description,
            Conditions conditions,
            IEntityClass entityClass,
            SelectConfig selectConfig,
            long[] expectedIds,
            Function<? super SelectResult, ? super String> otherCheck) {
            this.description = description;
            this.conditions = conditions;
            this.entityClass = entityClass;
            this.selectConfig = selectConfig;
            this.expectedIds = expectedIds;
            this.otherCheck = otherCheck;
        }
    }

    // 初始化数据,以JSON格式读取当前类路径下的OriginalEntityTestData.json
    private void initData() throws Exception {
        Path path = Paths.get(ClassLoader.getSystemResource("OriginalEntityTestData.json").toURI());
        String value = new String(Files.readAllBytes(path), "utf8");
        Collection<OriginalEntity> datas = JSONArray.parseArray(value, OriginalEntity.class);
        datas.stream().forEach(o -> {
            o.setEntityClass(l2EntityClass);
        });

        IndexInitialization.getInstance().getIndexStorage().saveOrDeleteOriginalEntities(datas);

        this.expectedDatas = datas;
    }

    private Collection<OriginalEntity> expectedDatas;
}
