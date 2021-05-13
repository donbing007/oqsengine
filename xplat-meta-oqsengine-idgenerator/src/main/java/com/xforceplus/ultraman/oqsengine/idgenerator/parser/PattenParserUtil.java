package com.xforceplus.ultraman.oqsengine.idgenerator.parser;

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
public class PattenParserUtil implements ApplicationContextAware {

    private  static  ApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        applicationContext = applicationContext;
    }

    public static PattenParserManager getInstance() {
        return (PattenParserManager)applicationContext.getBean(PattenParserManager.class);
    }

    public static String parse(String patten,Long id) {
        return getInstance().parse(patten,id);
    }
}
