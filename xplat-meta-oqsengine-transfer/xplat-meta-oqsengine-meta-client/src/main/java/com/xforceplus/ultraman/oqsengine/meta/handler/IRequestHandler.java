package com.xforceplus.ultraman.oqsengine.meta.handler;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncResponse;

import java.util.AbstractMap;
import java.util.List;

/**
 * desc :
 * name : EntityClassExecutor
 *
 * @author : xujia
 * date : 2021/2/3
 * @since : 1.8
 */
public interface IRequestHandler {

    /**
     * 注册一个appId，并开始监听
     * @param appId
     * @param version
     * @return boolean
     */
    boolean register(String appId, int version);

    /**
     * 注册多个appId，并开始监听
     * @param appIdEntries
     * @return boolean
     */
    boolean register(List<AbstractMap.SimpleEntry<String, Integer>> appIdEntries);

    /**
     * 断流自动重新注册
     * @return boolean
     */
    boolean reRegister();

    /**
     * 接收处理返回结果
     * @return boolean
     */
    void accept(EntityClassSyncResponse entityClassSyncResponse);
}
