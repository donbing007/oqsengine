package com.xforceplus.ultraman.oqsengine.metadata.dto.metrics;

/**
 * Created by justin.xu on 11/2021.
 *
 * @since 1.8
 */
public class MetaLogs extends MetaBase {
    private Long time;
    private String logs;

    public MetaLogs() {
    }

    /**
     * construct method.
     */
    public MetaLogs(String appId, int version, long time, String logs) {

        super(version, "", appId);

        this.logs = logs;
        this.time = time;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getLogs() {
        return logs;
    }

    public void setLogs(String logs) {
        this.logs = logs;
    }
}
