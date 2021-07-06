package com.xforceplus.ultraman.oqsengine.testcontainer.container;


import java.time.Duration;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * 容器启动器.
 */
@Ignore
public final class ContainerStarter {

    static final Logger LOGGER = LoggerFactory.getLogger(ContainerStarter.class);



    private static GenericContainer redis;
    private static GenericContainer mysql;
    private static GenericContainer manticore0;
    private static GenericContainer manticore1;
    private static GenericContainer searchManticore;
    private static GenericContainer cannal;

    private static final int WAIT_START_TIME_OUT = 200;
    private static final Network NETWORK = Network.newNetwork();

    static {
        System.setProperty("ds", "./src/test/resources/oqsengine-ds.conf");
    }

    private static void waitStop(GenericContainer genericContainer) {
        while (genericContainer.isRunning()) {
            try {
                LOGGER.info("The {} container is not closed, etc. 5 ms.", genericContainer.getDockerImageName());
                TimeUnit.MILLISECONDS.sleep(5);
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
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

    /**
     * 开始redis容器.
     */
    public static synchronized void startRedis() {
        if (redis == null) {
            redis = new GenericContainer("redis:6.0.9-alpine3.12")
                .withNetwork(NETWORK)
                .withNetworkAliases("redis")
                .withExposedPorts(6379)
                .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(WAIT_START_TIME_OUT)));
            redis.start();
            redis.followOutput((Consumer<OutputFrame>) outputFrame -> {
                LOGGER.info(outputFrame.getUtf8String());
            });

            System.setProperty("REDIS_HOST", redis.getContainerIpAddress());
            System.setProperty("REDIS_PORT", redis.getFirstMappedPort().toString());

            LOGGER.info("Start Redis server.({}:{})", redis.getContainerIpAddress(), redis.getFirstMappedPort());
        }
    }

    /**
     * 结束redis容器.
     */
    public static synchronized void stopRedis() {
        if (redis != null) {
            redis.stop();
            waitStop(redis);
            redis = null;

            System.clearProperty("REDIS_HOST");
            System.clearProperty("REDIS_PORT");

            LOGGER.info("Closed redis container!");
        }

    }

    /**
     * 开始 mysql 容器.
     */
    public static synchronized void startMysql() {
        if (mysql == null) {
            mysql = new GenericContainer("mysql:5.7")
                .withNetwork(NETWORK)
                .withNetworkAliases("mysql")
                .withExposedPorts(3306)
                .withEnv("MYSQL_DATABASE", "oqsengine")
                .withEnv("MYSQL_ROOT_USERNAME", "root")
                .withEnv("MYSQL_ROOT_PASSWORD", "root")
                .withClasspathResourceMapping("mastdb.sql", "/docker-entrypoint-initdb.d/1.sql", BindMode.READ_ONLY)
                .withClasspathResourceMapping("mysql.cnf", "/etc/my.cnf", BindMode.READ_ONLY)
                .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(WAIT_START_TIME_OUT)));
            mysql.start();
            mysql.followOutput((Consumer<OutputFrame>) outputFrame -> {
                LOGGER.info(outputFrame.getUtf8String());
            });

            System.setProperty("MYSQL_HOST", mysql.getContainerIpAddress());
            System.setProperty("MYSQL_PORT", mysql.getFirstMappedPort().toString());

            System.setProperty(
                "MYSQL_JDBC",
                String.format(
                    "jdbc:mysql://%s:%s/oqsengine?useUnicode=true&serverTimezone=GMT&useSSL=false&characterEncoding=utf8",
                    System.getProperty("MYSQL_HOST"), System.getProperty("MYSQL_PORT")));

            LOGGER.info("Start mysql server.({}:{})", mysql.getContainerIpAddress(), mysql.getFirstMappedPort());
        }
    }

    /**
     * 结束mysql容器.
     */
    public static synchronized void stopMysql() {
        if (mysql != null) {
            mysql.stop();
            waitStop(mysql);
            mysql = null;

            System.clearProperty("MYSQL_HOST");
            System.clearProperty("MYSQL_PORT");
            System.clearProperty("MYSQL_JDBC");

            LOGGER.info("Closed mysql container!");
        }
    }

    /**
     * 开始 manticore 容器.
     */
    public static synchronized void startManticore() {
        if (manticore0 == null) {
            manticore0 = new GenericContainer<>("manticoresearch/manticore:3.5.4")
                .withExposedPorts(9306)
                .withNetwork(NETWORK)
                .withNetworkAliases("manticore0")
                .withClasspathResourceMapping("manticore0.conf", "/manticore.conf", BindMode.READ_ONLY)
                .withCommand("/usr/bin/searchd", "--nodetach", "--config", "/manticore.conf")
                .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(WAIT_START_TIME_OUT)));
            manticore0.start();
            manticore0.followOutput((Consumer<OutputFrame>) outputFrame -> {
                LOGGER.info(outputFrame.getUtf8String());
            });

            System.setProperty("MANTICORE0_HOST", manticore0.getContainerIpAddress());
            System.setProperty("MANTICORE0_PORT", manticore0.getFirstMappedPort().toString());

            System.setProperty("MANTICORE0_JDBC",
                String.format(
                    "jdbc:mysql://%s:%s/oqsengine?characterEncoding=utf8&maxAllowedPacket=512000&"
                        + "useHostsInPrivileges=false&useLocalSessionState=true&serverTimezone=Asia/Shanghai",
                    System.getProperty("MANTICORE0_HOST"), System.getProperty("MANTICORE0_PORT")));


            LOGGER
                .info("Start manticore server.({}:{})", manticore0.getContainerIpAddress(),
                    manticore0.getFirstMappedPort());
        }

        if (manticore1 == null) {
            manticore1 = new GenericContainer<>("manticoresearch/manticore:3.5.4")
                .withExposedPorts(9306)
                .withNetwork(NETWORK)
                .withNetworkAliases("manticore1")
                .withClasspathResourceMapping("manticore1.conf", "/manticore.conf", BindMode.READ_ONLY)
                .withCommand("/usr/bin/searchd", "--nodetach", "--config", "/manticore.conf")
                .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(WAIT_START_TIME_OUT)));
            manticore1.start();
            manticore1.followOutput((Consumer<OutputFrame>) outputFrame -> {
                LOGGER.info(outputFrame.getUtf8String());
            });

            System.setProperty("MANTICORE1_HOST", manticore1.getContainerIpAddress());
            System.setProperty("MANTICORE1_PORT", manticore1.getFirstMappedPort().toString());

            System.setProperty("MANTICORE1_JDBC",
                String.format(
                    "jdbc:mysql://%s:%s/oqsengine?characterEncoding=utf8&maxAllowedPacket=512000&useHostsInPrivileges=false"
                        + "&useLocalSessionState=true&serverTimezone=Asia/Shanghai",
                    System.getProperty("MANTICORE1_HOST"), System.getProperty("MANTICORE1_PORT")));


            LOGGER
                .info("Start manticore server.({}:{})", manticore1.getContainerIpAddress(),
                    manticore1.getFirstMappedPort());
        }

        if (searchManticore == null) {
            searchManticore = new GenericContainer<>("manticoresearch/manticore:3.5.4")
                .withExposedPorts(9306)
                .withNetwork(NETWORK)
                .withNetworkAliases("searchManticore")
                .withClasspathResourceMapping("search-manticore.conf", "/manticore.conf", BindMode.READ_ONLY)
                .withCommand("/usr/bin/searchd", "--nodetach", "--config", "/manticore.conf")
                .dependsOn(manticore0, manticore1)
                .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(WAIT_START_TIME_OUT)));
            searchManticore.start();
            searchManticore.followOutput((Consumer<OutputFrame>) outputFrame -> {
                LOGGER.info(outputFrame.getUtf8String());
            });

            System.setProperty("SEARCH_MANTICORE_HOST", searchManticore.getContainerIpAddress());
            System.setProperty("SEARCH_MANTICORE_PORT", searchManticore.getFirstMappedPort().toString());

            System.setProperty("SEARCH_MANTICORE_JDBC",
                String.format(
                    "jdbc:mysql://%s:%s/oqsengine?characterEncoding=utf8&maxAllowedPacket=512000&useHostsInPrivileges=false"
                        + "&useLocalSessionState=true&serverTimezone=Asia/Shanghai",
                    System.getProperty("SEARCH_MANTICORE_HOST"), System.getProperty("SEARCH_MANTICORE_PORT")));

            LOGGER.info("Start search manticore server.({}:{})", searchManticore.getContainerIpAddress(),
                searchManticore.getFirstMappedPort());
        }
    }

    /**
     * 结束 manticore 容器.
     */
    public static synchronized void stopManticore() {
        if (searchManticore != null) {
            searchManticore.stop();
            waitStop(searchManticore);
            searchManticore = null;

            System.clearProperty("SEARCH_MANTICORE_HOST");
            System.clearProperty("SEARCH_MANTICORE_PORT");
            System.clearProperty("SEARCH_MANTICORE_JDBC");

            LOGGER.info("Closed searchManticore container!");
        }

        if (manticore0 != null) {
            manticore0.stop();
            waitStop(manticore0);
            manticore0 = null;

            System.clearProperty("MANTICORE0_HOST");
            System.clearProperty("MANTICORE0_PORT");
            System.clearProperty("MANTICORE0_JDBC");

            LOGGER.info("Closed manticore0 container!");
        }

        if (manticore1 != null) {
            manticore1.stop();
            waitStop(manticore1);
            manticore1 = null;

            System.clearProperty("MANTICORE1_HOST");
            System.clearProperty("MANTICORE1_PORT");
            System.clearProperty("MANTICORE1_JDBC");

            LOGGER.info("Closed manticore1 container!");
        }
    }

    /**
     * 开始 cannal 容器.
     */
    public static synchronized void startCannal() {
        if (cannal == null) {
            System.setProperty("CANAL_DESTINATION", getRandomString(6));

            cannal = new GenericContainer("canal/canal-server:v1.1.4")
                .withNetwork(NETWORK)
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
                .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(WAIT_START_TIME_OUT)));
            cannal.start();
            cannal.followOutput((Consumer<OutputFrame>) outputFrame -> {
                LOGGER.info(outputFrame.getUtf8String());
            });

            System.setProperty("CANAL_HOST", cannal.getContainerIpAddress());
            System.setProperty("CANAL_PORT", cannal.getFirstMappedPort().toString());

            LOGGER.info("Start cannal server.({}:{})", cannal.getContainerIpAddress(), cannal.getFirstMappedPort());
        }
    }

    /**
     * 结束 cannal 容器.
     */
    public static synchronized void stopCannal() {
        if (cannal != null) {
            cannal.stop();
            waitStop(cannal);
            cannal = null;

            System.clearProperty("CANAL_DESTINATION");
            System.clearProperty("CANAL_HOST");
            System.clearProperty("CANAL_PORT");

            LOGGER.info("Closed cannal container!");
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
