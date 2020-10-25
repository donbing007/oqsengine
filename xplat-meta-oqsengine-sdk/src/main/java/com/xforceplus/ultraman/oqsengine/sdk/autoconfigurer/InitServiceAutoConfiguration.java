package com.xforceplus.ultraman.oqsengine.sdk.autoconfigurer;


import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import com.xforceplus.ultraman.metadata.grpc.CheckServiceClient;
import com.xforceplus.ultraman.oqsengine.sdk.EntityServiceClient;
import com.xforceplus.ultraman.oqsengine.sdk.autoconfigurer.configuration.GatewayUrlSupplier;
import com.xforceplus.ultraman.oqsengine.sdk.autoconfigurer.configuration.MessageAppIdSupplier;
import com.xforceplus.ultraman.oqsengine.sdk.autoconfigurer.configuration.MessageTokenSupplier;
import com.xforceplus.ultraman.oqsengine.sdk.config.AuthSearcherConfig;
import com.xforceplus.ultraman.oqsengine.sdk.config.init.*;
import com.xforceplus.ultraman.oqsengine.sdk.controller.DownloadController;
import com.xforceplus.ultraman.oqsengine.sdk.handler.DefaultEntityServiceHandler;
import com.xforceplus.ultraman.oqsengine.sdk.interceptor.*;
import com.xforceplus.ultraman.oqsengine.sdk.listener.ExportEventLoggerListener;
import com.xforceplus.ultraman.oqsengine.sdk.listener.MessageCenterEntityExportEventListener;
import com.xforceplus.ultraman.oqsengine.sdk.listener.ModuleEventListener;
import com.xforceplus.ultraman.oqsengine.sdk.service.*;
import com.xforceplus.ultraman.oqsengine.sdk.service.impl.*;
import com.xforceplus.ultraman.oqsengine.sdk.service.operation.*;
import com.xforceplus.ultraman.oqsengine.sdk.service.operation.validator.FieldValidator;
import com.xforceplus.ultraman.oqsengine.sdk.service.operation.validator.RegxValidator;
import com.xforceplus.ultraman.oqsengine.sdk.service.operation.validator.RequiredValidator;
import com.xforceplus.ultraman.oqsengine.sdk.service.operation.validator.TypeCheckValidator;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.DictMapLocalStore;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.FormBoMapLocalStore;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.MetadataRepository;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.PageBoMapLocalStore;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.impl.MetadataRepositoryInMemoryImpl;
import com.xforceplus.ultraman.oqsengine.sdk.transactional.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.sdk.transactional.TransactionManager;
import com.xforceplus.ultraman.oqsengine.sdk.util.flow.FlowRegistry;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionQueryRequest;
import com.xforceplus.xplat.galaxy.framework.context.ContextService;
import com.xforceplus.xplat.galaxy.framework.dispatcher.interceptor.MessageDispatcherInterceptor;
import com.xforceplus.xplat.galaxy.grpc.spring.EnableGrpcServiceClients;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

//import com.xforceplus.ultraman.oqsengine.sdk.staticmode.StaticServiceLoader;

/**
 * sdk auto-configuration
 */
@ConditionalOnProperty(value = "xplat.oqsengine.sdk.enabled", matchIfMissing = true)
@AutoConfigureOrder
@EnableGrpcServiceClients(basePackages = {"com.xforceplus.ultraman.metadata.grpc", "com.xforceplus.ultraman.oqsengine.sdk"})
public class InitServiceAutoConfiguration {


    @Autowired
    private AuthSearcherConfig config;

    @Bean(destroyMethod = "terminate")
    @ConditionalOnMissingBean(ActorSystem.class)
    public ActorSystem actorSystem() {
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

    @ConditionalOnProperty(value = "xplat.oqsengine.sdk.meta.enabled", matchIfMissing = true)
    @Bean
    public DictInitService dictInitService() {
        return new DictInitService();
    }

    @ConditionalOnProperty(value = "xplat.oqsengine.sdk.meta.enabled", matchIfMissing = true)
    @Bean
    public ModuleInitService moduleInitService(CheckServiceClient checkServiceClient
            , ActorMaterializer mat, AuthSearcherConfig config
            , ApplicationEventPublisher publisher) {
        return new ModuleInitService(checkServiceClient, mat, config, publisher);
    }

    @ConditionalOnProperty(value = "xplat.oqsengine.sdk.meta.enabled", matchIfMissing = true)
    @Bean
    public NodeReporterInitService nodeReporterInitService() {
        return new NodeReporterInitService();
    }

    @Bean
    public DefaultEntityServiceHandler entityServiceHandler() {
        return new DefaultEntityServiceHandler();
    }


    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @ConditionalOnMissingBean(MetadataRepository.class)
    @Bean
    public MetadataRepository metadataRepository(@Value("${xplat.oqsengine.sdk.max-version:3}") Integer versionSize, ApplicationEventPublisher publisher) {
        return new MetadataRepositoryInMemoryImpl(versionSize, publisher);
    }

    //service
    @Bean
    public EntityService entityService(MetadataRepository metadataRepository
            , EntityServiceClient entityServiceClient
            , ContextService contextService) {
        return new EntityServiceImpl(metadataRepository, entityServiceClient, contextService);
    }

    @Bean
    public PlainEntityService plainEntityService(EntityService entityService) {
        return new PlainEntityServiceImpl(entityService);
    }

    @Bean
    public EntityServiceEx entityServiceEx(MetadataRepository metadataRepository
            , EntityServiceClient entityServiceClient
            , ContextService contextService) {
        return new EntityServiceExImpl(contextService, entityServiceClient);
    }

    @Bean
    public DefaultEntityServiceHandler defaultEntityServiceHandler() {
        return new DefaultEntityServiceHandler();
    }

    //REST client
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
//        return new RestTemplate(factory);

        //先获取到converter列表
        List<HttpMessageConverter<?>> converters = builder.build().getMessageConverters();
        for (HttpMessageConverter<?> converter : converters) {
            //因为我们只想要jsonConverter支持对text/html的解析
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                try {
                    //先将原先支持的MediaType列表拷出
                    List<MediaType> mediaTypeList = new ArrayList<>(converter.getSupportedMediaTypes());
                    //加入对JSON的支持
                    mediaTypeList.add(MediaType.APPLICATION_JSON);
//                    mediaTypeList.add(MediaType.TEXT_HTML);
                    //将已经加入了text/html的MediaType支持列表设置为其支持的媒体类型列表
                    ((MappingJackson2HttpMessageConverter) converter).setSupportedMediaTypes(mediaTypeList);
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            }
        }
        return builder.build();
    }

    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        //单位为ms
        factory.setReadTimeout(5000);
        //单位为ms
        factory.setConnectTimeout(5000);
        return factory;
    }

    @Bean
    public MessageDispatcherInterceptor<?> codeExtendInterceptor(MetadataRepository metadataRepository) {
        return new CodeExtendedInterceptor<>(metadataRepository);
    }

    @Bean
    public MessageDispatcherInterceptor<?> contextAwareInterceptor(ContextService contextService) {
        return new ContextInterceptor<>(contextService);
    }


    @ConditionalOnBean(name = "searchCondition", value = {MatchRouter.class})
    @Bean
    public MessageDispatcherInterceptor<?> DefaultSearchInterceptor(MatchRouter<String, ConditionQueryRequest> matchRouter) {
        return new DefaultSearchInterceptor<>(matchRouter);
    }

    //----------------------------init for operation and validator

    @Bean
    public FieldValidator regex() {
        return new RegxValidator();
    }

    @Bean
    public FieldValidator required() {
        return new RequiredValidator();
    }

    @Bean
    public FieldValidator typedCheck() {
        return new TypeCheckValidator();
    }

    @Bean
    public FieldOperationHandler defaultField() {
        return new DefaultFieldOperationHandler();
    }


    @Bean
    public FieldOperationHandler defaultValueField() {
        return new DefaultFieldValueOperationHandler();
    }

    @Bean
    public FieldOperationHandler defaultSystemField(ContextService contextService, @Value("${xplat.oqsengine.sdk.override:false}") Boolean isOverride) {
        return new FixedDefaultSystemOperationHandler(contextService, isOverride);
    }

    @Bean
    public FieldOperationHandler simpleExpressionFieldOperationHandler(ContextService contextService) {
        return new SimpleExpressionFieldOperationHandler(contextService);
    }

    @Bean
    public HandleValueService defaultHandleValueService(List<FieldOperationHandler> fieldOperationHandlers, List<FieldValidator<Object>> fieldValidators) {
        return new DefaultHandleValueService(fieldOperationHandlers, fieldValidators);
    }

    @Bean
    public HandleQueryValueService defaultHandleQueryValueService(List<QuerySideFieldOperationHandler> querySideFieldOperationHandler) {
        return new DefaultHandleQueryValueService(querySideFieldOperationHandler);
    }

    @Bean
    public HandleResultValueService defaultHandleResultValueService(List<RecordOperationHandler> handlers
            , List<ResultSideOperationHandler> resultSideOperationHandlers) {
        return new DefaultHandleResultValueService(handlers, resultSideOperationHandlers);
    }

    @Bean
    public ResultSideOperationHandler booleanTyped() {
        return new BooleanFieldOperationHandler();
    }

    @Bean
    public RecordOperationHandler idAppend() {
        return new IdAppenderRecordOperationHandler();
    }

    @Bean
    public UltFormInitService ultFormInitService() {
        return new UltFormInitService();
    }

    @Bean
    public UltPageInitService ultPageInitService() {
        return new UltPageInitService();
    }


    @Bean
    public ModuleEventListener listener() {
        return new ModuleEventListener();
    }

    @Bean
    public ExportSource exportSource(EntityService entityService
            , @Value("${xplat.oqsengine.sdk.export.step:1000}") int step
            , ContextService contextService
    ) {
        return new SequenceExportSource(entityService, step, contextService);
    }

    @ConditionalOnMissingBean(ExportSink.class)
    @ConditionalOnProperty(value = "xplat.oqsengine.sdk.export.local-sink", matchIfMissing = true)
    @Bean
    public ExportSink localFileSink(@Value("${xplat.oqsengine.sdk.export.local.root:/}") String root) {
        return new LocalFileExportSink(root);
    }

    /**
     * xplat:
     * oqsengine:
     * sdk:
     * override: true
     * enabled: true
     * verisoned: false
     *
     * @return
     */
    @ConditionalOnProperty(value = "xplat.oqsengine.sdk.stopverisoned", matchIfMissing = true)
    @Bean
    public MessageDispatcherInterceptor<?> clearVersion() {
        return new VersionInterceptor<>();
    }


    @ConditionalOnBean(value = {MessageAppIdSupplier.class, MessageTokenSupplier.class, GatewayUrlSupplier.class, RestTemplate.class})
    @ConditionalOnProperty(value = "xplat.oqsengine.sdk.export.message.enabled", matchIfMissing = true)
    @Bean
    public MessageCenterEntityExportEventListener exportEventListener(MessageTokenSupplier tokenSupplier
            , MessageAppIdSupplier appIdSupplier, GatewayUrlSupplier gatewayUrlSupplier
            , @Value("${xplat.oqsengine.sdk.export.message.template.content:#{null}}") String content
            , @Value("${xplat.oqsengine.sdk.export.message.template.title:#{null}}") String title
            , @Value("${xplat.oqsengine.sdk.export.message.context-path:''}") String contextPath
            , @Value("${xplat.oqsengine.sdk.export.message.ignore-on-sync:true}") boolean ignoreOnSync
            , RestTemplate restTemplate) {
        return new MessageCenterEntityExportEventListener(tokenSupplier::getToken
                , appIdSupplier::getStorageAppId, gatewayUrlSupplier::getGatewayUrl
                , content, title, restTemplate, contextPath, ignoreOnSync);
    }

    @ConditionalOnBean(ExportSink.class)
    @ConditionalOnProperty(value = "xplat.oqsengine.sdk.export.local.download", matchIfMissing = true)
    @Bean
    public DownloadController downloadController(ExportSink exportSink) {
        return new DownloadController(exportSink);
    }

    @ConditionalOnProperty(value = "xplat.oqsengine.sdk.export.log", matchIfMissing = true)
    @Bean
    public ExportEventLoggerListener loggerListener() {
        return new ExportEventLoggerListener();
    }

    @Bean
    public RetryRegistry retryRegistry(){
        return RetryRegistry.ofDefaults();
    }

    @Bean
    public RateLimiterRegistry rateLimiterRegistry(){
        return RateLimiterRegistry.ofDefaults();
    }

    @Bean
    public FlowRegistry flowRegistry(ActorMaterializer mat){
        return new FlowRegistry(mat);
    }

    @Bean
    public RateLimiter rateLimiter(RateLimiterRegistry registry){
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMillis(10))
                .limitForPeriod(10)
                .timeoutDuration(Duration.ofMillis(25))
                .build();

        RateLimiter rateLimiter = registry.rateLimiter("retry", config);
        return rateLimiter;
    }

    @Bean
    public EntityExportService entityExportService(){
        return new EntityExportServiceImpl();
    }

    @Bean
    public TransactionManager transactionManager(){
        return new DefaultTransactionManager();
    }
}
