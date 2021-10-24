package com.xforceplus.ultraman.oqsengine.testcontainer.container.impl;

import com.xforceplus.ultraman.oqsengine.testcontainer.constant.Global;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.AbstractContainerExtension;
import com.xforceplus.ultraman.oqsengine.testcontainer.enums.ContainerSupport;
import com.xforceplus.ultraman.oqsengine.testcontainer.pojo.ContainerWrapper;
import com.xforceplus.ultraman.oqsengine.testcontainer.pojo.FixedContainerWrapper;
import com.xforceplus.ultraman.oqsengine.testcontainer.utils.RemoteCallUtils;
import com.xforceplus.ultraman.oqsengine.testcontainer.utils.SqlInitUtils;
import java.time.Duration;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * Created by justin.xu on 09/2021.
 *
 * @since 1.8
 */
public class MysqlContainer extends AbstractContainerExtension {

    private static final String mysqlUserPass = "root";
    private static final Logger
        LOGGER = LoggerFactory.getLogger(MysqlContainer.class);

    @Override
    protected ContainerWrapper setupContainer(String uid) {
        ContainerWrapper containerWrapper = null;

        if (null != uid) {
            containerWrapper = RemoteCallUtils.startUseRemoteContainer(uid, containerSupport());

            if (null == containerWrapper) {
                throw new RuntimeException("get remote container failed.");
            }
            /*
             * 设置oqs中的环境变量
             */
            setSystemProperties(containerWrapper.host(), containerWrapper.port());
        } else {
            GenericContainer mysql = new GenericContainer("mysql:5.7")
                .withNetwork(Global.NETWORK)
                .withNetworkAliases("mysql")
                .withExposedPorts(3306)
                .withEnv("MYSQL_DATABASE", "oqsengine")
                .withEnv("MYSQL_ROOT_USERNAME", mysqlUserPass)
                .withEnv("MYSQL_ROOT_PASSWORD", mysqlUserPass)
                .withClasspathResourceMapping("mysql/mysql.cnf", "/etc/my.cnf", BindMode.READ_ONLY)
                .waitingFor(
                    Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(Global.WAIT_START_TIME_OUT)));

            mysql.start();

            mysql.followOutput((Consumer<OutputFrame>) outputFrame -> {
                LOGGER.info(outputFrame.getUtf8String());
            });

            /*
             * 设置oqs中的环境变量
             */
            setSystemProperties(mysql.getContainerIpAddress(), mysql.getFirstMappedPort().toString());

            containerWrapper = new FixedContainerWrapper(mysql);
        }


        try {
            SqlInitUtils.init("/mysql", "MYSQL_JDBC_WITH_AUTH");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return containerWrapper;
    }

    @Override
    protected void containerClose() {
        try {
            SqlInitUtils.init("/mysql/drop", "MYSQL_JDBC_WITH_AUTH");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected ContainerSupport containerSupport() {
        return ContainerSupport.MYSQL;
    }

    private void setSystemProperties(String address, String port) {
        if (null == address || null == port) {
            throw new RuntimeException(String.format("container mysql init failed of null value, address[%s] or port[%s]", address, port));
        }

        System.setProperty("ds", "./src/test/resources/oqsengine-ds.conf");

        System.setProperty("MYSQL_HOST", address);
        System.setProperty("MYSQL_PORT", port);

        System.setProperty(
            "MYSQL_JDBC",
            String.format("jdbc:mysql://%s:%s/oqsengine?useUnicode=true&serverTimezone=GMT&useSSL=false&characterEncoding=utf8",
                address, port)
        );

        System.setProperty(
            "MYSQL_JDBC_WITH_AUTH",
            String.format("jdbc:mysql://%s:%s/oqsengine?useUnicode=true&serverTimezone=GMT&useSSL=false&characterEncoding=utf8&user=%s&password=%s",
                address, port, mysqlUserPass, mysqlUserPass)
        );

        LOGGER.info("Start mysql server.({}:{})", address, port);
    }
}
