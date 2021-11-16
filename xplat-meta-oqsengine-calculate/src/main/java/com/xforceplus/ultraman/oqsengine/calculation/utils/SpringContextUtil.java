package com.xforceplus.ultraman.oqsengine.calculation.utils;

import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by leo on 07/2021.
 *
 * @since 1.8
 */
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext m_applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        m_applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return m_applicationContext;
    }

    public static Map<String, Object> getBeans(Class type) {
        return m_applicationContext.getBeansOfType(type);
    }

    public static Object getBean(String beanName) {
        return m_applicationContext.getBean(beanName);
    }
}
