package com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation;


import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InitCalculationAbstractParticipant;
import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import io.vavr.Tuple2;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 计算字段初始化.
 *
 * @version 0.1 2021/12/01 14:05
 * @Auther weikai
 * @since 1.8
 */
public interface CalculationInitLogic extends Lifecycle {

    /**
     * 计算字段初始化入口.
     *
     * @param run 本次初始化的计算字段参与者集合.
     * @return 失败列表、成功列表.
     */
    public Map<String, List<InitCalculationAbstractParticipant>> accept(ArrayList<Map<IEntityClass, Collection<InitCalculationAbstractParticipant>>> run) throws InterruptedException;


    /**
     * 具体计算字段初始化逻辑.
     */
    public Tuple2<Boolean, List<InitCalculationAbstractParticipant>> initLogic(IEntityClass entityClass, Collection<InitCalculationAbstractParticipant> participants);


}
