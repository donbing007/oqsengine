package com.xforceplus.ultraman.oqsengine.meta.common.handler;


/**
 * desc :
 * name : SyncHandler
 *
 * @author : xujia
 * date : 2021/2/6
 * @since : 1.8
 */
public interface SyncHandler {

    void confirmRegister(String appId, int version, String uid);

    void confirmHeartBeat(String uid);
}
