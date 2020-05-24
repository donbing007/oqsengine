package com.xforceplus.ultraman.oqsengine.sdk.listener;

import com.xforcecloud.noification.model.BaseResponse;
import com.xforcecloud.noification.model.MessageInfo;
import com.xforcecloud.noification.model.Scope;
import com.xforceplus.ultraman.oqsengine.sdk.event.EntityExported;
import com.xforceplus.xplat.galaxy.framework.context.ContextKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestTemplate;
import org.stringtemplate.v4.ST;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

/**
 * message can config template
 */
public class MessageCenterEntityExportEventListener {

    private Supplier<String> tokenSupplier;

    private String senderId;

    private String gatewayUrl;

    private String titleTemplate;
    private String contentTemplate;

    private final RestTemplate restTemplate;

    private String routePattern = "%s/api/%s/message/v1/messages?appId=%s";

    private Logger logger = LoggerFactory.getLogger(MessageCenterEntityExportEventListener.class);

    private ST content;

    private ST title;

    private final String defaultContentStr = "<a href='$downloadUrl$'>$fileName$</a>";

    private ST defaultContent = new ST(defaultContentStr, '$', '$');

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
            , Supplier<String> senderIdSupplier, Supplier<String> gatewayUrl, String contentTemplate, String titleTemplate, RestTemplate restTemplate) {
        //dynamic
        this.tokenSupplier = tokenSupplier;
        // only once
        this.senderId = senderIdSupplier.get();
        this.gatewayUrl = gatewayUrl.get();
        this.restTemplate = restTemplate;

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

        content = new ST(this.contentTemplate, '$', '$');
        title = new ST(this.titleTemplate, '$', '$');
    }

    @Async
    @EventListener(EntityExported.class)
    public void sendToMessage(EntityExported entityExported) {
        Map<String, Object> context = entityExported.getContext();

        if (context != null) {
            Object tenantIdObj = context.get(ContextKeys.LongKeys.TENANT_ID.name());
            Object userId = context.get(ContextKeys.LongKeys.ACCOUNT_ID.name());
            if (tenantIdObj != null) {
                Long tenantId = (Long) tenantIdObj;
                MessageInfo messageInfo = new MessageInfo();


                String downloadUrl = entityExported.getDownloadUrl();
                String fileName = entityExported.getFileName();

                messageInfo.setScope(Scope.SINGLE);
                messageInfo.setTitle(getRendered(title, fileName, downloadUrl, () -> "导出成功"));
                messageInfo.setContent(getRendered(content, fileName, downloadUrl, () -> getRendered(defaultContent, downloadUrl, fileName, () -> "下载地址")));
                messageInfo.setReceiverIds(Arrays.asList((Long) userId));
                messageInfo.setType(0);

                HttpHeaders headers = new HttpHeaders();
                MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
                headers.setContentType(type);
                headers.add("Accept", MediaType.APPLICATION_JSON.toString());
                headers.add("x-app-token", tokenSupplier.get());

                HttpEntity messageEntity = new HttpEntity<>(messageInfo, headers);
                String url = String.format(routePattern, gatewayUrl, tenantId, senderId);
                try {
                    ResponseEntity<BaseResponse> response = restTemplate.postForEntity(url, messageEntity, BaseResponse.class);
                    //TODO if check this response
                } catch (RuntimeException ex) {
                    logger.error("{}", ex);
                }
            }
        }
    }


    private String getRendered(ST st, String fileName, String downloadUrl, Supplier<String> fallbackStr) {
        try {
            st.add("downloadUrl", downloadUrl);
            st.add("fileName", fileName);
            return st.render();
        } catch (Exception ex) {
            logger.error("{}", ex);
            return fallbackStr.get();
        }
    }
}
