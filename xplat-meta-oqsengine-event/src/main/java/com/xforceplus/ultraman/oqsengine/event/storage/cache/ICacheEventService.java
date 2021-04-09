package com.xforceplus.ultraman.oqsengine.event.storage.cache;

import java.util.Collection;

/**
 * desc :
 * name : ICacheEventService
 *
 * @author : xujia
 * date : 2021/4/9
 * @since : 1.8
 */
public interface ICacheEventService {

    /**
     * 查询当前txId的列表信息
     * 过滤条件为id, version, eventType，如需要过滤则必须传以上3值，否则将返回txId的列表信息
     * @param txId
     * @param id
     * @param version
     * @param eventType
     * @return
     */
    public Collection<String> eventsQuery(long txId, Long id, Integer version, Integer eventType);

    /**
     * 根据txId删除缓存信息
     * @param txId
     */
    public void cleanByTxId(long txId);

    /**
     * 根据时间戳删除缓存信息
     */
    public void cleanByTimeRange(long start, long end);

    /**
     * 获取当前event队列长度
     * @return
     */
    public long size();
}
