package com.xforceplus.ultraman.oqsengine.testcontainer.container.impl;

import com.xforceplus.ultraman.oqsengine.testcontainer.constant.Global;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.AbstractContainerExtension;
import com.xforceplus.ultraman.oqsengine.testcontainer.enums.ContainerSupport;
import com.xforceplus.ultraman.oqsengine.testcontainer.pojo.ContainerWrapper;
import com.xforceplus.ultraman.oqsengine.testcontainer.pojo.FixedContainerWrapper;
import java.time.Duration;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * Created by justin.xu on 09/2021.
 *
 * @since 1.8
 */
public class CanalContainer extends AbstractContainerExtension {


    private static final Logger
        LOGGER = LoggerFactory.getLogger(CanalContainer.class);

    @Override
    protected ContainerWrapper setupContainer(String uid) {
        System.setProperty("CANAL_DESTINATION", "oqsengine");

        GenericContainer canal = new GenericContainer("canal/canal-server:v1.1.4")
            .withNetwork(Global.NETWORK)
            .withNetworkAliases("canal")
            .withExposedPorts(11111)
            .withEnv("canal.instance.mysql.slaveId", "12")
            .withEnv("canal.auto.scan", "false")
            .withEnv("canal.destinations", System.getProperty("CANAL_DESTINATION"))
            .withEnv("canal.instance.master.address", "mysql:3306")
            .withEnv("canal.instance.dbUsername", "root")
            .withEnv("canal.instance.dbPassword", "root")
            .withEnv("canal.instance.filter.regex", ".*\\.oqsbigentity.*")
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(Global.WAIT_START_TIME_OUT)));

        canal.start();
        canal.followOutput((Consumer<OutputFrame>) outputFrame -> {
            LOGGER.info(outputFrame.getUtf8String());
        });

        /*
         * 设置oqs中的环境变量
         */
        setSystemProperties(canal.getContainerIpAddress(), canal.getFirstMappedPort().toString());

        return new FixedContainerWrapper(canal);
    }

    @Override
    protected void containerClose() {

    }

    @Override
    protected ContainerSupport containerSupport() {
        return ContainerSupport.CANAL;
    }

    private void setSystemProperties(String address, String port) {

        if (null == address || null == port) {
            throw new RuntimeException(
                String.format("container canal init failed of null value, address[%s] or port[%s]", address, port));
        }

        System.setProperty("CANAL_HOST", address);
        System.setProperty("CANAL_PORT", port);

        LOGGER.info("Start canal server.({}:{})", address, port);
    }
}
