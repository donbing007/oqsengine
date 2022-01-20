package com.xforceplus.ultraman.oqsengine.calculation;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;

/**
 * 实例的计算字段计算.
 *
 * @author dongbin
 * @version 0.1 2021/09/17 15:12
 * @since 1.8
 */
public interface Calculation {

    /**
     * 针对计算字段进行计算.
     * 计算依赖一个上下文,此上下文需要提供如下必须信息.
     * focusEntity  当前焦点实例,即当前需要被计算的实例.
     * focusClass   当前焦点实例元信息.
     * 还需要额外关注如下属性.
     * ValueChange 记录一个字段从一个值至另一个值的变化,当前焦点字段如果产生变化那需要在此产生记录.
     * CalculationContext 还提供了一个实例池,用以缓存当前处理相关的实例(包含自己).
     * 所有被焦点的实例都应该可以在实例缓存池中找到.
     * <p></p>
     * 注意: 当前焦点实例中的
     *
     * @param context 计算的上下文.
     * @return 计算后的结果.
     * @throws CalculationException 计算异常.
     */
    public IEntity calculate(CalculationContext context) throws CalculationException;

    /**
     * 当某个实例由于写入事务产生了改变,那么有可能需要进行维护.
     *
     * @param context 维护上下文.
     * @throws CalculationException 维护错误.
     */
    public void maintain(CalculationContext context) throws CalculationException;

}
