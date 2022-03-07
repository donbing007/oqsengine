package com.xforceplus.ultraman.oqsengine.cdc;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.DAEMON_NODE_ID;

import com.xforceplus.ultraman.oqsengine.cdc.connect.AbstractCDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.CDCRunner;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.service.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsHandler;
import com.xforceplus.ultraman.oqsengine.common.id.node.NodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.RebuildIndexExecutor;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CDC 服务.
 *
 * @author xujia 2020/11/3
 * @since : 1.8
 */
public class CDCDaemonService implements Lifecycle {

    final Logger logger = LoggerFactory.getLogger(CDCDaemonService.class);

    @Resource(name = "nodeIdGenerator")
    private NodeIdGenerator nodeIdGenerator;

    @Resource
    private ConsumerService consumerService;

    @Resource
    private CDCMetricsHandler metricsHandler;

    @Resource
    private AbstractCDCConnector cdcConnector;

    @Resource
    private RebuildIndexExecutor rebuildIndexExecutor;

    private CDCRunner cdcRunner;

    private static boolean isStart = false;

    @PostConstruct
    @Override
    public void init() throws Exception {
        Integer nodeId = nodeIdGenerator.next();

        logger.info("[cdc-daemon] current node = {}", nodeId);
        if (nodeId == DAEMON_NODE_ID && !isStart) {
            logger.info("[cdc-daemon] node-{} start CDC daemon process thread...", nodeId);
            cdcRunner =
                new CDCRunner(consumerService, metricsHandler, cdcConnector, rebuildIndexExecutor);

            cdcRunner.start();

            isStart = true;
            logger.info("[cdc-daemon] node-{} start CDC daemon process thread success...", nodeId);
        }
    }

    @PreDestroy
    @Override
    public void destroy() throws Exception {
        if (isStart) {
            logger.info("[cdc-daemon] try close CDC daemon process thread...");
            cdcRunner.shutdown();
            isStart = false;
            logger.info("[cdc-daemon] try close CDC daemon process thread success...");
        }
    }
}
