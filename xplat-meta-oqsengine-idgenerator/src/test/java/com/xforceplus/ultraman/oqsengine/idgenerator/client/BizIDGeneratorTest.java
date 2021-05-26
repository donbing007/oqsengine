package com.xforceplus.ultraman.oqsengine.idgenerator.client;

import static com.xforceplus.ultraman.oqsengine.idgenerator.common.constant.Constants.DATE_PATTEN_PARSER;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import com.xforceplus.ultraman.oqsengine.idgenerator.generator.IDGeneratorFactoryImpl;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PattenParserManager;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParserUtil;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl.DatePattenParser;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl.NumberPattenParser;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sql.DataSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 5/9/21 11:57 PM
 */
@RunWith(ContainerRunner.class)
@DependentContainers({ContainerType.MYSQL})
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

    private ExecutorService executorService;
    private DataSource dataSource;



    @Before
    public void before() throws SQLException {

        executorService = Executors.newFixedThreadPool(30);

        dataSource = buildDataSource("./src/test/resources/generator.conf");

        storage1 = new SqlSegmentStorage();
        storage1.setTable("segment");
        storage1.init();
        ReflectionTestUtils.setField(storage1,"dataSource",dataSource);

        this.segmentService1 = new SegmentServiceImpl();
        this.idGeneratorFactory1 = new IDGeneratorFactoryImpl();
        this.bizIDGenerator1 = new BizIDGenerator();
        ReflectionTestUtils.setField(segmentService1, "sqlSegmentStorage", storage1);
        ReflectionTestUtils.setField(idGeneratorFactory1, "segmentService", segmentService1);
        ReflectionTestUtils.setField(bizIDGenerator1, "idGeneratorFactory", idGeneratorFactory1);

        SegmentInfo info = SegmentInfo.builder().withBeginId(1l).withBizType(bizType)
            .withCreateTime(new Timestamp(System.currentTimeMillis()))
            .withMaxId(0L).withPatten("{yyyy}-{MM}-{dd}:{00000}").withMode(1).withStep(1000)
            .withUpdateTime(new Timestamp(System.currentTimeMillis()))
            .withVersion(1l)
            .withResetable(1)
            .withPatternKey("")
            .build();
        int ret =  storage1.build(info);
        Assert.assertEquals(ret,1);
    }

    @Test
    public void testBizIDGenerator() {
        String bizId = "";
        for (int i = 0; i < 10; i++) {
            bizId = bizIDGenerator1.nextId(bizType);
            System.out.println(bizId);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String expected = LocalDateTime.now().format(formatter) + ":0010";
        Assert.assertEquals(bizId, expected);
        for (int i = 0; i < 1000; i++) {
            bizId = bizIDGenerator1.nextId(bizType);
            System.out.println(bizId);
        }
        String expected1 = LocalDateTime.now().format(formatter) + ":1010";
        Assert.assertEquals(expected1, bizId);
    }

    @Test
    public void testResetIDGenerator() throws SQLException {
        PattenParserManager manager = new PattenParserManager();
        NumberPattenParser parser = new NumberPattenParser();
        DatePattenParser datePattenParser = new DatePattenParser();
        manager.registVariableParser(parser);
        manager.registVariableParser(datePattenParser);
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(PattenParserManager.class)).thenReturn(manager);
        ReflectionTestUtils.setField(PatternParserUtil.class,"applicationContext",applicationContext);
        String bizId = "";
        for (int i = 0; i < 3; i++) {
            if(i == 2) {
                LocalDateTime localDateTime = LocalDateTime.now().plusDays(1);
                DatePattenParser spy = Mockito.spy(datePattenParser);
                doReturn(localDateTime.toLocalDate()).when(spy).getLocalDate();
                manager.unRegist(DATE_PATTEN_PARSER);
                manager.registVariableParser(spy);
            }
            bizId = bizIDGenerator1.nextId(bizType);
        }
        System.out.println(bizId);
        Assert.assertEquals(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))+":00001",bizId);
    }


    private DataSource buildDataSource(String file) throws SQLException {
        System.setProperty(DataSourceFactory.CONFIG_FILE, file);
        DataSourcePackage dataSourcePackage = DataSourceFactory.build(true);
        return dataSourcePackage.getMaster().get(0);
    }


}

