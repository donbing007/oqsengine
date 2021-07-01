package com.xforceplus.ultraman.oqsengine.calculate.function;

import com.alibaba.google.common.collect.Maps;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorBigInt;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;
import com.xforceplus.ultraman.oqsengine.calculate.dto.ExpressionWrapper;
import com.xforceplus.ultraman.oqsengine.calculate.utils.ExpressionUtils;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cglib.core.Local;

/**
 * .
 *
 * @author leo
 * @version 0.1 7/1/21 6:09 PM
 * @since 1.8
 */
public class OffsetTest {


    @Before
    public void before() {
        ExpressionUtils.addFunction(new TimeOffsetFunction());
    }

    @Test
    public void testOffsetData() {
     ExpressionWrapper wrapper = ExpressionWrapper.Builder.anExpression()
            .withCached(true)
            .withCode("code")
            .withExpression("timeOffset(createTime,1,1)").build();
        Map<String,Object> params = Maps.newHashMap();
        params.put("createTime", LocalDateTime.now());
       Object result =  ExpressionUtils.execute(wrapper,params);
        Assert.assertEquals(result instanceof LocalDateTime ,true);
       Assert.assertEquals (((LocalDateTime)result).getYear(), LocalDateTime.now().getYear() + 1);
       System.out.println(result);
    }

    @Test
    public void testFunction() {
        Map<String,Object> params = Maps.newHashMap();
        params.put("createTime", LocalDateTime.now());
        TimeOffsetFunction function = new TimeOffsetFunction();
        Object result = function.call(params, FunctionUtils.wrapReturn(LocalDateTime.now())
            ,new AviatorBigInt(1),new AviatorBigInt(1));
       LocalDateTime expect =  (LocalDateTime) ((AviatorRuntimeJavaType) result).getValue(params);
        Assert.assertEquals (expect.getYear(), LocalDateTime.now().getYear() + 1);
    }
}
