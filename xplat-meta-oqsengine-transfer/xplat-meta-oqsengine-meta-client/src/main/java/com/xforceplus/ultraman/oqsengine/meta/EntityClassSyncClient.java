package com.xforceplus.ultraman.oqsengine.meta;

import java.util.AbstractMap;
import java.util.List;

/**
 * desc :
 * name : EntityClassSyncClient
 *
 * @author : xujia
 * date : 2021/2/2
 * @since : 1.8
 */
public interface EntityClassSyncClient {
    /**
     * 注册一个appId，并开始监听
     * @param appId
     * @param version
     * @return
     */
    public boolean register(String appId, int version);

    /**
     * 注册多个appId，并开始监听
     * @param appIdEntries
     * @return
     */
    public boolean register(List<AbstractMap.SimpleEntry<String, Integer>> appIdEntries);
}
