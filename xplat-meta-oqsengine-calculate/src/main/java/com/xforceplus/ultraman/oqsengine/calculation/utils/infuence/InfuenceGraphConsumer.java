package com.xforceplus.ultraman.oqsengine.calculation.utils.infuence;

import java.util.Collection;

/**
 * 影响的消费动作.<br />
 * 每一次的消费都需要明确给出继续的动作.<br />
 *
 * @author dongbin
 * @version 0.1 2022/7/27 09:07
 * @since 1.8
 */
@FunctionalInterface
public interface InfuenceGraphConsumer {

    /**
     * 迭代树的消费动作.
     *
     * @param parent      双亲参与者,图的结点会有多个双亲参与者.
     * @param participant 当前参与者.
     * @param inner       当前消费的目标影响图实例.
     * @return 下一步动作.
     */
    Action accept(Collection<Participant> parent, Participant participant, InfuenceGraph inner);

    /**
     * 迭代图时的后续动作.
     */
    enum Action {
        /**
         * 继续正常迭代.
         */
        CONTINUE,
        /**
         * 终止迭代.
         */
        OVER,
        /**
         * 终止当前及以下所有影响力的迭代,从另外的分支继续.
         */
        OVER_SELF,
        /**
         * 终止扫描,并且去除当前结点之下所有父参与者为1的参与者.
         */
        OVER_REMOVE_SELF,
    }
}
