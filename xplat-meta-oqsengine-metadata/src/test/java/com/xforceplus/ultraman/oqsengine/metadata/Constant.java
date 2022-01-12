package com.xforceplus.ultraman.oqsengine.metadata;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams;

/**
 * desc :.
 * name : Constant
 *
 * @author : xujia 2021/4/7
 * @since : 1.8
 */
public class Constant {
    public static final String LOCAL_HOST = "localhost";
    public static final int LOCAL_PORT = 8081;
    public static final String REMOTE_HOST = "120.55.249.44";
    public static final int REMOTE_PORT = 23111;

    public static final boolean IF_TEST_LOCAL = false;
    public static final boolean IF_TEST_REMOTE = false;

    //  local test use
    public static volatile boolean IS_SERVER_OK = false;
    public static volatile boolean IS_CLIENT_CLOSED = false;

    public static final String TEST_APP_ID = "1421998962514796545";
    public static final String TEST_ENV = "0";
    public static final int TEST_START_VERSION = 0;
    public static final long TEST_ENTITY_CLASS_ID = 1434714478818562049L;

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
}
