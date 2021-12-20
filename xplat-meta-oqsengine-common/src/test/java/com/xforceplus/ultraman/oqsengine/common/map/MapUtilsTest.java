package com.xforceplus.ultraman.oqsengine.common.map;

import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 哈希工具测试.
 *
 * @author dongbin
 * @version 0.1 2021/12/4 01:01
 * @since 1.8
 */
public class MapUtilsTest {

    @Test
    public void testAsMap() throws Exception {
        Map map = MapUtils.asMap("1", "v1", "2", "v2");

        Assertions.assertEquals("v1", map.get("1"));
        Assertions.assertEquals("v2", map.get("2"));

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            MapUtils.asMap("1", "v1", "2");
        });
    }
}