package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * @author dongbin
 * @version 0.1 2020/12/2 14:57
 * @since 1.8
 */
public abstract class AbstractContainerTest {

    private static GenericContainer manticore0;
    private static GenericContainer manticore1;
    private static GenericContainer searchManticore;
    static GenericContainer redis;

    static {
        Network network = Network.newNetwork();
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

        redis = new GenericContainer("redis:6.0.9-alpine3.12")
            .withNetworkAliases("redis")
            .withExposedPorts(6379)
            .waitingFor(Wait.forListeningPort());
        redis.start();

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
        System.setProperty("REDIS_HOST", redis.getContainerIpAddress());
        System.setProperty("REDIS_PORT", redis.getFirstMappedPort().toString());
    }

}
