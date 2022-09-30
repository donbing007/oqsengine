package com.xforceplus.ultraman.oqsengine.calculation.logic.formula;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationScenarios;
import com.xforceplus.ultraman.oqsengine.calculation.dto.AffectedInfo;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.logic.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.logic.formula.helper.FormulaHelper;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.CalculationParticipant;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InfuenceGraph;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InfuenceGraphConsumer;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Participant;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Formula;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IValueUtils;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
public class FormulaCalculationLogic implements CalculationLogic {

    final Logger logger = LoggerFactory.getLogger(FormulaCalculationLogic.class);

    private static final int MAX_ERROR_MESSAGE_LENGTH = 256;

    @Override
    public Optional<IValue> calculate(CalculationContext context) throws CalculationException {
        Formula formula = (Formula) context.getFocusField().config().getCalculation();

        //  执行公式
        try {
            //  调用公式执行器执行
            Object r = FormulaHelper.calculate(formula.getExpression(), formula.getArgs(), context);

            if (null == r) {
                logger.debug("formula executed, but result is null.");
                return Optional.empty();
            }

            return Optional.of(IValueUtils.toIValue(context.getFocusField(), r));
        } catch (Exception e) {
            //  异常时
            if (formula.getFailedPolicy().equals(Formula.FailedPolicy.USE_FAILED_DEFAULT_VALUE)) {

                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "formula [entityFieldId-{}] has executed failed, will use failed default value to instead, [reason-{}]",
                        context.getFocusField().id(), e.getMessage());
                }

                context.hint(
                    context.getFocusField(),
                    e.getMessage().substring(0, Math.min(e.getMessage().length(), MAX_ERROR_MESSAGE_LENGTH)));

                return Optional.of(IValueUtils.toIValue(context.getFocusField(), formula.getFailedDefaultValue()));


            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("formula [entityFieldId-{}] has executed failed, execution will broken, [reason-{}]",
                        context.getFocusField().id(), e.getMessage());
                }

                throw new CalculationException(e.getMessage(), e);
            }
        }
    }

    @Override
    public void scope(CalculationContext context, InfuenceGraph infuence) {
        infuence.scanNoSource((parentParticipant, participant, infuenceInner) -> {

            IEntityClass participantClass = participant.getEntityClass();
            IEntityField participantField = participant.getField();

            List<IEntityField> fields = participantClass.fields().stream()
                .filter(f -> f.calculationType() == CalculationType.FORMULA).collect(Collectors.toList());
            if (fields != null && fields.size() > 0) {
                fields.forEach(f -> {
                    Formula formula = (Formula) f.config().getCalculation();

                    List<String> args = formula.getArgs().stream()
                        .filter(s -> !s.equals(FormulaHelper.FORMULA_THIS_VALUE)).collect(
                            Collectors.toList());

                    if (args.size() > 0) {
                        if (args.contains(participantField.name())) {

                            Participant p = CalculationParticipant.Builder.anParticipant()
                                .withEntityClass(participantClass)
                                .withField(f)
                                .build();

                            infuenceInner.impact(participant, p);
                        }
                    }
                });
            }

            return InfuenceGraphConsumer.Action.CONTINUE;
        });
    }

    @Override
    public Collection<AffectedInfo> getMaintainTarget(
        CalculationContext context, Participant participant, Collection<IEntity> entities)
        throws CalculationException {

        return entities.stream().filter(e -> e.id() > 0)
            .map(e -> new AffectedInfo(e, e.id()))
            .collect(Collectors.toList());
    }

    @Override
    public CalculationType supportType() {
        return CalculationType.FORMULA;
    }

    /**
     * 需要维护的场景.
     *
     * @return 需要维护的场景列表.
     */
    @Override
    public CalculationScenarios[] needMaintenanceScenarios() {
        return new CalculationScenarios[] {
            CalculationScenarios.BUILD,
            CalculationScenarios.REPLACE,
            CalculationScenarios.DELETE
        };
    }

}
