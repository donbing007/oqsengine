package com.xforceplus.ultraman.oqsengine.metadata.dto.log;

/**
 * Created by justin.xu on 04/2022.
 *
 * @since 1.8
 */
public class UpGradeLog {
    private String appId;
    private String env;
    private int startVersion;
    private long startTimeStamp;

    private int currentVersion;
    private long currentTimeStamp;

    public UpGradeLog() {
    }

    /**
     * construct.
     */
    public UpGradeLog(String appId, String env, int startVersion, long startTimeStamp, int currentVersion,
                      long currentTimeStamp) {
        this.appId = appId;
        this.env = env;
        this.startVersion = startVersion;
        this.startTimeStamp = startTimeStamp;
        this.currentVersion = currentVersion;
        this.currentTimeStamp = currentTimeStamp;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public int getStartVersion() {
        return startVersion;
    }

    public void setStartVersion(int startVersion) {
        this.startVersion = startVersion;
    }

    public long getStartTimeStamp() {
        return startTimeStamp;
    }

    public void setStartTimeStamp(long startTimeStamp) {
        this.startTimeStamp = startTimeStamp;
    }

    public int getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(int currentVersion) {
        this.currentVersion = currentVersion;
    }

    public long getCurrentTimeStamp() {
        return currentTimeStamp;
    }

    public void setCurrentTimeStamp(long currentTimeStamp) {
        this.currentTimeStamp = currentTimeStamp;
    }
}
