package com.xforceplus.ultraman.oqsengine.boot.grpc.devops.dto;

import java.util.Map;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class ApplicationInfo {
    private Map<String, String> systemInfo;
    private Map<String, String> applicationEnv;

    public Map<String, String> getSystemInfo() {
        return systemInfo;
    }

    public Map<String, String> getApplicationEnv() {
        return applicationEnv;
    }

    public void setApplicationEnv(Map<String, String> applicationEnv) {
        this.applicationEnv = applicationEnv;
    }

    public void setSystemInfo(Map<String, String> systemInfo) {
        this.systemInfo = systemInfo;
    }

    public ApplicationInfo() {
    }
}
