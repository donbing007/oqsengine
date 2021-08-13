package com.xforceplus.ultraman.oqsengine.idgenerator.client;

import static com.xforceplus.ultraman.oqsengine.idgenerator.common.constant.Constants.DATE_PATTEN_PARSER;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alibaba.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import com.xforceplus.ultraman.oqsengine.idgenerator.generator.IDGeneratorFactoryImpl;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParserManager;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParserUtil;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl.DatePatternParser;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl.NumberPatternParser;
import com.xforceplus.ultraman.oqsengine.idgenerator.service.SegmentService;
import com.xforceplus.ultraman.oqsengine.idgenerator.service.impl.SegmentServiceImpl;
import com.xforceplus.ultraman.oqsengine.idgenerator.storage.SqlSegmentStorage;
import com.xforceplus.ultraman.test.tools.core.container.basic.MysqlContainer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.sql.DataSource;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 5/9/21 11:57 PM
 */

@ExtendWith(MysqlContainer.class)
public class BizIDGeneratorTest {

    private static final String BIZ_TEST = "bizTest";
    private static final String BIZ_TYPE_1 = "bizTest1";
    private static final String LINEAR_BIZ_TYPE_3 = "bizLinear3";
    private IDGeneratorFactoryImpl idGeneratorFactory1;
    private SegmentService segmentService1;
    private SqlSegmentStorage storage1;
    private BizIDGenerator bizIDGenerator1;
    private BizIDGenerator bizIDGenerator3;
    private ExecutorService executorService;
    private DataSource dataSource;
    private ApplicationContext applicationContext;
    private DataSourcePackage dataSourcePackage;
    private static final Logger LOGGER = LoggerFactory.getLogger(BizIDGeneratorTest.class);

    /**
     * 测试初始化.
     */
    @BeforeEach
    public void before() throws SQLException {
        System.setProperty(
            "MYSQL_JDBC_ID",
            String.format(
                "jdbc:mysql://%s:%s/oqsengine?useUnicode=true&serverTimezone=GMT&useSSL=false&characterEncoding=utf8",
                System.getProperty("MYSQL_HOST"), System.getProperty("MYSQL_PORT")));

        executorService = Executors.newFixedThreadPool(30);

        dataSource = buildDataSource("./src/test/resources/generator.conf");

        storage1 = new SqlSegmentStorage();
        storage1.setTable("segment");
        storage1.init();
        ReflectionTestUtils.setField(storage1, "dataSource", dataSource);

        this.segmentService1 = new SegmentServiceImpl();
        this.idGeneratorFactory1 = new IDGeneratorFactoryImpl();
        this.bizIDGenerator1 = new BizIDGenerator();
        this.bizIDGenerator3 = new BizIDGenerator();
        ReflectionTestUtils.setField(segmentService1, "sqlSegmentStorage", storage1);
        ReflectionTestUtils.setField(idGeneratorFactory1, "segmentService", segmentService1);
        ReflectionTestUtils.setField(bizIDGenerator1, "idGeneratorFactory", idGeneratorFactory1);
        ReflectionTestUtils.setField(bizIDGenerator3, "idGeneratorFactory", idGeneratorFactory1);

        PatternParserManager manager = new PatternParserManager();
        NumberPatternParser parser = new NumberPatternParser();
        DatePatternParser datePattenParser = new DatePatternParser();
        manager.registVariableParser(parser);
        manager.registVariableParser(datePattenParser);
        applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(PatternParserManager.class)).thenReturn(manager);
        ReflectionTestUtils.setField(PatternParserUtil.class, "applicationContext", applicationContext);


        SegmentInfo info = SegmentInfo.builder().withBeginId(1L).withBizType(BIZ_TEST)
            .withCreateTime(new Timestamp(System.currentTimeMillis()))
            .withMaxId(0L).withPatten("{yyyy}-{MM}-{dd}:{00000}").withMode(1).withStep(1000)
            .withUpdateTime(new Timestamp(System.currentTimeMillis()))
            .withVersion(1L)
            .withResetable(1)
            .withPatternKey("")
            .build();
        int ret = storage1.build(info);
        Assertions.assertEquals(ret, 1);

        SegmentInfo info1 = SegmentInfo.builder().withBeginId(1L).withBizType(BIZ_TYPE_1)
            .withCreateTime(new Timestamp(System.currentTimeMillis()))
            .withMaxId(0L).withPatten("{yyyy}{MM}{dd}-{00000}").withMode(1).withStep(1000)
            .withUpdateTime(new Timestamp(System.currentTimeMillis()))
            .withVersion(1L)
            .withResetable(1)
            .withPatternKey("")
            .build();
        int ret1 = storage1.build(info1);
        Assertions.assertEquals(ret1, 1);

        SegmentInfo info3 = SegmentInfo.builder().withBeginId(1l).withBizType(LINEAR_BIZ_TYPE_3)
            .withCreateTime(new Timestamp(System.currentTimeMillis()))
            .withMaxId(0L).withPatten("{yyyy}-{MM}-{dd}:{00000}").withMode(1).withStep(100)
            .withUpdateTime(new Timestamp(System.currentTimeMillis()))
            .withVersion(1l)
            .withResetable(0)
            .withPatternKey("")
            .build();
        int ret3 = storage1.build(info3);
        Assertions.assertEquals(ret3, 1);
    }

    /**
     * 测试清理.
     */
    @AfterEach
    public void after() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            Statement st = conn.createStatement();
            st.executeUpdate("truncate table segment");
            st.close();
        } finally {
            dataSourcePackage.close();
        }
    }


    @Test
    public void testBizIDGenerator() {
        PatternParserManager manager = new PatternParserManager();
        NumberPatternParser parser = new NumberPatternParser();
        DatePatternParser datePattenParser = new DatePatternParser();
        manager.registVariableParser(parser);
        manager.registVariableParser(datePattenParser);
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(PatternParserManager.class)).thenReturn(manager);
        ReflectionTestUtils.setField(PatternParserUtil.class, "applicationContext", applicationContext);
        String bizId = "";
        for (int i = 0; i < 10; i++) {
            bizId = bizIDGenerator1.nextId(BIZ_TEST);
            System.out.println(bizId);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String expected = LocalDateTime.now().format(formatter) + ":00010";
        Assertions.assertEquals(bizId, expected);
        for (int i = 0; i < 1000; i++) {
            bizId = bizIDGenerator1.nextId(BIZ_TEST);
            System.out.println(bizId);
        }
        String expected1 = LocalDateTime.now().format(formatter) + ":01010";
        Assertions.assertEquals(expected1, bizId);
    }

    @Test
    public void testMutliThreadOver() throws InterruptedException {
        Set<String> result = Sets.newConcurrentHashSet();
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch closeLatch = new CountDownLatch(50);
        for (int j = 0; j < 50; j++) {
            executorService.submit(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < 100; i++) {

                    result.add(bizIDGenerator3.nextId(LINEAR_BIZ_TYPE_3));
                }
                closeLatch.countDown();
            });
        }
        System.out.println("prepare execute nextID.....");
        latch.countDown();
        closeLatch.await();
        result.add(bizIDGenerator3.nextId(LINEAR_BIZ_TYPE_3));
        Assert.assertEquals(5001, result.size());
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
            bizId = bizIDGenerator1.nextId(BIZ_TEST);
        }
        System.out.println(bizId);
        Assertions
            .assertEquals(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ":00001",
                bizId);
    }


    @Test
    public void testResetIDGeneratorAnotherPattern() throws SQLException {

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
            bizId = bizIDGenerator1.nextId(BIZ_TYPE_1);
        }
        Assertions
            .assertEquals(
                LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-00001",
                bizId);
    }


    private DataSource buildDataSource(String file) throws SQLException {
        System.setProperty(DataSourceFactory.CONFIG_FILE, file);
        dataSourcePackage = DataSourceFactory.build(true);
        return dataSourcePackage.getMaster().get(0);
    }
}

