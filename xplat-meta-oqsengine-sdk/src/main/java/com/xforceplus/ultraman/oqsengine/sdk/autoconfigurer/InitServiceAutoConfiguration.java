package com.xforceplus.ultraman.oqsengine.sdk.autoconfigurer;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import com.xforceplus.ultraman.oqsengine.pojo.dto.UltForm;
import com.xforceplus.ultraman.oqsengine.sdk.EntityServiceClient;
import com.xforceplus.ultraman.oqsengine.sdk.config.AuthSearcherConfig;
import com.xforceplus.ultraman.oqsengine.sdk.config.init.DictInitService;
import com.xforceplus.ultraman.oqsengine.sdk.config.init.ModuleInitService;
import com.xforceplus.ultraman.oqsengine.sdk.controller.*;
import com.xforceplus.ultraman.oqsengine.sdk.handler.DefaultEntityServiceHandler;
import com.xforceplus.ultraman.oqsengine.sdk.handler.EntityMetaFieldDefaultHandler;
import com.xforceplus.ultraman.oqsengine.sdk.handler.EntityMetaHandler;
import com.xforceplus.ultraman.oqsengine.sdk.interceptor.CodeExtendedInterceptor;
import com.xforceplus.ultraman.oqsengine.sdk.interceptor.DefaultSearchInterceptor;
import com.xforceplus.ultraman.oqsengine.sdk.interceptor.MatchRouter;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityService;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityServiceEx;
import com.xforceplus.ultraman.oqsengine.sdk.service.impl.EntityServiceExImpl;
import com.xforceplus.ultraman.oqsengine.sdk.service.impl.EntityServiceImpl;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.DictMapLocalStore;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.FormBoMapLocalStore;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.MetadataRepository;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.PageBoMapLocalStore;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.impl.MetadataRepositoryInMemoryImpl;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionQueryRequest;
import com.xforceplus.xplat.galaxy.framework.context.ContextService;
import com.xforceplus.xplat.galaxy.framework.dispatcher.interceptor.MessageDispatcherInterceptor;
import com.xforceplus.xplat.galaxy.framework.dispatcher.messaging.QueryMessage;
import com.xforceplus.xplat.galaxy.grpc.spring.EnableGrpcServiceClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@ConditionalOnProperty(value = "xplat.oqsengine.sdk.enabled", matchIfMissing = true)
@AutoConfigureOrder
@EnableGrpcServiceClients(basePackages = { "com.xforceplus.ultraman.metadata.grpc",  "com.xforceplus.ultraman.oqsengine.sdk"})
public class InitServiceAutoConfiguration {


    @Autowired
    private AuthSearcherConfig config;

    @Bean(destroyMethod = "terminate")
    @ConditionalOnMissingBean(ActorSystem.class)
    public ActorSystem actorSystem(){
        return ActorSystem.create("grpc-server");
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(ActorMaterializer.class)
    public ActorMaterializer mat(ActorSystem system) {
        return ActorMaterializer.create(system);
    }


    //@Deprecated
    //TODO
    @Bean("dictLocalStore")
    public DictMapLocalStore dictLocalStore() {
        return DictMapLocalStore.create();
    }

    //添加pageBo
    @Bean("pageBoMapLocalStore")
    public PageBoMapLocalStore pageBoLocalStore() {
        return PageBoMapLocalStore.create();
    }

    //添加formBo
    @Bean("formBoMapLocalStore")
    public FormBoMapLocalStore formBoLocalStore() {
        return FormBoMapLocalStore.create();
    }

    @Bean
    public DictInitService dictInitService(){
        return new DictInitService();
    }

    @Bean
    public ModuleInitService moduleInitService(){
        return new ModuleInitService();
    }

    @Bean
    public DefaultEntityServiceHandler entityServiceHandler(){
        return new DefaultEntityServiceHandler();
    }

    @Bean
    @ConditionalOnMissingBean(MetadataRepository.class)
    public MetadataRepository metadataRepository(){
        return new MetadataRepositoryInMemoryImpl();
    }

    //service
    @Bean
    public EntityService entityService(MetadataRepository metadataRepository
                                    , EntityServiceClient entityServiceClient
                                    , ContextService contextService){
        return new EntityServiceImpl(metadataRepository, entityServiceClient, contextService);
    }


    @Bean
    public EntityServiceEx entityServiceEx(MetadataRepository metadataRepository
                                         , EntityServiceClient entityServiceClient
                                         , ContextService contextService){
        return new EntityServiceExImpl(contextService, entityServiceClient);
    }

    @Bean
    public DefaultEntityServiceHandler defaultEntityServiceHandler(){
        return new DefaultEntityServiceHandler();
    }

    //controller
    @Bean
    public ModuleController moduleController(){
        return new ModuleController();
    }

    @Bean
    public EntityController entityController(){
        return new EntityController();
    }

    @Bean
    public DictController dictController(){
        return new DictController();
    }

    @Bean
    public UltFormSettingController ultFormSettingController(){
        return new UltFormSettingController();
    }

    @Bean
    public UltPageSettingController ultPageSettingController(){
        return new UltPageSettingController();
    }

    //REST client
    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory factory){
        return new RestTemplate(factory);
    }

    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory(){
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(5000);//单位为ms
        factory.setConnectTimeout(5000);//单位为ms
        return factory;
    }

    @Bean
    public MessageDispatcherInterceptor<?> codeExtendInterceptor(MetadataRepository metadataRepository, ContextService contextService){
        return new CodeExtendedInterceptor(metadataRepository, contextService);
    }

    @ConditionalOnBean(name = "searchCondition", value = { MatchRouter.class} )
    @Bean
    public MessageDispatcherInterceptor<?> DefaultSearchInterceptor(MatchRouter<String, ConditionQueryRequest> matchRouter){
        return new DefaultSearchInterceptor<>(matchRouter);
    }

    @Bean
    public EntityMetaHandler entityMetaHandler(){
        return new EntityMetaHandler();
    }

    @Bean
    public EntityMetaFieldDefaultHandler entityMetaFieldDefaultHandler() { return new EntityMetaFieldDefaultHandler(); }


}
