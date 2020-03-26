package com.xforceplus.ultraman.oqsengine.storage.undo.store;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.CacheConstant;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.TxUndoLog;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/24/2020 2:06 PM
 * 功能描述:
 * 修改历史:
 */
public class RedisUndoLogStore implements UndoLogStore {

    private RedissonClient redissonClient;

    public RedisUndoLogStore(RedissonClient redissonClient){
        this.redissonClient = redissonClient;
    }

    @Override
    public List<TxUndoLog> get(Long txId) {
        String txIdKey = CacheConstant.getTxIdKey(txId);

        RMap undoLog = redissonClient.getMap(CacheConstant.UNDO_LOG);

        List<TxUndoLog> txUndoLogs = undoLog.containsKey(txIdKey) ?
                (List)CompressUtil.decompressToObj((byte[]) undoLog.get(txIdKey)):new ArrayList();

        return txUndoLogs;
    }

    @Override
    public void update(Long txId, List<TxUndoLog> txUndoLogs) {
        RMap undoLog = redissonClient.getMap(CacheConstant.UNDO_LOG);
        undoLog.put(CacheConstant.getTxIdKey(txId), CompressUtil.compress(txUndoLogs));
    }

    @Override
    public void remove(Long txId) {
        RMap undoLog = redissonClient.getMap(CacheConstant.UNDO_LOG);
        undoLog.remove(CacheConstant.getTxIdKey(txId));
    }

    @Override
    public void add(Long txId, TxUndoLog txUndoLog) {
        List<TxUndoLog> txUndoLogs = get(txId);
        txUndoLogs.add(txUndoLog);

        String txIdKey = CacheConstant.getTxIdKey(txId);
        RMap undoLog = redissonClient.getMap(CacheConstant.UNDO_LOG);

        undoLog.put(txIdKey, CompressUtil.compress(txUndoLogs));
    }

    private TxUndoLog getTxUndoLog(Long txId, DbTypeEnum dbType, OpTypeEnum opType, IEntity entity) throws SQLException {
        return new TxUndoLog(txId, dbType.value(), opType.value(), entity);
//        TxUndoLog txUndoLog;
//        switch (opType) {
//            case "build":
//                txUndoLog = new TxUndoLog(txId, UndoTypeEnum.BUILD.value(), JSON.toJSONString(entity));
//                break;
//            case "replace":
//                txUndoLog = new TxUndoLog(txId, UndoTypeEnum.REPLACE.value(), JSON.toJSONString(entity));
//                break;
//            case "delete":
//                txUndoLog = new TxUndoLog(txId, UndoTypeEnum.DELETE.value(), JSON.toJSONString(entity));
//                break;
//            default:
//                throw new RuntimeException("");
//        }
//        return txUndoLog;
    }

    private void recordUndoLog(Long txId,  DbTypeEnum dbType, OpTypeEnum opType, IEntity entity) {
        RMap undoLog = redissonClient.getMap(CacheConstant.UNDO_LOG);

        List<TxUndoLog> txUndoLogs;
        if(undoLog.containsKey(CacheConstant.getTxIdKey(txId))) {
            txUndoLogs = (List)undoLog.get(CacheConstant.getTxIdKey(txId));
        } else {
            txUndoLogs = new ArrayList();
        }

        TxUndoLog txUndoLog = new TxUndoLog(txId, dbType.value(), opType.value(), entity);
        txUndoLogs.add(txUndoLog);

        undoLog.put(CacheConstant.getTxIdKey(txId), CompressUtil.compress(txUndoLogs));
    }
}
