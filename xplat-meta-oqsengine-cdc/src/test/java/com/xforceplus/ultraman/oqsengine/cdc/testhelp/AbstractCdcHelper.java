package com.xforceplus.ultraman.oqsengine.cdc.testhelp;


import com.google.common.collect.Lists;
import com.xforceplus.ultraman.oqsengine.cdc.CDCDaemonService;
import com.xforceplus.ultraman.oqsengine.cdc.connect.AbstractCDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.connect.ErrorCDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.CDCRunner;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.process.BatchProcessor;
import com.xforceplus.ultraman.oqsengine.cdc.connect.ErrorBatchProcessor;
import com.xforceplus.ultraman.oqsengine.cdc.mock.CdcInitialization;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.meta.EntityClassBuilder;
import com.xforceplus.ultraman.oqsengine.common.mock.EnvMockConstant;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.common.mock.ReflectionUtils;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.mock.RebuildInitialization;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MockMetaManagerHolder;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.testcontainer.basic.AbstractContainerExtends;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class AbstractCdcHelper extends AbstractContainerExtends {

    protected CDCRunner cdcRunner;

    protected void init(boolean isStartRunner, IEntityClass entityClass) throws Exception {
        List<IEntityClass> entityClassList;
        if (null == entityClass) {
            entityClassList = Lists.newArrayList(EntityClassBuilder.ENTITY_CLASS_0, EntityClassBuilder.ENTITY_CLASS_1,
                EntityClassBuilder.ENTITY_CLASS_2, EntityClassBuilder.ENTITY_CLASS_STATIC);
        } else {
            entityClassList = Collections.singletonList(entityClass);
        }

        MockMetaManagerHolder.initEntityClassBuilder(entityClassList);

        if (isStartRunner) {
            cdcRunner = initCDCRunner();
            cdcRunner.start();
        }
    }

    protected void init(IEntityClass entityClass, BatchProcessor batchProcessor, AbstractCDCConnector connector) throws Exception {
        List<IEntityClass> entityClassList;
        if (null == entityClass) {
            entityClassList = Lists.newArrayList(EntityClassBuilder.ENTITY_CLASS_0, EntityClassBuilder.ENTITY_CLASS_1,
                EntityClassBuilder.ENTITY_CLASS_2, EntityClassBuilder.ENTITY_CLASS_STATIC);
        } else {
            entityClassList = Collections.singletonList(entityClass);
        }

        MockMetaManagerHolder.initEntityClassBuilder(entityClassList);

        cdcRunner = initCDCRunner(batchProcessor, connector);
        cdcRunner.start();
    }

    protected void clear(boolean isStopRunner) throws Exception {
        if (isStopRunner) {
            cdcRunner.shutdown();
        }

        InitializationHelper.clearAll();
    }

    public static void destroy() {
        InitializationHelper.destroy();
    }

    protected CDCRunner initCDCRunner(BatchProcessor processor, AbstractCDCConnector connector) throws Exception {
        return new CDCRunner(processor, connector);
    }

    protected CDCRunner initCDCRunner() throws Exception {
        return new CDCRunner(
            CdcInitialization.getInstance().getBatchProcessor(), CdcInitialization.getInstance().getSingleCDCConnector()
        );
    }

    protected CDCDaemonService initDaemonService() throws Exception {

        CDCDaemonService cdcDaemonService = new CDCDaemonService();
        ReflectionTestUtils.setField(cdcDaemonService, "batchProcessor", CdcInitialization.getInstance().getBatchProcessor());
        ReflectionTestUtils.setField(cdcDaemonService, "cdcConnector", CdcInitialization.getInstance().getSingleCDCConnector());

        return cdcDaemonService;
    }

    public static ErrorCDCConnector initErrorCDCConnector() {
        ErrorCDCConnector errorCDCConnector = new ErrorCDCConnector(
            System.getProperty(EnvMockConstant.CANAL_HOST),
            System.getProperty(EnvMockConstant.CANAL_DESTINATION),
            System.getProperty(EnvMockConstant.CANAL_USER),
            System.getProperty(EnvMockConstant.CANAL_PASSWORD),
            Integer.parseInt(System.getProperty(EnvMockConstant.CANAL_PORT)));

        errorCDCConnector.init();

        return errorCDCConnector;
    }

    public static ErrorBatchProcessor initErrorBatchProcessor() throws Exception {
        ErrorBatchProcessor batchProcessor = new ErrorBatchProcessor();
        Collection<Field> fields = ReflectionUtils.printAllMembers(batchProcessor);

        ReflectionUtils.reflectionFieldValue(fields, "rebuildIndexExecutor",
            batchProcessor, RebuildInitialization.getInstance().getTaskExecutor());
        ReflectionUtils.reflectionFieldValue(fields, "consumerService", batchProcessor, CdcInitialization.getInstance().getConsumerService());

        return batchProcessor;
    }
}
