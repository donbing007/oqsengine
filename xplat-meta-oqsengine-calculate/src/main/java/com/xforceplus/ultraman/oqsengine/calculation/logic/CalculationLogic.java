package com.xforceplus.ultraman.oqsengine.calculation.logic;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationScenarios;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Infuence;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Participant;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import java.util.Collection;
import java.util.Optional;

/**
 * 字段值计算定义.
 *
 * @author dongbin
 * @version 0.1 2021/07/01 17:40
 * @since 1.8
 */
public interface CalculationLogic {

    /**
     * 字段值计算.
     *
     * @param context 计算上下文.
     * @return 计算结果.
     * @throws CalculationException 计算发生异常.
     */
    public Optional<IValue> calculate(CalculationContext context) throws CalculationException;

    /**
     * 计算单个计算字段改变的影响范围.
     *
     * @param context  上下文.
     * @param infuence 影响树.
     */
    public default void scope(CalculationContext context, Infuence infuence) {
        // do nothing.
    }

    /**
     * 得到需要维护的实例标识列表.
     *
     * @param context         计算上下文.
     * @param participant     参与者.
     * @param triggerEntities 引起当前维护的改变实例.
     * @return 需要维护的对象实例ID.
     * @throws CalculationException 计算发生异常.
     */
    public default long[] getMaintainTarget(
        CalculationContext context,
        Participant participant,
        Collection<IEntity> triggerEntities) throws CalculationException {
        return new long[0];
    }

    /**
     * 需要维护的场景.
     *
     * @return 需要维护的场景列表.
     */
    public default CalculationScenarios[] needMaintenanceScenarios() {
        return new CalculationScenarios[0];
    }

    /**
     * 支持的计算类型.
     *
     * @return 计算类型.
     */
    public default CalculationType supportType() {
        return CalculationType.UNKNOWN;
    }
}
