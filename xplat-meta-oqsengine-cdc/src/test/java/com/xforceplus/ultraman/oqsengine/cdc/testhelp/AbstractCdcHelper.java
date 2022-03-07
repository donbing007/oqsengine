package com.xforceplus.ultraman.oqsengine.cdc.testhelp;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.ZERO;

import com.google.common.collect.Lists;
import com.xforceplus.ultraman.oqsengine.cdc.CDCDaemonService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.CDCRunner;
import com.xforceplus.ultraman.oqsengine.cdc.mock.CdcInitialization;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.mock.RebuildInitialization;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MockMetaManagerHolder;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.testcontainer.basic.AbstractContainerExtends;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class AbstractCdcHelper extends AbstractContainerExtends {
    protected CDCRunner cdcRunner;

    protected void init(boolean isStartRunner, IEntityClass entityClass) throws Exception {
        if (null == entityClass) {
            entityClass = EntityClassBuilder.ENTITY_CLASS_2;
        }

        MockMetaManagerHolder.initEntityClassBuilder(Lists.newArrayList(entityClass));

        if (isStartRunner) {
            cdcRunner = initCDCRunner();
            cdcRunner.start();
        }
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

    protected CDCRunner initCDCRunner() throws Exception {
        return new CDCRunner(
                CdcInitialization.getInstance().getConsumerService(),
                CdcInitialization.getInstance().getCdcMetricsHandler(),
                CdcInitialization.getInstance().getSingleCDCConnector(),
                RebuildInitialization.getInstance().getTaskExecutor()
        );
    }

    protected CDCDaemonService initDaemonService() throws Exception {

        CDCDaemonService cdcDaemonService = new CDCDaemonService();
        ReflectionTestUtils.setField(cdcDaemonService, "nodeIdGenerator", new StaticNodeIdGenerator(ZERO));
        ReflectionTestUtils.setField(cdcDaemonService, "consumerService", CdcInitialization.getInstance().getConsumerService());
        ReflectionTestUtils.setField(cdcDaemonService, "metricsHandler", CdcInitialization.getInstance().getCdcMetricsHandler());
        ReflectionTestUtils.setField(cdcDaemonService, "cdcConnector", CdcInitialization.getInstance().getSingleCDCConnector());
        ReflectionTestUtils.setField(cdcDaemonService, "rebuildIndexExecutor", RebuildInitialization.getInstance().getTaskExecutor());

        return cdcDaemonService;
    }
}
