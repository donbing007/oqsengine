package com.xforceplus.ultraman.oqsengine.common;

/**
 * 字符串工具箱.
 *
 * @author dongbin
 * @version 0.1 2021/04/20 14:31
 * @since 1.8
 */
public class StringUtils {

    /**
     * 过滤所有不可见字符.
     * 0-31以及127
     *
     * @param value 目标字符串.
     * @return 过滤结果.
     */
    public static String filterCanSeeChar(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        StringBuilder buff = new StringBuilder();
        for (char c : value.toCharArray()) {
            if ((c >= 0 && c <= 31) || c == 127) {
                continue;
            } else {
                buff.append(c);
            }
        }
        return buff.toString();
    }
}
