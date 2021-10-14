package com.xforceplus.ultraman.oqsengine.calculation.utils.infuence;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
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
     * @param parentClassOp 父结点元信息.
     * @param participant   参与者.
     * @param infuenceInner 当前的影响树.
     * @return true 继续,false中断.
     */
    boolean accept(Optional<IEntityClass> parentClassOp, Participant participant, Infuence infuenceInner);

}
