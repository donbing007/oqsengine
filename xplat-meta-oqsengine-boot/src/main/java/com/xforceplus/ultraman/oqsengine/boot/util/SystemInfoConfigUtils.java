package com.xforceplus.ultraman.oqsengine.boot.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import com.xforceplus.ultraman.oqsengine.common.StringUtils;
import java.util.Map;
import java.util.Optional;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class SystemInfoConfigUtils {

    /**
     * 获取一个简单的url连接字符串.
     */
    public static String getSimpleUrl(String startString, String endString, String complexUrl) {
        if (StringUtils.isEmpty(complexUrl)) {
            return complexUrl;
        }

        int start = complexUrl.indexOf(startString);
        int end = complexUrl.indexOf(endString, start);

        return complexUrl.substring(start <= 0 ? 0 : start + 1, end > 0 ? end : complexUrl.length());
    }

    /**
     * 获取config中的url连接字符串.
     */
    public static String getJdbcConfigUri(Config config) {
        Optional<Map.Entry<String, ConfigValue>> entryOptional =
            config.entrySet().stream().filter(e -> e.getKey().equals("jdbcUrl")).findFirst();

        if (entryOptional.isPresent()) {
            return SystemInfoConfigUtils.getSimpleUrl(
                "", "?", entryOptional.get().getValue().unwrapped().toString()
            );
        }

        return "";
    }
}
