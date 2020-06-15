package com.xforceplus.ultraman.oqsengine.sdk.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.xforceplus.ultraman.config.ConfigNode;
import com.xforceplus.ultraman.metadata.grpc.DictUpResult;
import com.xforceplus.ultraman.metadata.grpc.ModuleUpResult;
import com.xforceplus.ultraman.oqsengine.pojo.dto.UltForm;
import com.xforceplus.ultraman.oqsengine.pojo.dto.UltPage;
import com.xforceplus.ultraman.oqsengine.sdk.config.AuthSearcherConfig;
import com.xforceplus.ultraman.oqsengine.sdk.event.config.ConfigChangeEvent;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.DictMapLocalStore;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.FormBoMapLocalStore;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.MetadataRepository;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.PageBoMapLocalStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ConfigListener {

    @Autowired
    private PageBoMapLocalStore pageBoMapLocalStore;

    @Autowired
    private DictMapLocalStore dictMapLocalStore;

    @Autowired
    private FormBoMapLocalStore formBoMapLocalStore;

    @Autowired
    private MetadataRepository repository;

    @Autowired
    private AuthSearcherConfig config;

    @Autowired
    private ObjectMapper mapper;

    private Logger logger = LoggerFactory.getLogger(ConfigListener.class);

    @EventListener(condition = "#event.type.equals('PAGE')")
    public void pageChangeListener(ConfigChangeEvent event) {
        logger.info("UPDATE PAGE");
        ConfigNode confignode = (ConfigNode) event.getChangeList().getCurrent();
        JsonNode jsonNode = (JsonNode) confignode.getOrigin();

        try {
            UltPage ultPage = mapper.treeToValue(jsonNode, UltPage.class);
            pageBoMapLocalStore.save(ultPage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @EventListener(condition = "#event.type.equals('BO')")
    public void boChangeListener(ConfigChangeEvent event) {
        logger.info("UPDATE BO");
        ConfigNode confignode = (ConfigNode) event.getChangeList().getCurrent();
        JsonNode jsonNode = (JsonNode) confignode.getOrigin();
        ModuleUpResult.Builder moduleBuilder = ModuleUpResult.newBuilder();
        try {
            JsonFormat.parser().merge(jsonNode.toString(), moduleBuilder);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        repository.save(moduleBuilder.build(), config.getTenant(), config.getAppId());
    }

    @EventListener(condition = "#event.type.equals('DICT')")
    public void dictChangeListener(ConfigChangeEvent event) {
        logger.info("UPDATE DICT");

        ConfigNode confignode = (ConfigNode) event.getChangeList().getCurrent();
        JsonNode jsonNode = (JsonNode) confignode.getOrigin();
        DictUpResult.Builder dictBuilder = DictUpResult.newBuilder();
        try {
            JsonFormat.parser().merge(jsonNode.toString(), dictBuilder);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        DictUpResult result = dictBuilder.build();
        dictMapLocalStore.save(result, config.getAppId());

        if (logger.isInfoEnabled()) {
            result.getDictsList().forEach(dict -> {
                logger.info("Dict {}:{}:{} saved", dict.getCode(), dict.getPublishDictId(), dict.getId());
            });
        }
    }

    @EventListener(condition = "#event.type.equals('FORM')")
    public void formChangeListener(ConfigChangeEvent event) {
        logger.info("UPDATE FORM");

        ConfigNode confignode = (ConfigNode) event.getChangeList().getCurrent();
        JsonNode jsonNode = (JsonNode) confignode.getOrigin();

        try {
            UltForm ultForm = mapper.treeToValue(jsonNode, UltForm.class);
            formBoMapLocalStore.save(ultForm);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
