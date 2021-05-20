package com.xforceplus.ultraman.oqsengine.idgenerator.service.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentId;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PattenParserManager;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParserUtil;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl.DatePattenParser;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl.NumberPattenParser;
import com.xforceplus.ultraman.oqsengine.idgenerator.storage.SqlSegmentStorage;
import com.xforceplus.ultraman.oqsengine.idgenerator.transaction.SegmentTransactionResourceFactory;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.status.impl.CommitIdStatusServiceImpl;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoJoinTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.MasterDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.DoNothingCacheEventHandler;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerRunner;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerType;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.DependentContainers;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * .
 *
 * @author leo
 * @version 0.1 5/20/21 11:26 AM
 * @since 1.8
 */

@RunWith(ContainerRunner.class)
@DependentContainers({ContainerType.MYSQL})
@PrepareForTest(PatternParserUtil.class)
public class SegmentServiceImplTest {

    private TransactionManager transactionManager;
    private CommitIdStatusServiceImpl commitIdStatusService;
    private DataSource dataSource;
    private SqlSegmentStorage storage;
    private SegmentServiceImpl service;
    private PattenParserManager manager;
    ApplicationContext applicationContext;

    @Before
    @PrepareForTest(PatternParserUtil.class)
    public void before() throws Exception {
        dataSource = buildDataSource("./src/test/resources/generator.conf");
        // 等待加载完毕
        TimeUnit.SECONDS.sleep(1L);
        transactionManager = DefaultTransactionManager.Builder.anDefaultTransactionManager()
            .withTxIdGenerator(new IncreasingOrderLongIdGenerator(0))
            .withCommitIdGenerator(new IncreasingOrderLongIdGenerator(0))
            .withCommitIdStatusService(commitIdStatusService)
            .withCacheEventHandler(new DoNothingCacheEventHandler())
            .withWaitCommitSync(false)
            .build();
        TransactionExecutor executor = new AutoJoinTransactionExecutor(
            transactionManager, new SegmentTransactionResourceFactory("segment"),
            new NoSelector<>(dataSource), new NoSelector<>("segment"));


        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new MasterDecimalStorageStrategy());


        storage = new SqlSegmentStorage();
        ReflectionTestUtils.setField(storage, "transactionExecutor", executor);
        storage.setTable("segment");
        storage.setQueryTimeout(100000000);
        storage.init();

        service = new SegmentServiceImpl();
        ReflectionTestUtils.setField(service,"sqlSegmentStorage",storage);

        SegmentInfo info = SegmentInfo.builder().withBeginId(1l).withBizType("testBiz")
            .withCreateTime(new Timestamp(System.currentTimeMillis()))
            .withMaxId(0l).withPatten("{yyyy}-{MM}-{dd}-{000}").withMode(2).withStep(1000)
            .withUpdateTime(new Timestamp(System.currentTimeMillis()))
            .withVersion(1l)
            .withResetable(0)
            .withPatternKey("")
            .build();
        storage.build(info);
        manager = new PattenParserManager();
        PattenParserManager manager = new PattenParserManager();
        NumberPattenParser parser = new NumberPattenParser();
        DatePattenParser datePattenParser = new DatePattenParser();
        manager.registVariableParser(parser);
        manager.registVariableParser(datePattenParser);
        applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(PattenParserManager.class)).thenReturn(manager);
        ReflectionTestUtils.setField(PatternParserUtil.class,"applicationContext",applicationContext);

    }

    @Test
    public void testGetNextSegmentId() throws SQLException {
        LocalDateTime localDateTime = LocalDateTime.now();
        String ext = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
     SegmentId segmentId =  service.getNextSegmentId("testBiz");
     String actual = segmentId.getCurrentId().getValue();
     Assert.assertEquals(actual,ext+"-000");
    }


    private DataSource buildDataSource(String file) throws SQLException {
        System.setProperty(DataSourceFactory.CONFIG_FILE, file);
        DataSourcePackage dataSourcePackage = DataSourceFactory.build(true);
        return dataSourcePackage.getMaster().get(0);
    }
}
