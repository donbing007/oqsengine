package com.xforceplus.ultraman.oqsengine.sdk.config.init;

import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import com.xforceplus.ultraman.metadata.grpc.CheckServiceClient;
import com.xforceplus.ultraman.oqsengine.sdk.config.AuthSearcherConfig;
import com.xforceplus.ultraman.oqsengine.sdk.event.MetadataModuleGotEvent;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.MetadataRepository;
import com.xforceplus.xplat.galaxy.grpc.client.LongConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

/**
 * module grpc init service
 */
public class ModuleInitService implements SmartInitializingSingleton {

    private Logger logger = LoggerFactory.getLogger(ModuleInitService.class);

    @Autowired
    CheckServiceClient checkServiceClient;

    @Autowired
    private ActorMaterializer mat;

    @Autowired
    private AuthSearcherConfig config;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    MetadataRepository store;

    @Value("${xplat.oqsengine.sdk.init-size:5}")
    private Integer size;

    @Value("${xplat.oqsengine.sdk.init-time:10}")
    private Integer time;

    @Override
    public void afterSingletonsInstantiated() {
        //INIT Count
        CountDownLatch countDownLatch = new CountDownLatch(1);

        com.xforceplus.ultraman.metadata.grpc.Base.Authorization request = com.xforceplus
                .ultraman.metadata.grpc.Base.Authorization.newBuilder()
                .setAppId(config.getAppId())
                .setEnv(config.getEnv())
                .setTenantId(config.getTenant())
                .build();

        LongConnect.safeSource(2, 20
                , () -> checkServiceClient.checkStreaming(request))
                .groupedWithin(size, Duration.ofSeconds(time))
                .runWith(Sink.foreach(x -> {

                    logger.info("Got module size {}", x.size());
                    MetadataModuleGotEvent event = new MetadataModuleGotEvent(request, x);
                    publisher.publishEvent(event);
                    logger.info("dispatched module ");

                    if (countDownLatch.getCount() > 0) {
                        logger.info("first Modules lock count down");
                        countDownLatch.countDown();
                    }
                }), mat);

        logger.info("------- Waiting For Module init expected max module size {} max waiting time {}s-------", size, time);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
