package com.xforceplus.ultraman.oqsengine.common.serializable;

import org.junit.jupiter.api.Test;

/**
 * hessian2 序列化策略测试.
 *
 * @author dongbin
 * @version 0.1 2021/07/16 14:16
 * @since 1.8
 */
public class HessianSerializeStrategyTest extends AbstractSerializeStrategyTest {

    @Test
    public void test() throws Exception {
        doTest(new HessianSerializeStrategy());
    }
}
