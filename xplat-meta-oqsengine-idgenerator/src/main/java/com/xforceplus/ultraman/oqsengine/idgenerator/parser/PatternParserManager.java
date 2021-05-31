package com.xforceplus.ultraman.oqsengine.idgenerator.parser;

import com.alibaba.google.common.collect.Maps;
import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 2020/9/8 6:24 PM
 */
public class PatternParserManager implements InitializingBean, ApplicationContextAware {

    private Map<String, PatternParser> registedParser = Maps.newConcurrentMap();

    private ApplicationContext applicationContext;

    public void registVariableParser(PatternParser parser) {
        registedParser.put(parser.getName(), parser);
    }

    public void unRegist(String name) {
        registedParser.remove(name);
    }


    /**
     * Parse the id by pattern.
     *
     * @param pattern id pattern
     * @param id target id
     *
     * @return target id
     */
    public String parse(String pattern, Long id) {
        String newValue = pattern;
        for (PatternParser parser : registedParser.values()) {
            if (parser.needHandle(newValue)) {
                newValue = parser.parse(newValue, id);
            }
        }
        return newValue;
    }

    @Override
    public void afterPropertiesSet() {
        applicationContext.getBeansOfType(PatternParser.class).entrySet().stream().forEach(entry -> {
            registVariableParser(entry.getValue());
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
