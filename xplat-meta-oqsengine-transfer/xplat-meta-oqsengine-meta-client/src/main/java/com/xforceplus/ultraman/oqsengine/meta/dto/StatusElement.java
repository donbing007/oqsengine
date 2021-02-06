package com.xforceplus.ultraman.oqsengine.meta.dto;

/**
 * desc :
 * name : StatusElement
 *
 * @author : xujia
 * date : 2021/2/5
 * @since : 1.8
 */
public class StatusElement {
    private String appId;
    private int version;
    private Status status;
    private long registerTime;

    public StatusElement(String appId, int version, StatusElement.Status status) {
        this.appId = appId;
        this.version = version;
        this.status = status;
        if (status == Status.Register) {
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


    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * 将版本设置为未确认状态
     */
    public void reset() {
        status = Status.Register;
    }

    public static enum Status {
        Wait,
        Register,
        Confirmed
    }
}
