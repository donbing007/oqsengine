package com.xforceplus.ultraman.oqsengine.calculation.function;

import static com.xforceplus.ultraman.oqsengine.calculation.helper.FormulaHelper.FORMULA_CTX_PARAM;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

import com.alibaba.google.common.collect.Maps;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;
import com.xforceplus.ultraman.oqsengine.calculation.dto.ExecutionWrapper;
import com.xforceplus.ultraman.oqsengine.calculation.dto.ExpressionWrapper;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.utils.SpringContextUtil;
import com.xforceplus.ultraman.oqsengine.calculation.utils.aviator.AviatorHelper;
import com.xforceplus.ultraman.oqsengine.common.id.IdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.RedisOrderContinuousLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import io.lettuce.core.RedisClient;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

/**
 * .
 *
 * @author leo
 * @version 0.1 7/15/21 5:41 PM
 * @since 1.8
 */
@ExtendWith(RedisContainer.class)
public class GetIDFunctionTest {

    private static IdGenerator redisIDGenerator;

    /**
     * 初始化.
     */
    @BeforeAll
    public static void before() throws IllegalAccessException {
        RedisClient redisClient = CommonInitialization.getInstance().getRedisClient();
        redisIDGenerator = new RedisOrderContinuousLongIdGenerator(redisClient);
        AviatorHelper.addFunction(new GetIDFunction());
        MockedStatic mocked = mockStatic(SpringContextUtil.class);
        mocked.when(() -> SpringContextUtil.getBean(anyString())).thenReturn(redisIDGenerator);
    }

    @Test
    public void testGetIDFunction() throws CalculationException {
        GetIDFunction function = new GetIDFunction();
        Map<String, Object> params = com.alibaba.google.common.collect.Maps.newHashMap();
        IEntityField entityField = EntityField.Builder.anEntityField().withId(101010101).build();
        params.put(FORMULA_CTX_PARAM, entityField);
        AviatorObject result = function.call(params, new AviatorString("{000}"), new AviatorString("testOne"));
        Assertions.assertEquals("001", result.getValue(params).toString());
    }

    @Test
    public void testIDFunction() throws CalculationException {
        ExpressionWrapper wrapper = ExpressionWrapper.Builder.anExpression()
            .withCached(true)
            .withExpression("getId(\"{0000}\",\"tag1\")").build();
        Map<String, Object> params = Maps.newHashMap();
        IEntityField entityField = EntityField.Builder.anEntityField().withId(101010101).build();
        params.put(FORMULA_CTX_PARAM, entityField);
        Object result = AviatorHelper.execute(new ExecutionWrapper(wrapper, params));
        Assertions.assertEquals("0001", result.toString());
    }


    @Test
    public void testIDFunctionWithMap() throws CalculationException {

        ExpressionWrapper wrapper = ExpressionWrapper.Builder.anExpression()
            .withCached(true)
            .withExpression("tenantId+\":\"+getId(\"{0000}\",tenantId)").build();
        Map<String, Object> params = Maps.newHashMap();
        params.put("tenantId", "vanke");
        IEntityField entityField = EntityField.Builder.anEntityField().withId(101010101).build();
        params.put(FORMULA_CTX_PARAM, entityField);
        Object result = AviatorHelper.execute(new ExecutionWrapper(wrapper, params));
        Assertions.assertEquals("vanke:0001", result.toString());
    }

    @Test
    public void testIDFunctionWithDataMap() throws CalculationException {
        ExpressionWrapper wrapper = ExpressionWrapper.Builder.anExpression()
            .withCached(true)
            .withExpression("tenantId+\":\"+date_to_string(sysdate(),\"yyyy-MM-dd\")+\":\"+getId(\"{0000}\",tenantId)")
            .build();
        Map<String, Object> params = Maps.newHashMap();
        params.put("tenantId", "vanke1");
        IEntityField entityField = EntityField.Builder.anEntityField().withId(101010101).build();
        params.put(FORMULA_CTX_PARAM, entityField);
        Object result = AviatorHelper.execute(new ExecutionWrapper(wrapper, params));
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Assertions.assertEquals("vanke1:" + dateStr + ":0001", result.toString());
        Object result1 = AviatorHelper.execute(new ExecutionWrapper(wrapper, params));
        Assertions.assertEquals("vanke1:" + dateStr + ":0002", result1.toString());
        params.put("tenantId", "vanke2");
        Object result3 = AviatorHelper.execute(new ExecutionWrapper(wrapper, params));
        Assertions.assertEquals("vanke2:" + dateStr + ":0001", result3.toString());
    }


}
