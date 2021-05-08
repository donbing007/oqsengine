package com.xforceplus.ultraman.oqsengine.devops.repair;

import java.sql.SQLException;
import java.util.Optional;

/**
 * 提交号修复.
 *
 * @author xujia 2020/12/11
 * @since 1.8
 */
public interface CommitIdRepairExecutor {
    /*
        根据id列表清理Redis中的CommitId
     */
    void clean(Long... ids);

    /**
     * 获取当前commitId的范围.
     */
    long[] rangeOfCommitId();

    /**
     * 删除比传入commitId小的所有commitId.
     */
    void cleanLessThan(long id);

    /**
     * 修复redis中的commitId，当参数commitId为NULL时，取目前数据库中最大CommitId + 1.
     */
    void repair(Optional<Long> commitId) throws SQLException;
}
