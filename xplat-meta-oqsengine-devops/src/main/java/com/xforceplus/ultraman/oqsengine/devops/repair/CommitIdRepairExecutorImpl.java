package com.xforceplus.ultraman.oqsengine.devops.repair;

import static com.xforceplus.ultraman.oqsengine.status.impl.CommitIdStatusServiceImpl.INVALID_COMMITID;

import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import javax.annotation.Resource;

/**
 * 提交号修复执行器.
 *
 * @author xujia 2020/12/11
 * @since 1.8
 */
public class CommitIdRepairExecutorImpl implements CommitIdRepairExecutor {
    @Resource
    private MasterStorage masterStorage;

    @Resource
    private CommitIdStatusService commitIdStatusService;

    private static final long INIT_COMMIT_ID = INVALID_COMMITID + 1;

    @Override
    public void clean(Long... ids) {
        //  全清除
        if (null == ids || ids.length == 0) {
            commitIdStatusService.obsoleteAll();
        } else {
            //  根据ID列表清除
            long[] arrayIds = new long[ids.length];
            for (int i = 0; i < ids.length; i++) {
                arrayIds[i] = ids[i];
            }

            commitIdStatusService.obsolete(arrayIds);
        }
    }

    @Override
    public long[] rangeOfCommitId() {
        long[] range = new long[2];
        range[0] = commitIdStatusService.getMin().orElse(INVALID_COMMITID);
        range[1] = commitIdStatusService.getMax().orElse(INVALID_COMMITID);
        return range;
    }

    @Override
    public long[] allCommitIds() {
        return commitIdStatusService.getAll();
    }

    @Override
    public void cleanLessThan(long id) {
        long[] result = Arrays.stream(commitIdStatusService.getAll())
            .filter(s -> {
                return s > id;
            })
            .toArray();

        commitIdStatusService.obsolete(result);
    }


    @Deprecated
    @Override
    public void repair(Optional<Long> commitId) throws SQLException {

    }
}
