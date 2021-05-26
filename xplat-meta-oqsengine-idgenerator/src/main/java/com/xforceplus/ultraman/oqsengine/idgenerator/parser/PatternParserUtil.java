package com.xforceplus.ultraman.oqsengine.idgenerator.parser;

import com.xforceplus.ultraman.oqsengine.idgenerator.common.constant.DatePatternModel;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.PatternValue;
import com.xforceplus.ultraman.oqsengine.idgenerator.exception.IDGeneratorException;
import com.xforceplus.ultraman.oqsengine.idgenerator.generator.IDGenerator;
import java.util.regex.Matcher;
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

    private  static  ApplicationContext applicationContext;

    private static final java.util.regex.Pattern lastNumberPattern = java.util.regex.Pattern.compile("\\d+$");



    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    public static PattenParserManager getInstance() {
        return applicationContext.getBean(PattenParserManager.class);
    }

    public static String parse(String patten,Long id) {
        return getInstance().parse(patten,id);
    }

    public static boolean needReset(String pattern, PatternValue current, PatternValue next) {
        return !getPatternKey(current).equals(getPatternKey(next));
    }

    public static String getPatternKey(PatternValue patternValue) {
        String value = patternValue.getValue();
        Matcher matcher = lastNumberPattern.matcher(value);
        if(matcher.find()) {
            return value.substring(0, matcher.start());
        }
        return "";
    }
}
