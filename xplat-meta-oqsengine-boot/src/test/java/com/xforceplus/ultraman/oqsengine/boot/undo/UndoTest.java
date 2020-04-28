package com.xforceplus.ultraman.oqsengine.boot.undo;

import com.xforceplus.ultraman.oqsengine.boot.undo.mock.MockConnectionTransactionResource;
import com.xforceplus.ultraman.oqsengine.boot.undo.mock.MockSphinxQLTransactionResource;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.core.service.impl.EntityManagementServiceImpl;
import com.xforceplus.ultraman.oqsengine.core.service.impl.EntitySearchServiceImpl;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoCreateTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoShardTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.SphinxQLIndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.BuildStorageCommand;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.DeleteStorageCommand;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.ReplaceStorageCommand;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.SphinxQLConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.SQLMasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.selector.SuffixNumberHashSelector;
import com.xforceplus.ultraman.oqsengine.storage.selector.TakeTurnsSelector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.MultiLocalTransaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.sql.ConnectionTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.sql.SphinxQLTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.UndoExecutor;
import com.xforceplus.ultraman.oqsengine.storage.undo.UndoFactory;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.DefaultStorageCommandExecutor;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbType;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpType;
import com.xforceplus.ultraman.oqsengine.storage.undo.store.SimpleUndoLogStore;
import com.xforceplus.ultraman.oqsengine.storage.undo.store.UndoLogStore;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 4/28/2020 9:55 AM
 * 功能描述:
 * 修改历史:
 */
public class UndoTest {
    final Logger logger = LoggerFactory.getLogger(UndoTest.class);

    private EntityManagementServiceImpl manageService;
    private EntitySearchServiceImpl searchService;

    private TransactionManager transactionManager;
    private LongIdGenerator idGenerator = new LongIdGenerator() {

        private AtomicLong id = new AtomicLong(Long.MAX_VALUE);

        @Override
        public Long next() {
            return id.getAndDecrement();
        }
    };
    private SphinxQLIndexStorage indexStorage;
    private SQLMasterStorage masterStorage;
    private DataSourcePackage indexDataSourcePackage;
    private DataSourcePackage masterDataSourcePackage;

    private Selector<DataSource> indexWriteDataSourceSelector;
    private String indexTableName = "oqsindextest";
    private Selector<DataSource> masterDataSourceSelector;
    private Selector<String> tableNameSelector;

    private UndoFactory undoFactory;
    private UndoExecutor undoExecutor;

    private static IEntity[] entityes;
    private List<IEntity> expectedEntitys;
    private IEntityField fixStringsField = new EntityField(100000, "strings", FieldType.STRINGS);
    private StringsValue fixStringsValue = new StringsValue(fixStringsField, "1,2,3,500002,测试".split(","));


    @Before
    public void init() throws Exception{
        indexWriteDataSourceSelector = buildWriteDataSourceSelector("./src/test/resources/sql_index_storage.conf");
        tableNameSelector = buildTableNameSelector("oqsbigentity", 3);
        masterDataSourceSelector = buildDataSourceSelector("./src/test/resources/sql_master_storage_build.conf");

        initUndo();

        transactionManager = new DefaultTransactionManager(3000,
                new IncreasingOrderLongIdGenerator(0));

        initSphinxQLIndexStorage();
        initSQLMasterStorage();
        initEntiyManangement();
    }

    @Test
    public void testBuild() throws SQLException {

        Transaction tx = transactionManager.create();

        IEntity entity = buildEntity(1);

        manageService.build(entity);

        try {
            tx.commit();
        } catch (SQLException e) {

        }

        Optional<IEntity> entityOptional = null;
        try {
            transactionManager.create();
            entityOptional = searchService.selectOne(entity.id(), entity.entityClass());
        } catch (Exception e) {
            logger.warn("entity has been already deleted");
        }

        Assert.assertFalse(entityOptional.isPresent());
    }

    void initSphinxQLIndexStorage() throws Exception{
        Selector<DataSource> searchDataSourceSelector = buildSearchDataSourceSelector("./src/test/resources/sql_index_storage.conf");

        truncate();

        // 等待加载完毕
        TimeUnit.SECONDS.sleep(1L);

        TransactionExecutor executor = new AutoShardTransactionExecutor(
                transactionManager, MockSphinxQLTransactionResource.class, undoExecutor);

        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new SphinxQLDecimalStorageStrategy());

        SphinxQLConditionsBuilderFactory sphinxQLConditionsBuilderFactory = new SphinxQLConditionsBuilderFactory();
        sphinxQLConditionsBuilderFactory.setStorageStrategy(storageStrategyFactory);
        sphinxQLConditionsBuilderFactory.init();

        indexStorage = new SphinxQLIndexStorage();
        ReflectionTestUtils.setField(indexStorage, "writerDataSourceSelector", indexWriteDataSourceSelector);
        ReflectionTestUtils.setField(indexStorage, "searchDataSourceSelector", searchDataSourceSelector);
        ReflectionTestUtils.setField(indexStorage, "transactionExecutor", executor);
        ReflectionTestUtils.setField(indexStorage, "sphinxQLConditionsBuilderFactory", sphinxQLConditionsBuilderFactory);
        ReflectionTestUtils.setField(indexStorage, "storageStrategyFactory", storageStrategyFactory);
        indexStorage.setIndexTableName(indexTableName);

//        transactionManager.create();

//        initData(indexStorage);

        // 确认没有事务.
//        Assert.assertFalse(transactionManager.getCurrent().isPresent());
    }

    void initSQLMasterStorage() throws Exception {
        // 等待加载完毕
        TimeUnit.SECONDS.sleep(1L);

        TransactionExecutor executor = new AutoShardTransactionExecutor(
                transactionManager, MockConnectionTransactionResource.class, undoExecutor);


        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();

        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(500),
                ExecutorHelper.buildNameThreadFactory("oqs-engine", false),
                new ThreadPoolExecutor.AbortPolicy()
        );

        masterStorage = new SQLMasterStorage();
        ReflectionTestUtils.setField(masterStorage, "dataSourceSelector", masterDataSourceSelector);
        ReflectionTestUtils.setField(masterStorage, "tableNameSelector", tableNameSelector);
        ReflectionTestUtils.setField(masterStorage, "transactionExecutor", executor);
        ReflectionTestUtils.setField(masterStorage, "storageStrategyFactory", storageStrategyFactory);
        ReflectionTestUtils.setField(masterStorage, "threadPool", threadPool);
        masterStorage.init();

//        transactionManager.create();
//        expectedEntitys = initDataForSqlMaster(masterStorage, 10);
    }

    void initEntiyManangement(){
        TransactionExecutor te = new AutoCreateTransactionExecutor(transactionManager);

        manageService = new EntityManagementServiceImpl();
        ReflectionTestUtils.setField(manageService, "idGenerator", idGenerator);
        ReflectionTestUtils.setField(manageService, "transactionExecutor", te);
        ReflectionTestUtils.setField(manageService, "masterStorage", masterStorage);
        ReflectionTestUtils.setField(manageService, "indexStorage", indexStorage);

        searchService = new EntitySearchServiceImpl();
        ReflectionTestUtils.setField(searchService, "masterStorage", masterStorage);
        ReflectionTestUtils.setField(searchService, "indexStorage", indexStorage);
    }

    void initUndo(){
        DefaultStorageCommandExecutor storageCommandInvoker = new DefaultStorageCommandExecutor();
        storageCommandInvoker.register(DbType.INDEX, OpType.BUILD, new BuildStorageCommand(indexTableName));
        storageCommandInvoker.register(DbType.INDEX, OpType.REPLACE, new ReplaceStorageCommand(indexTableName));
        storageCommandInvoker.register(DbType.INDEX, OpType.DELETE, new DeleteStorageCommand(indexTableName));

        storageCommandInvoker.register(DbType.MASTER, OpType.BUILD, new com.xforceplus.ultraman.oqsengine.storage.master.command.BuildStorageCommand(tableNameSelector));
        storageCommandInvoker.register(DbType.MASTER, OpType.REPLACE, new com.xforceplus.ultraman.oqsengine.storage.master.command.ReplaceStorageCommand(tableNameSelector));
        storageCommandInvoker.register(DbType.MASTER, OpType.DELETE, new com.xforceplus.ultraman.oqsengine.storage.master.command.DeleteStorageCommand(tableNameSelector));

        UndoLogStore undoLogStore = new SimpleUndoLogStore();

        undoExecutor = new UndoExecutor(undoLogStore, storageCommandInvoker);

        undoFactory = new UndoFactory();
        ReflectionTestUtils.setField(undoFactory, "indexWriteDataSourceSelector", indexWriteDataSourceSelector);
        ReflectionTestUtils.setField(undoFactory, "masterDataSourceSelector", masterDataSourceSelector);
        ReflectionTestUtils.setField(undoFactory, "undoLogStore", undoLogStore);
        ReflectionTestUtils.setField(undoFactory, "undoExecutor", undoExecutor);
        undoFactory.init();

    }

    // 初始化数据
    private void initData(SphinxQLIndexStorage storage) throws Exception {
        try {
            Arrays.stream(entityes).forEach(e -> {
                try {
                    storage.build(e);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex.getMessage(), ex);
                }
            });
        } catch (Exception ex) {
            transactionManager.getCurrent().get().rollback();
            throw ex;
        }

        //将事务正常提交,并从事务管理器中销毁事务.
        Transaction tx = transactionManager.getCurrent().get();
        tx.commit();
        transactionManager.finish();
    }

    private Selector<DataSource> buildWriteDataSourceSelector(String file) {
        if (indexDataSourcePackage == null) {
            System.setProperty(DataSourceFactory.CONFIG_FILE, file);

            indexDataSourcePackage = DataSourceFactory.build();
        }

        return new TakeTurnsSelector<>(indexDataSourcePackage.getIndexWriter());

    }

    private Selector<DataSource> buildSearchDataSourceSelector(String file) {
        if (indexDataSourcePackage == null) {
            System.setProperty(DataSourceFactory.CONFIG_FILE, file);

            indexDataSourcePackage = DataSourceFactory.build();
        }

        return new TakeTurnsSelector<>(indexDataSourcePackage.getIndexSearch());

    }

    private void truncate() {
        List<DataSource> ds = indexDataSourcePackage.getIndexWriter();
        ds.stream().forEach(d -> {
            try {
                Connection conn = d.getConnection();
                boolean autocommit = conn.getAutoCommit();
                conn.setAutoCommit(true);

                Statement st = conn.createStatement();
                st.executeUpdate("TRUNCATE RTINDEX oqsindextest");

                st.close();

                conn.setAutoCommit(autocommit);

                conn.close();

            } catch (SQLException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }

        });
    }

    private List<IEntity> initData(SQLMasterStorage storage, int size) throws Exception {
        List<IEntity> expectedEntitys = new ArrayList<>(size);
        for (int i = 1; i <= size; i++) {
            expectedEntitys.add(buildEntity(i * size));
        }

        try {
            expectedEntitys.stream().forEach(e -> {
                try {
                    storage.build(e);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex.getMessage(), ex);
                }
            });
        } catch (Exception ex) {
            transactionManager.getCurrent().get().rollback();
            throw ex;
        }

        //将事务正常提交,并从事务管理器中销毁事务.
        Transaction tx = transactionManager.getCurrent().get();
        tx.commit();
        transactionManager.finish();

        return expectedEntitys;
    }

    private IEntity buildEntity(long baseId) {
        Collection<IEntityField> fields = buildRandomFields(baseId, 3);
        fields.add(fixStringsField);

        return new Entity(
                baseId,
                new EntityClass(baseId, "test", fields),
                buildRandomValue(baseId, fields)
        );
    }

    private Selector<DataSource> buildDataSourceSelector(String file) {
        System.setProperty(DataSourceFactory.CONFIG_FILE, file);

        masterDataSourcePackage = DataSourceFactory.build();

        return new TakeTurnsSelector<>(masterDataSourcePackage.getMaster());

    }

    private Selector<String> buildTableNameSelector(String base, int size) {
        return new SuffixNumberHashSelector(base, size);
    }

    // 初始化数据
    private List<IEntity> initDataForSqlMaster(SQLMasterStorage storage, int size) throws Exception {
        List<IEntity> expectedEntitys = new ArrayList<>(size);
        for (int i = 1; i <= size; i++) {
            expectedEntitys.add(buildEntity(i * size));
        }

        try {
            expectedEntitys.stream().forEach(e -> {
                try {
                    storage.build(e);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex.getMessage(), ex);
                }
            });
        } catch (Exception ex) {
            transactionManager.getCurrent().get().rollback();
            throw ex;
        }

        //将事务正常提交,并从事务管理器中销毁事务.
        Transaction tx = transactionManager.getCurrent().get();
        tx.commit();
        transactionManager.finish();

        return expectedEntitys;
    }

    private Collection<IEntityField> buildRandomFields(long baseId, int size) {
        List<IEntityField> fields = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            long fieldId = baseId + i;
            fields.add(new EntityField(fieldId, "c" + fieldId,
                    ("c" + fieldId).hashCode() % 2 == 1 ? FieldType.LONG : FieldType.STRING));
        }

        return fields;
    }

    private IEntityValue buildRandomValue(long id, Collection<IEntityField> fields) {
        Collection<IValue> values = fields.stream().map(f -> {
            switch (f.type()) {
                case STRING:
                    return new StringValue(f, buildRandomString(30));
                case STRINGS:
                    return fixStringsValue;
                default:
                    return new LongValue(f, (long) buildRandomLong(10, 100000));
            }
        }).collect(Collectors.toList());

        EntityValue value = new EntityValue(id);
        value.addValues(values);
        return value;
    }

    private String buildRandomString(int size) {
        StringBuilder buff = new StringBuilder();
        Random rand = new Random(47);
        for (int i = 0; i < size; i++) {
            buff.append(rand.nextInt(26) + 'a');
        }
        return buff.toString();
    }

    private int buildRandomLong(int min, int max) {
        Random random = new Random();

        return random.nextInt(max) % (max - min + 1) + min;
    }
}
