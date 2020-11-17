package com.xforceplus.ultraman.oqsengine.core.service.integration;

import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import org.junit.Ignore;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.time.Duration;

@Ignore
public abstract class AbstractContainerTest {

    static DockerComposeContainer environment;

    static {
        environment =
                new DockerComposeContainer(new File("src/test/resources/compose-all.yaml"))
                        .withLocalCompose(true)
                        .withExposedService("mysql_1", 3306, Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30)))
                        .withExposedService("manticore0_1", 9306, Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30)))
                        .withExposedService("manticore1_1", 9306, Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30)))
                        .withExposedService("search-manticore_1", 9306, Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30)))
                        .withExposedService("redis_1", 6379, Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30)))
                        .withExposedService("canal-server_1", 11111, Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30)));

        environment.start();

        System.setProperty("MYSQL_HOST", environment.getServiceHost("mysql_1", 3306));
        System.setProperty("MYSQL_PORT", environment.getServicePort("mysql_1", 3306).toString());

        System.setProperty("REDIS_HOST", environment.getServiceHost("redis_1", 6379));
        System.setProperty("REDIS_PORT", environment.getServicePort("redis_1", 6379).toString());

        System.setProperty("MANTICORE0_HOST", environment.getServiceHost("manticore0_1", 9306));
        System.setProperty("MANTICORE0_PORT", environment.getServicePort("manticore0_1", 9306).toString());

        System.setProperty("MANTICORE1_HOST", environment.getServiceHost("manticore1_1", 9306));
        System.setProperty("MANTICORE1_PORT", environment.getServicePort("manticore1_1", 9306).toString());

        System.setProperty("SEARCH_MANTICORE_HOST", environment.getServiceHost("search-manticore_1", 9306));
        System.setProperty("SEARCH_MANTICORE_PORT", environment.getServicePort("search-manticore_1", 9306).toString());

        System.setProperty("CANAL_HOST", environment.getServiceHost("canal-server_1", 11111));
        System.setProperty("CANAL_PORT", environment.getServicePort("canal-server_1", 11111).toString());

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
}
