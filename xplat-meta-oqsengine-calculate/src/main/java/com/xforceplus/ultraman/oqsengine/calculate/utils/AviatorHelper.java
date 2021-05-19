package com.xforceplus.ultraman.oqsengine.calculate.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * aviator辅助类.
 */
public class AviatorHelper {

    private static final String REGEX_META = "(#\\{[^#${}]*\\})";
    private static final String REGEX_ENUM = "(\\$\\{[^#${}]*\\})";

    /**
     * 将规则转换成aviator可以识别的格式.
     */
    public static String parseRule(String ruleContent) {
        Pattern pattern = Pattern.compile(REGEX_META);
        Pattern enumPatten = Pattern.compile(REGEX_ENUM);
        return parse(parse(ruleContent, pattern), enumPatten);
    }

    private static String parse(String ruleContent, Pattern pattern) {
        Matcher matcher = pattern.matcher(ruleContent);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String matchStr = matcher.group();
            matcher.appendReplacement(sb, Matcher.quoteReplacement(matchStr.substring(2, matchStr.length() - 1)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
