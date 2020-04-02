package com.xforceplus.ultraman.oqsengine.storage.undo.util;

import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommand;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommandInvoker;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;

import java.util.Map;

import static com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum.REPLACE_ATTRIBUTE;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 4/2/2020 11:07 AM
 * 功能描述:
 * 修改历史:
 */
public class UndoUtil {

    public static StorageCommand selectStorageCommand(
            Map<DbTypeEnum, StorageCommandInvoker> storageCommandInvokers,
            DbTypeEnum dbType, OpTypeEnum opType){
        if(dbType == null || opType == null) {
            return null;
        }

        if(!storageCommandInvokers.containsKey(dbType)
                || storageCommandInvokers.get(dbType) == null) {
            return null;
        }

        return storageCommandInvokers.get(dbType).selectCommand(opType);
    }

    public static StorageCommand selectUndoStorageCommand(
            Map<DbTypeEnum, StorageCommandInvoker> storageCommandInvokers,
            DbTypeEnum dbType, OpTypeEnum opType){
        OpTypeEnum undoOpType = null;
        switch (opType) {
            case BUILD: undoOpType = OpTypeEnum.DELETE; break;
            case DELETE: undoOpType = OpTypeEnum.BUILD; break;
            case REPLACE: undoOpType = OpTypeEnum.REPLACE; break;
            case REPLACE_ATTRIBUTE: undoOpType = REPLACE_ATTRIBUTE; break;
            default:
        }

        return selectStorageCommand(storageCommandInvokers, dbType, undoOpType);
    }
}
