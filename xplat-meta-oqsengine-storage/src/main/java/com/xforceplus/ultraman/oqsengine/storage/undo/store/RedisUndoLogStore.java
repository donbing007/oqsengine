package com.xforceplus.ultraman.oqsengine.storage.undo.store;

import com.xforceplus.ultraman.oqsengine.storage.undo.constant.CacheConstant;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.UndoInfo;
import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.UndoLog;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/24/2020 2:06 PM
 * 功能描述:
 * 修改历史:
 */
public class RedisUndoLogStore implements UndoLogStore {

    final Logger logger = LoggerFactory.getLogger(RedisUndoLogStore.class);

    private RedissonClient redissonClient;

    public RedisUndoLogStore(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public UndoLog get(Long txId, DbTypeEnum dbType, OpTypeEnum opType) {
        String key = CacheConstant.getLogKey(txId, dbType, opType);
        return getUndoLog().containsKey(key) ?
                (UndoLog) CompressUtil.decompressToObj((byte[]) getUndoLog().get(key)) : null;
    }

    @Override
    public void save(Long txId, String dbKey, DbTypeEnum dbType, OpTypeEnum opType, Object data) {
        String key = CacheConstant.getLogKey(txId, dbType, opType);
        UndoLog undoLog = new UndoLog(dbKey, dbType, opType, data);
        getUndoLog().put(key, CompressUtil.compress(undoLog));
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

    @Override
    public List<UndoInfo> loadAllUndoInfo() {
        RMap undoLogs = getUndoLog();

        Set<String> keySet = undoLogs.keySet();
        List<UndoInfo> undoInfos = new ArrayList<>();
        for(String key:keySet) {
            UndoLog undoLog = get(key);

            Long txId = CacheConstant.getTxIdByKey(key);

            UndoInfo undoInfo = new UndoInfo(
                    txId, undoLog.getDbKey(), undoLog.getDbType(),
                    undoLog.getOpType(), undoLog.getData());

            undoInfos.add(undoInfo);
        }
        logger.debug("Loading undo info size {} from redis", undoInfos.size());
        return undoInfos;
    }

    RMap getUndoLog() {
        return redissonClient.getMap(CacheConstant.UNDO_LOG);
    }

    UndoLog get(String key){
        return getUndoLog().containsKey(key) ?
                (UndoLog) CompressUtil.decompressToObj((byte[]) getUndoLog().get(key)) : null;
    }
}
