package com.xforceplus.ultraman.oqsengine.calculation.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * .
 *
 * @author leo
 * @version 0.1 7/15/21 5:07 PM
 * @since 1.8
 */
public class NumberFormatUtils {

    private static final String REGEX_PATTEN = "\\{(0+)\\}";


    public static String parse(String patten, Long id) {
        String ret = id.toString();
        Pattern pattern = Pattern.compile(REGEX_PATTEN);
        Matcher matcher = pattern.matcher(patten);
        if (matcher.find()) {
            String idStr = matcher.group(1);
            String formatStr = "%0" + (idStr.length()) + "d";
            ret = patten.replace(patten.substring(matcher.start(), matcher.end()), String.format(formatStr, id));
        }
        return ret;
    }
}
