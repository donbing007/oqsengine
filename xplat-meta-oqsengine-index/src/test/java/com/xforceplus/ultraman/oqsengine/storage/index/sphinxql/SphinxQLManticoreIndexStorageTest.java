package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql;

import com.github.javafaker.Faker;
import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
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
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EnumValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.pojo.define.OperationType;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.mock.IndexInitialization;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import com.xforceplus.ultraman.oqsengine.storage.value.ShortStorageName;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.ManticoreContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import com.xforceplus.ultraman.oqsengine.tokenizer.Tokenizer;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * MantiocreIndexStorage Tester.
 *
 * @author dongbin
 * @version 1.0 03/08/2021
 * @since <pre>Mar 8, 2021</pre>
 */
@ExtendWith({RedisContainer.class, ManticoreContainer.class})
public class SphinxQLManticoreIndexStorageTest {

    private Faker faker = new Faker(Locale.CHINA);

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
    }

    @AfterEach
    public void after() throws Exception {
        InitializationHelper.clearAll();
        InitializationHelper.destroy();
    }

    /**
     * 测试主库物理储存类型转换到索引储存时的转换是否正确.
     * 比如浮点数,在主库是以字符串非多值存在,但是索引需要以数字型的多值形式存在.
     */
    @Test
    public void testPhysicalStorageCorrect() throws Exception {

        OriginalEntity target = OriginalEntity.Builder.anOriginalEntity()
            .withId(Long.MAX_VALUE - 300)
            .withAttribute(l2DecField.id() + "S", "123.789")
            .withEntityClass(l2EntityClass)
            .withCreateTime(System.currentTimeMillis())
            .withVersion(0)
            .withDeleted(false)
            .withOp(OperationType.CREATE.getValue())
            .withCommitid(0)
            .withTx(0)
            .withOqsMajor(OqsVersion.MAJOR)
            .build();

        IndexInitialization.getInstance().getIndexStorage().saveOrDeleteOriginalEntities(Arrays.asList(target));

        Collection<EntityRef> refs = IndexInitialization.getInstance().getIndexStorage().select(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        l2EntityClass.field("l2-dec").get(),
                        ConditionOperator.EQUALS,
                        new DecimalValue(l2EntityClass.field("l2-dec").get(), new BigDecimal("123.789"))
                    )
                ),
            l2EntityClass,
            SelectConfig.Builder.anSelectConfig().withPage(Page.newSinglePage(100)).build()
        );

        Assertions.assertEquals(1, refs.size());
        Assertions.assertEquals(Long.MAX_VALUE - 300, refs.stream().findFirst().get().getId());

        refs = IndexInitialization.getInstance().getIndexStorage().select(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        l2EntityClass.field("l2-dec").get(),
                        ConditionOperator.GREATER_THAN_EQUALS,
                        new DecimalValue(l2EntityClass.field("l2-dec").get(), new BigDecimal("123.789"))
                    )
                ),
            l2EntityClass,
            SelectConfig.Builder.anSelectConfig().withPage(Page.newSinglePage(100)).build()
        );
        Assertions.assertEquals(1, refs.size());
        Assertions.assertEquals(Long.MAX_VALUE - 300, refs.stream().findFirst().get().getId());

        target = OriginalEntity.Builder.anOriginalEntity()
            .withId(Long.MAX_VALUE - 600)
            .withAttribute(l0StringsField.id() + "S", "[RMB][JPY][USD]")
            .withEntityClass(l2EntityClass)
            .withCreateTime(System.currentTimeMillis())
            .withVersion(0)
            .withDeleted(false)
            .withOp(OperationType.CREATE.getValue())
            .withCommitid(0)
            .withTx(0)
            .withOqsMajor(OqsVersion.MAJOR)
            .build();
        IndexInitialization.getInstance().getIndexStorage().saveOrDeleteOriginalEntities(Arrays.asList(target));

        refs = IndexInitialization.getInstance().getIndexStorage().select(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        l2EntityClass.field("l0-strings").get(),
                        ConditionOperator.EQUALS,
                        new StringsValue(l2EntityClass.field("l0-strings").get(), "RMB")
                    )
                ),
            l2EntityClass,
            SelectConfig.Builder.anSelectConfig().withPage(Page.newSinglePage(100)).build()
        );

        Assertions.assertEquals(1, refs.size());
        Assertions.assertEquals(Long.MAX_VALUE - 600, refs.stream().findFirst().get().getId());
    }

    /**
     * 测试值中有特殊符号,单引号,双引号等.
     */
    @Test
    public void testSymbolValue() throws Exception {
        // 单引号
        StorageStrategy l0StorageStrategy = IndexInitialization.getInstance().getStorageStrategyFactory().getStrategy(
            l2EntityClass.field("l0-string").get().type());
        StorageValue l0StorageValue = l0StorageStrategy.toStorageValue(
            new StringValue(
                l2EntityClass.field("l0-string").get(),
                "1d'f"));

        OriginalEntity target = OriginalEntity.Builder.anOriginalEntity()
            .withId(Long.MAX_VALUE - 300)
            .withAttribute(l0StorageValue.storageName(), l0StorageValue.value())
            .withEntityClass(l2EntityClass)
            .withCreateTime(System.currentTimeMillis())
            .withVersion(0)
            .withDeleted(false)
            .withOp(OperationType.CREATE.getValue())
            .withCommitid(0)
            .withTx(0)
            .withOqsMajor(OqsVersion.MAJOR)
            .build();

        IndexInitialization.getInstance().getIndexStorage().saveOrDeleteOriginalEntities(Arrays.asList(target));

        Collection<EntityRef> refs = IndexInitialization.getInstance().getIndexStorage().select(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        l2EntityClass.field("l0-string").get(),
                        ConditionOperator.EQUALS,
                        new StringValue(l2EntityClass.field("l0-string").get(), "1d'f")
                    )
                ),
            l2EntityClass,
            SelectConfig.Builder.anSelectConfig().withPage(Page.newSinglePage(100)).build()
        );
        Assertions.assertEquals(1, refs.size());
        Assertions.assertEquals(target.getId(), refs.stream().findFirst().get().getId());

        target.setOp(OperationType.DELETE.getValue());
        IndexInitialization.getInstance().getIndexStorage().saveOrDeleteOriginalEntities(Arrays.asList(target));

        // 双引号
        l0StorageStrategy = IndexInitialization.getInstance().getStorageStrategyFactory().getStrategy(
            l2EntityClass.field("l0-string").get().type());
        l0StorageValue = l0StorageStrategy.toStorageValue(
            new StringValue(
                l2EntityClass.field("l0-string").get(),
                "1d\"f"));

        target = OriginalEntity.Builder.anOriginalEntity()
            .withId(Long.MAX_VALUE - 400)
            .withAttribute(l0StorageValue.storageName(), l0StorageValue.value())
            .withEntityClass(l2EntityClass)
            .withCreateTime(System.currentTimeMillis())
            .withVersion(0)
            .withDeleted(false)
            .withOp(OperationType.CREATE.getValue())
            .withCommitid(0)
            .withTx(0)
            .withOqsMajor(OqsVersion.MAJOR)
            .build();

        IndexInitialization.getInstance().getIndexStorage().saveOrDeleteOriginalEntities(Arrays.asList(target));

        refs = IndexInitialization.getInstance().getIndexStorage().select(
            Conditions.buildEmtpyConditions()
                .addAnd(
                    new Condition(
                        l2EntityClass.field("l0-string").get(),
                        ConditionOperator.EQUALS,
                        new StringValue(l2EntityClass.field("l0-string").get(), "1d\"f")
                    )
                ),
            l2EntityClass,
            SelectConfig.Builder.anSelectConfig().withPage(Page.newSinglePage(100)).build()
        );
        Assertions.assertEquals(1, refs.size());
        Assertions.assertEquals(target.getId(), refs.stream().findFirst().get().getId());
    }

    @Test
    public void testFuzzyValue() throws Exception {
        StorageStrategy l0StorageStrategy = IndexInitialization.getInstance().getStorageStrategyFactory().getStrategy(
            l2EntityClass.field("l0-string").get().type());
        StorageValue l0StorageValue = l0StorageStrategy.toStorageValue(
            new StringValue(
                l2EntityClass.field("l0-string").get(),
                "这是一个测试"));

        StorageStrategy l1StorageStrategy = IndexInitialization.getInstance().getStorageStrategyFactory().getStrategy(
            l2EntityClass.field("l1-string").get().type());
        StorageValue l1StorageValue = l1StorageStrategy.toStorageValue(
            new StringValue(
                l2EntityClass.field("l1-string").get(),
                "abcdefg"
            ));

        OriginalEntity target = OriginalEntity.Builder.anOriginalEntity()
            .withId(Long.MAX_VALUE - 300)
            .withAttribute(l0StorageValue.storageName(), l0StorageValue.value())
            .withAttribute(l1StorageValue.storageName(), l1StorageValue.value())
            .withEntityClass(l2EntityClass)
            .withCreateTime(System.currentTimeMillis())
            .withVersion(0)
            .withDeleted(false)
            .withOp(OperationType.CREATE.getValue())
            .withCommitid(0)
            .withTx(0)
            .withOqsMajor(OqsVersion.MAJOR)
            .build();

        IndexInitialization.getInstance().getIndexStorage().saveOrDeleteOriginalEntities(Arrays.asList(target));

        List<String> attrs;
        try (Connection conn = CommonInitialization.getInstance().getDataSourcePackage(true).getIndexSearch().get(0)
            .getConnection()) {
            try (Statement st = conn.createStatement()) {
                try (ResultSet rs = st.executeQuery(
                    String.format(
                        "select %s from %s where %s = %d",
                        FieldDefine.ATTRIBUTEF, "oqsindex", FieldDefine.ID, target.getId()))) {

                    rs.next();

                    String attrf = rs.getString(FieldDefine.ATTRIBUTEF);
                    attrs = Arrays.asList(attrf.split(" "));
                }
            }
        }

        ShortStorageName shortStorageName = l0StorageValue.shortStorageName();
        Tokenizer tokenizer = IndexInitialization.getInstance().getTokenizerFactory()
            .getTokenizer(l2EntityClass.field("l0-string").get());
        Iterator<String> words = tokenizer.tokenize(l0StorageValue.value().toString());
        while (words.hasNext()) {
            String word = words.next();
            String expected =
                String.format("%s%sw%s", shortStorageName.getPrefix(), word, shortStorageName.getSuffix());
            Assertions.assertTrue(attrs.contains(expected));
        }
        String value = l0StorageValue.value().toString();
        String expected = String.format("%s%s%s", shortStorageName.getPrefix(), value, shortStorageName.getSuffix());
        Assertions.assertTrue(attrs.contains(expected));

        shortStorageName = l1StorageValue.shortStorageName();
        tokenizer = IndexInitialization.getInstance().getTokenizerFactory()
            .getTokenizer(l2EntityClass.field("l1-string").get());
        words = tokenizer.tokenize(l1StorageValue.value().toString());
        while (words.hasNext()) {
            String word = words.next();
            expected = String.format("%s%sw%s", shortStorageName.getPrefix(), word, shortStorageName.getSuffix());
            Assertions.assertTrue(attrs.contains(expected));
        }
        value = l1StorageValue.value().toString();
        expected = String.format("%s%s%s", shortStorageName.getPrefix(), value, shortStorageName.getSuffix());
        Assertions.assertTrue(attrs.contains(expected));
    }

    @Test
    public void testSaveOriginalEntities() throws Exception {
        List<OriginalEntity> initDatas = new LinkedList<>();
        initDatas.addAll(buildSyncData(OperationType.CREATE, 10, Long.MAX_VALUE));

        IndexInitialization.getInstance().getIndexStorage().saveOrDeleteOriginalEntities(initDatas);

        // 查询id降序
        Page page = Page.newSinglePage(1000);
        Collection<EntityRef> refs =
            IndexInitialization.getInstance().getIndexStorage().select(Conditions.buildEmtpyConditions(), l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(page).withCommitId(0).withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD)).build());
        Assertions.assertEquals(initDatas.size(), refs.size());
        Assertions.assertEquals(initDatas.size(), page.getTotalCount());

        // 新创建2条
        List<OriginalEntity> processDatas = new LinkedList<>();
        processDatas.addAll(buildSyncData(OperationType.CREATE, 2, Long.MAX_VALUE - 11));

        // 删除2条
        OriginalEntity target = initDatas.get(0);
        OriginalEntity d1 = OriginalEntity.Builder.anOriginalEntity()
            .withId(target.getId())
            .withOp(OperationType.DELETE.getValue())
            .withEntityClass(target.getEntityClass())
            .build();
        processDatas.add(d1);
        target = initDatas.get(1);
        OriginalEntity d2 = OriginalEntity.Builder.anOriginalEntity()
            .withId(target.getId())
            .withOp(OperationType.DELETE.getValue())
            .withEntityClass(target.getEntityClass())
            .build();
        processDatas.add(d2);
        target = initDatas.get(2);
        OriginalEntity d3 = OriginalEntity.Builder.anOriginalEntity()
            .withId(target.getId())
            .withOp(OperationType.DELETE.getValue())
            .withEntityClass(target.getEntityClass())
            .build();
        processDatas.add(d3);

        // 更新两条
        target = initDatas.get(3);
        OriginalEntity u1 = OriginalEntity.Builder.anOriginalEntity()
            .withId(target.getId())
            .withAttributes(target.getAttributes())
            .withCommitid(target.getCommitid())
            .withVersion(target.getVersion())
            .withEntityClass(target.getEntityClass())
            .withCreateTime(target.getCreateTime())
            .withUpdateTime(System.currentTimeMillis())
            .withDeleted(false)
            .withOp(OperationType.UPDATE.getValue())
            .withTx(target.getTx())
            .withVersion(target.getVersion() + 1)
            .withOqsMajor(OqsVersion.MAJOR)
            .build();
        processDatas.add(u1);

        target = initDatas.get(4);
        OriginalEntity u2 = OriginalEntity.Builder.anOriginalEntity()
            .withId(target.getId())
            .withAttributes(target.getAttributes())
            .withCommitid(target.getCommitid())
            .withVersion(target.getVersion())
            .withEntityClass(target.getEntityClass())
            .withCreateTime(target.getCreateTime())
            .withUpdateTime(System.currentTimeMillis())
            .withDeleted(false)
            .withOp(OperationType.UPDATE.getValue())
            .withTx(target.getTx())
            .withVersion(target.getVersion() + 1)
            .withOqsMajor(OqsVersion.MAJOR)
            .build();
        processDatas.add(u2);

        IndexInitialization.getInstance().getIndexStorage().saveOrDeleteOriginalEntities(processDatas);

        page = Page.newSinglePage(1000);
        refs =
            IndexInitialization.getInstance().getIndexStorage().select(Conditions.buildEmtpyConditions(), l2EntityClass,
                SelectConfig.Builder.anSelectConfig()
                    .withPage(page).withCommitId(0).withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD)).build());
        Assertions.assertEquals(10 + 2 - 3, refs.size());
        Assertions.assertEquals(10 + 2 - 3, page.getTotalCount());
    }

    @Test
    public void testClean() throws Exception {
        List<OriginalEntity> initDatas = new LinkedList<>();
        initDatas.addAll(buildSyncData(OperationType.CREATE, 10, Long.MAX_VALUE));

        IndexInitialization.getInstance().getIndexStorage().saveOrDeleteOriginalEntities(initDatas);
        IndexInitialization.getInstance().getIndexStorage().clean(l2EntityClass, 10, 0, Long.MAX_VALUE);

        Page page = Page.newSinglePage(1000);
        Collection<EntityRef> refs =
            IndexInitialization.getInstance().getIndexStorage().select(Conditions.buildEmtpyConditions(), l2EntityClass,
                SelectConfig.Builder.anSelectConfig().withPage(page).withCommitId(0).build());
        Assertions.assertEquals(0, refs.size());
        Assertions.assertEquals(0, page.getTotalCount());

        initDatas.clear();

        initDatas.addAll(buildSyncData(OperationType.CREATE, 10, Long.MAX_VALUE));
        IndexInitialization.getInstance().getIndexStorage().saveOrDeleteOriginalEntities(initDatas);
        IndexInitialization.getInstance().getIndexStorage().clean(l2EntityClass, 0, 0, Long.MAX_VALUE);

        page = Page.newSinglePage(1000);
        refs =
            IndexInitialization.getInstance().getIndexStorage().select(Conditions.buildEmtpyConditions(), l2EntityClass,
                SelectConfig.Builder.anSelectConfig().withPage(page).withCommitId(0).build());
        Assertions.assertEquals(initDatas.size(), refs.size());
        Assertions.assertEquals(initDatas.size(), page.getTotalCount());
    }

    // 构造同步数据
    private Collection<OriginalEntity> buildSyncData(OperationType op, int size, long lastId) {
        return IntStream.range(0, size).mapToObj(i -> buildSyncData(op, i, l2EntityClass, lastId))
            .collect(Collectors.toList());
    }

    private OriginalEntity buildSyncData(OperationType op, int index, IEntityClass entityClass, long lastId) {
        OriginalEntity.Builder builder = OriginalEntity.Builder.anOriginalEntity()
            .withId(lastId - index)
            .withOp(op.getValue())
            .withCreateTime(System.currentTimeMillis())
            .withUpdateTime(System.currentTimeMillis())
            .withCommitid(index)
            .withTx(Integer.MAX_VALUE - index)
            .withDeleted(OperationType.DELETE == op)
            .withEntityClass(l2EntityClass)
            .withOqsMajor(OqsVersion.MAJOR);

        List<StorageValue> svs = new ArrayList<>();
        entityClass.fields().forEach(f -> {
            StorageStrategy storageStrategy = null;
            try {
                storageStrategy = IndexInitialization.getInstance().getStorageStrategyFactory().getStrategy(f.type());
            } catch (Exception e) {
                throw new RuntimeException("storageStrategy is null.");
            }
            switch (f.name()) {
                case "l0-long": {
                    long v = faker.number().numberBetween(1, Long.MAX_VALUE);
                    IValue iv = new LongValue(f, v, Long.toString(v));
                    svs.add(storageStrategy.toStorageValue(iv));
                    svs.add(storageStrategy.toAttachmentStorageValue(iv).get());
                    break;
                }
                case "l0-string": {
                    String v = faker.name().fullName();
                    IValue iv = new StringValue(f, v, v);
                    svs.add(storageStrategy.toStorageValue(iv));
                    svs.add(storageStrategy.toAttachmentStorageValue(iv).get());
                    break;
                }
                case "l0-strings": {
                    StringBuilder buff = new StringBuilder();
                    buff.append("[").append(faker.color().name()).append("]");
                    buff.append("[").append(faker.color().name()).append("]");
                    buff.append("[").append(faker.color().name()).append("]");
                    svs.add(new StringStorageValue(
                        Long.toString(f.id()),
                        buff.toString(),
                        true
                    ));

                    break;
                }
                case "l1-long": {
                    long v = faker.number().numberBetween(100, 200);
                    IValue iv = new LongValue(f, v, Long.toString(v));
                    svs.add(storageStrategy.toStorageValue(iv));
                    svs.add(storageStrategy.toAttachmentStorageValue(iv).get());
                    break;
                }
                case "l1-string": {
                    String v = faker.phoneNumber().cellPhone();
                    IValue iv = new StringValue(f, v, v);
                    svs.add(storageStrategy.toStorageValue(iv));
                    svs.add(storageStrategy.toAttachmentStorageValue(iv).get());
                    break;
                }
                case "l2-string": {
                    String v = faker.idNumber().invalid();
                    IValue iv = new StringValue(f, v, v);
                    svs.add(storageStrategy.toStorageValue(iv));
                    svs.add(storageStrategy.toAttachmentStorageValue(iv).get());
                    break;
                }
                case "l2-time": {
                    LocalDateTime localDateTime =
                        LocalDateTime.ofInstant(faker.date().birthday().toInstant(), ZoneId.systemDefault());
                    IValue iv = new DateTimeValue(f, localDateTime, localDateTime.toString());
                    svs.add(storageStrategy.toStorageValue(iv));
                    svs.add(storageStrategy.toAttachmentStorageValue(iv).get());
                    break;
                }
                case "l2-enum": {
                    String v = faker.color().name();
                    IValue iv = new EnumValue(f, v, v);
                    svs.add(storageStrategy.toStorageValue(iv));
                    svs.add(storageStrategy.toAttachmentStorageValue(iv).get());
                    break;
                }
                case "l2-dec": {
                    svs.add(new StringStorageValue(
                        Long.toString(f.id()),
                        Double.toString(faker.number().randomDouble(10, 100, 100000)),
                        true
                    ));

                    break;
                }
                default: {
                    throw new IllegalArgumentException(String.format("Cannot process the field.[%s]", f.name()));
                }

            }

        });

        Map<String, Object> attrs = new HashMap<>();
        for (StorageValue sv : svs) {
            attrs.put(sv.storageName(), sv.value());
        }

        builder.withAttributes(attrs);

        return builder.build();

    }
}
