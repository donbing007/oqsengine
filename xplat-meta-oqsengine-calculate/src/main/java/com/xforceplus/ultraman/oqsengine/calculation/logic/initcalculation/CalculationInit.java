package com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import java.util.List;

/**
 * 实例重算接口，支持单实例和多实例重算.
 *
 */
public interface CalculationInit {
    /**
     * 单实例重算.
     *
     * @param initInstance 重算实例单元.
     * @return 重算后实例.
     */
    public IEntity init(InitInstance initInstance);


    /**
     * 多实例重算.
     *
     * @param initInstances 重算实例单元集合.
     * @return 重算后实例.
     */
    public List<IEntity> init(List<InitInstance> initInstances);
}
