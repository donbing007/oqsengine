package com.xforceplus.ultraman.oqsengine.sdk.service.export.impl;

import akka.NotUsed;
import akka.stream.ActorMaterializer;
import akka.stream.IOResult;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.reader.record.Record;
import com.xforceplus.ultraman.oqsengine.sdk.event.EntityErrorExported;
import com.xforceplus.ultraman.oqsengine.sdk.event.EntityExported;
import com.xforceplus.ultraman.oqsengine.sdk.service.*;
import com.xforceplus.ultraman.oqsengine.sdk.service.export.*;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionQueryRequest;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.DictItem;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.NameMapping;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * entity export Service
 */
public class EntityExportServiceImpl implements EntityExportService {

    @Autowired
    private ActorMaterializer materializer;

    @Autowired
    private ExportSink exportSink;

    @Autowired
    private ExportRecordStringFlow toStringFlow;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private ExportSource exportService;

    @Autowired
    private ExportStringTransformer stringTransformer;

    private Logger logger = LoggerFactory.getLogger(EntityExportService.class);

    /**
     * stream assemble
     * @param entityClass
     * @param query
     * @param token       token is a path name
     * @param fileName    filename is filename
     * @return
     */
    @Override
    public CompletableFuture<Either<String, String>> export(IEntityClass entityClass, ConditionQueryRequest query, String token
            , String fileName
            , Map<String, Object> context, String exportType, String appId) {

        /**
         * got source
         */
        Source<Record, NotUsed> source = exportService.source(entityClass, query);

        /**
         * got sink
         */
        Sink<ByteString, CompletionStage<Tuple2<IOResult, String[]>>> fileSink = exportSink.getSink(token, fileName);

        /**
         * name mapping to get page column name
         */
        List<NameMapping> nameMapping = query.getMapping();

        /**
         * get order columns
         */
        List<String> columns = query.getStringKeysOrdered();

        /**
         * visable columns
         */
        Set<String> filterColumns = Optional.ofNullable(columns)
                .<Set<String>>map(HashSet::new).orElseGet(Collections::emptySet);

        /**
         * check if is first line
         */
        AtomicBoolean isFirstLine = new AtomicBoolean(true);

        /**
         * find all flow (this only support flow)
         */
        Flow<Record, String, NotUsed> flow = toStringFlow.getFlow(entityClass, isFirstLine, nameMapping, columns, filterColumns, stringTransformer, context);

        try {
            byte[] bom = new byte[]{(byte) 0xef, (byte) 0xbb, (byte) 0xbf};
            Source<ByteString, NotUsed> bomSource = Source.single(ByteString.fromArray(bom));
            CompletableFuture<Either<String, String>> syncCompleteResult = bomSource.concat(source
                    .via(flow)
                    .map(x -> ByteString.fromString(x, StandardCharsets.UTF_8)))
                    .runWith(fileSink, materializer)
                    .toCompletableFuture().thenApply(x -> {
                        String downloadUrl = exportSink.getDownloadUrl(x._2());
                        publisher.publishEvent(new EntityExported(context, downloadUrl, token, exportType, appId));
                        return Either.<String, String>right(downloadUrl);
                    }).exceptionally(th -> {
                        publisher.publishEvent(new EntityErrorExported(context, token, th.getMessage(), appId));
                        return Either.left(th.getMessage());
                    });

            if ("sync".equalsIgnoreCase(exportType)) {
                return syncCompleteResult;
            } else {
                return CompletableFuture.completedFuture(Either.right("请求完成"));
            }
        } catch (Exception ex) {
            logger.error("{}", ex);
            return CompletableFuture.completedFuture(Either.left(ex.getMessage()));
        }
    }
}
