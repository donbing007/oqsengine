package com.xforceplus.ultraman.oqsengine.storage.master.mock;

import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.mock.BeanInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.common.mock.ReflectionUtils;
import com.xforceplus.ultraman.oqsengine.common.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MetaInitialization;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoJoinTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.SQLMasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.conditions.SQLJsonConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.MasterDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.MasterStringsStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.transaction.SqlConnectionTransactionResourceFactory;
import com.xforceplus.ultraman.oqsengine.storage.master.unique.UniqueKeyGenerator;
import com.xforceplus.ultraman.oqsengine.storage.master.unique.impl.SimpleFieldKeyGenerator;
import com.xforceplus.ultraman.oqsengine.storage.mock.StorageInitialization;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.tokenizer.DefaultTokenizerFactory;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Collection;
import javax.sql.DataSource;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */
public class MasterDBInitialization implements BeanInitialization {

    private static volatile MasterDBInitialization instance = null;

    private DataSource dataSource;
    private SQLMasterStorage masterStorage;
    private TransactionExecutor masterTransactionExecutor;
    private UniqueKeyGenerator keyGenerator;
    private StorageStrategyFactory masterStorageStrategyFactory;
    private SQLJsonConditionsBuilderFactory sqlJsonConditionsBuilderFactory;

    public static final String MASTER_STORAGE_TABLE = "oqsbigentity";
    public static final String MASTER_STORAGE_FAILED_TABLE = "entityfaileds";
    public static final String MASTER_STORAGE_UNIQUE_TABLE = "oqsunique";

    private MasterDBInitialization() {
    }

    /**
     * 获取单例.
     */
    public static MasterDBInitialization getInstance() throws Exception {
        if (null == instance) {
            synchronized (MasterDBInitialization.class) {
                if (null == instance) {
                    instance = new MasterDBInitialization();
                    instance.init();
                    InitializationHelper.add(instance);
                }
            }
        }
        return instance;
    }

    @Override
    public void init() throws Exception {
        dataSource = buildDataSourceSelectorMaster();

        masterStorageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        masterStorageStrategyFactory.register(FieldType.DECIMAL, new MasterDecimalStorageStrategy());
        masterStorageStrategyFactory.register(FieldType.STRINGS, new MasterStringsStorageStrategy());

        sqlJsonConditionsBuilderFactory = new SQLJsonConditionsBuilderFactory();
        sqlJsonConditionsBuilderFactory.setStorageStrategy(masterStorageStrategyFactory);
        sqlJsonConditionsBuilderFactory.setTokenizerFacotry(new DefaultTokenizerFactory());
        sqlJsonConditionsBuilderFactory.init();

        keyGenerator = new SimpleFieldKeyGenerator();
        MetaManager metaManager = MetaInitialization.getInstance().getMetaManager();

        Collection<Field> fields = ReflectionUtils.printAllMembers(keyGenerator);
        ReflectionUtils.reflectionFieldValue(fields, "metaManager", keyGenerator, metaManager);

        masterStorage = new SQLMasterStorage();
        Collection<Field> masterFields = ReflectionUtils.printAllMembers(masterStorage);

        resetTransactionExecutor(MASTER_STORAGE_TABLE);
        ReflectionUtils
            .reflectionFieldValue(masterFields, "storageStrategyFactory", masterStorage, masterStorageStrategyFactory);
        ReflectionUtils
            .reflectionFieldValue(masterFields, "conditionsBuilderFactory", masterStorage, sqlJsonConditionsBuilderFactory);
        ReflectionUtils.reflectionFieldValue(masterFields, "keyGenerator", masterStorage, keyGenerator);
        ReflectionUtils.reflectionFieldValue(masterFields, "metaManager", masterStorage, metaManager);
        ReflectionUtils.reflectionFieldValue(masterFields, "asyncErrorExecutor", masterStorage,
            CommonInitialization.getInstance().getRunner());

        masterStorage.setTableName(MASTER_STORAGE_TABLE);
        masterStorage.setErrorTable(MASTER_STORAGE_FAILED_TABLE);
        masterStorage.setUniqueTableName(MASTER_STORAGE_UNIQUE_TABLE);
        masterStorage.init();
    }

    @Override
    public void clear() throws Exception {
        MasterDBInitialization.getInstance().resetTransactionExecutor(MASTER_STORAGE_TABLE);

        DataSourcePackage dataSourcePackage = CommonInitialization.getInstance().getDataSourcePackage(true);
        if (null != dataSourcePackage && null != dataSourcePackage.getMaster()) {
            for (DataSource ds : dataSourcePackage.getMaster()) {
                Connection conn = ds.getConnection();
                Statement st = conn.createStatement();
                st.executeUpdate("truncate table " + MASTER_STORAGE_TABLE);
                st.executeUpdate("truncate table " + MASTER_STORAGE_FAILED_TABLE);
                st.executeUpdate("truncate table " + MASTER_STORAGE_UNIQUE_TABLE);
                st.close();
                conn.close();
            }
        }
    }

    @Override
    public void destroy() throws Exception {

        dataSource = null;
        masterTransactionExecutor = null;
        keyGenerator = null;
        masterStorageStrategyFactory = null;
        sqlJsonConditionsBuilderFactory = null;
        masterStorage.destroy();

        instance = null;

    }

    /**
     * reset TransactionExecutor by name.
     */
    public void resetTransactionExecutor(String name) throws Exception {
        masterTransactionExecutor = new AutoJoinTransactionExecutor(
            StorageInitialization.getInstance().getTransactionManager(),
            new SqlConnectionTransactionResourceFactory(name),
            NoSelector.build(dataSource),
            NoSelector.build(name));

        Collection<Field> masterFields = ReflectionUtils.printAllMembers(masterStorage);
        ReflectionUtils.reflectionFieldValue(masterFields, "transactionExecutor",
            masterStorage, instance.masterTransactionExecutor);
    }

    protected DataSource buildDataSourceSelectorMaster() throws IllegalAccessException {
        return CommonInitialization.getInstance().getDataSourcePackage(true).getMaster().get(0);
    }


    public SQLJsonConditionsBuilderFactory getSqlJsonConditionsBuilderFactory() {
        return sqlJsonConditionsBuilderFactory;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public SQLMasterStorage getMasterStorage() {
        return masterStorage;
    }

    public TransactionExecutor getMasterTransactionExecutor() {
        return masterTransactionExecutor;
    }

    public UniqueKeyGenerator getKeyGenerator() {
        return keyGenerator;
    }

    public StorageStrategyFactory getMasterStorageStrategyFactory() {
        return masterStorageStrategyFactory;
    }
}
