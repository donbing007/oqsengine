package com.xforceplus.ultraman.oqsengine.meta.common.dto;

/**
 * desc :.
 * name : AppSyncElement
 *
 * @author : xujia
 * date : 2021/2/5
 * @since : 1.8
 */
public class WatchElement {
    private String appId;
    private String env;
    private int version;
    private ElementStatus status;
    private long registerTime;

    public WatchElement(String appId, String env, int version, WatchElement.ElementStatus status) {
        this.appId = appId;
        this.version = version;
        this.env = env;
        this.status = status;
        this.registerTime = System.currentTimeMillis();
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

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public ElementStatus getStatus() {
        return status;
    }

    public void setStatus(ElementStatus status) {
        this.status = status;
    }

    /**
     * 将版本设置为未确认状态
     */
    public void reset() {
        status = ElementStatus.Register;
    }

    public static enum ElementStatus {
        Init,       //  表示当前由于错误处于初始化
        Register,   //  表示客户端发送注册
        Notice,     //  表示服务端关注
        Confirmed   //  表示客户端告知服务端确认
    }

    @Override
    public String toString() {
        return "WatchElement{" +
                "appId='" + appId + '\'' +
                ", version=" + version +
                ", status=" + status +
                ", registerTime=" + registerTime +
                '}';
    }


}
