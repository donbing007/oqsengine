package com.xforceplus.ultraman.oqsengine.metadata.dto.metrics;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class AppSimpleInfo {
    private String appId;
    private String env;
    private String code;
    private Integer version;

    /**
     * 初始化APP简单信息.
     *
     * @param appId 应用标识.
     * @param env 环境信息.
     * @param code 代码.
     * @param version 版本.
     */
    public AppSimpleInfo(String appId, String env, String code, Integer version) {
        this.appId = appId;
        this.env = env;
        this.code = code;
        this.version = version;
    }

    public String getAppId() {
        return appId;
    }

    public String getEnv() {
        return env;
    }

    public String getCode() {
        return code;
    }

    public Integer getVersion() {
        return version;
    }
}
