package com.xforceplus.ultraman.oqsengine.idgenerator.client;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.PatternValue;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentId;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import com.xforceplus.ultraman.oqsengine.idgenerator.generator.IDGeneratorFactoryImpl;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParserManager;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParserUtil;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl.DatePatternParser;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl.NumberPatternParser;
import com.xforceplus.ultraman.oqsengine.idgenerator.service.SegmentService;
import com.xforceplus.ultraman.oqsengine.idgenerator.service.impl.SegmentServiceImpl;
import com.xforceplus.ultraman.oqsengine.idgenerator.storage.SqlSegmentStorage;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerRunner;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerType;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.DependentContainers;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.sql.DataSource;
import org.apache.commons.compress.utils.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * .
 *
 * @author leo
 * @version 0.1 5/21/21 3:51 PM
 * @since 1.8
 */
@RunWith(ContainerRunner.class)
@DependentContainers({ContainerType.REDIS,ContainerType.MYSQL})
public class BizIDGeneratorRedisTest {

    private ApplicationContext applicationContext;


    private IDGeneratorFactoryImpl idGeneratorFactory1;
    private SegmentService segmentService1;
    private SqlSegmentStorage storage1;
    private BizIDGenerator bizIDGenerator1;
    private static final String linearBizType = "bizLinear";
    private RedissonClient redissonClient;
    private DataSource dataSource;
    private ExecutorService executorService;


    @Before
    public void before() throws SQLException {

        executorService = Executors.newFixedThreadPool(30);

        dataSource = buildDataSource("./src/test/resources/generator.conf");

        storage1 = new SqlSegmentStorage();
        storage1.setTable("segment");
        storage1.init();
        ReflectionTestUtils.setField(storage1,"dataSource",dataSource);

        Config config = new Config();
        String redisIp = System.getProperty("REDIS_HOST");
        int redisPort = Integer.parseInt(System.getProperty("REDIS_PORT"));
        config.useSingleServer().setAddress(String.format("redis://%s:%s",redisIp,redisPort));
        redissonClient = Redisson.create(config);

        PatternParserManager manager = new PatternParserManager();
        NumberPatternParser parser = new NumberPatternParser();
        DatePatternParser datePattenParser = new DatePatternParser();
        manager.registVariableParser(parser);
        manager.registVariableParser(datePattenParser);
        applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(PatternParserManager.class)).thenReturn(manager);
        ReflectionTestUtils.setField(PatternParserUtil.class,"applicationContext",applicationContext);

        this.segmentService1 = new SegmentServiceImpl();
        this.idGeneratorFactory1 = new IDGeneratorFactoryImpl();
        this.bizIDGenerator1 = new BizIDGenerator();
        ReflectionTestUtils.setField(segmentService1, "sqlSegmentStorage", storage1);
        ReflectionTestUtils.setField(idGeneratorFactory1, "segmentService", segmentService1);
        ReflectionTestUtils.setField(idGeneratorFactory1, "redissonClient", redissonClient);
        ReflectionTestUtils.setField(bizIDGenerator1, "idGeneratorFactory", idGeneratorFactory1);

        SegmentInfo info = SegmentInfo.builder().withBeginId(1l).withBizType(linearBizType)
            .withCreateTime(new Timestamp(System.currentTimeMillis()))
            .withMaxId(0L).withPatten("{yyyy}-{MM}-{dd}:{00000}").withMode(2).withStep(1000)
            .withUpdateTime(new Timestamp(System.currentTimeMillis()))
            .withVersion(1l)
            .withResetable(0)
            .withPatternKey("")
            .build();
       int ret =  storage1.build(info);
       Assert.assertEquals(ret,1);
    }

    @Test
    public void testRedisson() {
        SegmentId value = new SegmentId();
        value.setResetable(1);
        value.setPattern("1");
        value.setCurrentId(new PatternValue(1,"1"));
        value.setLoadingId(1L);
        value.setMaxId(1000l);
        RBucket<SegmentId> al = redissonClient.getBucket("testBiz");
        al.set(value);
        SegmentId next = value.clone();
        next.nextId();
        Assert.assertEquals(true,al.compareAndSet(value,next ));
    }

    @Test
    public void testDistributeBizIDGenerator() throws InterruptedException {
        String bizId = "";
        for (int i = 0; i < 10; i++) {
            bizId = bizIDGenerator1.nextId(linearBizType);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String expected = LocalDateTime.now().format(formatter) + ":00010";
        Assert.assertEquals(expected, bizId);
        for (int i = 0; i < 100; i++) {
            bizId = bizIDGenerator1.nextId(linearBizType);
            System.out.println(bizId);
        }
        String expected1 = LocalDateTime.now().format(formatter) + ":00110";
        Assert.assertEquals(expected1, bizId);
        bizId = bizIDGenerator1.nextId(linearBizType);
        String expected2 =  LocalDateTime.now().format(formatter) + ":00111";
        Assert.assertEquals(expected2, bizId);

    }

    private DataSource buildDataSource(String file) throws SQLException {
        System.setProperty(DataSourceFactory.CONFIG_FILE, file);
        DataSourcePackage dataSourcePackage = DataSourceFactory.build(true);
        return dataSourcePackage.getMaster().get(0);
    }

    @Test
    public void testMutliThreadCount() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);
        for(int j =0;j<10;j++) {
            executorService.submit(() -> {
                for (int i = 0; i < 50; i++) {
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(bizIDGenerator1.nextId(linearBizType));
                }
            });
        }
        System.out.println("prepare execute nextID.....");
        latch.countDown();
        Thread.sleep(3000);
        String bizID =  bizIDGenerator1.nextId(linearBizType);
        System.out.println("last bizID : " + bizID);
        LocalDateTime localDateTime = LocalDateTime.now();
       String date =  localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
       Assert.assertEquals(date+":00501",bizID);
    }


    @Test
    public void testMutliThreadOver() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);
        List<Future> futures = Lists.newArrayList();
        for(int j =0;j<10;j++) {
           Future future =  executorService.submit(() -> {
                for (int i = 0; i < 300; i++) {
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(bizIDGenerator1.nextId(linearBizType));
                }
            });
           futures.add(future);
        }
        System.out.println("prepare execute nextID.....");
        long start = System.currentTimeMillis();
        latch.countDown();
        Thread.sleep(10000);
        String bizID =  bizIDGenerator1.nextId(linearBizType);
        System.out.println("last bizID : " + bizID);
        LocalDateTime localDateTime = LocalDateTime.now();
        String date =  localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Assert.assertEquals(date+":03001",bizID);
    }

}
