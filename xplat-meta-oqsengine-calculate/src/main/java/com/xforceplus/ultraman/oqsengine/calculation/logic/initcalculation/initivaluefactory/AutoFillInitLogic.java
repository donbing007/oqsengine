package com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.initivaluefactory;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationScenarios;
import com.xforceplus.ultraman.oqsengine.calculation.context.DefaultCalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.factory.CalculationLogicFactory;
import com.xforceplus.ultraman.oqsengine.calculation.logic.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InitCalculationParticipant;
import com.xforceplus.ultraman.oqsengine.idgenerator.client.BizIDGenerator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import java.util.Optional;
import javax.annotation.Resource;


/**
 * 自增编号初始化.
 *
 * @version 0.1 2021/12/2 14:51
 * @Auther weikai
 * @since 1.8
 */
public class AutoFillInitLogic implements InitIvalueLogic {
    @Resource
    private BizIDGenerator bizIDGenerator;

    @Override
    public CalculationType getCalculationType() {
        return CalculationType.AUTO_FILL;
    }

    @Override
    public IEntity init(IEntity entity, InitCalculationParticipant participant) {
        Optional<IValue> value = entity.entityValue().getValue(participant.getField().id());
        if (!value.isPresent() || (value.get().getValue() instanceof EmptyTypedValue)) {
            participant.setProcess(entity);
            DefaultCalculationContext build = DefaultCalculationContext.Builder.anCalculationContext().withScenarios(CalculationScenarios.BUILD).withBizIDGenerator(bizIDGenerator).build();
            build.focusField(participant.getField());
            build.focusEntity(entity, participant.getEntityClass());
            // 单例获取工厂
            CalculationLogic calculationLogic = CalculationLogicFactory.getInstance().getCalculationLogic(participant.getField().calculationType());
            Optional<IValue> calculate = calculationLogic.calculate(build);
            calculate.ifPresent(ivalue -> {
                entity.entityValue().addValue(ivalue);
                participant.setProcess(entity);
            });
        }
        return entity;
    }
}
