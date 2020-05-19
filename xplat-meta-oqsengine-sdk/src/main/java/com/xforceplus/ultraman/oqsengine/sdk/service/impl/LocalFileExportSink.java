package com.xforceplus.ultraman.oqsengine.sdk.service.impl;

import akka.stream.IOResult;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Sink;
import akka.util.ByteString;
import com.xforceplus.ultraman.oqsengine.sdk.service.ExportSink;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletionStage;

/**
 * localFileExport Sink
 */
public class LocalFileExportSink implements ExportSink {

    String contextPath = "download/file/%s";

    @Override
    public Sink<ByteString, CompletionStage<IOResult>> getSink(String token) {
        final Path file = Paths.get(token + ".csv");
        return FileIO.toPath(file);
    }

    @Override
    public String getDownloadUrl(String token) {
        return String.format(contextPath, token);
    }
}
