package com.xforceplus.ultraman.oqsengine.meta.common.dto;

/**
 * desc :
 * name : AppSyncElement
 *
 * @author : xujia
 * date : 2021/2/5
 * @since : 1.8
 */
public class WatchElement {
    private String appId;
    private int version;
    private AppStatus status;
    private long registerTime;

    public WatchElement(String appId, int version, WatchElement.AppStatus status) {
        this.appId = appId;
        this.version = version;
        this.status = status;
        if (status == AppStatus.Register) {
            registerTime = System.currentTimeMillis();
        }
    }

    public long getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(long registerTime) {
        this.registerTime = registerTime;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getAppId() {
        return appId;
    }

    public int getVersion() {
        return version;
    }


    public AppStatus getStatus() {
        return status;
    }

    public void setStatus(AppStatus status) {
        this.status = status;
    }

    /**
     * 将版本设置为未确认状态
     */
    public void reset() {
        status = AppStatus.Register;
    }

    public static enum AppStatus {
        Init,
        Register,
        Notice,
        Confirmed
    }
}
