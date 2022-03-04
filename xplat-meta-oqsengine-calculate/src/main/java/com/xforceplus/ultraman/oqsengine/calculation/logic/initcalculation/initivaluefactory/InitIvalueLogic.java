package com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.initivaluefactory;

import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InitCalculationParticipant;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import java.sql.SQLException;

/**
 * 初始化字段字段逻辑工厂.
 *
 * @version 0.1 2021/12/2 14:46
 * @Auther weikai
 * @since 1.8
 */
public interface InitIvalueLogic {

    public CalculationType getCalculationType();

    public IEntity init(IEntity entity, InitCalculationParticipant participant) throws SQLException;
}
