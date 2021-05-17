package com.xforceplus.ultraman.oqsengine.metadata.remote;

import com.xforceplus.ultraman.oqsengine.meta.common.config.GRpcParams;

/**
 * test-use.
 *
 * @author j.xu
 * @version 0.1 2021/05/2021/5/17
 * @since 1.8
 */
public class RemoteConstant {
    /**
     * 连接元数据dev环境
     */
    public static final String HOST = "120.55.249.44";
    public static final int PORT = 23111;
    public static final boolean IF_TEST = false;

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

    public static final String TEST_APP_ID = "7";
    public static final String TEST_ENV = "0";
    public static final int TEST_START_VERSION = -1;
    public static final long TEST_ENTITY_CLASS_ID = 1263391796170928130L;

}
