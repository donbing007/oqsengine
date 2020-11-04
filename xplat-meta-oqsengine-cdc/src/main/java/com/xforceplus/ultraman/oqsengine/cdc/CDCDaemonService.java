package com.xforceplus.ultraman.oqsengine.cdc;

import com.xforceplus.ultraman.oqsengine.cdc.config.CDCConsumerProperties;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerRunner;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.CDCMetricsCallback;
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

    @Resource
    private NodeIdGenerator nodeIdGenerator;

    @Resource
    private ConsumerService consumerService;

    @Resource
    private CDCMetricsCallback cdcMetricsCallback;

    @Resource
    private CDCConsumerProperties cdcConsumerProperties;

    private ConsumerRunner consumerRunner;

    @PostConstruct
    public void startDaemon() {

        Integer nodeId = nodeIdGenerator.next();

        if (nodeId == DAEMON_NODE_ID && null == consumerRunner) {
            logger.info("node {} 启动CDC守护进程", nodeId);
            consumerRunner = new ConsumerRunner(consumerService, cdcMetricsCallback, cdcConsumerProperties);
            consumerRunner.start();
            logger.info("node {} 启动CDC守护进程成功", nodeId);
        }
    }
}
