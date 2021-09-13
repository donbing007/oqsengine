package com.xforceplus.ultraman.oqsengine.cdc;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.DAEMON_NODE_ID;

import com.xforceplus.ultraman.oqsengine.cdc.connect.AbstractCDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerRunner;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.common.id.node.NodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
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
    private CDCMetricsService cdcMetricsService;

    @Resource
    private AbstractCDCConnector abstractCdcConnector;

    private ConsumerRunner consumerRunner;

    private static boolean isStart = false;

    @PostConstruct
    @Override
    public void init() throws Exception {
        Integer nodeId = nodeIdGenerator.next();

        logger.info("[cdc-daemon] current node = {}", nodeId);
        if (nodeId == DAEMON_NODE_ID && !isStart) {
            logger.info("[cdc-daemon] node-{} start CDC daemon process thread...", nodeId);
            consumerRunner = new ConsumerRunner(consumerService, cdcMetricsService, abstractCdcConnector);
            consumerRunner.start();
            isStart = true;
            logger.info("[cdc-daemon] node-{} start CDC daemon process thread success...", nodeId);
        }
    }

    @PreDestroy
    @Override
    public void destroy() throws Exception {
        if (isStart) {
            logger.info("[cdc-daemon] try close CDC daemon process thread...");
            consumerRunner.shutdown();
            isStart = false;
            logger.info("[cdc-daemon] try close CDC daemon process thread success...");
        }
    }
}
