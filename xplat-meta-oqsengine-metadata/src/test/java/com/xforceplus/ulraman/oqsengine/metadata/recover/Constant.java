package com.xforceplus.ulraman.oqsengine.metadata.recover;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams;

/**
 * desc :
 * name : Constant
 *
 * @author : xujia
 * date : 2021/4/7
 * @since : 1.8
 */
public class Constant {
    public static final String HOST = "localhost";
    public static final int PORT = 8081;

    public static final boolean ifTest = true;
    public volatile static boolean isServerOk = false;
    public volatile static boolean isClientClosed = false;

    public static GRpcParams gRpcParamsConfig() {
        GRpcParams gRpcParamsConfig = new GRpcParams();
        gRpcParamsConfig.setDefaultDelayTaskDuration(30_000);
        gRpcParamsConfig.setKeepAliveSendDuration(5_000);
        gRpcParamsConfig.setReconnectDuration(5_000);
        gRpcParamsConfig.setDefaultHeartbeatTimeout(30_000);
        gRpcParamsConfig.setMonitorSleepDuration(1_000);

        return gRpcParamsConfig;
    }

    public static final String TEST_APP_ID = "TEST_70";
    public static final String TEST_ENV = "0";
    public static final int TEST_START_VERSION = 12;
    public static final long TEST_ENTITY_CLASS_ID = 10001;

}
