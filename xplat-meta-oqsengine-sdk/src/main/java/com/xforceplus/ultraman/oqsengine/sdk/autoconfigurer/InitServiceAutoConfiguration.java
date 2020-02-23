package com.xforceplus.ultraman.oqsengine.sdk.autoconfigurer;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import com.xforceplus.ultraman.oqsengine.sdk.EntityServiceClient;
import com.xforceplus.ultraman.oqsengine.sdk.config.AuthSearcherConfig;
import com.xforceplus.ultraman.oqsengine.sdk.config.init.DictInitService;
import com.xforceplus.ultraman.oqsengine.sdk.config.init.ModuleInitService;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.DictMapLocalStore;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.FormBoMapLocalStore;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.PageBoMapLocalStore;
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


}
