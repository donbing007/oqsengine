package com.xforceplus.ultraman.oqsengine.calculation.function;

import com.alibaba.google.common.base.Preconditions;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.xforceplus.ultraman.oqsengine.calculation.logic.autofill.adapt.RedisIDGenerator;
import com.xforceplus.ultraman.oqsengine.calculation.utils.NumberFormatUtils;
import com.xforceplus.ultraman.oqsengine.calculation.utils.SpringContextUtil;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 * .
 *
 * @author leo
 * @version 0.1 7/15/21 5:01 PM
 * @since 1.8
 */
public class GetIDFunction extends AbstractFunction {

    private static final String ID_GENERATOR_NAME = "redisIDGenerator";

    @Override
    public String getName() {
        return "getId";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject numberFormat, AviatorObject bizTag,
                              AviatorObject step) {
        RedisIDGenerator generator = (RedisIDGenerator) SpringContextUtil.getBean(ID_GENERATOR_NAME);
        Long stepValue = (Long) step.getValue(env);
        String bizTagValue = String.valueOf(bizTag.getValue(env));
        String numverFormatValue = String.valueOf(numberFormat.getValue(env));
        Preconditions.checkNotNull(generator);
        Preconditions.checkArgument(!StringUtils.isBlank(bizTagValue), "BizTag must not be empty!");
        Preconditions.checkArgument(!StringUtils.isBlank(numverFormatValue), "Number format must not be empty!");
        Long id = generator.nextId(bizTagValue, stepValue.intValue());
        String result = NumberFormatUtils.parse(numverFormatValue, id);
        return FunctionUtils.wrapReturn(result);
    }
}
