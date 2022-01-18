package com.xforceplus.ultraman.oqsengine.cdc.consumer.tools;

import com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant;

/**
 * Created by justin.xu on 01/2022.
 *
 * @since 1.8
 */
public class CommonUtils {

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
