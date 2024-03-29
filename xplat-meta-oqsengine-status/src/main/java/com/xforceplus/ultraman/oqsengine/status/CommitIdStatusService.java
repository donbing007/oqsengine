package com.xforceplus.ultraman.oqsengine.status;

/**
 * 提交号状态管理服务.
 *
 * @author dongbin
 * @version 0.1 2020/11/13 17:22
 * @since 1.8
 */
public interface CommitIdStatusService {

    /**
     * 小于等于此值的判定为无效的commitid.
     */
    public static final long INVALID_COMMITID = 0;

    /**
     * 保存一个新的提交号.
     *
     * @param commitId 提交号.
     * @param ready    true就绪,false还没就绪.
     * @return 保存的提交号.
     */
    boolean save(long commitId, boolean ready);

    /**
     * 判断指定的提交号是否就绪.
     * 如果判断为就绪,那么之后再次的判断都将为非就绪.
     *
     * @param commitId 目标提交号.
     * @return true 就绪,false没有就绪.
     */
    boolean isReady(long commitId);

    /**
     * 判断指定的提交号是否就绪.
     *
     * @param commitIds 提交号列表.
     * @return true 就绪, false 没有就绪.
     */
    boolean[] isReady(long[] commitIds);

    /**
     * 使某个提交个状态进入就绪.
     *
     * @param commitId 目标提交号.
     */
    void ready(long commitId);

    /**
     * 返回所有未就绪的提交号.
     *
     * @return 未就绪的提交号.
     */
    long[] getUnreadiness();

    /**
     * 获取当前最小的提交号.
     *
     * @return 提交号.最小为0.
     */
    long getMinWithKeep();

    /**
     * 获取当前最小的提交号.
     *
     * @return 提交号.小于0表示没有.
     */
    long getMin();

    /**
     * 获取当前最大提交号.
     *
     * @return 提交号.最小为0.
     */
    long getMax();

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

    /**
     * 淘汰所有提交号.
     */
    void obsoleteAll();

    /**
     * 判断指定提交号是否已经淘汰.
     *
     * @param commitId 目标提交号.
     * @return true 淘汰,false没有淘汰.
     */
    boolean isObsolete(long commitId);
}
