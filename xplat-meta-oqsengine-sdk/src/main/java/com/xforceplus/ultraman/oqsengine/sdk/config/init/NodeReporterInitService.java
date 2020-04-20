package com.xforceplus.ultraman.oqsengine.sdk.config.init;

import akka.NotUsed;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.xforceplus.ultraman.metadata.grpc.NodeServiceClient;
import com.xforceplus.ultraman.metadata.grpc.NodeUp;
import com.xforceplus.ultraman.oqsengine.sdk.config.AuthSearcherConfig;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.CurrentVersion;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.MetadataRepository;
import com.xforceplus.xplat.common.utils.JsonHelper;
import com.xforceplus.xplat.galaxy.grpc.client.LongConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * report node info every config interval
 */
public class NodeReporterInitService implements InitializingBean {

    @Autowired
    private NodeServiceClient nodeServiceClient;

    private Logger logger = LoggerFactory.getLogger(NodeReporterInitService.class);

    @Autowired
    private ActorMaterializer mat;

    @Autowired
    private AuthSearcherConfig config;

    @Value("${spring.application.name:default}")
    private String applicationName;

    @Autowired
    private MetadataRepository metadataRepository;

    @Override
    public void afterPropertiesSet() throws Exception {

        //TODO config-able
        Source<NodeUp, NotUsed> reportSource = Source.tick(Duration.ZERO, Duration.ofSeconds(30), "report")
                .map(x -> fetchCurrentNodeStatus())
                .mapMaterializedValue(m -> NotUsed.getInstance());

        LongConnect.safeSource(2, 20
                , () -> nodeServiceClient.report(reportSource))
                .runWith(Sink.foreach(x -> {
                    logger.debug("Got reply at {}", LocalDateTime.now());
                }), mat);
    }

    private NodeUp fetchCurrentNodeStatus() throws SocketException, UnknownHostException {
        Long appId = Long.parseLong(config.getAppId());
        Long envId = Long.parseLong(config.getEnv());

        String sdkVersion = getClass().getPackage().getImplementationVersion();
        System.out.println("show version" + sdkVersion);
        CurrentVersion currentVersion = metadataRepository.currentVersion();

        return NodeUp.newBuilder()
                .setAppId(appId)
                .setEnvId(envId)
                .setCode(getNodeName())
                .setName(applicationName)
                .setSdkVersion(Optional.ofNullable(sdkVersion).orElse("UNKNOWN-FROM-TEST"))
                .setCurrentVersion(JsonHelper.toJsonStr(currentVersion))
                .setStatus("OK")
                .build();
    }

    private String getNodeName() throws SocketException, UnknownHostException {
        InetAddress inetAddress = InetAddress.getLocalHost();
        NetworkInterface byInetAddress = NetworkInterface.getByInetAddress(inetAddress);

        byte[] mac = byInetAddress.getHardwareAddress();

        String macName = null;
        if (mac != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }

            macName = sb.toString();
        }

        return inetAddress.getHostAddress() + ":" + Optional.ofNullable(macName).orElse("UNKNOWN");
    }
}
