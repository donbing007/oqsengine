package com.xforceplus.ultraman.oqsengine.storage.transaction.commit;

/**
 * 提交号的帮助工具.
 * @author dongbin
 * @version 0.1 2020/11/11 17:31
 * @since 1.8
 */
public class CommitHelper {

    private static long UNCOMMIT_ID = Long.MAX_VALUE;

    /**
     * 校验提交号是否可用合法.
     * @param commitId 提交号.
     * @return true合法,false不合法.
     */
    public static boolean isLegal(long commitId) {
        if (UNCOMMIT_ID == commitId) {
            return false;
        }

        if (commitId < 0) {
            return false;
        }

        return true;
    }

    /**
     * 给出表示未提交的临时提交号.
     * @return 临时提交号.
     */
    public static long getUncommitId() {
        return UNCOMMIT_ID;
    }
}
