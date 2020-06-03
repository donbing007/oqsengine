//package com.xforceplus.ultraman.oqsengine.sdk.staticmode;
//
//import com.baomidou.mybatisplus.extension.service.IService;
//import com.xforceplus.ultraman.bocp.gen.annotation.BoService;
//import io.vavr.Tuple;
//import io.vavr.Tuple2;
//import org.springframework.beans.factory.SmartInitializingSingleton;
//import org.springframework.beans.factory.config.BeanDefinition;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
//import org.springframework.core.annotation.AnnotationUtils;
//import org.springframework.core.type.filter.AnnotationTypeFilter;
//
//import java.lang.reflect.ParameterizedType;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//
//
//public class StaticServiceLoader implements SmartInitializingSingleton {
//
//    private String packageName;
//
//    private ApplicationContext context;
//
//    public Map<String, Tuple2<Class, IService>> iServiceMap = new ConcurrentHashMap<>();
//
//    public StaticServiceLoader(String packageName, ApplicationContext context){
//        this.packageName = packageName;
//        this.context = context;
//    }
//
//    public void loadService(){
//        findIService();
//    }
//
//    public Tuple2<Class, IService> getService(Long id){
//        return iServiceMap.get(id.toString());
//    }
//
//    @Override
//    public void afterSingletonsInstantiated() {
//        loadService();
//    }
//
//    private IService findIService(){
//        Set<BeanDefinition> candidateComponents = getScanner().findCandidateComponents(packageName);
//
//        for ( BeanDefinition candidateComponent : candidateComponents ) {
//            try {
//                Class<?> clazz = Class.forName(candidateComponent.getBeanClassName());
//                if (IService.class.isAssignableFrom(clazz)) {
//                    Class<?>[] interfaces = clazz.getInterfaces();
//                    if(interfaces.length > 0){
//                        Class<?> interfaceClazz = interfaces[0];
//                        BoService annotation = AnnotationUtils.getAnnotation(clazz, BoService.class);
//                        final Map<String , ?> beansOfType = context.getBeansOfType(interfaceClazz);
//                        if(!beansOfType.isEmpty()){
//
//                            Class<?> param = null;
//                            try {
//                                param = (Class)((ParameterizedType) (((Class) clazz.getGenericInterfaces()[0]).getGenericInterfaces()[0])).getActualTypeArguments()[0];
//                            }catch(Exception ex){
//
//                            }
//                            Class<?> finalParam = param;
//                            beansOfType.entrySet().stream().findFirst()
//                                    .ifPresent(x -> iServiceMap.put(annotation.value(), Tuple.of(finalParam, (IService)x.getValue())));
//                        }
//                    }
//                }
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }
//
//    protected ClassPathScanningCandidateComponentProvider getScanner() {
//        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
//        provider.addIncludeFilter(new AnnotationTypeFilter(BoService.class));
//        return provider;
//    }
//}
