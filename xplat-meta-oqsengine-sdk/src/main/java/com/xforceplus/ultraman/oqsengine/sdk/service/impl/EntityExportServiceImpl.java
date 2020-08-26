package com.xforceplus.ultraman.oqsengine.sdk.service.impl;

import akka.NotUsed;
import akka.stream.ActorMaterializer;
import akka.stream.IOResult;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.sdk.event.EntityErrorExported;
import com.xforceplus.ultraman.oqsengine.sdk.event.EntityExported;
import com.xforceplus.ultraman.oqsengine.sdk.service.*;
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
    private ApplicationEventPublisher publisher;

    @Autowired
    private ExportSource exportService;

    @Autowired
    private EntityService entityService;

    @Autowired
    private EntityServiceEx entityServiceEx;

    private Logger logger = LoggerFactory.getLogger(EntityExportService.class);

    /**
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

        AtomicBoolean isFirstLine = new AtomicBoolean(true);

        Sink<ByteString, CompletionStage<Tuple2<IOResult, String[]>>> fileSink = exportSink.getSink(token, fileName);

        Map<String, List<DictItem>> mapping = new HashMap<>();

        Map<String, Map<String, String>> searchTable = new HashMap<>();

        List<NameMapping> nameMapping = query.getMapping();

        List<String> columns = query.getStringKeysOrdered();

        Set<String> filterColumns = Optional.ofNullable(columns)
                .<Set<String>>map(HashSet::new).orElseGet(Collections::emptySet);

        try {
            byte[] bom = new byte[]{(byte) 0xef, (byte) 0xbb, (byte) 0xbf};
            Source<ByteString, NotUsed> bomSource = Source.single(ByteString.fromArray(bom));

            Source<ByteString, NotUsed> content = exportService.source(entityClass, query)
                    .map(record -> {
                        StringBuilder sb = new StringBuilder();
                        if (isFirstLine.get()) {

                            //nameMapping
                            //caution!! nameMapping is maybe
                            //using merge func to always return first elem
                            Map<String, String> map =
                                    Optional.ofNullable(nameMapping).map(x -> x.stream()
                                            .collect(Collectors.toMap(NameMapping::getCode, NameMapping::getText, (a, b) -> a)))
                                            .orElse(Collections.emptyMap());

                            String header = record
                                    .stream(filterColumns)
                                    .sorted(Comparator.comparingInt(field -> columns.indexOf(field._1().name())))
                                    .map(Tuple2::_1)
                                    .map(x -> Optional.ofNullable(x)
                                            .map(y -> {
                                                String fieldName = y.name();
                                                String name = map.get(fieldName);
                                                if (name == null) {
                                                    return y.cnName();
                                                } else {
                                                    return name;
                                                }
                                            })
                                            .orElse(""))
                                    .collect(Collectors.joining(","));

                            //setup headers
                            sb.append(header);
                            sb.append("\n");
                            isFirstLine.set(false);
                        }

                        //may have null conver to ""
                        String line = record
                                .stream(filterColumns)
                                .sorted(Comparator.comparingInt(field -> columns.indexOf(field._1().name())))
                                .map(x -> {

                                    IEntityField field = x._1();
                                    Object value = x._2();

                                    //TODO take field type in consideration?
                                    //convert enum to dict value
                                    // \t is a tricky for csv see
                                    //     https://qastack.cn/superuser/318420/formatting-a-comma-delimited-csv-to-force-excel-to-interpret-value-as-a-string
                                    // using escape instead
                                    return getString(entityClass, field, value, mapping, searchTable);
                                })
                                .collect(Collectors.joining(","));
                        sb.append(line);
                        sb.append("\n");
                        return sb.toString();

                    })
                    .map(x -> ByteString.fromString(x, StandardCharsets.UTF_8));

            CompletableFuture<Either<String, String>> syncCompleteResult = bomSource.concat(content)
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


    /**
     * searchTable
     *
     * @return
     */
    private String getString(IEntityClass entityClass
            , IEntityField entityField, Object value
            , Map<String, List<DictItem>> mapping
            , Map<String, Map<String, String>> searchTable) {

        FieldType type = entityField.type();

        String retStr = null;

        String safeSourceValue = Optional.ofNullable(value).map(Object::toString).orElse("");

        switch (type) {
            case ENUM:
                String dictId = entityField.dictId();
                List<DictItem> items = mapping.get(dictId);
                if (items == null) {
                    List<DictItem> dictItems = entityServiceEx.findDictItems(dictId, null);
                    mapping.put(dictId, dictItems);
                    items = dictItems;
                }

                retStr = items.stream()
                        .filter(x -> x.getValue().equals(safeSourceValue))
                        .map(DictItem::getText)
                        .findAny().orElse("");
                break;
            case STRINGS:

                if (safeSourceValue.trim().isEmpty()) {
                    retStr = "";
                    break;
                }

                String[] ids = safeSourceValue.split(",");
                Long fieldId = entityField.id();

                Optional<Long> relatedId = entityClass.relations().stream()
                        .filter(x -> x.getEntityField() != null)
                        .filter(x -> x.getEntityField().id() == fieldId)
                        .map(Relation::getEntityClassId).findAny();

                if (relatedId.isPresent()) {
                    Optional<IEntityClass> relatedEntityOp = entityService.load(relatedId.get().toString());
                    if (relatedEntityOp.isPresent()) {
                        IEntityClass relatedEntity = relatedEntityOp.get();

                        Map<String, String> cachedList = searchTable.get(relatedId.get().toString());
                        if (cachedList == null) {
                            //search
                            Map<String, String> cached = new HashMap<>();
                            searchTable.put(relatedId.get().toString(), cached);
                            cachedList = cached;
                        }

                        //TODO how to find all
                        Map<String, String> finalCachedList = cachedList;


                        //try to find something to display
                        Optional<IEntityField> displayField = relatedEntity.fields().stream()
                                .filter(x -> Optional.ofNullable(x.config())
                                        .filter(field -> "1".equals(field.getDisplayType())).isPresent()).findFirst();

                        retStr = Stream.of(ids).map(x -> {
                            String name = finalCachedList.get(x);
                            String innerRetStr = x;
                            if (name == null) {
                                //only when displayField is present will find one for id
                                if (displayField.isPresent()) {
                                    try {
                                        //search
                                        Either<String, Map<String, Object>> one = entityService.findOne(relatedEntity, Long.parseLong(x));
                                        if (one.isRight()) {
                                            String key = displayField.get().name();
                                            Object relatedDisplayName = one.get().get(key);
                                            if (relatedDisplayName != null) {
                                                innerRetStr = relatedDisplayName.toString();
                                            }
                                        }
                                    } catch (Exception ex) {
                                        logger.error("{}", ex);
                                    }
                                }
                                finalCachedList.put(x, innerRetStr);
                            } else {
                                innerRetStr = name;
                            }

                            return innerRetStr;
                        }).collect(Collectors.joining(","));
                    }
                }
                break;

            default:
                retStr = entityField.type().toTypedValue(entityField, safeSourceValue)
                        .map(IValue::getValue)
                        .map(Object::toString).orElse("");

        }

        return StringEscapeUtils.escapeCsv("\t" + retStr);
    }
}
