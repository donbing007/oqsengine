package com.xforceplus.ultraman.oqsengine.storage.undo.store;

import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/24/2020 1:59 PM
 * 功能描述:
 * 修改历史:
 */
public interface UndoLogStore {

    Object get(Long txId, DbTypeEnum dbType, OpTypeEnum opType);

    void save(Long txId, DbTypeEnum dbType, OpTypeEnum opType, Object data);

    boolean isExist(Long txId, DbTypeEnum dbType, OpTypeEnum opType);

    void remove(Long txId, DbTypeEnum dbType, OpTypeEnum opType);

    void remove(Long txId);
}
