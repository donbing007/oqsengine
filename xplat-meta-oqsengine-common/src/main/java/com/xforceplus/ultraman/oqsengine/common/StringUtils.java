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
     * 将转义符染色,使其变为可显示字符.
     * 比如 "\n" 将被替换成 "\\n".
     *
     * @param target 目标字符串.
     * @return 染色结果.
     */
    public static String encodeEscapeCharacters(String target) {
        if (isEmpty(target)) {
            return target;
        }

        int len = target.length();
        StringBuilder buf = new StringBuilder();

        char c;
        for (int i = 0; i < len; i++) {
            c = target.charAt(i);

            switch (c) {
                case 0:
                    buf.append('\\').append('0');
                    break;
                case '\n':
                    buf.append('\\').append('n');
                    break;
                case '\r':
                    buf.append('\\').append('r');
                    break;
                case '\\':
                    buf.append('\\').append('\\');
                    break;
                case '\'':
                    buf.append('\\').append("'");
                    break;
                case '"': // 小心驶得万年船
                    buf.append('\\').append('"');
                    break;
                case '\032': // WIN32 上可能的问题.
                    buf.append('\\').append('Z');
                    break;
                default:
                    buf.append(c);
            }
        }

        return buf.toString();
    }

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

    /**
     * 判断目标字符串是否为空.
     * 为空标准为 == null或者长度为0.
     *
     * @param value 目标字符串.
     * @return true 为空, false非空.
     */
    public static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }
}
