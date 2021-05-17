package com.xforceplus.ultraman.oqsengine.calculate.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AviatorUtil {

    private static final String regexMeta = "(#\\{[^#${}]*\\})";
    private static final String regexEnum = "(\\$\\{[^#${}]*\\})";

    /**
     *将规则转换成aviator可以识别的格式
     * @param ruleContent
     * @return
     */
    public static String parseRule(String ruleContent) {
        Pattern pattern =  Pattern.compile(regexMeta);
        Pattern EnumPatten = Pattern.compile(regexEnum);
        return parse(parse(ruleContent, pattern),EnumPatten);
    }

    private static String parse(String ruleContent, Pattern pattern) {
        Matcher matcher =  pattern.matcher(ruleContent);
        StringBuffer sb = new StringBuffer();
        while(matcher.find()) {
            String  matchStr = matcher.group();
            matcher.appendReplacement(sb,Matcher.quoteReplacement(matchStr.substring(2,matchStr.length()-1)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
