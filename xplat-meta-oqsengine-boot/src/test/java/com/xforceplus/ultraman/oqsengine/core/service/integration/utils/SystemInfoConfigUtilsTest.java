package com.xforceplus.ultraman.oqsengine.core.service.integration.utils;

import com.xforceplus.ultraman.oqsengine.boot.util.SystemInfoConfigUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class SystemInfoConfigUtilsTest {

    @Test
    public void simpleUrlTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        SystemInfoConfigUtils systemInfoConfigUtils = new SystemInfoConfigUtils();

        Class c = systemInfoConfigUtils.getClass();
        Method getPureUrl = c.getMethod("getSimpleUrl", String.class, String.class, String.class);

        String url = (String) getPureUrl.invoke(systemInfoConfigUtils, "@", "?",
            "redis://8eSf4M97VLhP6hq8@127.0.0.1:6379/0?timeout=6000ms");

        Assertions.assertEquals("127.0.0.1:6379/0", url);


        url = (String) getPureUrl.invoke(systemInfoConfigUtils, "", "?",
            "jdbc:mysql://127.0.0.1:9306/oqsengine?characterEncoding=utf8&useHostsInPrivileges=false&useLocalSessionState=true&serverTimezone=Asia/Shanghai");

        Assertions.assertEquals("jdbc:mysql://127.0.0.1:9306/oqsengine", url);
    }
}
