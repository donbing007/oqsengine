package com.xforceplus.ultraman.oqsengine.meta.provider.metrics;

import com.xforceplus.ultraman.oqsengine.meta.dto.ServerMetricsInfo;
import java.util.Collection;
import java.util.Optional;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
public interface ServerMetrics {
    /**
     * 获取当前关注列表
     * @return
     */
    Optional<ServerMetricsInfo> showMetrics();
}
