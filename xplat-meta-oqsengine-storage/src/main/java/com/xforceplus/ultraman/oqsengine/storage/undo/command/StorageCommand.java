package com.xforceplus.ultraman.oqsengine.storage.undo.command;

import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;

import java.sql.SQLException;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/27/2020 3:38 PM
 * 功能描述:
 * 修改历史:
 */
public interface  StorageCommand {

    OpTypeEnum opType();

    Object execute(TransactionResource resource, Object data) throws SQLException;

}
