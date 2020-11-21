package com.xforceplus.ultraman.oqsengine.devops;

import com.xforceplus.ultraman.oqsengine.pojo.devops.cdc.CdcErrorTask;

import java.sql.SQLException;

/**
 * desc :
 * name : DevOpsStorage
 *
 * @author : xujia
 * date : 2020/11/21
 * @since : 1.8
 */
public interface DevOpsStorage {

    /*
        record cdc error
     */
    int recordCdcError(CdcErrorTask cdcErrorTask) throws SQLException;
}
