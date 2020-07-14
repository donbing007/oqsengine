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
import java.util.concurrent.CompletionStage;

/**
 * localFileExport Sink
 */
public class LocalFileExportSink implements ExportSink {


    private String contextPath = "download/file/%s";

    private String contextPathWithFileName = "download/file/%s?filename=%s";

    private String root = "/";

    private Logger logger = LoggerFactory.getLogger(LocalFileExportSink.class);

    public LocalFileExportSink(String root) {
        this.root = root;
    }

    @Override
    public Sink<ByteString, CompletionStage<Tuple2<IOResult, String[]>>> getSink(String... token) {

        if (token.length > 0) {
            //mark the first token is real filename
            final Path file = Paths.get(root).resolve(token[0] + ".csv");
            return FileIO.toPath(file).mapMaterializedValue(x -> x.thenApply(io -> Tuple.of(io, token)));
        } else {
            //TODO
            logger.error("Error Input Token {}", token);
            return null;
        }
    }

    @Override
    public String getDownloadUrl(String... token) {

        if (token.length > 1) {
            return String.format(contextPathWithFileName, token[0], token[1]);
        } else if (token.length > 0) {
            return String.format(contextPath, token[0]);
        } else {
            logger.error("Error Input Token {}", token);
            return "download/file";
        }
    }

    @Override
    public InputStream getInputStream(String... token) {
        try {
            return Files.newInputStream(Paths.get(root).resolve(token[0] + ".csv"));
        } catch (IOException e) {
            logger.error("{}", e);
            return null;
        }
    }
}
