package com.xforceplus.ultraman.oqsengine.boot.config.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author dongbin
 * @version 0.1 2020/11/16 14:12
 * @since 1.8
 */
@Component
@ConfigurationProperties(prefix = "redis")
public class LettuceConfiguration {

    private int maxReqQueue = Integer.MAX_VALUE;
    private String uri = "redis://localhost:6379";

    private String host;
    private int port;
    private String userName;
    private String passWord;

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
