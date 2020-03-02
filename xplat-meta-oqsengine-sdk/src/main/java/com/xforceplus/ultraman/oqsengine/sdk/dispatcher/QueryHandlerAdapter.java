package com.xforceplus.ultraman.oqsengine.sdk.dispatcher;

import com.xforceplus.ultraman.oqsengine.sdk.dispatcher.messaging.GeneralResponse;
import com.xforceplus.ultraman.oqsengine.sdk.dispatcher.messaging.Message;
import com.xforceplus.ultraman.oqsengine.sdk.dispatcher.messaging.QueryExpressionEvaluator;
import com.xforceplus.ultraman.oqsengine.sdk.service.ContextService;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueryHandlerAdapter {

    private final String beanName;

    private final Method method;

    private final Method targetMethod;

    private final AnnotatedElementKey methodKey;

    private final List<ResolvableType> declaredEventTypes;

    private final ResolvableType declaredReturenType;

    @Nullable
    private final String condition;

    private final int order;

    private final boolean isDefault;

    private final ContextService contextService;

    @Nullable
    private ApplicationContext applicationContext;

    @Nullable
    private QueryExpressionEvaluator evaluator;


    public QueryHandlerAdapter(String beanName, Class<?> targetClass, Method method
            , ApplicationContext applicationContext, QueryExpressionEvaluator evaluator
            , ContextService contextService) {
        this.beanName = beanName;
        this.method = BridgeMethodResolver.findBridgedMethod(method);
        this.targetMethod = (!Proxy.isProxyClass(targetClass) ?
                AopUtils.getMostSpecificMethod(method, targetClass) : this.method);
        this.methodKey = new AnnotatedElementKey(this.targetMethod, targetClass);

        QueryHandler ann = AnnotatedElementUtils.findMergedAnnotation(this.targetMethod, QueryHandler.class);
        this.declaredEventTypes = resolveDeclaredCmdTypes(method, ann);
        this.declaredReturenType = resolveDeclaredReturnType(method, ann);
        this.condition = (ann != null ? ann.condition() : null);
        this.order = resolveOrder(this.targetMethod);
        this.applicationContext = applicationContext;
        this.evaluator = evaluator;
        this.isDefault = (ann != null && ann.isDefault());
        this.contextService = contextService;
    }

    private static int resolveOrder(Method method) {
        Order ann = AnnotatedElementUtils.findMergedAnnotation(method, Order.class);
        return (ann != null ? ann.value() : 0);
    }

    //TODO
    //check if this can cover all the situation
    public boolean supportsQueryType(ResolvableType cmdType, ResolvableType retType) {

        boolean isSupport = false;
        for (ResolvableType declaredEventType : this.declaredEventTypes) {
            if (declaredEventType.isAssignableFrom(cmdType)) {
                isSupport = true;
                break;
            }
        }

        if(!this.declaredReturenType.isAssignableFrom(retType)) {
            return cmdType.hasUnresolvableGenerics();
        }

        return isSupport;
    }

    //TODO
    private static List<ResolvableType> resolveDeclaredCmdTypes(Method method, @Nullable QueryHandler ann) {

        int count = method.getParameterCount();
        if (count > 1) {
            throw new IllegalStateException(
                    "Maximum one parameter is allowed for query listener method: " + method);
        }

        if (count == 0) {
            throw new IllegalStateException(
                    "Query parameter is mandatory for query listener method: " + method);
        }
        return Collections.singletonList(ResolvableType.forMethodParameter(method, 0));
    }

    //TODO
    private static ResolvableType resolveDeclaredReturnType(Method method, @Nullable QueryHandler ann) {

        if(method.getReturnType().equals(Void.TYPE)){
            throw new IllegalStateException(
                    "return type is mandatory for query listener method: " + method);
        }

        return ResolvableType.forMethodReturnType(method);
    }

    public GeneralResponse processMsg(Message msg){
        Object[] args = resolveArguments(msg);
        if (shouldHandle(msg, args)) {
            Object result = doInvoke(args);
            if (result != null) {
                return handleResult(result);
            }
            else {
                return null;
            }
        }
        return null;
    }

    //TODO
    private GeneralResponse handleResult(Object result){
        return new GeneralResponse(result);
    }

    public boolean isDefault() {
        return this.isDefault;
    }

    public int getOrder() {
        return this.order;
    }

    protected Object getTargetBean() {
        Assert.notNull(this.applicationContext, "ApplicationContext must no be null");
        return this.applicationContext.getBean(this.beanName);
    }

    private void assertTargetBean(Method method, Object targetBean, Object[] args) {
        Class<?> methodDeclaringClass = method.getDeclaringClass();
        Class<?> targetBeanClass = targetBean.getClass();
        if (!methodDeclaringClass.isAssignableFrom(targetBeanClass)) {
            String msg = "The event listener method class '" + methodDeclaringClass.getName() +
                    "' is not an instance of the actual bean class '" +
                    targetBeanClass.getName() + "'. If the bean requires proxying " +
                    "(e.g. due to @Transactional), please use class-based proxying.";
            throw new IllegalStateException(getInvocationErrorMessage(targetBean, msg, args));
        }
    }

    protected String getDetailedErrorMessage(Object bean, String message) {
        StringBuilder sb = new StringBuilder(message).append("\n");
        sb.append("HandlerMethod details: \n");
        sb.append("Bean [").append(bean.getClass().getName()).append("]\n");
        sb.append("Method [").append(this.method.toGenericString()).append("]\n");
        return sb.toString();
    }

    private String getInvocationErrorMessage(Object bean, String message, Object[] resolvedArgs) {
        StringBuilder sb = new StringBuilder(getDetailedErrorMessage(bean, message));
        sb.append("Resolved arguments: \n");
        for (int i = 0; i < resolvedArgs.length; i++) {
            sb.append("[").append(i).append("] ");
            if (resolvedArgs[i] == null) {
                sb.append("[null] \n");
            }
            else {
                sb.append("[type=").append(resolvedArgs[i].getClass().getName()).append("] ");
                sb.append("[value=").append(resolvedArgs[i]).append("]\n");
            }
        }
        return sb.toString();
    }

    @Nullable
    protected Object doInvoke(Object... args) {
        Object bean = getTargetBean();
        // Detect package-protected NullBean instance through equals(null) check
        if (bean.equals(null)) {
            return null;
        }

        ReflectionUtils.makeAccessible(this.method);
        try {
            return this.method.invoke(bean, args);
        }
        catch (IllegalArgumentException ex) {
            assertTargetBean(this.method, bean, args);
            throw new IllegalStateException(getInvocationErrorMessage(bean, ex.getMessage(), args), ex);
        }
        catch (IllegalAccessException ex) {
            throw new IllegalStateException(getInvocationErrorMessage(bean, ex.getMessage(), args), ex);
        }
        catch (InvocationTargetException ex) {
            // Throw underlying exception
            Throwable targetException = ex.getTargetException();
            if (targetException instanceof RuntimeException) {
                throw (RuntimeException) targetException;
            }
            else {
                String msg = getInvocationErrorMessage(bean, "Failed to invoke event listener method", args);
                throw new UndeclaredThrowableException(targetException, msg);
            }
        }
    }

    @Nullable
    protected Object[] resolveArguments(Message event) {
        ResolvableType declaredEventType = getResolvableType(event);
        if (declaredEventType == null) {
            return null;
        }
        if (this.method.getParameterCount() == 0) {
            return new Object[0];
        }
        Class<?> declaredEventClass = declaredEventType.toClass();
        Object payload = event.getPayload();
        if (declaredEventClass.isInstance(payload)) {
            return new Object[] {payload};
        }
        return new Object[] {event};
    }

    @Nullable
    private ResolvableType getResolvableType(Message event) {
        ResolvableType payloadType = null;
        payloadType = ResolvableType.forClass(event.getPayloadType());

        for (ResolvableType declaredEventType : this.declaredEventTypes) {
            Class<?> eventClass = declaredEventType.toClass();
            if (!Message.class.isAssignableFrom(eventClass) &&
                    payloadType != null && declaredEventType.isAssignableFrom(payloadType)) {
                return declaredEventType;
            }
            if (eventClass.isInstance(event)) {
                return declaredEventType;
            }
        }
        return null;
    }

    private boolean shouldHandle(Message msg, @Nullable Object[] args) {
        if (args == null) {
            return false;
        }
        String condition = getCondition();
        if (StringUtils.hasText(condition)) {
            Assert.notNull(this.evaluator, "EventExpressionEvaluator must not be null");
            return this.evaluator.condition(
                    condition, msg, this.targetMethod, this.methodKey, args, this.applicationContext, this.contextService);
        }
        return true;
    }

    @Nullable
    protected String getCondition() {
        return this.condition;
    }

}
