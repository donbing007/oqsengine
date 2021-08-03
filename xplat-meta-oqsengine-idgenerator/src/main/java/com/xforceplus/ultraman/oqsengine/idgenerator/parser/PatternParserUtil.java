package com.xforceplus.ultraman.oqsengine.idgenerator.parser;

import static com.xforceplus.ultraman.oqsengine.idgenerator.parser.Pattern.DAY;
import static com.xforceplus.ultraman.oqsengine.idgenerator.parser.Pattern.MONTH;
import static com.xforceplus.ultraman.oqsengine.idgenerator.parser.Pattern.YEAR;

import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.PatternValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 5/13/21 10:54 AM
 */
public class PatternParserUtil implements ApplicationContextAware {

    private static final String REGEX_PATTEN = "\\{(0+)\\}";


    private static ApplicationContext applicationContext;

    private static final java.util.regex.Pattern LAST_NUMBER_PATTERN = java.util.regex.Pattern.compile("\\d+$");


    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    public static PatternParserManager getInstance() {
        return applicationContext.getBean(PatternParserManager.class);
    }

    public static String parse(String patten, Long id) {
        return getInstance().parse(patten, id);
    }

    /**
     * Judge need reset.
     *
     * @param pattern The pattern
     * @param current current ID object
     * @param next    Next ID object
     * @return true or false.
     */
    public static boolean needReset(String pattern, PatternValue current, PatternValue next) {
        return !getPatternKey(pattern, current).equals(getPatternKey(pattern, next));
    }

    /**
     * Get the pattern key.
     *
     * @param patternValue patternValue
     * @return patternKey
     */
    public static String getPatternKey(String pattern, PatternValue patternValue) {
        int numberPatternLength = 0;
        int start = 0;
        java.util.regex.Pattern regexPattern = Pattern.compile(REGEX_PATTEN);
        Matcher matcher = regexPattern.matcher(pattern);
        if (matcher.find()) {
            numberPatternLength = matcher.group(1).length();
            start = matcher.start();
        }
        if (numberPatternLength < String.valueOf(patternValue.getId()).length()) {
            numberPatternLength = String.valueOf(patternValue.getId()).length();
        }
        String left = pattern.substring(0, matcher.start());

        if (left.contains(YEAR)) {
            start -= 2;
        }
        if (left.contains(MONTH)) {
            start -= 2;
        }
        if (left.contains(DAY)) {
            start -= 2;
        }
        String value = patternValue.getValue();
        String numberStr = value.substring(start, start + numberPatternLength);
        return value.replace(numberStr, "");
    }
}
