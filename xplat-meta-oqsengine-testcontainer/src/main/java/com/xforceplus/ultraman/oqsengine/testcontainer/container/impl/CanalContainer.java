package com.xforceplus.ultraman.oqsengine.testcontainer.container.impl;

import com.xforceplus.ultraman.oqsengine.testcontainer.constant.Global;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.AbstractContainerExtension;
import com.xforceplus.ultraman.oqsengine.testcontainer.enums.ContainerSupport;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * Created by justin.xu on 09/2021.
 *
 * @since 1.8
 */
public class CanalContainer extends AbstractContainerExtension {


    private static final Logger
        LOGGER = LoggerFactory.getLogger(CanalContainer.class);

    private GenericContainer container;

    @Override
    protected GenericContainer buildContainer() {
        System.setProperty("CANAL_DESTINATION", "oqsengine");

        container = new GenericContainer("canal/canal-server:v1.1.4")
            .withNetworkAliases(buildAliase("canal"))
            .withEnv("canal.instance.mysql.slaveId", "12")
            .withEnv("canal.auto.scan", "false")
            .withEnv("canal.destinations", System.getProperty("CANAL_DESTINATION"))
            .withEnv("canal.instance.master.address", String.join(":", buildAliase("mysql"), "3306"))
            .withEnv("canal.instance.dbUsername", "root")
            .withEnv("canal.instance.dbPassword", "root")
            .withEnv("canal.instance.filter.regex", ".*\\.oqsbigentity.*")
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(Global.WAIT_START_TIME_OUT)));

        if (!isCiRuntime()) {
            container.withExposedPorts(11111);
        }

        return container;
    }

    @Override
    protected void init() {
        if (isCiRuntime()) {
            setSystemProperties(container.getHost(), "11111");
        } else {
            setSystemProperties(container.getContainerIpAddress(), container.getFirstMappedPort().toString());
        }
    }

    @Override
    protected void clean() {

    }

    @Override
    protected ContainerSupport containerSupport() {
        return ContainerSupport.CANAL;
    }

    @Override
    protected GenericContainer getGenericContainer() {
        return this.container;
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
