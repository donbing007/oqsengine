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
public class RedisContainer extends AbstractContainerExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisContainer.class);

    private GenericContainer container;

    /**
     * local start redis container实例.
     */
    @Override
    protected GenericContainer buildContainer() {
        container = new GenericContainer("redis:6.0.9-alpine3.12")
            .withNetworkAliases(buildAliase("redis"))
            .withExposedPorts(6379)
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(Global.WAIT_START_TIME_OUT)));


        return container;
    }

    @Override
    protected void init() {
        setSystemProperties(container.getHost(), container.getMappedPort(6379).toString());
    }

    @Override
    protected void clean() {

    }

    @Override
    protected ContainerSupport containerSupport() {
        return ContainerSupport.REDIS;
    }

    @Override
    protected GenericContainer getGenericContainer() {
        return this.container;
    }

    private void setSystemProperties(String address, String port) {
        if (null == address || null == port) {
            throw new RuntimeException(
                String.format("container init failed of null value, address[%s] or port[%s]", address, port));
        }
        System.setProperty("REDIS_HOST", address);
        System.setProperty("REDIS_PORT", port);

        LOGGER.info("Start Redis server.({}:{})", address, port);
    }
}
