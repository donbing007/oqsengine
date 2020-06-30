package com.xforceplus.ultraman.oqsengine.sdk.service.impl;

import akka.Done;
import akka.stream.IOResult;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.StreamConverters;
import akka.util.ByteString;
import com.xforceplus.tower.file.client.model.Policy;
import com.xforceplus.tower.storage.StorageFactory;
import com.xforceplus.tower.storage.model.UploadFileRequest;
import com.xforceplus.ultraman.oqsengine.sdk.service.ExportSink;
import com.xforceplus.xplat.galaxy.framework.context.ContextKeys;
import com.xforceplus.xplat.galaxy.framework.context.ContextService;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import scala.util.Try;

import java.io.InputStream;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * storageSink
 */
public class StorageSink implements ExportSink {

    private StorageFactory storageFactory;

    private ContextService contextService;

    private String appId;

    private String contextPath = "download/file/%s";

    public StorageSink(StorageFactory storageFactory, ContextService contextService, String appId) {
        this.storageFactory = storageFactory;
        this.contextService = contextService;
        this.appId = appId;
    }

    @Override
    public Sink<ByteString, CompletionStage<Tuple2<IOResult, String>>> getSink(String token) {

        Long telnetID = contextService.get(ContextKeys.LongKeys.TENANT_ID);
        Long userID = contextService.get(ContextKeys.LongKeys.ACCOUNT_ID);

        return StreamConverters.asInputStream(Duration.ofSeconds(50)).mapMaterializedValue(x -> {
            //should always in a async context or will block the queue
            //TODO contextService is not ok when using Async Thread
            return CompletableFuture.supplyAsync(() -> {
                Long fileId = upload(token, x, telnetID, userID);
                IOResult ioResult = new IOResult(0, Try.apply(Done::getInstance));
                return Tuple.of(ioResult, fileId.toString());
            });
        });
    }

    @Override
    public String getDownloadUrl(String... token) {
        return String.format(contextPath, token[0]);
    }

    @Override
    public InputStream getInputStream(String token) {

        Long tenantId = contextService.get(ContextKeys.LongKeys.TENANT_ID);
        Long userId = contextService.get(ContextKeys.LongKeys.ACCOUNT_ID);
        return storageFactory.downloadInputStream(userId, tenantId, Long.parseLong(token), null);
    }

    private Long upload(String name, InputStream inputStream, Long telnetID, Long userID) {

        UploadFileRequest uploadFileRequest = new UploadFileRequest();
        uploadFileRequest.setAppId(appId);
        uploadFileRequest.setExpires(1);
        uploadFileRequest.setInputStream(inputStream);
        uploadFileRequest.setFileName(name);
        uploadFileRequest.setPolicy(Policy.PUBLIC_POLICY);
        uploadFileRequest.setTenantId(telnetID);
        uploadFileRequest.setUserId(userID);
        uploadFileRequest.setOverwrite(true);
        uploadFileRequest.setFilePath("/export/");
        return storageFactory.uploadByInputStream(uploadFileRequest);
    }
}