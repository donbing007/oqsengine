package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.mock;

import com.xforceplus.ultraman.oqsengine.common.mock.BeanInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.common.mock.ReflectionUtils;
import com.xforceplus.ultraman.oqsengine.common.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoJoinTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.SphinxQLManticoreIndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.SphinxQLConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLStringsStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.transaction.SphinxQLTransactionResourceFactory;
import com.xforceplus.ultraman.oqsengine.storage.mock.StorageInitialization;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.tokenizer.DefaultTokenizerFactory;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactory;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import javax.sql.DataSource;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */
public class IndexInitialization implements BeanInitialization {

    private static volatile IndexInitialization instance = null;

    private Selector<String> indexWriteIndexNameSelector;
    private Selector<DataSource> writeDataSourceSelector;
    private SphinxQLManticoreIndexStorage indexStorage;
    private TokenizerFactory tokenizerFactory;
    private StorageStrategyFactory storageStrategyFactory;

    private static final String INDEX_TABLE = "oqsindex";

    private IndexInitialization() {
    }

    /**
     * 获取单例.
     */
    public static IndexInitialization getInstance() throws Exception {
        if (null == instance) {
            synchronized (IndexInitialization.class) {
                if (null == instance) {
                    instance = new IndexInitialization();
                    instance.init();
                    InitializationHelper.add(instance);
                }
            }
        }
        return instance;
    }

    @Override
    public void init() throws Exception {
        writeDataSourceSelector = buildWriteDataSourceSelector();

        storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new SphinxQLDecimalStorageStrategy());
        storageStrategyFactory.register(FieldType.STRINGS, new SphinxQLStringsStorageStrategy());

        tokenizerFactory = new DefaultTokenizerFactory();
        SphinxQLConditionsBuilderFactory sphinxQLConditionsBuilderFactory = new SphinxQLConditionsBuilderFactory();
        sphinxQLConditionsBuilderFactory.setStorageStrategy(storageStrategyFactory);
        sphinxQLConditionsBuilderFactory.setTokenizerFacotry(tokenizerFactory);
        sphinxQLConditionsBuilderFactory.init();

        indexWriteIndexNameSelector = new NoSelector<>(INDEX_TABLE);

        indexStorage = new SphinxQLManticoreIndexStorage();

        DataSource searchDataSource = buildSearchDataSourceSelector();

        TransactionManager transactionManager = StorageInitialization.getInstance().getTransactionManager();

        TransactionExecutor searchExecutor =
            new AutoJoinTransactionExecutor(transactionManager, new SphinxQLTransactionResourceFactory(),
                new NoSelector<>(searchDataSource), new NoSelector<>(INDEX_TABLE));

        TransactionExecutor writeExecutor =
            new AutoJoinTransactionExecutor(
                transactionManager, new SphinxQLTransactionResourceFactory(),
                writeDataSourceSelector, indexWriteIndexNameSelector);

        Collection<Field> fields = ReflectionUtils.printAllMembers(indexStorage);
        ReflectionUtils.reflectionFieldValue(fields, "writerDataSourceSelector", indexStorage, writeDataSourceSelector);
        ReflectionUtils.reflectionFieldValue(fields, "searchTransactionExecutor", indexStorage, searchExecutor);
        ReflectionUtils.reflectionFieldValue(fields, "writeTransactionExecutor", indexStorage, writeExecutor);
        ReflectionUtils.reflectionFieldValue(fields, "sphinxQLConditionsBuilderFactory", indexStorage,
            sphinxQLConditionsBuilderFactory);
        ReflectionUtils.reflectionFieldValue(fields, "storageStrategyFactory", indexStorage, storageStrategyFactory);
        ReflectionUtils
            .reflectionFieldValue(fields, "indexWriteIndexNameSelector", indexStorage, indexWriteIndexNameSelector);
        ReflectionUtils.reflectionFieldValue(fields, "tokenizerFactory", indexStorage, tokenizerFactory);
        ReflectionUtils
            .reflectionFieldValue(fields, "threadPool", indexStorage, CommonInitialization.getInstance().getRunner());

        indexStorage.setSearchIndexName(INDEX_TABLE);
        indexStorage.setTimeoutMs(1000);
        indexStorage.init();
    }

    @Override
    public void clear() throws Exception {
        List<DataSource> dataSources = CommonInitialization.getInstance().getDataSourcePackage(true).getIndexWriter();
        for (DataSource ds : dataSources) {
            Connection conn = ds.getConnection();
            Statement st = conn.createStatement();
            st.executeUpdate("truncate table " + INDEX_TABLE);
            st.close();
            conn.close();
        }
    }

    @Override
    public void destroy() throws Exception {
        indexWriteIndexNameSelector = null;
        writeDataSourceSelector = null;
        tokenizerFactory = null;
        storageStrategyFactory = null;
        indexStorage.destroy();
        indexStorage = null;

        instance = null;
    }

    private Selector<DataSource> buildWriteDataSourceSelector() throws IllegalAccessException {
        return new NoSelector<>(CommonInitialization.getInstance().getDataSourcePackage(true).getIndexWriter().get(0));
    }

    private DataSource buildSearchDataSourceSelector() throws IllegalAccessException {
        return CommonInitialization.getInstance().getDataSourcePackage(true).getIndexSearch().get(0);
    }


    public Selector<String> getIndexWriteIndexNameSelector() {
        return indexWriteIndexNameSelector;
    }

    public Selector<DataSource> getWriteDataSourceSelector() {
        return writeDataSourceSelector;
    }

    public SphinxQLManticoreIndexStorage getIndexStorage() {
        return indexStorage;
    }

    public TokenizerFactory getTokenizerFactory() {
        return tokenizerFactory;
    }

    public StorageStrategyFactory getStorageStrategyFactory() {
        return storageStrategyFactory;
    }
}
