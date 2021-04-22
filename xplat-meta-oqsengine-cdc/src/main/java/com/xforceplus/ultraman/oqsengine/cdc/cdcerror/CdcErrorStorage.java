package com.xforceplus.ultraman.oqsengine.cdc.cdcerror;

import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.condition.CdcErrorQueryCondition;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.dto.ErrorType;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * desc :
 * name : CdcErrorStorage
 *
 * @author : xujia
 * date : 2020/11/21
 * @since : 1.8
 */
public interface CdcErrorStorage {

    /*
        build cdc error
     */
    int buildCdcError(CdcErrorTask cdcErrorTask) throws SQLException;

    /*
        update cdc error status
    */
    int updateCdcError(long seqNo, FixedStatus fixedStatus) throws SQLException;

    /*
        submit recover request
     */
    int submitRecover(long seqNo, FixedStatus fixedStatus, String operationObjectString) throws SQLException;


    /*
        query by condition
     */
    Collection<CdcErrorTask> queryCdcErrors(CdcErrorQueryCondition res) throws SQLException;

}
