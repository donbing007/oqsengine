package com.xforceplus.ultraman.oqsengine.sdk.service;

import akka.stream.IOResult;
import akka.stream.javadsl.Sink;
import akka.util.ByteString;
import io.vavr.Tuple2;

import java.util.concurrent.CompletionStage;

/**
 * ExportSink Sinker
 */
public interface ExportSink {

    Sink<ByteString,  CompletionStage<Tuple2<IOResult, String>>> getSink(String token);

    String getDownloadUrl(String... token);

}
