package com.xforceplus.ultraman.oqsengine.idgenerator.client;

import static com.xforceplus.ultraman.oqsengine.idgenerator.common.constant.Constants.DATE_PATTEN_PARSER;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.PatternValue;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentId;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import com.xforceplus.ultraman.oqsengine.idgenerator.generator.IDGeneratorFactoryImpl;
import com.xforceplus.ultraman.oqsengine.idgenerator.mock.IdGenerateDbScript;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParserManager;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParserUtil;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl.DatePatternParser;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl.NumberPatternParser;
import com.xforceplus.ultraman.oqsengine.idgenerator.service.SegmentService;
import com.xforceplus.ultraman.oqsengine.idgenerator.service.impl.SegmentServiceImpl;
import com.xforceplus.ultraman.oqsengine.idgenerator.storage.SqlSegmentStorage;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.MysqlContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.apache.commons.compress.utils.Lists;
import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.redisson.Redisson;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 业务自增id测试.
 *
 * @author leo
 * @version 0.1 5/21/21 3:51 PM
 * @since 1.8
 */
@ExtendWith({MysqlContainer.class, RedisContainer.class})
public class BizIDGeneratorRedisTest {

    private ApplicationContext applicationContext;

    private IDGeneratorFactoryImpl idGeneratorFactory1;
    private SegmentService segmentService1;
    private SqlSegmentStorage storage1;
    private BizIDGenerator bizIDGenerator1;
    private BizIDGenerator bizIDGenerator2;
    private BizIDGenerator bizIDGenerator3;

    private static final String LINEAR_BIZ_TYPE = "bizLinear";
    private static final String LINEAR_BIZ_TYPE_2 = "bizLinear2";
    private static final String LINEAR_BIZ_TYPE_3 = "bizLinear3";
    private static final String LINER_BIZ_TYPE_4 = "bizLinear4";
    private static final String LINER_BIZ_TYPE_5 = "bizLinear5";

    private RedissonClient redissonClient;
    private DataSource dataSource;
    private ExecutorService executorService;
    private DataSourcePackage dataSourcePackage;
    private PatternParserManager manager;


    @BeforeClass
    public static void afterClass() {
        Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);



    }

    /**
     * 清理.
     */
    @AfterEach
    public void after() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            Statement st = conn.createStatement();
            st.executeUpdate("drop table segment");
            st.close();
        } finally {
            dataSourcePackage.close();
        }

        redissonClient.shutdown();

        InitializationHelper.clearAll();
    }

    /**
     * 每个测试的初始化.
     */
    @BeforeEach
    public void before() throws SQLException, IllegalAccessException {
        System.setProperty(
            "MYSQL_JDBC_ID",
            String.format(
                "jdbc:mysql://%s:%s/oqsengine?useUnicode=true&serverTimezone=GMT&useSSL=false&characterEncoding=utf8",
                System.getProperty("MYSQL_HOST"), System.getProperty("MYSQL_PORT")));

        executorService = Executors.newFixedThreadPool(30);

        dataSource = buildDataSource("./src/test/resources/generator.conf");

        try (Connection conn = dataSource.getConnection()) {
            Statement st = conn.createStatement();
            st.executeUpdate(IdGenerateDbScript.CREATE_SEGMENT);
            st.close();
        }

        storage1 = new SqlSegmentStorage();
        storage1.setTable("segment");
        storage1.init();
        ReflectionTestUtils.setField(storage1, "dataSource", dataSource);

        Config config = new Config();
        String redisIp = System.getProperty("REDIS_HOST");
        int redisPort = Integer.parseInt(System.getProperty("REDIS_PORT"));
        config.useSingleServer().setAddress(String.format("redis://%s:%s", redisIp, redisPort));
        Codec codec = new JsonJacksonCodec();
        config.setCodec(codec);
        redissonClient = Redisson.create(config);


        manager = new PatternParserManager();
        NumberPatternParser parser = new NumberPatternParser();
        DatePatternParser datePattenParser = new DatePatternParser();
        manager.registVariableParser(parser);
        manager.registVariableParser(datePattenParser);

        applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(PatternParserManager.class)).thenReturn(manager);
        ReflectionTestUtils.setField(PatternParserUtil.class, "applicationContext", applicationContext);

        this.segmentService1 = new SegmentServiceImpl();
        this.idGeneratorFactory1 = new IDGeneratorFactoryImpl();
        this.bizIDGenerator1 = new BizIDGenerator();
        this.bizIDGenerator2 = new BizIDGenerator();
        this.bizIDGenerator3 = new BizIDGenerator();
        ReflectionTestUtils.setField(segmentService1, "sqlSegmentStorage", storage1);
        ReflectionTestUtils.setField(idGeneratorFactory1, "segmentService", segmentService1);
        ReflectionTestUtils.setField(idGeneratorFactory1, "redissonClient", redissonClient);
        ReflectionTestUtils.setField(bizIDGenerator1, "idGeneratorFactory", idGeneratorFactory1);
        ReflectionTestUtils.setField(bizIDGenerator2, "idGeneratorFactory", idGeneratorFactory1);
        ReflectionTestUtils.setField(bizIDGenerator3, "idGeneratorFactory", idGeneratorFactory1);


        SegmentInfo info = SegmentInfo.builder().withBeginId(1L).withBizType(LINEAR_BIZ_TYPE)
            .withCreateTime(new Timestamp(System.currentTimeMillis()))
            .withMaxId(0L).withPatten("{yyyy}-{MM}-{dd}:{00000}").withMode(2).withStep(1000)
            .withUpdateTime(new Timestamp(System.currentTimeMillis()))
            .withVersion(1L)
            .withResetable(0)
            .withPatternKey("")
            .build();
        int ret = storage1.build(info);
        Assertions.assertEquals(ret, 1);

        SegmentInfo info2 = SegmentInfo.builder().withBeginId(1L).withBizType(LINEAR_BIZ_TYPE_2)
            .withCreateTime(new Timestamp(System.currentTimeMillis()))
            .withMaxId(0L).withPatten("{yyyy}-{MM}-{dd}:{00000}").withMode(2).withStep(1000)
            .withUpdateTime(new Timestamp(System.currentTimeMillis()))
            .withVersion(1L)
            .withResetable(0)
            .withPatternKey("")
            .build();
        int ret2 = storage1.build(info2);
        Assertions.assertEquals(ret2, 1);

        SegmentInfo info3 = SegmentInfo.builder().withBeginId(1L).withBizType(LINEAR_BIZ_TYPE_3)
            .withCreateTime(new Timestamp(System.currentTimeMillis()))
            .withMaxId(0L).withPatten("{yyyy}-{MM}-{dd}:{00000}").withMode(2).withStep(100)
            .withUpdateTime(new Timestamp(System.currentTimeMillis()))
            .withVersion(1L)
            .withResetable(0)
            .withPatternKey("")
            .build();
        int ret3 = storage1.build(info3);
        Assertions.assertEquals(ret3, 1);

        SegmentInfo info4 = SegmentInfo.builder().withBeginId(1L).withBizType(LINER_BIZ_TYPE_4)
            .withCreateTime(new Timestamp(System.currentTimeMillis()))
            .withMaxId(0L).withPatten("{yyyy}-{MM}-{dd}:{00000}").withMode(2).withStep(1000)
            .withUpdateTime(new Timestamp(System.currentTimeMillis()))
            .withVersion(1L)
            .withResetable(1)
            .withPatternKey("")
            .build();
        int ret4 = storage1.build(info4);
        Assertions.assertEquals(ret4, 1);


        SegmentInfo info5 = SegmentInfo.builder().withBeginId(1L).withBizType(LINER_BIZ_TYPE_5)
            .withCreateTime(new Timestamp(System.currentTimeMillis()))
            .withMaxId(0L).withPatten("{yyyy}-{MM}-{dd}-{HH}:{0000}").withMode(2).withStep(1000)
            .withUpdateTime(new Timestamp(System.currentTimeMillis()))
            .withVersion(1L)
            .withResetable(1)
            .withPatternKey("")
            .build();
        int ret5 = storage1.build(info5);
        Assertions.assertEquals(ret5, 1);
    }

    @Test
    public void testRedisson() {
        SegmentId value = new SegmentId();
        value.setResetable(1);
        value.setPattern("1");
        value.setCurrentId(new PatternValue(1, "1"));
        value.setLoadingId(1L);
        value.setMaxId(1000L);
        RBucket<SegmentId> al = redissonClient.getBucket("testBiz");
        al.set(value);
        SegmentId next = value.clone();
        next.nextId();
        Assertions.assertTrue(al.compareAndSet(value, next));
    }

    @Test
    public void testDistributeBizIDGenerator() throws InterruptedException {
        String bizId = "";
        for (int i = 0; i < 10; i++) {
            bizId = bizIDGenerator2.nextId(LINEAR_BIZ_TYPE_2);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String expected = LocalDateTime.now().format(formatter) + ":00010";
        Assertions.assertEquals(expected, bizId);
        for (int i = 0; i < 100; i++) {
            bizId = bizIDGenerator2.nextId(LINEAR_BIZ_TYPE_2);
            System.out.println(bizId);
        }
        String expected1 = LocalDateTime.now().format(formatter) + ":00110";
        Assertions.assertEquals(expected1, bizId);
        bizId = bizIDGenerator2.nextId(LINEAR_BIZ_TYPE_2);
        String expected2 = LocalDateTime.now().format(formatter) + ":00111";
        Assertions.assertEquals(expected2, bizId);
    }

    @Test
    public void testExpire() throws InterruptedException {
       RAtomicLong key1 =  redissonClient.getAtomicLong("key1");
       Assertions.assertEquals(key1.remainTimeToLive(),-2);
       System.out.println(key1.incrementAndGet());
       Assertions.assertEquals(key1.remainTimeToLive(),-1);
       key1.expire(5, TimeUnit.SECONDS);
       System.out.println("第3次："+key1.remainTimeToLive());
       System.out.println(key1.incrementAndGet());
       Thread.sleep(6000);
       Assertions.assertEquals(key1.remainTimeToLive(),-2);
       System.out.println(key1.incrementAndGet());
       Assertions.assertEquals(key1.remainTimeToLive(),-1);

    }

    @Test
    public void testDistributeBizIDGeneratorWithReset() {
        String bizId = "";
        for(int i=0;i<10;i++) {
            bizId = bizIDGenerator2.nextId(LINER_BIZ_TYPE_5);
        }
        System.out.println(bizId);
        DatePatternParser datePattenParser = new DatePatternParser();
        LocalDateTime localDateTime = LocalDateTime.now().plusHours(1);
        DatePatternParser spy = Mockito.spy(datePattenParser);
        doReturn(localDateTime).when(spy).getLocalDate();
        manager.unRegist(DATE_PATTEN_PARSER);
        manager.registVariableParser(spy);
        bizId = bizIDGenerator2.nextId(LINER_BIZ_TYPE_5);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH");
        String dest = localDateTime.format(formatter) + ":0001";
        Assertions.assertEquals(dest,bizId);

    }

    private DataSource buildDataSource(String file) throws SQLException {
        System.setProperty(DataSourceFactory.CONFIG_FILE, file);
        dataSourcePackage = DataSourceFactory.build(true);
        return dataSourcePackage.getMaster().get(0);
    }

    @Test
    public void testMutliThreadCount() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch closeLatch = new CountDownLatch(10);
        for (int j = 0; j < 10; j++) {
            executorService.submit(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < 50; i++) {
                    System.out.println(bizIDGenerator1.nextId(LINEAR_BIZ_TYPE));
                }
                closeLatch.countDown();
            });
        }
        System.out.println("prepare execute nextID.....");
        latch.countDown();
        closeLatch.await();
        String bizID = bizIDGenerator1.nextId(LINEAR_BIZ_TYPE);
        System.out.println("last bizID : " + bizID);
        LocalDateTime localDateTime = LocalDateTime.now();
        String date = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Assertions.assertEquals(date + ":00501", bizID);
    }


    @Test
    public void testMutliThreadOver() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch closeLatch = new CountDownLatch(70);
        List<Future> futures = Lists.newArrayList();
        for (int j = 0; j < 70; j++) {
            Future future = executorService.submit(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < 10; i++) {

                    System.out.println(bizIDGenerator3.nextId(LINEAR_BIZ_TYPE_3));
                }
                closeLatch.countDown();
            });
            futures.add(future);
        }
        System.out.println("prepare execute nextID.....");
        latch.countDown();
        closeLatch.await();
        String bizID = bizIDGenerator3.nextId(LINEAR_BIZ_TYPE_3);
        System.out.println("last bizID : " + bizID);
        LocalDateTime localDateTime = LocalDateTime.now();
        String date = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Assertions.assertEquals(date + ":00701", bizID);
    }

    @Test
    public void testResetIDGenerator() throws SQLException {

        PatternParserManager manager = new PatternParserManager();
        NumberPatternParser parser = new NumberPatternParser();
        DatePatternParser datePattenParser = new DatePatternParser();
        manager.registVariableParser(parser);
        manager.registVariableParser(datePattenParser);
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(PatternParserManager.class)).thenReturn(manager);

        ReflectionTestUtils.setField(PatternParserUtil.class, "applicationContext", applicationContext);
        String bizId = "";
        for (int i = 0; i < 3; i++) {
            if (i == 2) {
                LocalDateTime localDateTime = LocalDateTime.now().plusDays(1);
                DatePatternParser spy = Mockito.spy(datePattenParser);
                doReturn(localDateTime).when(spy).getLocalDate();
                manager.unRegist(DATE_PATTEN_PARSER);
                manager.registVariableParser(spy);
            }
            bizId = bizIDGenerator1.nextId(LINER_BIZ_TYPE_4);
        }
        System.out.println(bizId);
        Assertions
            .assertEquals(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ":00001",
                bizId);
    }

}
