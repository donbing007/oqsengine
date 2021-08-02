package com.xforceplus.ultraman.oqsengine.common.serializable;

import org.junit.jupiter.api.Test;

/**
 * kryo 序列化测试.
 *
 * @author dongbin
 * @version 0.1 2021/07/16 13:39
 * @since 1.8
 */
public class KryoSerializeStrategyTest extends AbstractSerializeStrategyTest {

    @Test
    public void test() {
        doTest(new KryoSerializeStrategy());
    }

}