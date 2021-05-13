package com.xforceplus.ultraman.oqsengine.idgenerator.parser;

import com.alibaba.google.common.collect.Maps;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 2020/9/8 6:24 PM
 */
public class PattenParserManager implements InitializingBean, ApplicationContextAware {

    private  Map<String, PattenParser> registedParser = Maps.newConcurrentMap();

    private ApplicationContext applicationContext;

    public   void registVariableParser(PattenParser parser) {
        registedParser.put(parser.getName(), parser);
    }


    public String parse(String patten,Long id) {
        String newValue = patten;
        for(PattenParser parser: registedParser.values()) {
            if(parser.needHandle(newValue)) {
                newValue = parser.parse(newValue,id);
            }
        }
        return newValue;
    }

    @Override
    public void afterPropertiesSet() {
        applicationContext.getBeansOfType(PattenParser.class).entrySet().stream().forEach(entry -> {
            registVariableParser(entry.getValue());
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
