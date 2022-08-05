package com.xforceplus.ultraman.oqsengine.cdc.mock;

import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.SQLCdcErrorStorage;
import com.xforceplus.ultraman.oqsengine.cdc.connect.SingleCDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.error.DefaultErrorRecorder;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.error.ErrorRecorder;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.process.BatchProcessor;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.process.DefaultBatchProcessor;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.service.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.service.DefaultConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsHandler;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.DefaultCDCMetricsHandler;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.id.SnowflakeLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.mock.BeanInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.EnvMockConstant;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.common.mock.ReflectionUtils;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.mock.RebuildInitialization;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MetaInitialization;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.mock.IndexInitialization;
import com.xforceplus.ultraman.oqsengine.storage.mock.StorageInitialization;
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
public class CdcInitialization implements BeanInitialization {

    private static volatile CdcInitialization instance;

    private SQLCdcErrorStorage cdcErrorStorage;
    private SingleCDCConnector singleCDCConnector;
    private CDCMetricsHandler cdcMetricsHandler;
    private ConsumerService consumerService;
    private BatchProcessor batchProcessor;
    private MockCallBackService mockCallBackService;
    private ErrorRecorder errorRecorder;
    private DataSource devOpsDataSource;

    public static final String CDC_ERRORS = "cdcerrors";

    private CdcInitialization() {
    }

    /**
     * 获取单例.
     */
    public static CdcInitialization getInstance() throws Exception {
        if (null == instance) {
            synchronized (CdcInitialization.class) {
                if (null == instance) {
                    instance = new CdcInitialization();
                    instance.init();
                    InitializationHelper.add(instance);
                }
            }
        }
        return instance;
    }

    @Override
    public void init() throws Exception {
        singleCDCConnector = new SingleCDCConnector(
            System.getProperty(EnvMockConstant.CANAL_HOST),
            System.getProperty(EnvMockConstant.CANAL_DESTINATION),
            System.getProperty(EnvMockConstant.CANAL_USER),
            System.getProperty(EnvMockConstant.CANAL_PASSWORD),
            Integer.parseInt(System.getProperty(EnvMockConstant.CANAL_PORT)));

        singleCDCConnector.init();

        initCdcErrors();

        initMetrics();

        initConsumerService();

        initBatchProcessor();
    }

    @Override
    public void clear() throws Exception {

        if (null != mockCallBackService) {
            mockCallBackService.reset();
        }

        DataSourcePackage dataSourcePackage = CommonInitialization.getInstance().getDataSourcePackage(false);
        try {
            if (null != dataSourcePackage && null != dataSourcePackage.getDevOps()) {
                for (DataSource ds : dataSourcePackage.getMaster()) {
                    Connection conn = ds.getConnection();
                    Statement st = conn.createStatement();
                    st.execute("truncate table " + CDC_ERRORS);
                    st.close();
                    conn.close();
                }
            }
        } catch (Exception e) {
            //  ignore
        }
    }

    @Override
    public void destroy() throws Exception {
        cdcErrorStorage = null;
        singleCDCConnector = null;
        cdcMetricsHandler = null;
        mockCallBackService = null;
        consumerService = null;
        errorRecorder = null;
        batchProcessor = null;
        instance = null;
    }

    private void initBatchProcessor() throws Exception {
        batchProcessor = new DefaultBatchProcessor();
        Collection<Field> fields = ReflectionUtils.printAllMembers(batchProcessor);

        ReflectionUtils.reflectionFieldValue(fields, "consumerService", batchProcessor, consumerService);
    }

    private void initCdcErrors() throws Exception {

        devOpsDataSource = buildDevOpsDataSource();

        cdcErrorStorage = new SQLCdcErrorStorage();
        Collection<Field> fields = ReflectionUtils.printAllMembers(cdcErrorStorage);
        ReflectionUtils.reflectionFieldValue(fields, "devOpsDataSource", cdcErrorStorage, devOpsDataSource);

        cdcErrorStorage.setCdcErrorRecordTable(CDC_ERRORS);
        cdcErrorStorage.init();

        errorRecorder = new DefaultErrorRecorder();
        Collection<Field> errorFields = ReflectionUtils.printAllMembers(errorRecorder);
        ReflectionUtils.reflectionFieldValue(errorFields, "cdcErrorStorage", errorRecorder, cdcErrorStorage);
        ReflectionUtils.reflectionFieldValue(errorFields, "seqNoGenerator", errorRecorder,
            new SnowflakeLongIdGenerator(new StaticNodeIdGenerator(0)));
    }

    private void initMetrics() throws Exception {
        mockCallBackService = new MockCallBackService(StorageInitialization.getInstance().getCommitIdStatusService());

        cdcMetricsHandler = new DefaultCDCMetricsHandler();
        Collection<Field> fields = ReflectionUtils.printAllMembers(cdcMetricsHandler);
        ReflectionUtils.reflectionFieldValue(fields, "cdcMetricsCallback", cdcMetricsHandler, mockCallBackService);
    }

    /**
     * init.
     */
    public void initConsumerService() throws Exception {

        consumerService = initConsumer();

    }

    private DataSource buildDevOpsDataSource() throws IllegalAccessException {
        return CommonInitialization.getInstance().getDataSourcePackage(false).getDevOps();
    }

    public BatchProcessor getBatchProcessor() {
        return batchProcessor;
    }

    public ErrorRecorder getErrorRecorder() {
        return errorRecorder;
    }

    public SQLCdcErrorStorage getCdcErrorStorage() {
        return cdcErrorStorage;
    }

    public SingleCDCConnector getSingleCDCConnector() {
        return singleCDCConnector;
    }

    public CDCMetricsHandler getCdcMetricsHandler() {
        return cdcMetricsHandler;
    }

    public ConsumerService getConsumerService() {
        return consumerService;
    }

    public DataSource getDevOpsDataSource() {
        return devOpsDataSource;
    }

    /**
     * reset.
     */
    public void resetConsumerService(ConsumerService consumerService) throws IllegalAccessException {
        this.consumerService = consumerService;
        Collection<Field> fields = ReflectionUtils.printAllMembers(batchProcessor);
        ReflectionUtils.reflectionFieldValue(fields, "consumerService", batchProcessor, this.consumerService);
    }

    /**
     * init.
     */
    public ConsumerService initConsumer() throws Exception {
        ConsumerService consumerService = new DefaultConsumerService();

        Collection<Field> fields = ReflectionUtils.printAllMembers(consumerService);
        ReflectionUtils.reflectionFieldValue(fields, "sphinxQLIndexStorage", consumerService,
            IndexInitialization.getInstance().getIndexStorage());
        ReflectionUtils.reflectionFieldValue(fields, "errorRecorder", consumerService, errorRecorder);
        ReflectionUtils.reflectionFieldValue(fields, "metaManager", consumerService,
            MetaInitialization.getInstance().getMetaManager());

        ReflectionUtils.reflectionFieldValue(fields, "cdcMetricsHandler", consumerService, cdcMetricsHandler);

        return consumerService;
    }
}
