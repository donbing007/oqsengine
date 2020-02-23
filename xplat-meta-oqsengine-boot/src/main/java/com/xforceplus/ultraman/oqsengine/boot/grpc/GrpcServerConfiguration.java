package com.xforceplus.ultraman.oqsengine.boot.grpc;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import com.xforceplus.ultraman.oqsengine.boot.grpc.service.EntityServiceOqs;
import com.xforceplus.ultraman.oqsengine.sdk.EntityServicePowerApiHandlerFactory;
import com.xforceplus.xplat.galaxy.grpc.GrpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(GrpcServerProperties.class)
@Configuration
public class GrpcServerConfiguration {

    private Logger logger = LoggerFactory.getLogger(GrpcServerConfiguration.class);

    @Autowired
    private GrpcServerProperties properties;

    @Bean(destroyMethod = "terminate")
    public GrpcServer gprcServer(EntityServiceOqs oqs){

        ActorSystem actorSystem = ActorSystem.create();
        ActorMaterializer actorMaterializer = ActorMaterializer.create(actorSystem);

        GrpcServer grpcServer = new GrpcServer(actorSystem, actorMaterializer);


        grpcServer.run(properties.getHost(), properties.getPort()
                , EntityServicePowerApiHandlerFactory.create(oqs, actorMaterializer, actorSystem))
                .thenAccept(x -> {
                    logger.info("EntityService is on {}", x.localAddress() );
                });
        return grpcServer;
    }

}
