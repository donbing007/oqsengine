package com.xforceplus.ultraman.oqsengine.devops.rebuild.utils;

import com.xforceplus.ultraman.oqsengine.storage.transaction.commit.CommitHelper;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class DevOpsUtils {

    /**
     * 是否为维护ID.
     *
     * @param commitId 维护commitId.
     *
     * @return true/false.
     */
    public static boolean isMaintainRecord(long commitId) {
        return commitId == CommitHelper.getMaintainCommitId();
    }
}
