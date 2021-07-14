package com.xforceplus.ultraman.oqsengine.meta.provider.metrics.impl;

import com.xforceplus.ultraman.oqsengine.meta.dto.ServerMetricsInfo;
import com.xforceplus.ultraman.oqsengine.meta.executor.IResponseWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.provider.metrics.ServerMetrics;
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

    @Override
    public Optional<ServerMetricsInfo> showMetrics() {
        return responseWatchExecutor.showMetrics();
    }
}
