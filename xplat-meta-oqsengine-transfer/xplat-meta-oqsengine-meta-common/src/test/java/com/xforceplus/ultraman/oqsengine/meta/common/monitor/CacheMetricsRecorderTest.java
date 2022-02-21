package com.xforceplus.ultraman.oqsengine.meta.common.monitor;

import com.xforceplus.ultraman.oqsengine.meta.common.monitor.dto.MetricsLog;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class CacheMetricsRecorderTest {

    private CachedMetricsRecorder cachedMetricsRecorder;

    @BeforeEach
    public void before() {
        cachedMetricsRecorder = new CachedMetricsRecorder();
    }

    @Test
    public void infoTest() {
        String code = "test-info-code";
        String key = "test-info-key";
        String message = "test-info-message";

        cachedMetricsRecorder.info(code, key, message);

        List<MetricsLog> metricsLogs = cachedMetricsRecorder.showLogs(MetricsLog.ShowType.INFO);

        Assertions.assertEquals(1, metricsLogs.size());

        MetricsLog metricsLog = metricsLogs.get(0);

        Assertions.assertEquals(code, metricsLog.getCode());
        Assertions.assertEquals(key, metricsLog.getAppId());
        Assertions.assertEquals(message, metricsLog.getMessage().getMessage());

        metricsLogs = cachedMetricsRecorder.showLogs(MetricsLog.ShowType.ERROR);
        Assertions.assertEquals(0, metricsLogs.size());
    }

    @Test
    public void errorTest() {
        String code = "test-error-code";
        String key = "test-error-key";
        String message = "test-error-message";

        cachedMetricsRecorder.error(code, key, message);

        List<MetricsLog> metricsLogs = cachedMetricsRecorder.showLogs(MetricsLog.ShowType.ERROR);

        Assertions.assertEquals(1, metricsLogs.size());

        MetricsLog metricsLog = metricsLogs.get(0);

        Assertions.assertEquals(code, metricsLog.getCode());
        Assertions.assertEquals(key, metricsLog.getAppId());
        Assertions.assertEquals(message, metricsLog.getMessage().getMessage());

        metricsLogs = cachedMetricsRecorder.showLogs(MetricsLog.ShowType.INFO);
        Assertions.assertEquals(0, metricsLogs.size());
    }

    @Test
    public void showTest() {
        cachedMetricsRecorder.info("test-info-code", "test-info-key", "test-info-message");
        cachedMetricsRecorder.error("test-error-code", "test-error-key", "test-error-message");

        List<MetricsLog> metricsLogs = cachedMetricsRecorder.showLogs(MetricsLog.ShowType.ALL);

        Assertions.assertEquals(2, metricsLogs.size());

        metricsLogs = cachedMetricsRecorder.showLogs(MetricsLog.ShowType.INFO);

        Assertions.assertEquals(1, metricsLogs.size());

        metricsLogs = cachedMetricsRecorder.showLogs(MetricsLog.ShowType.ERROR);

        Assertions.assertEquals(1, metricsLogs.size());

    }
}
