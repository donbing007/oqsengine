package com.xforceplus.ultraman.oqsengine.core.service.integration;

import com.xforceplus.ultraman.oqsengine.status.StatusService;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;

@Ignore
public abstract class AbstractCDCTest {


    protected static GenericContainer manticore0;
    protected static GenericContainer manticore1;
    protected static GenericContainer searchManticore;

    protected static GenericContainer redis;
    protected static DockerComposeContainer environment;

    protected StorageStrategyFactory masterStorageStrategyFactory;

    static {
        Network network = Network.newNetwork();
        initDockerCompose();
        initManticore(network);

        redis = new GenericContainer("redis:6.0.9-alpine3.12").withExposedPorts(6379);
        redis.start();

        String redisUri = "redis://%s:%s";

        System.setProperty("REDIS_URI", String.format(redisUri, redis.getContainerIpAddress(), redis.getFirstMappedPort().toString()));
    }

    protected static void initDockerCompose() {
        environment =
                new DockerComposeContainer(new File("src/test/resources/compose-all.yaml"))
                        .withExposedService("mysql_1", 3306)
                        .withExposedService("canal-server_1", 11111);

        environment.start();

        String mysqlUrl = String.format(
                "jdbc:mysql://%s:%d/oqsengine?characterEncoding=utf8&maxAllowedPacket=512000&useHostsInPrivileges=false&useLocalSessionState=true&serverTimezone=UTC",
                environment.getServiceHost("mysql_1", 3306), environment.getServicePort("mysql_1", 3306));

        System.setProperty("MYSQL_JDBC_URL", mysqlUrl);

        System.setProperty("CDC_CONNECT",  environment.getServiceHost("canal-server_1", 11111));
        System.setProperty("CDC_PORT",  environment.getServicePort("canal-server_1", 11111).toString());

    }


    protected static void initManticore(Network network) {

        manticore0 = new GenericContainer<>("manticoresearch/manticore:3.5.0").withExposedPorts(9306)
                .withNetwork(network)
                .withNetworkAliases("manticore0")
                .withClasspathResourceMapping("manticore0.conf", "/manticore.conf", BindMode.READ_ONLY)
                .withCommand("/usr/bin/searchd", "--nodetach", "--config", "/manticore.conf")
                .waitingFor(Wait.forListeningPort());
        manticore0.start();

        manticore1 = new GenericContainer<>("manticoresearch/manticore:3.5.0").withExposedPorts(9306)
                .withNetwork(network)
                .withNetworkAliases("manticore1")
                .withClasspathResourceMapping("manticore1.conf", "/manticore.conf", BindMode.READ_ONLY)
                .withCommand("/usr/bin/searchd", "--nodetach", "--config", "/manticore.conf")
                .waitingFor(Wait.forListeningPort());
        manticore1.start();

        searchManticore = new GenericContainer<>("manticoresearch/manticore:3.5.0").withExposedPorts(9306)
                .withNetwork(network)
                .withNetworkAliases("searchManticore")
                .withClasspathResourceMapping("search-manticore.conf", "/manticore.conf", BindMode.READ_ONLY)
                .withCommand("/usr/bin/searchd", "--nodetach", "--config", "/manticore.conf")
                .dependsOn(manticore0, manticore1)
                .waitingFor(Wait.forListeningPort());
        searchManticore.start();

        String write0Jdbc = String.format(
                "jdbc:mysql://%s:%d/oqsengine?characterEncoding=utf8&maxAllowedPacket=512000&useHostsInPrivileges=false&useLocalSessionState=true&serverTimezone=UTC",
                manticore0.getContainerIpAddress(), manticore0.getFirstMappedPort());
        String write1Jdbc = String.format(
                "jdbc:mysql://%s:%d/oqsengine?characterEncoding=utf8&maxAllowedPacket=512000&useHostsInPrivileges=false&useLocalSessionState=true&serverTimezone=UTC",
                manticore1.getContainerIpAddress(), manticore1.getFirstMappedPort());

        String searchJdbc = String.format(
                "jdbc:mysql://%s:%d/oqsengine?characterEncoding=utf8&maxAllowedPacket=512000&useHostsInPrivileges=false&useLocalSessionState=true&serverTimezone=UTC",
                searchManticore.getContainerIpAddress(), searchManticore.getFirstMappedPort());

        System.setProperty("MANTICORE_WRITE0_JDBC_URL", write0Jdbc);
        System.setProperty("MANTICORE_WRITE1_JDBC_URL", write1Jdbc);
        System.setProperty("MANTICORE_SEARCH_JDBC_URL", searchJdbc);

        System.setProperty("MANTICORE_JDBC_URL", searchJdbc);
    }
}
