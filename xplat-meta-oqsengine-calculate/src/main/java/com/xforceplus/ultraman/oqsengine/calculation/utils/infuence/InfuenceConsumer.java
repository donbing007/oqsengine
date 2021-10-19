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
     * @param parentParticipant 上层参与者.
     * @param participant       参与者.
     * @param infuenceInner     当前的影响树.
     * @return true 继续,false中断.
     */
    boolean accept(Optional<Participant> parentParticipant, Participant participant, Infuence infuenceInner);

}
