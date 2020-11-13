package com.xforceplus.ultraman.oqsengine.common.metrics;

/**
 * 指标定义.
 * @author dongbin
 * @version 0.1 2020/4/22 15:21
 * @since 1.8
 */
public class MetricsDefine {

    /**
     * 所有指标前辍
     */
    public static final String PREFIX = "oqs";

    /**
     * 写入的总数.
     * 标签 action(build|replace|delete)
     */
    public static final String WRITE_COUNT_TOTAL = PREFIX + ".write.count.total";

    /**
     * 读取的总数
     * 标签 action(one|multiple|search)
     */
    public static final String READ_COUNT_TOTAL = PREFIX + ".read.count.total";

    /**
     * 操作失败的总数.
     */
    public static final String FAIL_COUNT_TOTAL = PREFIX + ".fail.count.total";

    /**
     * 操作延时.
     * 标签 action(build|replace|delete|one|multiple|condition).
     */
    public static final String PROCESS_DELAY_LATENCY_SECONDS = PREFIX + ".process.delay.latency";

    /**
     * 当前事务量
     */
    public static final String TRANSACTION_COUNT = PREFIX + ".transaction.count";

    /**
     * 事务持续时间分布.
     */
    public static final String TRANSACTION_DURATION_SECONDS = PREFIX + ".transaction.duration.seconds";

    /**
     * 未同步的提交号数量.
     */
    public static final String UN_SYNC_COMMIT_ID_COUNT_TOTAL = PREFIX + ".unsync.commitid.count.total";
}
