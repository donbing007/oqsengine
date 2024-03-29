package com.xforceplus.ultraman.oqsengine.meta.common.constant;

/**
 * request status.
 *
 * @author xujia
 * @since 1.8
 */
public enum RequestStatus {
    HEARTBEAT,      //  心跳 双边发送
    REGISTER,       //  客户端发起REGISTER 客户端发送、服务端关注状态
    REGISTER_OK,    //  服务端确认注册 服务端发送、客户端关注状态
    SYNC,           //  服务端推送的更新 服务端发送、客户端关注状态
    SYNC_OK,        //  同步成功 客户端发送、服务端关注状态
    SYNC_FAIL,      //  同步失败 客户端发送、服务端关注状态
    DATA_ERROR,     //  元数据data无法初始化，存在问题

    RESET;          //  重置某个watch, 效果等同重新注册

    /**
     * 通过ordinal获取RequestStatus.
     */
    public static RequestStatus getInstance(int ordinal) {
        for (RequestStatus r : RequestStatus.values()) {
            if (r.ordinal() == ordinal) {
                return r;
            }
        }
        return null;
    }

}
