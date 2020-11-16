package com.xforceplus.ultraman.oqsengine.boot.config.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author dongbin
 * @version 0.1 2020/11/16 14:12
 * @since 1.8
 */
@Component
@ConfigurationProperties(prefix = "redis.config.command")
public class LettuceConfiguration {

    private int maxReqQueue = Integer.MAX_VALUE;
    private String uri = "redis://localhost:6379";

    public int getMaxReqQueue() {
        return maxReqQueue;
    }

    public void setMaxReqQueue(int maxReqQueue) {
        this.maxReqQueue = maxReqQueue;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
