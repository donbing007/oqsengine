package com.xforceplus.ultraman.oqsengine.boot.config.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * lettuce 配置.
 *
 * @author dongbin
 * @version 0.1 2020/11/16 14:12
 * @since 1.8
 */
@Component
@ConfigurationProperties(prefix = "redis")
public class LettuceConfiguration {

    private int maxReqQueue = Integer.MAX_VALUE;
    private String uri = "redis://localhost:6379";
    private String dbSeparator = "/";

    private int changeLogDb = 14;
    private int cacheEventDb = 15;

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

    /**
     * changelog db config.
     */
    public String uriWithChangeLogDb() {
        if (hasDB()) {
            return String.format("%s/%d", uri.substring(0, uri.lastIndexOf(dbSeparator)), changeLogDb);
        } else {
            return String.format("%s/%d", uri, changeLogDb);
        }
    }

    /**
     * cache event config.
     */
    public String uriWithCacheEventDb() {
        if (hasDB()) {
            return String.format("%s/%d", uri.substring(0, uri.lastIndexOf(dbSeparator)), cacheEventDb);
        } else {
            return String.format("%s/%d", uri, cacheEventDb);
        }
    }

    private boolean hasDB() {
        int sepLast = uri.lastIndexOf(dbSeparator);
        int sepFirst = uri.indexOf(dbSeparator);
        return sepLast - sepFirst > 1;
    }
}
