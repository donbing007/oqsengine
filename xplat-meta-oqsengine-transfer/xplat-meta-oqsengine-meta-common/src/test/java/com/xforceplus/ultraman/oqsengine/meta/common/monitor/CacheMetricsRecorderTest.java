package com.xforceplus.ultraman.oqsengine.meta.common.monitor;

import com.xforceplus.ultraman.oqsengine.meta.common.monitor.dto.MetricsLog;
import com.xforceplus.ultraman.oqsengine.meta.common.monitor.dto.SyncCode;
import java.util.Arrays;
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

    private static class TP {
        String id;
        SyncCode key;
        String message;

        public TP(String id, SyncCode key, String message) {
            this.id = id;
            this.key = key;
            this.message = message;
        }

        public String getId() {
            return id;
        }

        public SyncCode getKey() {
            return key;
        }

        public String getMessage() {
            return message;
        }
    }


    @Test
    public void showTest() {

        List<TP> samples = Arrays.asList(
            new TP("appId1", SyncCode.REGISTER_OK, String.format("register success, uid : %s, env : %s, version : %s success.",
                "uid1", "env1", 1)),
            new TP("appId2", SyncCode.REGISTER_ERROR, String.format("register error, uid : %s, env : %s, version : %s success.",
                "uid2", "env0", 2))
        );

        cachedMetricsRecorder.info(samples.get(0).id, samples.get(0).getKey().name(), samples.get(0).getMessage());
        cachedMetricsRecorder.error(samples.get(1).id, samples.get(1).getKey().name(), samples.get(1).getMessage());

        List<MetricsLog> metricsLogs = cachedMetricsRecorder.showLogs(MetricsLog.ShowType.ALL);
        Assertions.assertEquals(2, metricsLogs.size());

        metricsLogs = cachedMetricsRecorder.showLogs(MetricsLog.ShowType.INFO);
        Assertions.assertEquals(1, metricsLogs.size());
        check(samples.get(0), metricsLogs.get(0));


        metricsLogs = cachedMetricsRecorder.showLogs(MetricsLog.ShowType.ERROR);
        Assertions.assertEquals(1, metricsLogs.size());
        check(samples.get(1), metricsLogs.get(0));
    }

    private void check(TP expected, MetricsLog actual) {
        Assertions.assertEquals(expected.getId(), actual.getAppId());
        Assertions.assertEquals(expected.getKey().name(), actual.getCode());
        Assertions.assertEquals(expected.getMessage(), actual.getMessage().getMessage());
    }
}
