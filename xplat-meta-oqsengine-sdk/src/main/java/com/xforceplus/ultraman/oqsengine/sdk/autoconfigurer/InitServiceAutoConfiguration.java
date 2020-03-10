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
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityService;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityServiceEx;
import com.xforceplus.ultraman.oqsengine.sdk.service.impl.EntityServiceExImpl;
import com.xforceplus.ultraman.oqsengine.sdk.service.impl.EntityServiceImpl;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.DictMapLocalStore;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.FormBoMapLocalStore;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.MetadataRepository;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.PageBoMapLocalStore;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.impl.MetadataRepositoryInMemoryImpl;
import com.xforceplus.xplat.galaxy.framework.context.ContextService;
import com.xforceplus.xplat.galaxy.grpc.spring.EnableGrpcServiceClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

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


//    @Bean
//    public EntityController entityController(){
//        return new EntityController();
//    }

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
}
