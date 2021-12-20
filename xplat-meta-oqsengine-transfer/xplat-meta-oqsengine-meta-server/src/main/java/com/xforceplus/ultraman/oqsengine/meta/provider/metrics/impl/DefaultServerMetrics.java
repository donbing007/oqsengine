package com.xforceplus.ultraman.oqsengine.meta.provider.metrics.impl;

import com.xforceplus.ultraman.oqsengine.meta.common.monitor.MetricsRecorder;
import com.xforceplus.ultraman.oqsengine.meta.common.monitor.dto.MetricsLog;
import com.xforceplus.ultraman.oqsengine.meta.dto.ServerConnectorInfo;
import com.xforceplus.ultraman.oqsengine.meta.executor.IResponseWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.provider.metrics.ServerMetrics;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Resource;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
public class DefaultServerMetrics implements ServerMetrics {

    @Resource
    private IResponseWatchExecutor responseWatchExecutor;

    @Resource
    private MetricsRecorder metricsRecorder;

    @Override
    public Optional<ServerConnectorInfo> connectorInfo() {
        return responseWatchExecutor.connectorInfo();
    }

    @Override
    public Collection<MetricsLog> metricsLogs(MetricsLog.ShowType showType) {
        return metricsRecorder.showLogs(showType);
    }

}
