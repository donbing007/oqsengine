package com.xforceplus.ultraman.oqsengine.calculation.utils.infuence;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

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
     * @param parentClass   父结点元信息.
     * @param entityClass   元信息.
     * @param field         字段.
     * @param infuenceInner 当前的影响树.
     * @return true 继续,false中断.
     */
    boolean accept(IEntityClass parentClass, IEntityClass entityClass, IEntityField field, Infuence infuenceInner);

}
