package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.cdc.CDCDaemonService;
import com.xforceplus.ultraman.oqsengine.cdc.connect.CDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.connect.ClusterCDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.connect.SingleCDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.impl.SphinxConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.impl.SphinxSyncExecutor;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.xforceplus.ultraman.oqsengine.cdc.constant.CDCConstant.EMPTY_BATCH_SIZE;

/**
 * desc :
 * name : CDCConfiguration
 *
 * @author : xujia
 * date : 2020/11/5
 * @since : 1.8
 */
@Configuration
public class CDCConfiguration {

    @Bean("sphinxConsumerService")
    public ConsumerService sphinxConsumerService() {
        SphinxConsumerService consumerService = new SphinxConsumerService();

        return consumerService;
    }

    @Bean("sphinxSyncExecutor")
    public SphinxSyncExecutor sphinxSyncExecutor(
        @Value("${cdc.execution.timeoutMs:30000}") int executionTimeout,
        @Value("${cdc.execution.single.consumer:true}") boolean singleConsumer) {
        SphinxSyncExecutor sphinxSyncService = new SphinxSyncExecutor();
        sphinxSyncService.setExecutionTimeout(executionTimeout);
        sphinxSyncService.setSingleSyncConsumer(singleConsumer);

        return sphinxSyncService;
    }

    @ConditionalOnExpression("'${cdc.connect.type}'.equals('cluster')")
    @Bean("clusterCDCConnector")
    public CDCConnector clusterCDCConnector(
        @Value("${cdc.connect.string}") String connectString,
        @Value("${cdc.connect.destination}") String destination,
        @Value("${cdc.connect.username}") String userName,
        @Value("${cdc.connect.password}") String password,
        @Value("${cdc.connect.subscribeFilter}") String subscribeFilter,
        @Value("${cdc.connect.batchSize:2048}") int batchSize) {


        ClusterCDCConnector clusterCanalConnector = new ClusterCDCConnector();
        clusterCanalConnector.init(connectString, destination, userName, password);

        initProperties(clusterCanalConnector, subscribeFilter, batchSize);
        return clusterCanalConnector;
    }

    @ConditionalOnExpression("'${cdc.connect.type}'.equals('single')")
    @Bean("singleCDCConnector")
    public CDCConnector singleCDCConnector(
        @Value("${cdc.connect.string}") String connectString,       //  general with ip
        @Value("${cdc.connect.port}") int port,
        @Value("${cdc.connect.destination}") String destination,
        @Value("${cdc.connect.username}") String userName,
        @Value("${cdc.connect.password}") String password,
        @Value("${cdc.connect.subscribeFilter}") String subscribeFilter,
        @Value("${cdc.connect.batchSize:2048}") int batchSize) {

        SingleCDCConnector singleCDCConnector = new SingleCDCConnector();
        singleCDCConnector.init(connectString, port, destination, userName, password);

        initProperties(singleCDCConnector, subscribeFilter, batchSize);
        return singleCDCConnector;
    }

    @Bean(destroyMethod = "stopDaemon")
    public CDCDaemonService cdcDaemonService(){
        return new CDCDaemonService();
    }

    @Bean
    CDCMetricsService cdcMetricsService(){
        return new CDCMetricsService();
    }

    private void initProperties(CDCConnector cdcConnector, String subscribeFilter, int batchSize) {
        if (!subscribeFilter.isEmpty()) {
            cdcConnector.setSubscribeFilter(subscribeFilter);
        }

        if (batchSize > EMPTY_BATCH_SIZE) {
            cdcConnector.setBatchSize(batchSize);
        }
    }
}
