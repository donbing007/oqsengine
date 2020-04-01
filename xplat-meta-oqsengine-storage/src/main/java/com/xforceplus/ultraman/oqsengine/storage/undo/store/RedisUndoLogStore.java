package com.xforceplus.ultraman.oqsengine.storage.undo.store;

import com.xforceplus.ultraman.oqsengine.storage.undo.constant.CacheConstant;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/24/2020 2:06 PM
 * 功能描述:
 * 修改历史:
 */
public class RedisUndoLogStore implements UndoLogStore {

    private RedissonClient redissonClient;

    public RedisUndoLogStore(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public Object get(Long txId, DbTypeEnum dbType, OpTypeEnum opType) {
        String key = CacheConstant.getLogKey(txId, dbType, opType);
        return getUndoLog().containsKey(key) ? CompressUtil.decompressToObj((byte[]) getUndoLog().get(key)) : null;
    }

    @Override
    public void save(Long txId, DbTypeEnum dbType, OpTypeEnum opType, Object data) {
        String key = CacheConstant.getLogKey(txId, dbType, opType);
        getUndoLog().put(key, CompressUtil.compress(data));
    }

    @Override
    public boolean isExist(Long txId, DbTypeEnum dbType, OpTypeEnum opType) {
        String key = CacheConstant.getLogKey(txId, dbType, opType);
        return getUndoLog().containsKey(key);
    }

    @Override
    public void remove(Long txId, DbTypeEnum dbType, OpTypeEnum opType) {
        String key = CacheConstant.getLogKey(txId, dbType, opType);
        getUndoLog().remove(key);
    }

    @Override
    public void remove(Long txId) {
        DbTypeEnum[] dbTypeEnums = DbTypeEnum.values();
        OpTypeEnum[] opTypeEnums = OpTypeEnum.values();
        for(DbTypeEnum dbType:dbTypeEnums) {
            for(OpTypeEnum opType:opTypeEnums) {
                remove(txId, dbType, opType);
            }
        }
    }

    RMap getUndoLog() {
        return redissonClient.getMap(CacheConstant.UNDO_LOG);
    }
}
