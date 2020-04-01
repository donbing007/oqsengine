package com.xforceplus.ultraman.oqsengine.storage.transaction;

import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.undo.pojo.UndoInfo;

import java.sql.SQLException;

/**
 * 表示一个事务资源.这是事务管理的最小单元.
 *
 * @param <V> 资源类型.
 * @author dongbin
 * @version 0.1 2020/2/15 21:51
 * @since 1.8
 */
public interface TransactionResource<V> {

    /**
     * 资源类型的标记
     * @return
     */
    DbTypeEnum dbType();

    /**
     * 资源的标识.
     * @return 资源的标识.
     */
    Object key();

    /**
     * 承载的资源实体.
     * @return 资源.
     */
    V value();

    /**
     * 提交资源.
     * @throws SQLException
     */
    void commit() throws SQLException;

    /**
     * 回滚资源.
     * @throws SQLException
     */
    void rollback() throws SQLException;

    /**
     * 资源的销毁或者回收.
     * @throws SQLException
     */
    void destroy() throws SQLException;

    /**
     * 资源是否销毁或者回收
     */
    boolean isDestroyed() throws SQLException;

    void setUndoInfo(Long txId, OpTypeEnum opType, Object obj);

    UndoInfo getUndoInfo();
}
