package com.xforceplus.ultraman.oqsengine.calculation.logic.autofill;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationScenarios;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.logic.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.logic.formula.helper.FormulaHelper;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.AutoFill;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IValueUtils;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
public class AutoFillCalculationLogic implements CalculationLogic {

    final Logger logger = LoggerFactory.getLogger(AutoFillCalculationLogic.class);

    @Override
    public Optional<IValue> calculate(CalculationContext context) throws CalculationException {
        if (context.getScenariso() != CalculationScenarios.BUILD) {
            return Optional.empty();
        }

        AutoFill autoFill = (AutoFill) context.getFocusField().config().getCalculation();

        switch (autoFill.getDomainNoType()) {
            case NORMAL: {
                return onNormal(context);
            }
            case SENIOR: {
                return onSenior(context, autoFill);
            }
            default: {
                throw new CalculationException(String.format("autoFill executed failed, unSupport domainNoType-[%s].",
                    autoFill.getDomainNoType().name()));
            }
        }
    }

    @Override
    public CalculationType supportType() {
        return CalculationType.AUTO_FILL;
    }

    private Optional<IValue> onNormal(CalculationContext context) throws CalculationException {

        Timer.Sample sample = Timer.start(Metrics.globalRegistry);
        try {
            Object result = context.getResourceWithEx(() -> context.getBizIDGenerator())
                .nextId(String.valueOf(context.getFocusField().id()));

            if (null == result) {
                throw new CalculationException("autoFill id generate is null.");
            }
            return Optional.of(IValueUtils.toIValue(context.getFocusField(), result.toString()));
        } finally {
            sample.stop(Timer.builder(MetricsDefine.CALCULATION_LOGIC_DELAY_LATENCY_SECONDS)
                .tags(
                    "logic", "autoFill",
                    "action", "normal",
                    "exception", "none"
                )
                .publishPercentileHistogram(false)
                .publishPercentiles(null)
                .register(Metrics.globalRegistry));

        }
    }

    private Optional<IValue> onSenior(CalculationContext context, AutoFill autoFill) throws CalculationException {
        Timer.Sample sample = Timer.start(Metrics.globalRegistry);
        try {
            //  调用公式执行器执行
            return Optional.of(IValueUtils.toIValue(context.getFocusField(),
                FormulaHelper.calculate(autoFill.getExpression(), autoFill.getArgs(), context)));
        } catch (Exception e) {
            logger.warn("autoFill [entityFieldId-{}] has executed failed, execution will broken, [reason-{}]",
                context.getFocusField().id(), e.getMessage());
            throw new CalculationException(e.getMessage(), e);
        } finally {
            sample.stop(Timer.builder(MetricsDefine.CALCULATION_LOGIC_DELAY_LATENCY_SECONDS)
                .tags(
                    "logic", "autoFill",
                    "action", "senior",
                    "exception", "none"
                )
                .publishPercentileHistogram(false)
                .publishPercentiles(null)
                .register(Metrics.globalRegistry));

        }
    }
}
