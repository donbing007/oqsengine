package com.xforceplus.ultraman.oqsengine.sdk.service.impl;

import akka.stream.IOResult;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Sink;
import akka.util.ByteString;
import com.xforceplus.ultraman.oqsengine.sdk.service.ExportSink;
import io.vavr.Tuple;
import io.vavr.Tuple2;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletionStage;

/**
 * localFileExport Sink
 */
public class LocalFileExportSink implements ExportSink {

    String contextPath = "download/file/%s";

    @Override
    public Sink<ByteString, CompletionStage<Tuple2<IOResult, String>>> getSink(String token) {
        final Path file = Paths.get(token + ".csv");
        return FileIO.toPath(file).mapMaterializedValue(x -> x.thenApply(io -> Tuple.of(io, token)));
    }

    @Override
    public String getDownloadUrl(String... token) {
        return String.format(contextPath, token[0]);
    }
}
