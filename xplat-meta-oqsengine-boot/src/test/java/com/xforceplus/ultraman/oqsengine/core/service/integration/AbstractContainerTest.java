package com.xforceplus.ultraman.oqsengine.core.service.integration;

import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import org.junit.Ignore;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.Random;

@Ignore
public abstract class AbstractContainerTest {

    static GenericContainer mysql;
    static GenericContainer manticore0;
    static GenericContainer manticore1;
    static GenericContainer searchManticore;
    static GenericContainer redis;
    static GenericContainer cannal;

    static {
        Network network = Network.newNetwork();
        mysql = new GenericContainer("mysql:5.7")
            .withNetwork(network)
            .withNetworkAliases("mysql")
            .withExposedPorts(3306)
            .withEnv("MYSQL_DATABASE", "oqsengine")
            .withEnv("MYSQL_ROOT_USERNAME", "root")
            .withEnv("MYSQL_ROOT_PASSWORD", "root")
            .withClasspathResourceMapping("mastdb.sql", "/docker-entrypoint-initdb.d/1.sql", BindMode.READ_ONLY)
            .withClasspathResourceMapping("mysql.cnf", "/etc/my.cnf", BindMode.READ_ONLY)
            .waitingFor(Wait.forListeningPort());
        mysql.start();

        manticore0 = new GenericContainer<>("manticoresearch/manticore:3.5.0")
            .withExposedPorts(9306)
            .withNetwork(network)
            .withNetworkAliases("manticore0")
            .withClasspathResourceMapping("manticore0.conf", "/manticore.conf", BindMode.READ_ONLY)
            .withCommand("/usr/bin/searchd", "--nodetach", "--config", "/manticore.conf")
            .waitingFor(Wait.forListeningPort());
        manticore0.start();

        manticore1 = new GenericContainer<>("manticoresearch/manticore:3.5.0")
            .withExposedPorts(9306)
            .withNetwork(network)
            .withNetworkAliases("manticore1")
            .withClasspathResourceMapping("manticore1.conf", "/manticore.conf", BindMode.READ_ONLY)
            .withCommand("/usr/bin/searchd", "--nodetach", "--config", "/manticore.conf")
            .waitingFor(Wait.forListeningPort());
        manticore1.start();

        searchManticore = new GenericContainer<>("manticoresearch/manticore:3.5.0")
            .withExposedPorts(9306)
            .withNetwork(network)
            .withNetworkAliases("searchManticore")
            .withClasspathResourceMapping("search-manticore.conf", "/manticore.conf", BindMode.READ_ONLY)
            .withCommand("/usr/bin/searchd", "--nodetach", "--config", "/manticore.conf")
            .dependsOn(manticore0, manticore1)
            .waitingFor(Wait.forListeningPort());
        searchManticore.start();

        redis = new GenericContainer("redis:6.0.9-alpine3.12")
            .withNetwork(network)
            .withNetworkAliases("redis")
            .withExposedPorts(6379)
            .waitingFor(Wait.forListeningPort());
        redis.start();

        System.setProperty("CANAL_DESTINATION", getRandomString(6));

        cannal = new GenericContainer("canal/canal-server:v1.1.4")
            .withNetwork(network)
            .withNetworkAliases("cannal")
            .withExposedPorts(11111)
            .withEnv("canal.instance.mysql.slaveId", "12")
            .withEnv("canal.auto.scan", "false")
            .withEnv("canal.destinations", System.getProperty("CANAL_DESTINATION"))
            .withEnv("canal.instance.master.address", "mysql:3306")
            .withEnv("canal.instance.dbUsername", "root")
            .withEnv("canal.instance.dbPassword", "root")
            .withEnv("canal.instance.filter.regex", ".*\\.oqsbigentity.*")
            .dependsOn(mysql)
            .waitingFor(Wait.forListeningPort());
        cannal.start();

        System.setProperty("MYSQL_HOST", mysql.getContainerIpAddress());
        System.setProperty("MYSQL_PORT", mysql.getFirstMappedPort().toString());

        System.setProperty("REDIS_HOST", redis.getContainerIpAddress());
        System.setProperty("REDIS_PORT", redis.getFirstMappedPort().toString());

        System.setProperty("MANTICORE0_HOST", manticore0.getContainerIpAddress());
        System.setProperty("MANTICORE0_PORT", manticore0.getFirstMappedPort().toString());

        System.setProperty("MANTICORE1_HOST", manticore1.getContainerIpAddress());
        System.setProperty("MANTICORE1_PORT", manticore1.getFirstMappedPort().toString());

        System.setProperty("SEARCH_MANTICORE_HOST", searchManticore.getContainerIpAddress());
        System.setProperty("SEARCH_MANTICORE_PORT", searchManticore.getFirstMappedPort().toString());

        System.setProperty("CANAL_HOST", cannal.getContainerIpAddress());
        System.setProperty("CANAL_PORT", cannal.getFirstMappedPort().toString());

        System.setProperty(
            "MYSQL_JDBC",
            String.format("jdbc:mysql://%s:%s/oqsengine?useUnicode=true&serverTimezone=GMT&useSSL=false&characterEncoding=utf8",
                System.getProperty("MYSQL_HOST"), System.getProperty("MYSQL_PORT")));

        System.setProperty("MANTICORE0_JDBC",
            String.format("jdbc:mysql://%s:%s/oqsengine?characterEncoding=utf8&maxAllowedPacket=512000&useHostsInPrivileges=false&useLocalSessionState=true&serverTimezone=Asia/Shanghai",
                System.getProperty("MANTICORE0_HOST"), System.getProperty("MANTICORE0_PORT")));

        System.setProperty("MANTICORE1_JDBC",
            String.format("jdbc:mysql://%s:%s/oqsengine?characterEncoding=utf8&maxAllowedPacket=512000&useHostsInPrivileges=false&useLocalSessionState=true&serverTimezone=Asia/Shanghai",
                System.getProperty("MANTICORE1_HOST"), System.getProperty("MANTICORE1_PORT")));

        System.setProperty("SEARCH_MANTICORE_JDBC",
            String.format("jdbc:mysql://%s:%s/oqsengine?characterEncoding=utf8&maxAllowedPacket=512000&useHostsInPrivileges=false&useLocalSessionState=true&serverTimezone=Asia/Shanghai",
                System.getProperty("SEARCH_MANTICORE_HOST"), System.getProperty("SEARCH_MANTICORE_PORT")));

        System.setProperty(DataSourceFactory.CONFIG_FILE, "./src/test/resources/oqsengine-ds.conf");

        System.out.println(System.getProperty("MANTICORE0_JDBC"));
        System.out.println(System.getProperty("MANTICORE1_JDBC"));
    }

    private static String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }
}
