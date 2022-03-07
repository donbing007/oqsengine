package com.xforceplus.ultraman.oqsengine.devops.rebuild.utils;

import com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant;

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
        return commitId == CDCConstant.MAINTAIN_COMMIT_ID;
    }
}
