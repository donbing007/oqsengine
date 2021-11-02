package com.xforceplus.ultraman.oqsengine.metadata.dto.metrics;

/**
 * Created by justin.xu on 11/2021.
 *
 * @since 1.8
 */
public class MetaBase {
    protected int version;
    protected String env;
    protected String appId;

    /**
     * construct method.
     */
    public MetaBase(int version, String env, String appId) {
        this.version = version;
        this.env = env;
        this.appId = appId;
    }

    public MetaBase() {
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
}
