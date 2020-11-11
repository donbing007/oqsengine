package com.xforceplus.ultraman.oqsengine.cdc;

import com.xforceplus.ultraman.oqsengine.cdc.connect.CDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerRunner;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.common.id.node.NodeIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import static com.xforceplus.ultraman.oqsengine.cdc.constant.CDCConstant.DAEMON_NODE_ID;

/**
 * desc :
 * name : CDCDaemonService
 *
 * @author : xujia
 * date : 2020/11/3
 * @since : 1.8
 */
public class CDCDaemonService {

    final Logger logger = LoggerFactory.getLogger(CDCDaemonService.class);

    @Resource(name = "nodeIdGenerator")
    private NodeIdGenerator nodeIdGenerator;

    @Resource
    private ConsumerService consumerService;

    @Resource
    private CDCMetricsService cdcMetricsService;

    @Resource
    private CDCConnector cdcConnector;

    private ConsumerRunner consumerRunner;

    private static boolean isStart = false;

    public void stopDaemon() {
        if (isStart) {
            logger.info("开始关闭CDC守护进程...");
            consumerRunner.shutdown();
            isStart = false;
            logger.info("关闭CDC守护进程成功...");
        }
    }

    @PostConstruct
    public void startDaemon() {

        Integer nodeId = nodeIdGenerator.next();

        logger.info("current node = {}", nodeId);
        if (nodeId == DAEMON_NODE_ID) {
            logger.info("node-{} 启动CDC守护进程", nodeId);
            consumerRunner = new ConsumerRunner(consumerService, cdcMetricsService, cdcConnector);
            consumerRunner.start();
            isStart = true;
            logger.info("node-{} 启动CDC守护进程成功", nodeId);
        }
    }
}
