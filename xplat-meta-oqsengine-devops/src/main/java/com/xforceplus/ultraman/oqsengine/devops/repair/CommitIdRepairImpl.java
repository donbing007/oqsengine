package com.xforceplus.ultraman.oqsengine.devops.repair;

import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.Optional;

import static com.xforceplus.ultraman.oqsengine.status.impl.CommitIdStatusServiceImpl.INVALID_COMMITID;

/**
 * desc :
 * name : CommitIdRepairImpl
 *
 * @author : xujia
 * date : 2020/12/11
 * @since : 1.8
 */
public class CommitIdRepairImpl implements CommitIdRepair {
    @Resource
    private MasterStorage masterStorage;

    @Resource
    private CommitIdStatusService commitIdStatusService;

    private static final long INIT_COMMIT_ID = INVALID_COMMITID + 1;

    @Override
    public void clean(long... ids) {
        //  全清除
        if (null == ids || ids.length == 0) {
            commitIdStatusService.obsoleteAll();
        } else {
            //  根据ID列表清除
            commitIdStatusService.obsolete(ids);
        }
    }

    @Override
    public void repair(Optional<Long> commitId) throws SQLException {
        Long repairId = 0L;
        if (!commitId.isPresent()) {
            //  获取主库最大的commitId
            Long dbMinCommitId = masterStorage.maxCommitId().orElseGet(() -> INIT_COMMIT_ID);
            //  获取redis最大的commitId
            Long redisMinCommitId = commitIdStatusService.getMax().orElseGet(() -> INIT_COMMIT_ID);
            //  两者取大
            repairId = dbMinCommitId > redisMinCommitId ? dbMinCommitId : redisMinCommitId;
        } else {
            repairId = commitId.get();
        }

        commitIdStatusService.save(repairId,true);
    }
}
