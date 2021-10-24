package com.xforceplus.ultraman.oqsengine.testcontainer.container.impl;

import com.xforceplus.ultraman.oqsengine.testcontainer.constant.Global;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.AbstractContainerExtension;
import com.xforceplus.ultraman.oqsengine.testcontainer.enums.ContainerSupport;
import com.xforceplus.ultraman.oqsengine.testcontainer.pojo.ContainerWrapper;
import com.xforceplus.ultraman.oqsengine.testcontainer.pojo.FixedContainerWrapper;
import com.xforceplus.ultraman.oqsengine.testcontainer.utils.RemoteCallUtils;
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
public class RedisContainer extends AbstractContainerExtension {

    private static final Logger
        LOGGER = LoggerFactory.getLogger(RedisContainer.class);

    /**
     * local start redis container实例.
     */
    @Override
    protected ContainerWrapper setupContainer(String uid) {
        ContainerWrapper containerWrapper = null;
        if (null != uid) {
            containerWrapper = RemoteCallUtils.startUseRemoteContainer(uid, containerSupport());
            if (null == containerWrapper) {
                throw new RuntimeException("get remote container failed.");
            }
            /**
             * 设置oqs中的环境变量
             */
            setSystemProperties(containerWrapper.host(), containerWrapper.port());
        } else {
            GenericContainer redis = new GenericContainer("redis:6.0.9-alpine3.12")
                .withNetwork(Global.NETWORK)
                .withNetworkAliases("redis")
                .withExposedPorts(6379)
                .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(Global.WAIT_START_TIME_OUT)));
            redis.start();
            redis.followOutput((Consumer<OutputFrame>) outputFrame -> {
                LOGGER.info(outputFrame.getUtf8String());
            });

            /**
            * 设置redis在oqs中的环境变量
            */
            setSystemProperties(redis.getContainerIpAddress(), redis.getFirstMappedPort().toString());

            containerWrapper = new FixedContainerWrapper(redis);
        }
        return containerWrapper;
    }

    @Override
    protected void containerClose() {

    }

    @Override
    protected ContainerSupport containerSupport() {
        return ContainerSupport.REDIS;
    }

    private void setSystemProperties(String address, String port) {
        if (null == address || null == port) {
            throw new RuntimeException(String.format("container init failed of null value, address[%s] or port[%s]", address, port));
        }
        System.setProperty("REDIS_HOST", address);
        System.setProperty("REDIS_PORT", port);

        LOGGER.info("Start Redis server.({}:{})", address, port);
    }
}
