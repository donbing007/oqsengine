package com.xforceplus.ultraman.oqsengine.status;

import java.util.Optional;

/**
 * 提交号状态管理服务.
 *
 * @author dongbin
 * @version 0.1 2020/11/13 17:22
 * @since 1.8
 */
public interface CommitIdStatusService {

    /**
     * 保存一个新的提交号.
     *
     * @param commitId 提交号.
     * @return 保存的提交号.
     */
    long save(long commitId);

    /**
     * 保存一个新的提交号,在执行前会执行一个预定动作.
     * 保证在提交号提交之前会执行这个动作,并且不会在此之前进行淘汰.
     *
     * @param commitId 提交号.
     * @param act      动作.
     * @return 保存的提交号.
     */
    long save(long commitId, Runnable act);

    /**
     * 获取当前最小的提交号.
     *
     * @return 提交号.
     */
    Optional<Long> getMin();

    /**
     * 获取当前最大提交号.
     *
     * @return 提交号.
     */
    Optional<Long> getMax();

    /**
     * 获取当前所有提交号的快照.
     *
     * @return 所有提交号.以提交号的顺序降序排列.
     */
    long[] getAll();

    /**
     * 当前提交号的数量.
     *
     * @return 当前提交号数量快照.
     */
    long size();

    /**
     * 淘汰多个提交号.
     *
     * @param commitIds 需要淘汰的提交号.
     */
    void obsolete(long... commitIds);
}
