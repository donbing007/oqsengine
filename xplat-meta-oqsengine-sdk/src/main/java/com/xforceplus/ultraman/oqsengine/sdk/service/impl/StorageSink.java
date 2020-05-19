package com.xforceplus.ultraman.oqsengine.sdk.service.impl;

import akka.Done;
import akka.stream.IOResult;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.StreamConverters;
import akka.util.ByteString;
import com.sun.xml.internal.ws.util.CompletedFuture;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class StorageSink implements ExportSink {

    private StorageFactory storageFactory;

    private ContextService contextService;

    private String appId;

    private String contextPath = "download/storage/%s";

    public StorageSink(StorageFactory storageFactory, ContextService contextService, String appId){
        this.storageFactory = storageFactory;
        this.contextService = contextService;
        this.appId = appId;
    }

    @Override
    public Sink<ByteString, CompletionStage<Tuple2<IOResult, String>>> getSink(String token) {


//        Long fileId = storageFactory.uploadFile(UploadFileRequest uploadFileRequest)
        return StreamConverters.asInputStream().mapMaterializedValue(x -> {

            Long fileId = upload(token,x);
            IOResult ioResult = new IOResult(0, Try.apply(Done::getInstance));
            return CompletableFuture.completedFuture(Tuple.of(ioResult, fileId.toString()));
        });
    }

    @Override
    public String getDownloadUrl(String... token) {
        return String.format(contextPath, token[0]);
    }

    private Long upload(String name, InputStream inputStream){

        UploadFileRequest uploadFileRequest = new UploadFileRequest();
        uploadFileRequest.setAppId(appId);
        uploadFileRequest.setExpires(1);
        uploadFileRequest.setInputStream(inputStream);
        uploadFileRequest.setFileName(name);
        uploadFileRequest.setPolicy(Policy.PUBLIC_POLICY);
        uploadFileRequest.setTenantId(contextService.get(ContextKeys.LongKeys.TENANT_ID));
        uploadFileRequest.setUserId(contextService.get(ContextKeys.LongKeys.ACCOUNT_ID));
        uploadFileRequest.setOverwrite(true);
        uploadFileRequest.setFilePath("/export/");
        return storageFactory.uploadByInputStream(uploadFileRequest);
    }
}
