package com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl;

import static com.xforceplus.ultraman.oqsengine.idgenerator.common.constant.Constants.NUMBER_PATTEN_PARSER;

import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParser;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明: 数字模版解析类.
 * 作者(@author): liwei
 * 创建时间: 5/11/21 4:45 PM
 */
public class NumberPatternParser implements PatternParser {

    private static final String REGEX_PATTEN = "\\{(0+)\\}";


    @Override
    public String getName() {
        return NUMBER_PATTEN_PARSER;
    }

    @Override
    public String parse(String patten, Long id) {
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

    @Override
    public boolean needHandle(String str) {
        Pattern pattern = Pattern.compile(REGEX_PATTEN);
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }
}
