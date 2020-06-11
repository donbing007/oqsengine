package com.xforceplus.ultraman.oqsengine.sdk.autoconfigurer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.util.JsonFormat;
import com.xforceplus.ultraman.config.ConfigConverter;
import com.xforceplus.ultraman.config.ConfigurationEngine;
import com.xforceplus.ultraman.config.EventStratregy;
import com.xforceplus.ultraman.config.json.JsonConfigNode;
import com.xforceplus.ultraman.config.storage.ConfigurationStorage;
import com.xforceplus.ultraman.config.storage.impl.DefaultFileConfigurationStorage;
import com.xforceplus.ultraman.config.stratregy.DiscardStrategy;
import com.xforceplus.ultraman.config.stratregy.VersiondDiscardStrategy;
import com.xforceplus.ultraman.config.stratregy.impl.DefaultJsonEventStrategy;
import com.xforceplus.ultraman.metadata.grpc.ModuleUpResult;
import com.xforceplus.ultraman.oqsengine.pojo.dto.UltPage;
import com.xforceplus.ultraman.oqsengine.sdk.config.engine.VersionedJsonConfig;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class RuntimeConfigAutoConfiguration {

    @Bean
    public Kryo kryo(){
        Kryo kryo = new Kryo();
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        kryo.setRegistrationRequired(false);
        return kryo;
    }

    //TODO config
    @Bean
    public EventStratregy jsonJsonEventStrategy(){
        return new DefaultJsonEventStrategy();
    }

    //TODO config
    @Bean
    public DiscardStrategy discardStrategy(){
        return new VersiondDiscardStrategy();
    }

    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

    @ConditionalOnProperty(value = "xplat.oqsengine.sdk.config.file.enabled", matchIfMissing = true)
    @ConditionalOnBean(value = { EventStratregy.class, DiscardStrategy.class })
    @Bean
    public ConfigurationStorage fileStorage(@Value("${xplat.oqsengine.sdk.config.file.root:/}") String root
            , Kryo kryo
            , EventStratregy eventStratregy
            , DiscardStrategy discardStrategy
    ){
        ConfigurationStorage fileStorage = new DefaultFileConfigurationStorage(
                root
                , kryo
                , eventStratregy
                , discardStrategy);

        fileStorage.rebuild();
        return fileStorage;
    }

   // @ConditionalOnBean(value = { Kryo.class, ConfigurationStorage.class })
    @Bean("moduleConfigEngine")
    public ConfigurationEngine<ModuleUpResult, JsonConfigNode> engineForModule(ConfigurationStorage storage, ObjectMapper mapper){

        ConfigurationEngine<ModuleUpResult, JsonConfigNode> engine = new ConfigurationEngine<>();
        engine.setConfigurationStorage(storage);


        ConfigConverter<ModuleUpResult, JsonConfigNode> converter = customConfig -> {
            try {
                String json = JsonFormat.printer().print(customConfig);
                return new VersionedJsonConfig(customConfig.getVersion(), "BO-" + customConfig.getId(), mapper.readTree(json), null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        };

        engine.setConverter(converter);
        engine.rebuild();

        return engine;
    }

    @Bean("dictConfigEngine")
    public ConfigurationEngine<ModuleUpResult, JsonConfigNode> engineForDict(ConfigurationStorage storage, ObjectMapper mapper){

        ConfigurationEngine<ModuleUpResult, JsonConfigNode> engine = new ConfigurationEngine<>();
        engine.setConfigurationStorage(storage);


        ConfigConverter<ModuleUpResult, JsonConfigNode> converter = customConfig -> {
            try {
                String json = JsonFormat.printer().print(customConfig);
                return new VersionedJsonConfig(customConfig.getVersion(), "DICT-" + customConfig.getId(), mapper.readTree(json), null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        };

        engine.setConverter(converter);

        return engine;
    }

    @Bean("formConfigEngine")
    public ConfigurationEngine<ModuleUpResult, JsonConfigNode> engineForForm(ConfigurationStorage storage, ObjectMapper mapper){

        ConfigurationEngine<ModuleUpResult, JsonConfigNode> engine = new ConfigurationEngine<>();
        engine.setConfigurationStorage(storage);


        ConfigConverter<ModuleUpResult, JsonConfigNode> converter = customConfig -> {
            try {
                String json = JsonFormat.printer().print(customConfig);
                return new VersionedJsonConfig(customConfig.getVersion(), "FORM-" + customConfig.getId(), mapper.readTree(json), null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        };

        engine.setConverter(converter);

        return engine;
    }


    @ConditionalOnBean(value = { Kryo.class, ConfigurationStorage.class })
    @Bean("pageConfigEngine")
    public ConfigurationEngine<UltPage, JsonConfigNode> engineForPage(ConfigurationStorage storage, ObjectMapper mapper){

        ConfigurationEngine<UltPage, JsonConfigNode> engine = new ConfigurationEngine<>();
        engine.setConfigurationStorage(storage);


        ConfigConverter<UltPage, JsonConfigNode> converter = page -> {
            try {
                return new VersionedJsonConfig(page.getVersion(), "PAGE-" + page.getId(), mapper.valueToTree(page), null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        };

        engine.setConverter(converter);
        return engine;
    }
}
