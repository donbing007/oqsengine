package com.xforceplus.ultraman.oqsengine.meta.provider.metrics;

import com.xforceplus.ultraman.oqsengine.meta.common.monitor.dto.MetricsLog;
import com.xforceplus.ultraman.oqsengine.meta.dto.ServerConnectorInfo;
import java.util.Collection;
import java.util.Optional;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
public interface ServerMetrics {
    /**
     * 获取当前连接信息.
     */
    Optional<ServerConnectorInfo> connectorInfo();

    /**
     * 获取处理日志.
     * @param showType
     * @return
     */
    Collection<MetricsLog> metricsLogs(MetricsLog.ShowType showType);
}
