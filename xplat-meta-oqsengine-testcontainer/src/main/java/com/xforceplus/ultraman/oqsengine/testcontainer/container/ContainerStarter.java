package com.xforceplus.ultraman.oqsengine.testcontainer.container;

import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * desc :
 * name : AbstractContainer
 *
 * @author : xujia
 * date : 2020/12/16
 * @since : 1.8
 */
@Ignore
public final class ContainerStarter {

    static final Logger logger = LoggerFactory.getLogger(ContainerStarter.class);

    private static GenericContainer redis;
    private static GenericContainer mysql;
    private static GenericContainer manticore0;
    private static GenericContainer manticore1;
    private static GenericContainer searchManticore;
    private static GenericContainer cannal;
    private static Network network = Network.newNetwork();

    static {
        System.setProperty("ds", "./src/test/resources/oqsengine-ds.conf");
    }

    private static void waitStop(GenericContainer genericContainer) {
        while (genericContainer.isRunning()) {
            try {
                logger.info("The {} container is not closed, etc. 5 ms.", genericContainer.getDockerImageName());
                TimeUnit.MILLISECONDS.sleep(5);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    /**
     * 重置所有打开过的容器.
     */
    public static synchronized void reset() {
        stopCannal();

        stopManticore();

        stopMysql();

        stopRedis();
    }

    public static synchronized void startRedis() {
        if (redis == null) {
            redis = new GenericContainer("redis:6.0.9-alpine3.12")
                .withNetwork(network)
                .withNetworkAliases("redis")
                .withExposedPorts(6379)
                .waitingFor(Wait.forListeningPort());
            redis.start();

            System.setProperty("REDIS_HOST", redis.getContainerIpAddress());
            System.setProperty("REDIS_PORT", redis.getFirstMappedPort().toString());

            logger.info("Start Redis server.({}:{})", redis.getContainerIpAddress(), redis.getFirstMappedPort());
        }
    }

    public static synchronized void stopRedis() {
        if (redis != null) {
            redis.stop();
            waitStop(redis);
            redis = null;

            System.clearProperty("REDIS_HOST");
            System.clearProperty("REDIS_PORT");

            logger.info("Closed redis container!");
        }
    }

    public static synchronized void startMysql() {
        if (mysql == null) {
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

            System.setProperty("MYSQL_HOST", mysql.getContainerIpAddress());
            System.setProperty("MYSQL_PORT", mysql.getFirstMappedPort().toString());

            System.setProperty(
                "MYSQL_JDBC",
                String.format("jdbc:mysql://%s:%s/oqsengine?useUnicode=true&serverTimezone=GMT&useSSL=false&characterEncoding=utf8",
                    System.getProperty("MYSQL_HOST"), System.getProperty("MYSQL_PORT")));

            logger.info("Start mysql server.({}:{})", mysql.getContainerIpAddress(), mysql.getFirstMappedPort());
        }
    }

    public static synchronized void stopMysql() {
        if (mysql != null) {
            mysql.stop();
            waitStop(mysql);
            mysql = null;

            System.clearProperty("MYSQL_HOST");
            System.clearProperty("MYSQL_PORT");
            System.clearProperty("MYSQL_JDBC");

            logger.info("Closed mysql container!");
        }
    }

    public static synchronized void startManticore() {
        if (manticore0 == null) {
            manticore0 = new GenericContainer<>("manticoresearch/manticore:3.5.0")
                .withExposedPorts(9306)
                .withNetwork(network)
                .withNetworkAliases("manticore0")
                .withClasspathResourceMapping("manticore0.conf", "/manticore.conf", BindMode.READ_ONLY)
                .withCommand("/usr/bin/searchd", "--nodetach", "--config", "/manticore.conf")
                .waitingFor(Wait.forListeningPort());
            manticore0.start();

            System.setProperty("MANTICORE0_HOST", manticore0.getContainerIpAddress());
            System.setProperty("MANTICORE0_PORT", manticore0.getFirstMappedPort().toString());

            System.setProperty("MANTICORE0_JDBC",
                String.format("jdbc:mysql://%s:%s/oqsengine?characterEncoding=utf8&maxAllowedPacket=512000&useHostsInPrivileges=false&useLocalSessionState=true&serverTimezone=Asia/Shanghai",
                    System.getProperty("MANTICORE0_HOST"), System.getProperty("MANTICORE0_PORT")));


            logger.info("Start manticore server.({}:{})", manticore0.getContainerIpAddress(), manticore0.getFirstMappedPort());
        }

        if (manticore1 == null) {
            manticore1 = new GenericContainer<>("manticoresearch/manticore:3.5.0")
                .withExposedPorts(9306)
                .withNetwork(network)
                .withNetworkAliases("manticore1")
                .withClasspathResourceMapping("manticore1.conf", "/manticore.conf", BindMode.READ_ONLY)
                .withCommand("/usr/bin/searchd", "--nodetach", "--config", "/manticore.conf")
                .waitingFor(Wait.forListeningPort());
            manticore1.start();

            System.setProperty("MANTICORE1_HOST", manticore1.getContainerIpAddress());
            System.setProperty("MANTICORE1_PORT", manticore1.getFirstMappedPort().toString());

            System.setProperty("MANTICORE1_JDBC",
                String.format("jdbc:mysql://%s:%s/oqsengine?characterEncoding=utf8&maxAllowedPacket=512000&useHostsInPrivileges=false&useLocalSessionState=true&serverTimezone=Asia/Shanghai",
                    System.getProperty("MANTICORE1_HOST"), System.getProperty("MANTICORE1_PORT")));


            logger.info("Start manticore server.({}:{})", manticore1.getContainerIpAddress(), manticore1.getFirstMappedPort());
        }

        if (searchManticore == null) {
            searchManticore = new GenericContainer<>("manticoresearch/manticore:3.5.0")
                .withExposedPorts(9306)
                .withNetwork(network)
                .withNetworkAliases("searchManticore")
                .withClasspathResourceMapping("search-manticore.conf", "/manticore.conf", BindMode.READ_ONLY)
                .withCommand("/usr/bin/searchd", "--nodetach", "--config", "/manticore.conf")
                .dependsOn(manticore0, manticore1)
                .waitingFor(Wait.forListeningPort());
            searchManticore.start();

            System.setProperty("SEARCH_MANTICORE_HOST", searchManticore.getContainerIpAddress());
            System.setProperty("SEARCH_MANTICORE_PORT", searchManticore.getFirstMappedPort().toString());

            System.setProperty("SEARCH_MANTICORE_JDBC",
                String.format("jdbc:mysql://%s:%s/oqsengine?characterEncoding=utf8&maxAllowedPacket=512000&useHostsInPrivileges=false&useLocalSessionState=true&serverTimezone=Asia/Shanghai",
                    System.getProperty("SEARCH_MANTICORE_HOST"), System.getProperty("SEARCH_MANTICORE_PORT")));

            logger.info("Start manticore server.({}:{})", searchManticore.getContainerIpAddress(), searchManticore.getFirstMappedPort());
        }
    }

    public static synchronized void stopManticore() {
        if (searchManticore != null) {
            searchManticore.stop();
            waitStop(searchManticore);
            searchManticore = null;

            System.clearProperty("SEARCH_MANTICORE_HOST");
            System.clearProperty("SEARCH_MANTICORE_PORT");
            System.clearProperty("SEARCH_MANTICORE_JDBC");

            logger.info("Closed searchManticore container!");
        }

        if (manticore0 != null) {
            manticore0.stop();
            waitStop(manticore0);
            manticore0 = null;

            System.clearProperty("MANTICORE0_HOST");
            System.clearProperty("MANTICORE0_PORT");
            System.clearProperty("MANTICORE0_JDBC");

            logger.info("Closed manticore0 container!");
        }

        if (manticore1 != null) {
            manticore1.stop();
            waitStop(manticore1);
            manticore1 = null;

            System.clearProperty("MANTICORE1_HOST");
            System.clearProperty("MANTICORE1_PORT");
            System.clearProperty("MANTICORE1_JDBC");

            logger.info("Closed manticore1 container!");
        }
    }

    public static synchronized void startCannal() {
        if (cannal == null) {
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

            System.setProperty("CANAL_HOST", cannal.getContainerIpAddress());
            System.setProperty("CANAL_PORT", cannal.getFirstMappedPort().toString());

            logger.info("Start cannal server.({}:{})", cannal.getContainerIpAddress(), cannal.getFirstMappedPort());
        }
    }

    public static synchronized void stopCannal() {
        if (cannal != null) {
            cannal.stop();
            waitStop(cannal);
            cannal = null;

            System.clearProperty("CANAL_DESTINATION");
            System.clearProperty("CANAL_HOST");
            System.clearProperty("CANAL_PORT");

            logger.info("Closed cannal container!");
        }
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
