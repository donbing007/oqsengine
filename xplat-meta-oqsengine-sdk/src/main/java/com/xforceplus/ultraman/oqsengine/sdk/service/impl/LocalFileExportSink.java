package com.xforceplus.ultraman.oqsengine.sdk.service.impl;

import akka.stream.IOResult;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Sink;
import akka.util.ByteString;
import com.xforceplus.ultraman.oqsengine.sdk.service.ExportSink;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * localFileExport Sink
 */
public class LocalFileExportSink implements ExportSink {

    private String contextPath = "download/file/%s";

    private String root = "/";

    private Logger logger = LoggerFactory.getLogger(LocalFileExportSink.class);

    public LocalFileExportSink(String root){
        this.root = root;
    }

    @Override
    public Sink<ByteString, CompletionStage<Tuple2<IOResult, String>>> getSink(String token) {
        final Path file = Paths.get(root).resolve(token + ".csv");
        return FileIO.toPath(file).mapMaterializedValue(x -> x.thenApply(io -> Tuple.of(io, token)));
    }

    @Override
    public String getDownloadUrl(String... token) {
        return String.format(contextPath, token[0]);
    }

    @Override
    public InputStream getInputStream(String token) {
        try {
            return Files.newInputStream(Paths.get(root).resolve(token + ".csv"));
        } catch (IOException e) {
            logger.error("{}", e);
            return null;
        }
    }
}
