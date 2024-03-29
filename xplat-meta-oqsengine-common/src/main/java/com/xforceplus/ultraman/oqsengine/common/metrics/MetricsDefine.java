package com.xforceplus.ultraman.oqsengine.common.metrics;

/**
 * 指标定义.
 *
 * @author dongbin
 * @version 0.1 2020/4/22 15:21
 * @since 1.8
 */
public class MetricsDefine {

    /**
     * 所有指标前辍.
     */
    public static final String PREFIX = "oqs";

    /**
     * IO线程池.名称由于之前版本原因继承.
     * 实际这个指标包含了读写.
     */
    public static final String READ_THREAD_POOL = ".read-call";

    /**
     * 任务线程池.名称由于之前版本原因继承.
     * 实际这里包括了所有后台任务的执行.
     */
    public static final String WRITE_THREAD_POOL = ".write-call";

    /**
     * 写入的总数.
     * 标签 action(build|replace|delete)
     */
    public static final String WRITE_COUNT_TOTAL = PREFIX + ".write.count.total";

    /**
     * 读取的总数.
     * 标签 action(one|multiple|search)
     */
    public static final String READ_COUNT_TOTAL = PREFIX + ".read.count.total";

    /**
     * 操作失败的总数.
     */
    public static final String FAIL_COUNT_TOTAL = PREFIX + ".fail.count.total";

    /**
     * 操作延时.
     * 标签 action(build|replace|delete|one|multiple|condition|save|clean|load|need).
     */
    public static final String PROCESS_DELAY_LATENCY_SECONDS = PREFIX + ".process.delay.latency";

    /**
     * 当前事务量.
     */
    public static final String TRANSACTION_COUNT = PREFIX + ".transaction.count";

    /**
     * 事务持续时间分布.
     */
    public static final String TRANSACTION_DURATION_SECONDS = PREFIX + ".transaction.duration";

    /**
     * 未同步的提交号数量.
     */
    public static final String UN_SYNC_COMMIT_ID_COUNT_TOTAL = PREFIX + ".unsync.commitid.count.total";

    /**
     * 未同步的提交号最小值.
     */
    public static final String UN_SYNC_COMMIT_ID_MIN = PREFIX + ".unsync.commitid.min";

    /**
     * 未同步的提交号最大值.
     */
    public static final String UN_SYNC_COMMIT_ID_MAX = PREFIX + ".unsync.commitid.max";

    /**
     * CDC同步的延时.
     */
    public static final String CDC_SYNC_DELAY_LATENCY_SECONDS = PREFIX + ".cdc.sync.delay.latency";

    /**
     * CDC同步每次的同步数量.
     */
    public static final String CDC_SYNC_EXECUTED_COUNT = PREFIX + ".cdc.sync.executed.count";

    /**
     * CDC-NOT-READY-COMMIT-ID 当前未获取到Ready状态的ID.
     */
    public static final String CDC_NOT_READY_COMMIT = PREFIX + ".cdc.not.ready.commit";
    /**
     * 当前的提交号.
     */
    public static final String NOW_COMMITID = PREFIX + ".now.commitid";

    /**
     * 当前OQS的工作模式.
     */
    public static final String MODE = PREFIX + ".mode";

    /**
     * 只读状态的原因.
     */
    public static final String READ_ONLEY_MODE_REASE = PREFIX + ".readonly.rease";

    /**
     * 当前未进入消费流的.
     */
    public static final String CHANGELOG_IN_QUEUE = PREFIX + ".changelog.in-queue";

    /**
     * 当前未进入消费流的.
     */
    public static final String CHANGELOG_IN_STREAM = PREFIX + ".changelog.in-stream";

    /**
     * 当前处理的TX.
     */
    public static final String CHANGELOG_CONCURRENT_TX = PREFIX + ".changelog.in-progress-tx";
    /**
     * 计算字段逻辑片计算.tag是 logic(aggregation, autofill, formula, lookup)和action(calculate, scope).
     */
    public static final String CALCULATION_LOGIC_DELAY_LATENCY_SECONDS = PREFIX + ".calculation.logic.delay.latency";
}
