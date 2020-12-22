package com.xforceplus.ultraman.oqsengine.testcontainer.container;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * @author dongbin
 * @version 0.1 2020/11/12 15:53
 * @since 1.8
 */
public abstract class AbstractRedisContainer {

    static Network network = Network.newNetwork();

    static GenericContainer redis;

    static {
        redis = new GenericContainer("redis:6.0.9-alpine3.12")
                .withNetwork(network)
                .withNetworkAliases("redis")
                .withExposedPorts(6379)
                .waitingFor(Wait.forListeningPort());
        redis.start();

        System.setProperty("REDIS_HOST", redis.getContainerIpAddress());
        System.setProperty("REDIS_PORT", redis.getFirstMappedPort().toString());
    }
}
