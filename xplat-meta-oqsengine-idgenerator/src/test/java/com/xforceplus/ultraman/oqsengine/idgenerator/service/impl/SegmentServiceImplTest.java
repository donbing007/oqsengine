package com.xforceplus.ultraman.oqsengine.idgenerator.service.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentId;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParserManager;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParserUtil;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl.DatePatternParser;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl.NumberPatternParser;
import com.xforceplus.ultraman.oqsengine.idgenerator.storage.SqlSegmentStorage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.status.impl.CommitIdStatusServiceImpl;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoJoinTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.MasterDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.transaction.SqlConnectionTransactionResourceFactory;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.DoNothingCacheEventHandler;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.test.tools.core.container.basic.MysqlContainer;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.sql.DataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * .
 *
 * @author leo
 * @version 0.1 5/20/21 11:26 AM
 * @since 1.8
 */

@ExtendWith({MysqlContainer.class, SpringExtension.class})
@SpringBootTest
public class SegmentServiceImplTest {

    private TransactionManager transactionManager;
    private CommitIdStatusServiceImpl commitIdStatusService;
    private DataSource dataSource;
    private SqlSegmentStorage storage;
    private SegmentServiceImpl service;
    private PatternParserManager manager;

    @Resource
    ApplicationContext applicationContext;

    @Resource
    private PatternParserUtil patternParserUtil;

    @BeforeEach
    public void before() throws Exception {
        System.setProperty(
            "MYSQL_JDBC_URL_IDGEN",
            String.format(
                "jdbc:mysql://%s:%s/oqsengine?useUnicode=true&serverTimezone=GMT&useSSL=false&characterEncoding=utf8",
                System.getProperty("MYSQL_HOST"), System.getProperty("MYSQL_PORT")));

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
            transactionManager, new SqlConnectionTransactionResourceFactory("segment"),
            new NoSelector<>(dataSource), new NoSelector<>("segment"));


        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new MasterDecimalStorageStrategy());


        storage = new SqlSegmentStorage();
        storage.setTable("segment");
        storage.setQueryTimeout(100000000);
        storage.init();

        service = new SegmentServiceImpl();
        ReflectionTestUtils.setField(service, "sqlSegmentStorage", storage);
        ReflectionTestUtils.setField(storage, "dataSource", dataSource);

        SegmentInfo info = SegmentInfo.builder().withBeginId(1l).withBizType("testBiz")
            .withCreateTime(new Timestamp(System.currentTimeMillis()))
            .withMaxId(0l).withPatten("{yyyy}-{MM}-{dd}-{000}").withMode(2).withStep(1000)
            .withUpdateTime(new Timestamp(System.currentTimeMillis()))
            .withVersion(1l)
            .withResetable(0)
            .withPatternKey("")
            .build();
        storage.build(info);
        manager = new PatternParserManager();
        PatternParserManager manager = new PatternParserManager();
        NumberPatternParser parser = new NumberPatternParser();
        DatePatternParser datePattenParser = new DatePatternParser();
        manager.registVariableParser(parser);
        manager.registVariableParser(datePattenParser);
        applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(PatternParserManager.class)).thenReturn(manager);
        ReflectionTestUtils.setField(PatternParserUtil.class, "applicationContext", applicationContext);

    }

    @Test
    public void testGetNextSegmentId() throws SQLException {
        LocalDateTime localDateTime = LocalDateTime.now();
        String ext = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        SegmentId segmentId = service.getNextSegmentId("testBiz");
        String actual = segmentId.getCurrentId().getValue();
        Assertions.assertEquals(ext + "-000", actual);
    }


    private DataSource buildDataSource(String file) throws SQLException {
        System.setProperty(DataSourceFactory.CONFIG_FILE, file);
        DataSourcePackage dataSourcePackage = DataSourceFactory.build(true);
        return dataSourcePackage.getMaster().get(0);
    }
}
