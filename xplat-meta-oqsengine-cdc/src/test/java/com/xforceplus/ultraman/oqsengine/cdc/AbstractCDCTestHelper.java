package com.xforceplus.ultraman.oqsengine.cdc;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.ZERO;

import com.google.common.collect.Lists;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerRunner;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.MockRedisCallbackService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.cdc.mock.CdcInitialization;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MockMetaManagerHolder;
import com.xforceplus.ultraman.oqsengine.storage.mock.StorageInitialization;
import com.xforceplus.ultraman.oqsengine.testcontainer.basic.AbstractContainerExtends;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
public abstract class AbstractCDCTestHelper extends AbstractContainerExtends {
    protected ConsumerRunner consumerRunner;

    protected MockRedisCallbackService mockRedisCallbackService;

    protected CDCMetricsService cdcMetricsService;

    protected void init(boolean isStartRunner) throws Exception {
        MockMetaManagerHolder.initEntityClassBuilder(Lists.newArrayList(EntityClassBuilder.ENTITY_CLASS_2));
        if (isStartRunner) {
            consumerRunner = initConsumerRunner();
            consumerRunner.start();
        }
    }

    protected void destroy(boolean isStopRunner) throws Exception {
        if (isStopRunner) {
            consumerRunner.shutdown();
        }
        if (null != mockRedisCallbackService) {
            mockRedisCallbackService.reset();
        }
        InitializationHelper.clearAll();
        InitializationHelper.destroy();
    }

    private ConsumerRunner initConsumerRunner() throws Exception {
        cdcMetricsService = new CDCMetricsService();
        mockRedisCallbackService = new MockRedisCallbackService(StorageInitialization.getInstance().getCommitIdStatusService());
        ReflectionTestUtils.setField(cdcMetricsService, "cdcMetricsCallback", mockRedisCallbackService);

        return new ConsumerRunner(CdcInitialization.getInstance().getConsumerService(), cdcMetricsService, CdcInitialization.getInstance().getSingleCDCConnector());
    }

    protected CDCDaemonService initDaemonService() throws Exception {

        CDCDaemonService cdcDaemonService = new CDCDaemonService();
        ReflectionTestUtils.setField(cdcDaemonService, "nodeIdGenerator", new StaticNodeIdGenerator(ZERO));
        ReflectionTestUtils.setField(cdcDaemonService, "consumerService", CdcInitialization.getInstance().getConsumerService());
        ReflectionTestUtils.setField(cdcDaemonService, "cdcMetricsService", cdcMetricsService);
        ReflectionTestUtils.setField(cdcDaemonService, "abstractCdcConnector", CdcInitialization.getInstance().getSingleCDCConnector());

        return cdcDaemonService;
    }
}
