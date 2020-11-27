package com.xforceplus.ultraman.oqsengine.core.service.integration;


import com.google.common.collect.Comparators;
import com.xforceplus.ultraman.oqsengine.boot.OqsengineBootApplication;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.TransactionManagementService;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.*;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = OqsengineBootApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SearchTest extends AbstractContainerTest {

    private boolean initialization;

    private Collection<IEntityField> mainFields;
    private IEntityClass fatherEntityClass;
    private IEntityClass childEntityClass;
    private List<IEntity> entities;

    private Random random = new Random();

    @Resource
    private EntitySearchService entitySearchService;

    @Resource
    private EntityManagementService managementService;

    @Resource(name = "snowflakeIdGenerator")
    private LongIdGenerator idGenerator;

    @Resource
    private DataSourcePackage dataSourcePackage;

    @Resource
    private RedisClient redisClient;

    @Resource
    private CommitIdStatusService commitIdStatusService;

    @Resource(name = "indexWriteDataSourceSelector")
    private Selector<DataSource> indexWriteDataSourceSelector;

    @Resource
    private TransactionManagementService transactionManagementService;


    private void initData() throws SQLException {

        LocalDateTime now = LocalDateTime.now();

        entities = Arrays.asList(
            new Entity(
                1,
                fatherEntityClass,
                new EntityValue(0).addValues(Arrays.asList(
                    new StringValue(mainFields.stream().findFirst().get(), "1"),
                    new LongValue(mainFields.stream().skip(1).findFirst().get(), 1L),
                    new DecimalValue(mainFields.stream().skip(2).findFirst().get(), new BigDecimal("11.03")),
                    new DateTimeValue(mainFields.stream().skip(3).findFirst().get(), now)
                )),
                OqsVersion.MAJOR
            ),
            new Entity(
                2,
                fatherEntityClass,
                new EntityValue(0).addValues(Arrays.asList(
                    new StringValue(mainFields.stream().findFirst().get(), "1"),
                    new LongValue(mainFields.stream().skip(1).findFirst().get(), 1L),
                    new DecimalValue(mainFields.stream().skip(2).findFirst().get(), new BigDecimal("11.3")),
                    new DateTimeValue(mainFields.stream().skip(3).findFirst().get(), now)
                )),
                OqsVersion.MAJOR
            ),
            new Entity(
                3,
                fatherEntityClass,
                new EntityValue(0).addValues(Arrays.asList(
                    new StringValue(mainFields.stream().findFirst().get(), "1"),
                    new LongValue(mainFields.stream().skip(1).findFirst().get(), 1L),
                    new DecimalValue(mainFields.stream().skip(2).findFirst().get(), new BigDecimal("-11.3")),
                    new DateTimeValue(mainFields.stream().skip(3).findFirst().get(), now)
                )),
                OqsVersion.MAJOR
            ),
            new Entity(
                4,
                fatherEntityClass,
                new EntityValue(0).addValues(Arrays.asList(
                    new StringValue(mainFields.stream().findFirst().get(), "1"),
                    new LongValue(mainFields.stream().skip(1).findFirst().get(), 1L),
                    new DecimalValue(mainFields.stream().skip(2).findFirst().get(), new BigDecimal("-11.03")),
                    new DateTimeValue(mainFields.stream().skip(3).findFirst().get(), now)
                )),
                OqsVersion.MAJOR
            ),
            new Entity(
                5,
                childEntityClass,
                new EntityValue(0).addValues(Arrays.asList(
                    new StringValue(mainFields.stream().findFirst().get(), "1"),
                    new LongValue(mainFields.stream().skip(1).findFirst().get(), 1L),
                    new DecimalValue(mainFields.stream().skip(2).findFirst().get(), new BigDecimal("-11.30")),
                    new DateTimeValue(mainFields.stream().skip(3).findFirst().get(), now),
                    new EnumValue(childEntityClass.field("c5").get(), "false")
                )),
                OqsVersion.MAJOR
            ),
            new Entity(
                6,
                childEntityClass,
                new EntityValue(0).addValues(Arrays.asList(
                    new StringValue(mainFields.stream().findFirst().get(), "1"),
                    new LongValue(mainFields.stream().skip(1).findFirst().get(), 1L),
                    new DecimalValue(mainFields.stream().skip(2).findFirst().get(), new BigDecimal("11.30")),
                    new DateTimeValue(mainFields.stream().skip(3).findFirst().get(), now),
                    new EnumValue(childEntityClass.field("c5").get(), "true")
                )),
                OqsVersion.MAJOR
            )
        );

        buildEntities(entities);
    }


    private Integer nextId() {
        int max = 1000;
        int min = -1000;
        int nextInt = random.nextInt(max - min) + min;
        return nextInt;
    }

    private Double nextDouble() {
        double minD = 400.0;
        double maxD = 2 * minD;
        Double nextD = Math.random() * maxD - minD;
        return nextD;
    }

    private String nextStr() {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();
        int length = 7;
        for (int y = 0; y < length; y++) {
            // generate random index number
            int index = random.nextInt(alphabet.length());
            // get character specified by index
            // from the string
            char randomChar = alphabet.charAt(index);
            // append the character to string builder
            sb.append(randomChar);
        }
        return sb.toString();
    }

    private LocalDateTime nextLocalDateTime() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime randomDate = start.plusDays(random.nextInt(1000 + 1));
        return randomDate;
    }

    private Tuple2<List<Long>, List<Long>> randomOperation(List<Long> ids) throws SQLException {

        List<Long> deleteIds = new LinkedList<>();

        List<Long> updateIds = new LinkedList<>();


        long txId = transactionManagementService.begin();


        ids.stream().forEach(x -> {
            try {
                transactionManagementService.restore(txId);
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }

            int i = random.nextInt(3);
            if (i == 1) {
                //update
                try {

                    Entity entity = new Entity(
                        x,
                        fatherEntityClass,
                        new EntityValue(0).addValues(Arrays.asList(
                            new StringValue(mainFields.stream().findFirst().get(), nextStr()),
                            new LongValue(mainFields.stream().skip(1).findFirst().get(), nextId()),
                            new DecimalValue(mainFields.stream().skip(2).findFirst().get(), new BigDecimal(nextDouble().toString())),
                            new DateTimeValue(mainFields.stream().skip(3).findFirst().get(), nextLocalDateTime())
                        )),
                        OqsVersion.MAJOR
                    );
                    managementService.replace(entity);
                    updateIds.add(x);

                } catch (SQLException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            } else if (i == 2) {
                //delete
                try {
                    transactionManagementService.restore(txId);
                    Entity entity = new Entity(x, fatherEntityClass, new EntityValue(0).addValues(Arrays.asList(
                        new StringValue(mainFields.stream().findFirst().get(), nextStr()),
                        new LongValue(mainFields.stream().skip(1).findFirst().get(), nextId()),
                        new DecimalValue(mainFields.stream().skip(2).findFirst().get(), new BigDecimal(nextDouble().toString())),
                        new DateTimeValue(mainFields.stream().skip(3).findFirst().get(), nextLocalDateTime())
                    )),
                        OqsVersion.MAJOR
                    );
                    managementService.delete(entity);
                    deleteIds.add(x);
                } catch (SQLException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }


            }
        });

        transactionManagementService.restore(txId);
        transactionManagementService.commit();

        return Tuple.of(deleteIds, updateIds);
    }


    private List<Long> initData(int masterSize) throws SQLException {

        // create random string builder

        entities = IntStream.range(0, masterSize).mapToObj(
            i -> {
                Long id = idGenerator.next();
                return new Entity(
                    id,
                    fatherEntityClass,
                    new EntityValue(0).addValues(Arrays.asList(
                        new StringValue(mainFields.stream().findFirst().get(), nextStr()),
                        new LongValue(mainFields.stream().skip(1).findFirst().get(), nextId()),
                        new DecimalValue(mainFields.stream().skip(2).findFirst().get(), new BigDecimal(nextDouble().toString())),
                        new DateTimeValue(mainFields.stream().skip(3).findFirst().get(), nextLocalDateTime())
                    )),
                    OqsVersion.MAJOR
                );
            }
        ).collect(Collectors.toList());


        buildEntities(entities);

        return entities.stream().map(x -> x.id()).collect(Collectors.toList());
    }


    private void buildEntities(List<IEntity> entities) throws SQLException {
        long txId = transactionManagementService.begin();

        for (IEntity e : entities) {
            transactionManagementService.restore(txId);
            managementService.build(e);
        }


        transactionManagementService.restore(txId);
        transactionManagementService.commit();
    }

    @Before
    public void before() throws Exception {

        initialization = false;
        mainFields = Arrays.asList(
            new EntityField(idGenerator.next(), "c1", FieldType.STRING, FieldConfig.build().searchable(true)),
            new EntityField(idGenerator.next(), "c2", FieldType.LONG, FieldConfig.build().searchable(true)),
            new EntityField(idGenerator.next(), "c3", FieldType.DECIMAL, FieldConfig.build().searchable(true)),
            new EntityField(idGenerator.next(), "c4", FieldType.DATETIME, FieldConfig.build().searchable(true))
        );

        fatherEntityClass = new EntityClass(idGenerator.next(), "main", null, null, null, mainFields);

        childEntityClass = new EntityClass(
            idGenerator.next(),
            "child",
            null,
            null,
            fatherEntityClass,
            Arrays.asList(
                new EntityField(idGenerator.next(), "c5", FieldType.ENUM, FieldConfig.build().searchable(true))));

        initialization = true;
    }

    @After
    public void after() throws Exception {
        if (initialization) {
            clear();
        }

        initialization = false;
    }

    private void clear() throws SQLException {
        for (DataSource ds : dataSourcePackage.getMaster()) {
            Connection conn = ds.getConnection();
            Statement st = conn.createStatement();
            st.executeUpdate("truncate table oqsbigentity");
            st.close();
            conn.close();
        }

        for (DataSource ds : dataSourcePackage.getIndexWriter()) {
            Connection conn = ds.getConnection();
            Statement st = conn.createStatement();
            st.executeUpdate("truncate table oqsindex0");
            st.executeUpdate("truncate table oqsindex1");
            st.executeUpdate("truncate table oqsindex2");
            st.close();
            conn.close();
        }

        StatefulRedisConnection<String, String> conn = redisClient.connect();
        conn.sync().flushall();
        conn.close();
    }

    @Test
    public void testChildSearch() throws Exception {
        initData();

        Collection<IEntity> iEntities =
            entitySearchService.selectByConditions(
                Conditions.buildEmtpyConditions(),
                childEntityClass,
                Sort.buildDescSort(mainFields.stream().skip(2).findFirst().get()),
                new Page(1, 10));

        Assert.assertEquals(2, iEntities.size());

        List<BigDecimal> bigDecimals = iEntities.stream().map(x -> {
            IValue iValue = x.entityValue().values().stream().filter(y -> y instanceof DecimalValue).findFirst().get();
            return ((DecimalValue) iValue).getValue();
        }).collect(Collectors.toList());

        Comparator<BigDecimal> bigDecimalComparator = (o1, o2) -> o1.compareTo(o2);

        assertTrue(Comparators.isInOrder(bigDecimals, bigDecimalComparator));
    }

    @Test
    public void testChildSecondPageSearch() throws Exception {
        initData();

        Collection<IEntity> iEntities =
            entitySearchService.selectByConditions(
                Conditions.buildEmtpyConditions(),
                childEntityClass,
                Sort.buildAscSort(mainFields.stream().skip(2).findFirst().get()),
                new Page(1, 1));

        Assert.assertEquals(1, iEntities.size());
        List<BigDecimal> bigDecimals = iEntities.stream().map(x -> {
            IValue iValue = x.entityValue().values().stream().filter(y -> y instanceof DecimalValue).findFirst().get();
            return ((DecimalValue) iValue).getValue();
        }).collect(Collectors.toList());
        Assert.assertEquals(new BigDecimal("-11.30"), bigDecimals.get(0));

        iEntities =
            entitySearchService.selectByConditions(
                Conditions.buildEmtpyConditions(),
                childEntityClass,
                Sort.buildAscSort(mainFields.stream().skip(2).findFirst().get()),
                new Page(2, 1));

        Assert.assertEquals(1, iEntities.size());
        bigDecimals = iEntities.stream().map(x -> {
            IValue iValue = x.entityValue().values().stream().filter(y -> y instanceof DecimalValue).findFirst().get();
            return ((DecimalValue) iValue).getValue();
        }).collect(Collectors.toList());
        Assert.assertEquals(new BigDecimal("11.30"), bigDecimals.get(0));
    }

    @Test
    public void basicSearch() throws Exception {

        initData();

        Thread.sleep(10000);

        Page page = new Page(0, 100);
        Sort sort = Sort.buildAscSort(fatherEntityClass.fields().get(2));

        Collection<IEntity> iEntities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions(), fatherEntityClass, sort, page);

        List<BigDecimal> bigDecimals = iEntities.stream().map(x -> {
            IValue iValue = x.entityValue().values().stream().filter(y -> y instanceof DecimalValue).findFirst().get();
            return ((DecimalValue) iValue).getValue();
        }).collect(Collectors.toList());

        Comparator<BigDecimal> bigDecimalComparator = (o1, o2) -> o1.compareTo(o2);

        assertTrue(Comparators.isInOrder(bigDecimals, bigDecimalComparator));
    }

    @Test
    public void stringOrderSearch() throws SQLException, InterruptedException {
        initData(100);

        Thread.sleep(10000);

        Page page = new Page(0, 100);
        Sort sort = Sort.buildAscSort(fatherEntityClass.fields().get(0));

        Collection<IEntity> iEntities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions(), fatherEntityClass, sort, page);

        List<String> stringList = iEntities.stream().map(x -> {
            IValue iValue = x.entityValue().values().stream().filter(y -> y instanceof StringValue).findFirst().get();
            return ((StringValue) iValue).getValue();
        }).collect(Collectors.toList());


        Comparator<String> stringComparator = (o1, o2) -> o1.compareTo(o2);

        assertTrue(Comparators.isInOrder(stringList, stringComparator));
    }

    @Test
    public void dateTimeOrderSearch() throws SQLException, InterruptedException {

        initData(100);

        Thread.sleep(10000);

        Page page = new Page(0, 100);
        Sort sort = Sort.buildAscSort(fatherEntityClass.fields().get(3));

        Collection<IEntity> iEntities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions(), fatherEntityClass, sort, page);

        List<LocalDateTime> dateList = iEntities.stream().map(x -> {
            IValue iValue = x.entityValue().values().stream().filter(y -> y instanceof DateTimeValue).findFirst().get();
            return ((DateTimeValue) iValue).getValue();
        }).collect(Collectors.toList());


        Comparator<LocalDateTime> stringComparator = (o1, o2) -> o1.compareTo(o2);

        assertTrue(Comparators.isInOrder(dateList, stringComparator));
    }

    @Test
    public void longOrderSearch() throws SQLException {
        initData(100);

        Page page = new Page(0, 100);
        Sort sort = Sort.buildAscSort(fatherEntityClass.fields().get(1));

        Collection<IEntity> iEntities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions(), fatherEntityClass, sort, page);

        List<Long> dateList = iEntities.stream().map(x -> {
            IValue iValue = x.entityValue().values().stream().filter(y -> y instanceof LongValue).findFirst().get();
            return ((LongValue) iValue).getValue();
        }).collect(Collectors.toList());

        Comparator<Long> longComparator = (o1, o2) -> o1.compareTo(o2);
        assertTrue(Comparators.isInOrder(dateList, longComparator));
    }

    @Test
    public void noSortSearch() throws SQLException, InterruptedException {
        initData(100);

        Thread.sleep(10000);

        Page page = new Page(0, 100);

        Collection<IEntity> iEntities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions(), fatherEntityClass, null, page);

        assertTrue(page.getTotalCount() == 100);
    }

    @Test
    public void mixedSearch() throws SQLException, InterruptedException {

        initData(100);

        Thread.sleep(10000);

        initData(100);

        Page page = new Page(0, 100);

        Collection<IEntity> iEntities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions(), fatherEntityClass, null, page);

        assertTrue(page.getTotalCount() == 200);
    }

    @Test
    public void mixedOperationSearch() throws SQLException, InterruptedException {
        List<Long> longs = initData(50);

        Thread.sleep(10000);

        Tuple2<List<Long>, List<Long>> listListTuple2 = randomOperation(longs);

        System.out.println("DELETED :" + listListTuple2._1().size());
        System.out.println("UPDATED :" + listListTuple2._2().size());


        Page page = new Page(0, 100);
        Collection<IEntity> iEntities = entitySearchService.selectByConditions(
            Conditions.buildEmtpyConditions(), fatherEntityClass, null, page);


        assertTrue(page.getTotalCount() == (50 - listListTuple2._1().size()));
        assertTrue(iEntities.stream().map(x -> x.id()).distinct().collect(Collectors.toList()).size() == iEntities.size());
        assertTrue(iEntities.stream().filter(x -> listListTuple2._2().contains(x.id())).filter(x -> x.version() > 0).collect(Collectors.toList()).size() == listListTuple2._2().size());
        assertTrue(iEntities.stream().map(x -> x.id()).filter(x -> listListTuple2._1().contains(x)).collect(Collectors.toList()).isEmpty());
    }
}
