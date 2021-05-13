package com.xforceplus.ultraman.oqsengine.idgenerator.client;

import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import com.xforceplus.ultraman.oqsengine.idgenerator.generator.IDGeneratorFactoryImpl;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PattenParserManager;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PattenParserUtil;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl.DatePattenParser;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl.NumberPattenParser;
import com.xforceplus.ultraman.oqsengine.idgenerator.service.SegmentService;
import com.xforceplus.ultraman.oqsengine.idgenerator.service.impl.SegmentServiceImpl;
import com.xforceplus.ultraman.oqsengine.idgenerator.storage.SqlSegmentStorage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 5/9/21 11:57 PM
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(PattenParserUtil.class)
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
                .withBeginId(1l).withBizType(bizType).withMaxId(2000l).withMode(1)
                .withPatten("YYYY-MM-DD:{0000}").withStep(1000).withCreateTime(new Timestamp(System.currentTimeMillis()))
                .withUpdateTime(new Timestamp(System.currentTimeMillis())).withVersion(1l).build();
        when(storage.query(any())).thenReturn(Optional.of(segmentInfo));
        when(storage.udpate(any())).thenReturn(1);

        this.segmentService = new SegmentServiceImpl();
        this.idGeneratorFactory = new IDGeneratorFactoryImpl();
        this.bizIDGenerator = new BizIDGenerator();
        ReflectionTestUtils.setField(segmentService, "sqlSegmentStorage",storage);
        ReflectionTestUtils.setField(idGeneratorFactory, "segmentService",segmentService);
        ReflectionTestUtils.setField(bizIDGenerator, "idGeneratorFactory",idGeneratorFactory);



        storage1 = mock(SqlSegmentStorage.class);
        SegmentInfo segmentInfo1 = SegmentInfo.builder().withId(2l)
                .withBeginId(1l).withBizType(linearBizType).withMaxId(2000l).withMode(2)
                .withPatten("YYYY-MM-DD:{0000}").withStep(1000).withCreateTime(new Timestamp(System.currentTimeMillis()))
                .withUpdateTime(new Timestamp(System.currentTimeMillis())).withVersion(1l).build();
        when(storage1.query(any())).thenReturn(Optional.of(segmentInfo1));
        when(storage1.udpate(any())).thenReturn(1);

        this.segmentService1 = new SegmentServiceImpl();
        this.idGeneratorFactory1 = new IDGeneratorFactoryImpl();
        this.bizIDGenerator1 = new BizIDGenerator();
        ReflectionTestUtils.setField(segmentService1, "sqlSegmentStorage",storage1);
        ReflectionTestUtils.setField(idGeneratorFactory1, "segmentService",segmentService1);
        ReflectionTestUtils.setField(bizIDGenerator1, "idGeneratorFactory",idGeneratorFactory1);

        PattenParserManager manager = new PattenParserManager();
        NumberPattenParser parser = new NumberPattenParser();
        DatePattenParser datePattenParser = new DatePattenParser();
        manager.registVariableParser(parser);
        manager.registVariableParser(datePattenParser);
        PowerMockito.mockStatic(PattenParserUtil.class);
        when(PattenParserUtil.getInstance()).thenReturn(manager);



    }

    @Test
    public void testBizIDGenerator() {
        String bizId = "";
        for(int i=0;i<10;i++) {
             bizId = bizIDGenerator.nextId(bizType);
             System.out.println(bizId);
        }
        Assert.assertEquals(bizId,"YYYY-MM-DD:2010");
        for(int i=0; i<1000; i++) {
            bizId = bizIDGenerator.nextId(bizType);
            System.out.println(bizId);
        }
        Assert.assertEquals("YYYY-MM-DD:3010",bizId);
    }

    @Test
    public void testDistributeBizIDGenerator() throws InterruptedException {
        String bizId = "";
        for(int i=0;i<10;i++) {
            bizId = bizIDGenerator1.nextId(linearBizType);
        }
        Assert.assertEquals(bizId,"YYYY-MM-DD:2010");
        for(int i=0; i<100; i++) {
            bizId = bizIDGenerator1.nextId(linearBizType);
            System.out.println(bizId);
        }
        Assert.assertEquals("YYYY-MM-DD:2110",bizId);
    }
}
