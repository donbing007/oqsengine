package com.xforceplus.ultraman.oqsengine.testcontainer.container;

import org.testcontainers.containers.GenericContainer;

/**
 * @author dongbin
 * @version 0.1 2020/11/12 15:53
 * @since 1.8
 */
public abstract class AbstractRedisContainerTest {

    static GenericContainer redis;

    static {
        redis = new GenericContainer("redis:6.0.9-alpine3.12").withExposedPorts(6379);
        redis.start();

        System.setProperty("status.redis.ip", redis.getContainerIpAddress());
        System.setProperty("status.redis.port", redis.getFirstMappedPort().toString());
    }
}
