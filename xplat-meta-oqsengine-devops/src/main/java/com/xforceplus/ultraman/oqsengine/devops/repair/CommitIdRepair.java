package com.xforceplus.ultraman.oqsengine.devops.repair;

import java.sql.SQLException;
import java.util.Optional;

/**
 * desc :
 * name : CommitIdRepair
 *
 * @author : xujia
 * date : 2020/12/11
 * @since : 1.8
 */
public interface CommitIdRepair {
    /*
        根据id列表清理Redis中的CommitId
     */
    void clean(long... ids);

    /*
        修复redis中的commitId，当参数commitId为NULL时，取目前数据库中最大CommitId + 1
     */
    void repair(Optional<Long> commitId) throws SQLException;
}
