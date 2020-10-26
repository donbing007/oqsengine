package com.xforceplus.ultraman.oqsengine.sdk.listener;

import com.xforcecloud.noification.model.BaseResponse;
import com.xforcecloud.noification.model.MessageInfo;
import com.xforcecloud.noification.model.Scope;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.sdk.event.EntityErrorExported;
import com.xforceplus.ultraman.oqsengine.sdk.event.EntityExported;
import com.xforceplus.xplat.galaxy.framework.context.ContextKeys;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestTemplate;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * message can config template
 */
public class MessageCenterEntityExportEventListener implements ExportEventAwareListener {

    private Supplier<String> tokenSupplier;

    private String senderId;

    private String gatewayUrl;

    private String titleTemplate;
    private String contentTemplate;

    private final RestTemplate restTemplate;

    private String routePattern = "%s/api/%s/message/v1/messages?appId=%s";

    private Logger logger = LoggerFactory.getLogger(MessageCenterEntityExportEventListener.class);

    private String contextPath = "";

    private final String defaultContentStr = "#set( $currentFileName = $fileName.split('-')[0] + '-' + $ldt.now().format($dtf.ofPattern('YYYYMMddHHmmSS')) ) \n" +
            "<a href='$downloadUrl.split('\\?')[0]?filename=$currentFileName'>$currentFileName</a>";

    private boolean ignoreOnSync;

    /**
     * sendId is a appId for message
     * <p>
     * variable is download url and
     *
     * @param tokenSupplier
     * @param senderIdSupplier
     * @param gatewayUrl
     * @param restTemplate
     */
    public MessageCenterEntityExportEventListener(Supplier<String> tokenSupplier
            , Supplier<String> senderIdSupplier, Supplier<String> gatewayUrl
            , String contentTemplate, String titleTemplate, RestTemplate restTemplate
            , String contextPath, boolean ignoreOnSync) {
        //dynamic
        this.tokenSupplier = tokenSupplier;
        // only once
        this.senderId = senderIdSupplier.get();
        this.gatewayUrl = gatewayUrl.get();
        this.restTemplate = restTemplate;
        this.ignoreOnSync = ignoreOnSync;

        //MessageContent
        if (contentTemplate != null) {
            this.contentTemplate = contentTemplate;
        } else {
            this.contentTemplate = defaultContentStr;
        }

        if (titleTemplate != null) {
            this.titleTemplate = titleTemplate;
        } else {
            this.titleTemplate = "导出下载";
        }

        this.contextPath = contextPath;

        Velocity.init();

        /**
         *   context.put("ldt", LocalDateTime.class);
         *   context.put("dtf", DateTimeFormatter.class);
         */
        //Velocity.addProperty("ldt", LocalDateTime.class);
        //Velocity.addProperty("dtf", DateTimeFormatter.class);
    }

    @Override
    @Async
    @EventListener(EntityErrorExported.class)
    public void errorListener(EntityErrorExported entityExported) {
        Map<String, Object> context = entityExported.getContext();

        if (context != null) {
            Object tenantIdObj = context.get(ContextKeys.LongKeys.TENANT_ID.name());
            Object userId = context.get(ContextKeys.LongKeys.ID.name());
            if (tenantIdObj != null) {
                Long tenantId = (Long) tenantIdObj;
                MessageInfo messageInfo = new MessageInfo();

                String fileName = entityExported.getFileName();
                String reason = entityExported.getReason();

                messageInfo.setScope(Scope.SINGLE);
                messageInfo.setTitle("导出失败");
                messageInfo.setContent("导出失败：" + fileName + "， 原因:" + reason);
                messageInfo.setReceiverIds(Arrays.asList((Long) userId));
                messageInfo.setType(0);

                HttpHeaders headers = new HttpHeaders();
                MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
                headers.setContentType(type);
                headers.add("Accept", MediaType.APPLICATION_JSON.toString());
                headers.add("x-app-token", tokenSupplier.get());

                String finalAppId = Optional.ofNullable(entityExported.getAppId()).orElse(senderId);

                HttpEntity messageEntity = new HttpEntity<>(messageInfo, headers);
                String url = String.format(routePattern, gatewayUrl, tenantId, finalAppId);
                try {
                    ResponseEntity<BaseResponse> response = restTemplate.postForEntity(url, messageEntity, BaseResponse.class);
                    //TODO if check this response
                } catch (RuntimeException ex) {
                    logger.error("{}", ex);
                }
            }
        }
    }

    @Override
    @Async
    @EventListener(EntityExported.class)
    public void messageListener(EntityExported entityExported) {

        if ("sync".equalsIgnoreCase(entityExported.getExportType()) && ignoreOnSync) {
            //in sync
            return;
        }

        Map<String, Object> context = entityExported.getContext();

//        ST content = new ST(this.contentTemplate, '$', '$');
//        ST title = new ST(this.titleTemplate, '$', '$');
//        ST defaultContent = new ST(defaultContentStr, '$', '$');

        if (context != null) {
            Object tenantIdObj = context.get(ContextKeys.LongKeys.TENANT_ID.name());
            Object userId = context.get(ContextKeys.LongKeys.ID.name());
            if (tenantIdObj != null) {
                Long tenantId = (Long) tenantIdObj;
                MessageInfo messageInfo = new MessageInfo();

                String downloadUrl = entityExported.getDownloadUrl();

                String finalDownloadUrl = contextPath + downloadUrl;

                String fileName = entityExported.getFileName();

                messageInfo.setScope(Scope.SINGLE);
                messageInfo.setTitle(getRendered(this.titleTemplate, fileName, finalDownloadUrl, entityExported.getEntityClass(), () -> "导出成功"));
                messageInfo.setContent(getRendered(this.contentTemplate, fileName, finalDownloadUrl, entityExported.getEntityClass()
                        , () -> getRendered(defaultContentStr, finalDownloadUrl, fileName, entityExported.getEntityClass(), () -> "下载地址")));
                messageInfo.setReceiverIds(Arrays.asList((Long) userId));
                messageInfo.setType(0);

                HttpHeaders headers = new HttpHeaders();
                MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
                headers.setContentType(type);
                headers.add("Accept", MediaType.APPLICATION_JSON.toString());
                headers.add("x-app-token", tokenSupplier.get());

                String finalAppId = Optional.ofNullable(entityExported.getAppId()).orElse(senderId);

                HttpEntity messageEntity = new HttpEntity<>(messageInfo, headers);
                String url = String.format(routePattern, gatewayUrl, tenantId, finalAppId);
                try {
                    ResponseEntity<BaseResponse> response = restTemplate.postForEntity(url, messageEntity, BaseResponse.class);
                    //TODO if check this response
                } catch (RuntimeException ex) {
                    logger.error("{}", ex);
                }
            }
        }
    }


    private String getRendered(String template, String fileName, String downloadUrl, IEntityClass entityClass, Supplier<String> fallbackStr) {
        try {
//            st.add("downloadUrl", downloadUrl);
//            st.add("fileName", fileName);
//            st.add("now", LocalDateTime.now());
//            return st.render();

            VelocityContext context = new VelocityContext();
            context.put("fileName", fileName);
            context.put("downloadUrl", downloadUrl);
            context.put("entityCls", entityClass);
            context.put("ldt", LocalDateTime.class);
            context.put("dtf", DateTimeFormatter.class);
            StringWriter writer = new StringWriter();
            Velocity.evaluate(context, writer, "info", template);
            return writer.toString();

        } catch (Exception ex) {
            logger.error("{}", ex);
            return fallbackStr.get();
        }
    }
}
