package com.xforceplus.ultraman.oqsengine.calculation.function;

import com.alibaba.google.common.collect.Maps;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorBigInt;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;
import com.xforceplus.ultraman.oqsengine.calculation.dto.ExecutionWrapper;
import com.xforceplus.ultraman.oqsengine.calculation.dto.ExpressionWrapper;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationLogicException;
import com.xforceplus.ultraman.oqsengine.calculation.utils.aviator.ExpressionUtils;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * .
 *
 * @author leo
 * @version 0.1 7/1/21 6:09 PM
 * @since 1.8
 */
public class OffsetTest {


    @BeforeEach
    public void before() {
        ExpressionUtils.addFunction(new TimeOffsetFunction());
    }

    @Test
    public void testOffsetData() throws CalculationLogicException {
        ExpressionWrapper wrapper = ExpressionWrapper.Builder.anExpression()
            .withCached(true)
            .withExpression("timeOffset(createTime,1,1)").build();
        Map<String, Object> params = Maps.newHashMap();
        params.put("createTime", LocalDateTime.now());
        Object result = ExpressionUtils.execute(new ExecutionWrapper(wrapper, params));
        Assertions.assertTrue(result instanceof LocalDateTime);
        Assertions.assertEquals(((LocalDateTime) result).getYear(), LocalDateTime.now().getYear() + 1);
        System.out.println(result);
    }

    @Test
    public void testFunction() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("createTime", LocalDateTime.now());
        TimeOffsetFunction function = new TimeOffsetFunction();
        Object result = function.call(params, FunctionUtils.wrapReturn(LocalDateTime.now())
            , new AviatorBigInt(1), new AviatorBigInt(1));
        LocalDateTime expect = (LocalDateTime) ((AviatorRuntimeJavaType) result).getValue(params);
        Assertions.assertEquals(expect.getYear(), LocalDateTime.now().getYear() + 1);
    }
}
