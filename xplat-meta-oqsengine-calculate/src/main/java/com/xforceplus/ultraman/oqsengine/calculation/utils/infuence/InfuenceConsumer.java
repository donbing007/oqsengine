package com.xforceplus.ultraman.oqsengine.calculation.utils.infuence;

import java.util.Optional;

/**
 * 迭代影响力树时的消费接口.
 *
 * @author dongbin
 * @version 0.1 2021/9/30 17:42
 * @since 1.8
 */
@FunctionalInterface
public interface InfuenceConsumer {

    /**
     * 消费.
     *
     * @param parentParticipantOp 上层参与者.
     * @param abstractParticipant         参与者.
     * @param infuenceInner       当前的影响树.
     * @return true 继续,false中断.
     */
    Action accept(Optional<AbstractParticipant> parentParticipantOp, AbstractParticipant abstractParticipant, Infuence infuenceInner);

    /**
     * 迭代影响力时用以控制之后迭代的动作表示.
     */
    static enum Action {
        /**
         * 继续正常迭代.
         */
        CONTINUE,
        /**
         * 终止迭代.
         */
        OVER,
        /**
         * 终止被当前影响力影响的.
         */
        OVER_SELF;
    }
}
