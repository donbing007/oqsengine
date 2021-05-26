package com.xforceplus.ultraman.oqsengine.boot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * .
 *
 * @author leo
 * @version 0.1 5/24/21 8:50 PM
 * @since 1.8
 */
public class RedisConfigUtil {
    private static Logger logger = LoggerFactory.getLogger(RedisConfigUtil.class);
    private static final String PWD_SPLITTER = "@";
    private static final String REDIS_PROTO_SIGN = "://";

    /**
     * getRedisUrlPassword.
     *
     * @param url
     *
     * @return password
     */
    public static String getRedisUrlPassword(String url) {

        if (url.indexOf(PWD_SPLITTER) != -1
            && url.indexOf(REDIS_PROTO_SIGN) != -1) {
            String password = url.substring(url.indexOf(REDIS_PROTO_SIGN) + 3, url.indexOf(PWD_SPLITTER));
            logger.info("Url is : {} password is {}", url, password);
            return password;
        }
        return "";
    }
}
