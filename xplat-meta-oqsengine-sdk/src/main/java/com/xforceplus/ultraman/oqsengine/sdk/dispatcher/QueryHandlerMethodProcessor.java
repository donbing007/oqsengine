package com.xforceplus.ultraman.oqsengine.sdk.dispatcher;

import com.xforceplus.ultraman.oqsengine.sdk.dispatcher.messaging.QueryExpressionEvaluator;
import com.xforceplus.ultraman.oqsengine.sdk.service.ContextService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.aop.scope.ScopedObject;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class QueryHandlerMethodProcessor
        implements SmartInitializingSingleton, ApplicationContextAware, BeanFactoryPostProcessor {

    protected final Log logger = LogFactory.getLog(getClass());

    @Nullable
    private ConfigurableApplicationContext applicationContext;

    @Nullable
    private ConfigurableListableBeanFactory beanFactory;

//    @Nullable
    private ServiceDispatcher serviceDispatcher;

    private final QueryExpressionEvaluator evaluator = new QueryExpressionEvaluator();

    private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));

    @Autowired(required = false)
    private ContextService contextService;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        Assert.isTrue(applicationContext instanceof ConfigurableApplicationContext,
                "ApplicationContext does not implement ConfigurableApplicationContext");
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;

        Map<String, ServiceDispatcher> beans = beanFactory.getBeansOfType(ServiceDispatcher.class, false, false);
        List<ServiceDispatcher> dispatchers = new ArrayList<>(beans.values());
//        AnnotationAwareOrderComparator.sort(factories);
        if(dispatchers.size() > 0) {
            this.serviceDispatcher = dispatchers.get(0);
        }

//        Map<String, ContextService> contextBeans = beanFactory.getBeansOfType(ContextService.class, false, false);
//        List<ContextService> contextServices = new ArrayList<>(contextBeans.values());
////        AnnotationAwareOrderComparator.sort(factories);
//        if(contextServices.size() > 0) {
//            this.contextService = contextServices.get(0);
//        }
    }


    @Override
    public void afterSingletonsInstantiated() {
        ConfigurableListableBeanFactory beanFactory = this.beanFactory;
        Assert.state(this.beanFactory != null, "No ConfigurableListableBeanFactory set");
        String[] beanNames = beanFactory.getBeanNamesForType(Object.class);
        for (String beanName : beanNames) {
            if (!ScopedProxyUtils.isScopedTarget(beanName)) {
                Class<?> type = null;
                try {
                    type = AutoProxyUtils.determineTargetClass(beanFactory, beanName);
                }
                catch (Throwable ex) {
                    // An unresolvable bean type, probably from a lazy bean - let's ignore it.
                    if (logger.isDebugEnabled()) {
                        logger.debug("Could not resolve target class for bean with name '" + beanName + "'", ex);
                    }
                }
                if (type != null) {
                    if (ScopedObject.class.isAssignableFrom(type)) {
                        try {
                            Class<?> targetClass = AutoProxyUtils.determineTargetClass(
                                    beanFactory, ScopedProxyUtils.getTargetBeanName(beanName));
                            if (targetClass != null) {
                                type = targetClass;
                            }
                        }
                        catch (Throwable ex) {
                            // An invalid scoped proxy arrangement - let's ignore it.
                            if (logger.isDebugEnabled()) {
                                logger.debug("Could not resolve target bean for scoped proxy '" + beanName + "'", ex);
                            }
                        }
                    }
                    try {
                        processBean(beanName, type);
                    }
                    catch (Throwable ex) {
                        throw new BeanInitializationException("Failed to process @EventListener " +
                                "annotation on bean with name '" + beanName + "'", ex);
                    }
                }
            }
        }
    }

    private void processBean(final String beanName, final Class<?> targetType) {
        if (!this.nonAnnotatedClasses.contains(targetType) &&
                AnnotationUtils.isCandidateClass(targetType, QueryHandler.class) &&
                !isSpringContainerClass(targetType)) {

            Map<Method, QueryHandler> annotatedMethods = null;
            try {
                annotatedMethods = MethodIntrospector.selectMethods(targetType,
                        (MethodIntrospector.MetadataLookup<QueryHandler>) method ->
                                AnnotatedElementUtils.findMergedAnnotation(method, QueryHandler.class));
            }
            catch (Throwable ex) {
                // An unresolvable type in a method signature, probably from a lazy bean - let's ignore it.
                if (logger.isDebugEnabled()) {
                    logger.debug("Could not resolve methods for bean with name '" + beanName + "'", ex);
                }
            }

            if (CollectionUtils.isEmpty(annotatedMethods)) {
                this.nonAnnotatedClasses.add(targetType);
                if (logger.isTraceEnabled()) {
                    logger.trace("No @Query annotations found on bean class: " + targetType.getName());
                }
            }
            else {
                // Non-empty set of methods
                ConfigurableApplicationContext context = this.applicationContext;
                Assert.state(context != null, "No ApplicationContext set");
               // List<EventListenerFactory> factories = this.eventListenerFactories;
               // Assert.state(factories != null, "EventListenerFactory List not initialized");
                for (Method method : annotatedMethods.keySet()) {
//                    for (EventListenerFactory factory : factories) {
//                        if (factory.supportsMethod(method)) {
                            Method methodToUse = AopUtils.selectInvocableMethod(method, context.getType(beanName));
                            QueryHandlerAdapter handler =
                                   new QueryHandlerAdapter(beanName, targetType, methodToUse, context,  this.evaluator, this.contextService);
                            serviceDispatcher.addQueryAdapter(handler);
//                        }
//                    }
                }
                if (logger.isDebugEnabled()) {
                    logger.debug(annotatedMethods.size() + " @Query methods processed on bean '" +
                            beanName + "': " + annotatedMethods);
                }
            }
        }
    }

    private static boolean isSpringContainerClass(Class<?> clazz) {
        return (clazz.getName().startsWith("org.springframework.") &&
                !AnnotatedElementUtils.isAnnotated(ClassUtils.getUserClass(clazz), Component.class));
    }

}