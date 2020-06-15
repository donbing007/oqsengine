package com.xforceplus.ultraman.oqsengine.sdk.autoconfigurer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.util.JsonFormat;
import com.xforceplus.ultraman.config.ConfigConverter;
import com.xforceplus.ultraman.config.ConfigurationEngine;
import com.xforceplus.ultraman.config.EventStratregy;
import com.xforceplus.ultraman.config.event.ChangeList;
import com.xforceplus.ultraman.config.json.JsonConfigNode;
import com.xforceplus.ultraman.config.storage.ConfigurationStorage;
import com.xforceplus.ultraman.config.storage.impl.DefaultFileConfigurationStorage;
import com.xforceplus.ultraman.config.stratregy.DiscardStrategy;
import com.xforceplus.ultraman.config.stratregy.VersiondDiscardStrategy;
import com.xforceplus.ultraman.config.stratregy.impl.DefaultJsonEventStrategy;
import com.xforceplus.ultraman.metadata.grpc.DictUpResult;
import com.xforceplus.ultraman.metadata.grpc.ModuleUpResult;
import com.xforceplus.ultraman.oqsengine.pojo.dto.UltForm;
import com.xforceplus.ultraman.oqsengine.pojo.dto.UltPage;
import com.xforceplus.ultraman.oqsengine.sdk.config.AuthSearcherConfig;
import com.xforceplus.ultraman.oqsengine.sdk.config.engine.VersionedJsonConfig;
import com.xforceplus.ultraman.oqsengine.sdk.config.init.ConfigType;
import com.xforceplus.ultraman.oqsengine.sdk.event.config.ConfigChangeEvent;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * runtime configuration
 */
@Configuration
public class RuntimeConfigAutoConfiguration {

    private Logger logger = LoggerFactory.getLogger(RuntimeConfigAutoConfiguration.class);

    @Bean
    public Kryo kryo() {
        Kryo kryo = new Kryo();
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        kryo.setRegistrationRequired(false);
        return kryo;
    }

    //TODO config
    @Bean
    public EventStratregy jsonJsonEventStrategy() {
        return new DefaultJsonEventStrategy();
    }

    //TODO config
    @Bean
    public DiscardStrategy discardStrategy() {
        return new VersiondDiscardStrategy();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @ConditionalOnProperty(value = "xplat.oqsengine.sdk.config.file.enabled", matchIfMissing = true)
    @ConditionalOnBean(value = {EventStratregy.class, DiscardStrategy.class})
    @Bean
    public ConfigurationStorage fileStorage(@Value("${xplat.oqsengine.sdk.config.file.root:/}") String root
            , Kryo kryo
            , EventStratregy eventStratregy
            , DiscardStrategy discardStrategy
    ) {
        ConfigurationStorage fileStorage = new DefaultFileConfigurationStorage(
                root
                , kryo
                , eventStratregy
                , discardStrategy);

        return fileStorage;
    }

    // @ConditionalOnBean(value = { Kryo.class, ConfigurationStorage.class })
    @Bean("moduleConfigEngine")
    public ConfigurationEngine<ModuleUpResult, JsonConfigNode> engineForModule(ConfigurationStorage storage, ObjectMapper mapper) {

        ConfigurationEngine<ModuleUpResult, JsonConfigNode> engine = new ConfigurationEngine<>();
        engine.setConfigurationStorage(storage);


        ConfigConverter<ModuleUpResult, JsonConfigNode> converter = customConfig -> {
            try {
                String json = JsonFormat.printer().print(customConfig);
                return new VersionedJsonConfig(customConfig.getVersion(), ConfigType.BO.name(), "" + customConfig.getId(), mapper.readTree(json), null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        };

        engine.setConverter(converter);
        return engine;
    }

    @Bean("dictConfigEngine")
    public ConfigurationEngine<DictUpResult, JsonConfigNode> engineForDict(ConfigurationStorage storage, ObjectMapper mapper, AuthSearcherConfig config) {

        ConfigurationEngine<DictUpResult, JsonConfigNode> engine = new ConfigurationEngine<>();
        engine.setConfigurationStorage(storage);


        ConfigConverter<DictUpResult, JsonConfigNode> converter = customConfig -> {
            try {
                String json = JsonFormat.printer().print(customConfig);
                if (!customConfig.getDictsList().isEmpty()) {
                    return new VersionedJsonConfig(customConfig.getDicts(0).getVersion(), ConfigType.DICT.name(), config.getAppId(), mapper.readTree(json), null);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        };

        engine.setConverter(converter);

        return engine;
    }

    @Bean("formConfigEngine")
    public ConfigurationEngine<UltForm, JsonConfigNode> engineForForm(ConfigurationStorage storage, ObjectMapper mapper) {

        ConfigurationEngine<UltForm, JsonConfigNode> engine = new ConfigurationEngine<>();
        engine.setConfigurationStorage(storage);

        ConfigConverter<UltForm, JsonConfigNode> converter = form -> {
            try {

                return new VersionedJsonConfig(form.getVersion(), ConfigType.FORM.name(), "" + form.getId(), mapper.valueToTree(form), null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        };

        engine.setConverter(converter);

        return engine;
    }


    @ConditionalOnBean(value = {Kryo.class, ConfigurationStorage.class})
    @Bean("pageConfigEngine")
    public ConfigurationEngine<UltPage, JsonConfigNode> engineForPage(ConfigurationStorage storage, ObjectMapper mapper) {

        ConfigurationEngine<UltPage, JsonConfigNode> engine = new ConfigurationEngine<>();
        engine.setConfigurationStorage(storage);


        ConfigConverter<UltPage, JsonConfigNode> converter = page -> {
            try {
                return new VersionedJsonConfig(page.getVersion()
                        , ConfigType.PAGE.name(), "" + page.getId(), mapper.valueToTree(page), null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        };

        engine.setConverter(converter);
        return engine;
    }

    @Bean
    public Object configurationRebuild(ConfigurationStorage configurationStorage
            , ConfigurationEngine<DictUpResult, JsonConfigNode> dictConfigEngine
            , ConfigurationEngine<UltPage, JsonConfigNode> pageConfigEngine
            , ConfigurationEngine<UltForm, JsonConfigNode> formConfigEngine
            , ConfigurationEngine<ModuleUpResult, JsonConfigNode> moduleConfigEngine
            , ApplicationEventPublisher publisher) {
        return new SmartInitializingSingleton() {

            @Override
            public void afterSingletonsInstantiated() {
                List<ChangeList> changeList = configurationStorage
                        .rebuild();

                logger.info("Rebuilding-------------------");

                changeList
                        .stream()
                        .forEach(x ->
                                publisher.publishEvent(new ConfigChangeEvent(x.getType(), x)));

                Optional.ofNullable(dictConfigEngine.getObservable()).ifPresent(ob -> ob.subscribe(x -> {

                    logger.info("Get New Dict-------------------");

                    publisher.publishEvent(new ConfigChangeEvent(x.getType(), x));
                }));

                Optional.ofNullable(moduleConfigEngine.getObservable()).ifPresent(ob -> ob.subscribe(x -> {
                    logger.info("Get New Module-------------------");
                    publisher.publishEvent(new ConfigChangeEvent(x.getType(), x));
                }));

                Optional.ofNullable(pageConfigEngine.getObservable()).ifPresent(ob -> ob.subscribe(x -> {

                    logger.info("Get New Page-------------------");

                    publisher.publishEvent(new ConfigChangeEvent(x.getType(), x));
                }));

                Optional.ofNullable(formConfigEngine.getObservable()).ifPresent(ob -> ob.subscribe(x -> {
                    logger.info("Get New Form-------------------");
                    publisher.publishEvent(new ConfigChangeEvent(x.getType(), x));
                }));
            }
        };
    }
}
