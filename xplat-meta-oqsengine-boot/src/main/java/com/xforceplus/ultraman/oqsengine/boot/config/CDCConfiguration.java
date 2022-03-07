package com.xforceplus.ultraman.oqsengine.boot.config;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.EMPTY_BATCH_SIZE;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.INIT_ID;

import com.xforceplus.ultraman.oqsengine.cdc.CDCDaemonService;
import com.xforceplus.ultraman.oqsengine.cdc.connect.AbstractCDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.connect.ClusterCDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.connect.SingleCDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.error.DefaultErrorRecorder;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.error.ErrorRecorder;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.service.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.service.DefaultConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsHandler;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.DefaultCDCMetricsHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CDC 配置.
 */
@Configuration
public class CDCConfiguration {

    /**
     * sphinxQL 数据消费服务.
     */
    @Bean("consumerService")
    public ConsumerService consumerService(
        @Value("${cdc.consumer.checkCommitReady:true}") boolean checkCommitReady,
        @Value("${cdc.consumer.skipCommitId:-1}") long skipCommitId) {
        DefaultConsumerService consumerService = new DefaultConsumerService();
        consumerService.setCheckCommitReady(checkCommitReady);
        if (skipCommitId > INIT_ID) {
            consumerService.setSkipCommitId(skipCommitId);
        }
        return consumerService;
    }

    /**
     * CDC 集群的cannal连接器.
     */
    @ConditionalOnExpression("'${cdc.connect.type}'.equals('cluster')")
    @Bean("clusterCDCConnector")
    public AbstractCDCConnector clusterCDCConnector(
        @Value("${cdc.connect.host}") String host,
        @Value("${cdc.connect.destination:}") String destination,
        @Value("${cdc.connect.username}") String userName,
        @Value("${cdc.connect.password}") String password,
        @Value("${cdc.connect.batchSize:2048}") int batchSize) {


        ClusterCDCConnector clusterCanalConnector = new ClusterCDCConnector(host, destination, userName, password);
        //  创建connector实例
        clusterCanalConnector.init();

        initProperties(clusterCanalConnector, batchSize);

        return clusterCanalConnector;
    }

    /**
     * 非集群的cannal连接器.
     */
    @ConditionalOnExpression("'${cdc.connect.type}'.equals('single')")
    @Bean("singleCDCConnector")
    public AbstractCDCConnector singleCDCConnector(
        @Value("${cdc.connect.host}") String host,       //  general with ip
        @Value("${cdc.connect.port}") int port,
        @Value("${cdc.connect.destination:}") String destination,
        @Value("${cdc.connect.username}") String userName,
        @Value("${cdc.connect.password}") String password,
        @Value("${cdc.connect.batchSize:2048}") int batchSize) {

        SingleCDCConnector singleCDCConnector = new SingleCDCConnector(host, destination, userName, password, port);
        //  创建connector实例
        singleCDCConnector.init();

        initProperties(singleCDCConnector, batchSize);

        return singleCDCConnector;
    }

    @Bean
    public CDCDaemonService cdcDaemonService() {
        return new CDCDaemonService();
    }

    @Bean
    public ErrorRecorder errorRecorder() {
        return new DefaultErrorRecorder();
    }

    @Bean
    CDCMetricsHandler cdcMetricsHandler() {
        return new DefaultCDCMetricsHandler();
    }

    private void initProperties(AbstractCDCConnector abstractCdcConnector, int batchSize) {
        if (batchSize > EMPTY_BATCH_SIZE) {
            abstractCdcConnector.setBatchSize(batchSize);
        }
    }
}
