package com.xforceplus.ultraman.oqsengine.testcontainer.container.impl;

import com.xforceplus.ultraman.oqsengine.testcontainer.constant.Global;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.AbstractContainerExtension;
import com.xforceplus.ultraman.oqsengine.testcontainer.enums.ContainerSupport;
import com.xforceplus.ultraman.oqsengine.testcontainer.utils.SqlInitUtils;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * Created by justin.xu on 09/2021.
 *
 * @since 1.8
 */
public class MysqlContainer extends AbstractContainerExtension {

    private static final String MYSQL_USER_PASS = "root";
    private static final Logger
        LOGGER = LoggerFactory.getLogger(MysqlContainer.class);

    private GenericContainer container;

    @Override
    protected GenericContainer buildContainer() {
        container = new GenericContainer("mysql:5.7")
            .withNetworkAliases(buildAliase("mysql"))
            .withEnv("MYSQL_DATABASE", "oqsengine")
            .withEnv("MYSQL_ROOT_USERNAME", MYSQL_USER_PASS)
            .withEnv("MYSQL_ROOT_PASSWORD", MYSQL_USER_PASS)
            .withClasspathResourceMapping("mysql/mysql.cnf", "/etc/my.cnf", BindMode.READ_ONLY)
            .waitingFor(
                Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(Global.WAIT_START_TIME_OUT)));

        if (!isCiRuntime()) {
            container.withExposedPorts(3306);
        }

        return container;
    }

    @Override
    protected void init() {
        if (isCiRuntime()) {
            setSystemProperties(container.getHost(), "3306");
        } else {
            setSystemProperties(container.getContainerIpAddress(), container.getFirstMappedPort().toString());
        }


        try {
            SqlInitUtils.execute("/mysql", "MYSQL_JDBC_WITH_AUTH");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    protected void clean() {
        if (this.container != null) {
            try {
                SqlInitUtils.execute("/mysql/drop", "MYSQL_JDBC_WITH_AUTH");
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @Override
    protected ContainerSupport containerSupport() {
        return ContainerSupport.MYSQL;
    }

    @Override
    protected GenericContainer getGenericContainer() {
        return this.container;
    }

    private void setSystemProperties(String address, String port) {
        if (null == address || null == port) {
            throw new RuntimeException(
                String.format("container mysql init failed of null value, address[%s] or port[%s]", address, port));
        }

        System.setProperty("MYSQL_HOST", address);
        System.setProperty("MYSQL_PORT", port);

        System.setProperty(
            "MYSQL_JDBC",
            String.format(
                "jdbc:mysql://%s:%s/oqsengine?useUnicode=true&serverTimezone=GMT&useSSL=false&characterEncoding=utf8",
                address, port)
        );

        System.setProperty(
            "MYSQL_JDBC_WITH_AUTH",
            String.format(
                "jdbc:mysql://%s:%s/oqsengine?"
                    + "useUnicode=true"
                    + "&serverTimezone=GMT"
                    + "&useSSL=false"
                    + "&characterEncoding=utf8"
                    + "&allowMultiQueries=true"
                    + "&rewriteBatchedStatements=true"
                    + "&user=%s&password=%s",
                address, port, MYSQL_USER_PASS, MYSQL_USER_PASS)
        );

        LOGGER.info("Start mysql server.({}:{})", address, port);
    }
}
