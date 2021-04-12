package com.xforceplus.ultraman.oqsengine.meta.common.metrics;

/**
 * desc :
 * name : ConnectorMetricsDefine
 *
 * @author : xujia
 * date : 2021/4/12
 * @since : 1.8
 */
public class ConnectorMetricsDefine {

    /**
     * 所有指标前辍
     */
    public static final String PREFIX = "oqs";

    /**
     * 连续多少次重置STREAM
     */
    public static final String CLIENT_CONTINUES_REBUILD_STREAM = PREFIX + ".connector.client.continues-rebuild-stream";

    /**
     * 数据格式错误
     */
    public static final String CLIENT_ACCEPT_DATA_FORMAT_ERROR = PREFIX + ".connector.client.data-format-error";

    /**
     * 数据格式错误
     */
    public static final String CLIENT_ACCEPT_DATA_HANDLER_ERROR = PREFIX + ".connector.client.data-handler-error";

    /**
     * 数据格式错误
     */
    public static final String SERVER_RESPONSE_HANDLE_FAILED_ERROR = PREFIX + ".connector.server.handle-failed-error";

    /**
     * 数据格式错误
     */
    public static final String SERVER_RESPONSE_DATA_FORMAT_ERROR = PREFIX + ".connector.server.data-format-error";
}
