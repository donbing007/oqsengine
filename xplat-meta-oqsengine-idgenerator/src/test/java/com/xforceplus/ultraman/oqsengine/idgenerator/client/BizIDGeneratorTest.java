package com.xforceplus.ultraman.oqsengine.idgenerator.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.booleanThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.longThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.PatternValue;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentId;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import com.xforceplus.ultraman.oqsengine.idgenerator.generator.IDGeneratorFactoryImpl;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PattenParserManager;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParserUtil;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl.DatePattenParser;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl.NumberPattenParser;
import com.xforceplus.ultraman.oqsengine.idgenerator.service.SegmentService;
import com.xforceplus.ultraman.oqsengine.idgenerator.service.impl.SegmentServiceImpl;
import com.xforceplus.ultraman.oqsengine.idgenerator.storage.SqlSegmentStorage;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 5/9/21 11:57 PM
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(PatternParserUtil.class)
public class BizIDGeneratorTest {

    private IDGeneratorFactoryImpl idGeneratorFactory;
    private SegmentService segmentService;
    private SqlSegmentStorage storage;
    private BizIDGenerator bizIDGenerator;
    private static final String bizType = "bizTest";


    private IDGeneratorFactoryImpl idGeneratorFactory1;
    private SegmentService segmentService1;
    private SqlSegmentStorage storage1;
    private BizIDGenerator bizIDGenerator1;
    private static final String linearBizType = "bizLinear";


    @Before
    public void before() throws SQLException {
        storage = mock(SqlSegmentStorage.class);
        SegmentInfo segmentInfo = SegmentInfo.builder().withId(1l)
            .withBeginId(1l).withBizType(bizType).withMaxId(0l).withMode(1)
            .withPatten("{yyyy}-{MM}-{dd}:{0000}").withStep(1000).withResetable(0)
            .withPatternKey("").
                withCreateTime(new Timestamp(System.currentTimeMillis()))
            .withUpdateTime(new Timestamp(System.currentTimeMillis())).withVersion(1l).build();
        when(storage.query(any())).thenReturn(Optional.of(segmentInfo));
        when(storage.udpate(any())).thenReturn(1);

        Config config = new Config();
        config.useSingleServer().setAddress("redis://localhost:6379/16");
        RedissonClient redissonClient = Redisson.create(config);
        this.segmentService = new SegmentServiceImpl();
        this.idGeneratorFactory = new IDGeneratorFactoryImpl();
        this.bizIDGenerator = new BizIDGenerator();
        ReflectionTestUtils.setField(segmentService, "sqlSegmentStorage", storage);
        ReflectionTestUtils.setField(idGeneratorFactory, "segmentService", segmentService);
        ReflectionTestUtils.setField(bizIDGenerator, "idGeneratorFactory", idGeneratorFactory);


        storage1 = mock(SqlSegmentStorage.class);
        SegmentInfo segmentInfo1 = SegmentInfo.builder().withId(2l)
            .withBeginId(1l).withBizType(linearBizType).withMaxId(2000l).withMode(2)
            .withPatternKey("").withResetable(0)
            .withPatten("{yyyy}-{MM}-{dd}:{0000}").withStep(1000)
            .withCreateTime(new Timestamp(System.currentTimeMillis()))
            .withUpdateTime(new Timestamp(System.currentTimeMillis())).withVersion(1l).build();
        when(storage1.query(any())).thenReturn(Optional.of(segmentInfo1));
        when(storage1.udpate(any())).thenReturn(1);

        this.segmentService1 = new SegmentServiceImpl();
        this.idGeneratorFactory1 = new IDGeneratorFactoryImpl();
        this.bizIDGenerator1 = new BizIDGenerator();
        ReflectionTestUtils.setField(segmentService1, "sqlSegmentStorage", storage1);
        ReflectionTestUtils.setField(idGeneratorFactory1, "segmentService", segmentService1);
        ReflectionTestUtils.setField(idGeneratorFactory1, "redissonClient", redissonClient);
        ReflectionTestUtils.setField(bizIDGenerator1, "idGeneratorFactory", idGeneratorFactory1);
        PattenParserManager manager = new PattenParserManager();
        NumberPattenParser parser = new NumberPattenParser();
        DatePattenParser datePattenParser = new DatePattenParser();
        manager.registVariableParser(parser);
        manager.registVariableParser(datePattenParser);
        PowerMockito.mockStatic(PatternParserUtil.class);
        when(PatternParserUtil.getInstance()).thenReturn(manager);


    }

    @Test
    public void testBizIDGenerator() {
        String bizId = "";
        for (int i = 0; i < 10; i++) {
            bizId = bizIDGenerator.nextId(bizType);
            System.out.println(bizId);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String expected = LocalDateTime.now().format(formatter) + ":0010";
        Assert.assertEquals(bizId, expected);
        for (int i = 0; i < 1000; i++) {
            bizId = bizIDGenerator.nextId(bizType);
            System.out.println(bizId);
        }
        String expected1 = LocalDateTime.now().format(formatter) + ":1010";
        Assert.assertEquals(expected1, bizId);
    }

    @Test
    public void testResetIDGenerator() throws SQLException {
        PattenParserManager manager = new PattenParserManager();
        NumberPattenParser parser = new NumberPattenParser();
        DatePattenParser datePattenParser = mock(DatePattenParser.class);
        when(datePattenParser.parse(anyString(), eq(0L))).thenReturn("2020-01-01:0000");
        when(datePattenParser.parse(anyString(), eq(1L))).thenReturn("2020-01-01:0001");
        when(datePattenParser.parse(anyString(), eq(2L))).thenReturn("2020-01-01:0002");
        when(datePattenParser.parse(anyString(), eq(3L))).thenReturn("2020-01-02:0003");
        when(datePattenParser.parse(anyString(), eq(4L))).thenReturn("2020-01-01:0002");
        when(datePattenParser.getName()).thenReturn("demo-date-parser");
        when(datePattenParser.needHandle(anyString())).thenReturn(true);
        manager.registVariableParser(parser);
        manager.registVariableParser(datePattenParser);
        PowerMockito.mockStatic(PatternParserUtil.class);
        when(PatternParserUtil.getInstance()).thenReturn(manager);

        String bizId = "";
        for (int i = 0; i < 3; i++) {
            if (i == 2) {
                when(PatternParserUtil.needReset(anyString(), any(PatternValue.class), any(PatternValue.class)))
                    .thenReturn(true).thenReturn(false);
                SegmentInfo segmentInfo = SegmentInfo.builder().withId(1l)
                    .withBeginId(1l).withBizType(bizType).withMaxId(0l).withMode(1)
                    .withPatten("{yyyy}-{MM}-{dd}:{0000}").withStep(1000).withResetable(0)
                    .withPatternKey("2020-01-02").
                        withCreateTime(new Timestamp(System.currentTimeMillis()))
                    .withUpdateTime(new Timestamp(System.currentTimeMillis())).withVersion(1l).build();
                when(storage.query(any())).thenReturn(Optional.of(segmentInfo));
            } else {
                when(PatternParserUtil.needReset(anyString(), any(PatternValue.class), any(PatternValue.class)))
                    .thenReturn(false);
            }
            bizId = bizIDGenerator.nextId(bizType);
            if (i == 0) {
                Assert.assertEquals("2020-01-01:0001", bizId);
            }
            if (i == 1) {
                Assert.assertEquals("2020-01-01:0002", bizId);
            }
            if (i == 2) {
                Assert.assertEquals("2020-01-01:0001", bizId);
            }
            System.out.println(bizId);
        }
    }




}

