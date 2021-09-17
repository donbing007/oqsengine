package com.xforceplus.ultraman.oqsengine.idgenerator.storage;

import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.status.impl.CommitIdStatusServiceImpl;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.MasterDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.DoNothingCacheEventHandler;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.MysqlContainer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * SegmentStorageTest Tester.
 *
 * @author <Authors name>
 * @version 1.0 02/25/2020
 * @since <pre>Feb 25, 2020</pre>
 */
@ExtendWith(MysqlContainer.class)
public class SegmentStorageTest {

    private TransactionManager transactionManager;
    private CommitIdStatusServiceImpl commitIdStatusService;
    private DataSource dataSource;
    private SqlSegmentStorage storage;


    @BeforeEach
    public void before() throws Exception {
        System.setProperty(
            "MYSQL_JDBC_ID",
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


        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new MasterDecimalStorageStrategy());


        storage = new SqlSegmentStorage();
        ReflectionTestUtils.setField(storage, "dataSource", dataSource);
        storage.setTable("segment");
        storage.setQueryTimeout(100000000);
        storage.init();
    }

    @AfterEach
    public void after() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            Statement st = conn.createStatement();
            st.executeUpdate("truncate table segment");
            st.close();
        }
    }

    /**
     * 测试写入并查询.
     *
     * @throws Exception
     */
    @Test
    public void testCRUD() throws Exception {
        Transaction tx = transactionManager.create(300000L);
        transactionManager.bind(tx.id());
        LocalDateTime updateTime = LocalDateTime.now();
        SegmentInfo info = SegmentInfo.builder().withBeginId(1l).withBizType("testBiz")
            .withCreateTime(new Timestamp(System.currentTimeMillis()))
            .withMaxId(1000l).withPatten("yyyy-mm-dd{000}").withMode(2).withStep(1000)
            .withUpdateTime(new Timestamp(System.currentTimeMillis()))
            .withVersion(1l)
            .withResetable(0)
            .withPatternKey("")
            .build();
        int size = storage.build(info);
        Assertions.assertEquals(1, size);

        Optional<SegmentInfo> entityOptional = storage.query("testBiz");
        Assertions.assertTrue(entityOptional.isPresent());
        SegmentInfo targetEntity = entityOptional.get();
        Assertions.assertEquals(targetEntity.getBeginId(), Long.valueOf(1L));
        Assertions.assertEquals(targetEntity.getPattern(), "yyyy-mm-dd{000}");

        storage.udpate(targetEntity);

        entityOptional = storage.query("testBiz");
        Assertions.assertTrue(entityOptional.isPresent());
        SegmentInfo segmentInfo = entityOptional.get();
        Assertions.assertEquals(segmentInfo.getMaxId(), Long.valueOf(2000l));
        Assertions.assertEquals(segmentInfo.getVersion(), Long.valueOf(2l));

        segmentInfo.setPatternKey("2020-02-02");
        int reset = storage.reset(segmentInfo);
        Assertions.assertEquals(reset, 1);
        entityOptional = storage.query("testBiz");
        Assertions.assertEquals(0, entityOptional.get().getMaxId().intValue());
        Assertions.assertEquals("2020-02-02", entityOptional.get().getPatternKey());
        tx.commit();
    }

    // 初始化数据
//    private List<IEntity> initData(SQLMasterStorage storage, int size) throws Exception {
//        List<IEntity> expectedEntitys = new ArrayList<>(size);
//        for (int i = 1; i <= size; i++) {
//            expectedEntitys.add(buildEntity(i * size));
//        }
//
//        try {
//            expectedEntitys.stream().forEach(e -> {
//                try {
//                    storage.build(e, l2EntityClass);
//                } catch (SQLException ex) {
//                    throw new RuntimeException(ex.getMessage(), ex);
//                }
//                commitIdStatusService.obsoleteAll();
//            });
//        } catch (Exception ex) {
//            transactionManager.getCurrent().get().rollback();
//            throw ex;
//        }
//
//        //将事务正常提交,并从事务管理器中销毁事务.
//        Transaction tx = transactionManager.getCurrent().get();
//
//        // 表示为非可读事务.
//        for (IEntity e : expectedEntitys) {
//            tx.getAccumulator().accumulateBuild(e);
//        }
//
//        tx.commit();
//        transactionManager.finish();
//
//        return expectedEntitys;
//    }


    private DataSource buildDataSource(String file) throws SQLException {
        System.setProperty(DataSourceFactory.CONFIG_FILE, file);
        DataSourcePackage dataSourcePackage = DataSourceFactory.build(true);
        return dataSourcePackage.getMaster().get(0);
    }

}
