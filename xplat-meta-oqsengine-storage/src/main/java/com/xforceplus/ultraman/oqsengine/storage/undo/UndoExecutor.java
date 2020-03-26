package com.xforceplus.ultraman.oqsengine.storage.undo;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;

import java.sql.SQLException;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/25/2020 4:38 PM
 * 功能描述:
 * 修改历史:
 */
public abstract class UndoExecutor {

    public abstract void setTxId(Long txId);

    public abstract void setDbType(DbTypeEnum dbTypeEnum);

    public void run(OpTypeEnum opType) throws SQLException {
        switch (opType) {
            case BUILD: this.build(); break;
            case REPLACE: this.replace(); break;
            case DELETE: this.delete(); break;
            default:
        }
    }

    public abstract void build() throws SQLException;

    public abstract void replace() throws SQLException;

    public abstract void delete() throws SQLException;

    public abstract void saveUndoLog(DbTypeEnum dbTypeEnum, OpTypeEnum opTypeEnum, Long txId, IEntity entity);
}
