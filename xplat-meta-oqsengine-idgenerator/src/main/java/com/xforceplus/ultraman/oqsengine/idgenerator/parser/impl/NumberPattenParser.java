package com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl;

import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PattenParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.xforceplus.ultraman.oqsengine.idgenerator.common.constant.Constants.NUMBER_PATTEN_PARSER;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 5/11/21 4:45 PM
 */
public class NumberPattenParser implements PattenParser {

    private static final String regexPatten = "\\{(0+)\\}";


    @Override
    public String getName() {
        return NUMBER_PATTEN_PARSER;
    }

    @Override
    public String parse(String patten, Long id) {
        String ret = id.toString();
        Pattern pattern = Pattern.compile(regexPatten);
        Matcher matcher =  pattern.matcher(patten);
        if (matcher.find()) {
            String idStr =  matcher.group(1);
            String formatStr = "%0"+(idStr.length())+"d";
            ret = patten.replace(patten.substring(matcher.start(),matcher.end()),String.format(formatStr,id));
        }
        return ret;
    }

    @Override
    public boolean needHandle(String str) {
        Pattern pattern = Pattern.compile(regexPatten);
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }
}
