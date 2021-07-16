package com.xforceplus.ultraman.oqsengine.calculation.function;

import static com.xforceplus.ultraman.test.tools.core.constant.ContainerEnvKeys.REDIS_HOST;
import static com.xforceplus.ultraman.test.tools.core.constant.ContainerEnvKeys.REDIS_PORT;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

import com.alibaba.google.common.collect.Maps;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorBigInt;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;
import com.xforceplus.ultraman.oqsengine.calculation.dto.ExecutionWrapper;
import com.xforceplus.ultraman.oqsengine.calculation.dto.ExpressionWrapper;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationLogicException;
import com.xforceplus.ultraman.oqsengine.calculation.adapt.RedisIDGenerator;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ExpressionUtils;
import com.xforceplus.ultraman.oqsengine.calculation.utils.SpringContextUtil;
import com.xforceplus.ultraman.oqsengine.common.mock.ReflectionUtils;
import com.xforceplus.ultraman.test.tools.core.container.basic.RedisContainer;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

/**
 * .
 *
 * @author leo
 * @version 0.1 7/15/21 5:41 PM
 * @since 1.8
 */
@ExtendWith(RedisContainer.class)
public class GetIDFunctionTest {

    private static RedissonClient redissonClient;
    private static RedisIDGenerator redisIDGenerator;

    @BeforeAll
    public static void before() throws IllegalAccessException {
        Config config = new Config();
        String redisIp = System.getProperty(REDIS_HOST);
        int redisPort = Integer.parseInt(System.getProperty(REDIS_PORT));
        config.useSingleServer().setAddress(String.format("redis://%s:%s", redisIp, redisPort));
        redissonClient = Redisson.create(config);
        redisIDGenerator = new RedisIDGenerator();
        Collection<Field> taskFields = ReflectionUtils.printAllMembers(redisIDGenerator);
        ReflectionUtils.reflectionFieldValue(taskFields,"redissonClient",redisIDGenerator,redissonClient);
        ExpressionUtils.addFunction(new GetIDFunction());
        MockedStatic mocked = mockStatic(SpringContextUtil.class);
        mocked.when(()->SpringContextUtil.getBean(anyString())).thenReturn(redisIDGenerator);
    }

    @Test
    public void testGetIDFunction() throws CalculationLogicException {
        GetIDFunction function = new GetIDFunction();
        Map<String, Object> params = com.alibaba.google.common.collect.Maps.newHashMap();
        AviatorObject result = function.call(params,new AviatorString("{000}"),new AviatorString("testOne"),
            FunctionUtils.wrapReturn(1l));
        Assert.assertEquals("001",result.getValue(params).toString());
    }

    @Test
    public void testIDFunction() throws CalculationLogicException {
        ExpressionWrapper wrapper = ExpressionWrapper.Builder.anExpression()
            .withCached(true)
            .withExpression("getId(\"{0000}\",\"tag1\",10)").build();
        Map<String, Object> params = Maps.newHashMap();
        Object result = ExpressionUtils.execute(new ExecutionWrapper(wrapper, params));
        Assert.assertEquals("0010",result.toString());
    }


    @Test
    public void testIDFunctionWithMap() throws CalculationLogicException {

        ExpressionWrapper wrapper = ExpressionWrapper.Builder.anExpression()
            .withCached(true)
            .withExpression("tenantId+\":\"+getId(\"{0000}\",tenantId,10)").build();
        Map<String, Object> params = Maps.newHashMap();
        params.put("tenantId","vanke");
        Object result = ExpressionUtils.execute(new ExecutionWrapper(wrapper, params));
        Assert.assertEquals("vanke:0010",result.toString());
    }


}
