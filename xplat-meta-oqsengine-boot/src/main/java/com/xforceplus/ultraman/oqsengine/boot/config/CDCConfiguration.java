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

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.EMPTY_BATCH_SIZE;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.INIT_ID;

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
    public ConsumerService sphinxConsumerService(
            @Value("${cdc.consumer.checkCommitReady:true}") boolean checkCommitReady,
            @Value("${cdc.consumer.skipCommitId:-1}") long skipCommitId) {
        SphinxConsumerService consumerService = new SphinxConsumerService();
        consumerService.setCheckCommitReady(checkCommitReady);
        if (skipCommitId > INIT_ID) {
            consumerService.setSkipCommitId(skipCommitId);
        }
        return consumerService;
    }

    @Bean("syncExecutor")
    public SphinxSyncExecutor syncExecutor() {
        return new SphinxSyncExecutor();
    }

    @ConditionalOnExpression("'${cdc.connect.type}'.equals('cluster')")
    @Bean("clusterCDCConnector")
    public CDCConnector clusterCDCConnector(
        @Value("${cdc.connect.host}") String host,
        @Value("${cdc.connect.destination:}") String destination,
        @Value("${cdc.connect.username}") String userName,
        @Value("${cdc.connect.password}") String password,
        @Value("${cdc.connect.batchSize:2048}") int batchSize) {


        ClusterCDCConnector clusterCanalConnector = new ClusterCDCConnector();
        clusterCanalConnector.init(host, destination, userName, password);

        initProperties(clusterCanalConnector, batchSize);
        return clusterCanalConnector;
    }

    @ConditionalOnExpression("'${cdc.connect.type}'.equals('single')")
    @Bean("singleCDCConnector")
    public CDCConnector singleCDCConnector(
        @Value("${cdc.connect.host}") String host,       //  general with ip
        @Value("${cdc.connect.port}") int port,
        @Value("${cdc.connect.destination:}") String destination,
        @Value("${cdc.connect.username}") String userName,
        @Value("${cdc.connect.password}") String password,
        @Value("${cdc.connect.batchSize:2048}") int batchSize) {

        SingleCDCConnector singleCDCConnector = new SingleCDCConnector();
        singleCDCConnector.init(host, port, destination, userName, password);

        initProperties(singleCDCConnector, batchSize);
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

    private void initProperties(CDCConnector cdcConnector, int batchSize) {
        if (batchSize > EMPTY_BATCH_SIZE) {
            cdcConnector.setBatchSize(batchSize);
        }
    }
}
