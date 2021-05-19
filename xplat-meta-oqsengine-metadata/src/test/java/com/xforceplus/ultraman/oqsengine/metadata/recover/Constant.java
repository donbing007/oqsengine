package com.xforceplus.ultraman.oqsengine.metadata.recover;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams;

/**
 * desc :.
 * name : Constant
 *
 * @author : xujia 2021/4/7
 * @since : 1.8
 */
public class Constant {
    public static final String HOST = "localhost";
    public static final int PORT = 8081;

    public static final boolean IF_TEST = false;
    public static volatile boolean IS_SERVER_OK = false;
    public static volatile boolean IS_CLIENT_CLOSED = false;

    /**
     * grpc 配置.
     */
    public static GRpcParams grpcParamsConfig() {
        GRpcParams grpcParamsConfig = new GRpcParams();
        grpcParamsConfig.setDefaultDelayTaskDuration(30_000);
        grpcParamsConfig.setKeepAliveSendDuration(5_000);
        grpcParamsConfig.setReconnectDuration(5_000);
        grpcParamsConfig.setDefaultHeartbeatTimeout(30_000);
        grpcParamsConfig.setMonitorSleepDuration(1_000);

        return grpcParamsConfig;
    }

    public static final String TEST_APP_ID = "TEST_70";
    public static final String TEST_ENV = "0";
    public static final int TEST_START_VERSION = 12;
    public static final long TEST_ENTITY_CLASS_ID = 10001;

}
