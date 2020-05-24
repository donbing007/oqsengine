package com.xforceplus.ultraman.oqsengine.sdk;

import akka.actor.ActorSystem;
import akka.grpc.GrpcClientSettings;
import akka.stream.ActorMaterializer;
import com.xforceplus.ultraman.metadata.grpc.Base;
import com.xforceplus.ultraman.metadata.grpc.CheckServiceClient;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.junit.Test;
import scala.concurrent.duration.Duration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class HttpsTest {

    @Test
    public void testSdkWithoutSpring() throws Exception {

        ActorSystem system = ActorSystem.create();
        ActorMaterializer mat = ActorMaterializer.create(system);

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        TrustManager[] trustManagers = InsecureTrustManagerFactory.INSTANCE.getTrustManagers();

        sslContext.init(null, trustManagers, new java.security.SecureRandom());

        GrpcClientSettings settings = GrpcClientSettings
                .connectToServiceAt("bocp-grpc.phoenix-t.xforceplus.com", 443, system)
                .withOverrideAuthority("google.fr")
                .withDeadline(Duration.Inf())
                .withUserAgent("Akka-gRPC")
                .withTls(true);


//        GrpcClientSettings settings = GrpcClientSettings.fromConfig("CheckService", system);

        CheckServiceClient entityServiceClient = CheckServiceClient.create(settings, mat, system.dispatcher());


        Base.Authorization authorization = Base.Authorization.newBuilder()
                .setAppId("5").setEnv("0")
                .build();

        entityServiceClient.checkStreaming(authorization).runForeach(x -> { System.out.println(x);  }, mat);


        Thread.sleep(5000);
    }
}
