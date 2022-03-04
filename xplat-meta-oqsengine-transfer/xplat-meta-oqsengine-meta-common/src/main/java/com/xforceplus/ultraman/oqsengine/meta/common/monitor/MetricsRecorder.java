package com.xforceplus.ultraman.oqsengine.meta.common.monitor;

import com.xforceplus.ultraman.oqsengine.meta.common.monitor.dto.MetricsLog;
import java.util.List;

/**
 * Created by justin.xu on 12/2021.
 *
 * @since 1.8
 */
public interface MetricsRecorder {

    /**
     * 记录日志
     * @param key
     * @param code
     * @param message
     */
    void info(String key, String code, String message);

    /**
     * 记录错误
     * @param key
     * @param code
     * @param message
     */
    void error(String key, String code, String message);

    /**
     * 展示日志
     * @param showType
     * @return
     */
    List<MetricsLog> showLogs(MetricsLog.ShowType showType);
}
