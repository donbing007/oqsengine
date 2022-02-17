package com.xforceplus.ultraman.oqsengine.common.load;

/**
 * 系统负载评估者定义.
 *
 * @author dongbin
 * @version 0.1 2022/1/17 11:55
 * @since 1.8
 */
public interface SystemLoadEvaluator {

    /**
     * 评估当前系统的负载,评估值范围在[0 - 100] 之间(闭区间).
     * 值越高表示负载越高.
     * 如果值为
     *
     * @return 0到100的评份.
     */
    public double evaluate();
}
